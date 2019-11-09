import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.*;


/**
 * it executes instruction and has registers for instructions.
 * also it controls memory loading and saving.
 * @author cozyu(Yeongmok You)
 * @author youcao  documented by youcao.
 * cpu of simulator
 */
public class CPU {
	
	public final static int END_OF_PROGRAM=-1;
	
	public final static int TRAP_CODE_ADDRESS=0;
	public final static int MF_CODE_ADDRESS=1;
	public final static int TRAP_PC_ADDRESS=2;
	public final static int MF_PC_ADDRESS=4;

	public final static int MF_ILLEGAL_MEMORY_ACCESS=0;
	public final static int MF_ILLEGAL_TRAP=1;
	public final static int MF_ILLEGAL_OPERATION=2;
	public final static int MF_ILLEGAL_MEMORY_ADDRESS=3;
	
	private final static Logger LOG = Logger.getGlobal();
	private Memory memory;
	private IOC ioc;
	private CISCSimulator simu;
	
	private Cache cache;
	private CPUState state=CPUState.LOAD_MAR; 	
	
	private InstructionHandler ih;
	
	private GBitSet PC=new GBitSet(12);		/// Program Counter
	private WORD MAR=new WORD();			/// Memory Address Register
	private WORD MBR=new WORD();			/// Memory Buffer Register
	private WORD MFR=new WORD();			/// Machine Fault Register
	
	private SignedWORD GPR[] = new SignedWORD[4];	/// General Purpose Register
	private SignedWORD IX[] = new SignedWORD[4];	/// Index Register
	
	private GBitSet CC=new GBitSet(4);		/// Condition Code
	private WORD IR=new WORD();				/// Instruction Register
	
	private ALU alu;	/// Arithmetic Logical Unit
	
	private int trap=-1;	/// flag for trap
	private long backupIX3=0;
	private long backupR0=0;
	
	private String message=new String(); // A text indicating current state
	
	/**
	 * Enum type to distinguish the state of cpu.
	 * LOAD_MAR = phase to load MAR
	 * LOAD_MBR = phase to load MBR from memory
	 * LOAD_IR = phase to IR from MBR
	 * EXECUTE = phase to execute instruction
	 * INTERRUPT = phase indicating interruption
	 * NO_INST = phase indicating no more instruction
	 */
	public enum CPUState
	{
		LOAD_MAR("LOAD MAR"), LOAD_MBR("LOAD MBR"), LOAD_IR("LOAD IR"), 
		EXECUTE("EXECUTE"), INTERRUPT("Interrupt"), NO_INST("NO INSTRUCTION");
		final private String name; 
		private CPUState(String name) 
		{
			this.name=name;
		}
		public String getName() {
			return this.name;
		}	
	}
	
	
	public CPU(CISCSimulator simulator)
	{
		this.simu=simulator;
		this.memory=simulator.getMemory();
		this.ioc=simulator.getIOC();
		alu=new ALU(this);
		cache=new Cache();
		
		// InstructionHandler is initialized at last because InstructionHandler use ALU
		ih=new InstructionHandler(this);
		ih.init();
		if(simu.isDebug()==false)
			LOG.setLevel(Level.WARNING);
	}
	
	/**
	 * Backup current R0,IX3 register
	 */
	private void backupRegister()
	{
		backupR0=getGPR(0).getLong();
		backupIX3=getIX(3).getLong();		
	}

	/**
	 * Restore current R0,IX3 register
	 */
	private void restoreRegister()
	{
		getGPR(0).setLong(backupR0);
		getIX(3).setLong(backupIX3);
	}

	/**
	 * Get memory status.
	 */
	public String getAllMemory()
	{
		boolean isDebug=simu.isDebug();
		return memory.getString(0,memory.getLength(),isDebug);
	}
	
	public String getSystemMemory()
	{
		boolean isDebug=simu.isDebug();
		return (memory.getString(0,Memory.USER_MEMORY_START,isDebug));
	}

	public String getCodeMemory()
	{
		boolean isDebug=simu.isDebug();
		return (memory.getString(memory.getUserProgramLocation(),memory.getUserProgramEnd(),isDebug));
	}

	public String getDataMemory()
	{
		boolean isDebug=simu.isDebug();
		//if(isDebug)
		//	return (memory.getString(800,900,isDebug));

		return (memory.getString(Memory.USER_MEMORY_START,Memory.USER_PROGRAM_START,isDebug));
	}

	public String getStackMemory()
	{
		boolean isDebug=simu.isDebug();
		long bp=0, sp=0;
		try {
			bp=memory.load(998, this).getLong();
			sp=memory.load(999, this).getLong();

		} catch (IOException e) {
			e.printStackTrace();
		}
		if (bp<=0) return "==> NO STACK\n";
		
		return String.format("[BP] %d, [SP] %d\n%s"
				, bp,sp, 
				memory.getString((int)bp-2,(int)sp+1,isDebug)
				);
	}

	/**
	 * Print out register status.
	 */
	public boolean showRegister()
	{
		System.out.println("### REGISTER STATUS START ###\n");
		System.out.println("[PC  ] "+PC);
		System.out.println("[GPR0] "+GPR[0]);
		System.out.println("[GPR1] "+GPR[1]);
		System.out.println("[GPR2] "+GPR[2]);
		System.out.println("[GPR3] "+GPR[3]);
		System.out.println("[IX1 ] "+IX[1]);
		System.out.println("[IX2 ] "+IX[2]);
		System.out.println("[IX3 ] "+IX[3]);
		System.out.println("[CC  ] "+CC);
		System.out.println("[MAR ] "+MAR);
		System.out.println("[MBR ] "+MBR);
		System.out.println("[MFR ] "+MFR);
		System.out.println("[IR  ] "+IR);
		System.out.println("### REGISTER STATUS END ###\n");
		return true;
	}
	
	/**
	 * Initialize all register and memory, and then transfer control to boot program.
	 */
	public boolean init()
	{
		memory.init();
		cache.init();
		PC.clear();
		CC.clear();
		MAR.clear();
		MBR.clear();
		MFR.clear();
		IR.clear();
		
		for (int i =0; i<4;i++)
			GPR[i]=new SignedWORD();
		for(GBitSet register:GPR) register.clear();
		for (int i =0; i<4;i++)
			IX[i]=new SignedWORD();
		for(GBitSet register:IX) register.clear();
		
		PC.setLong(Memory.BOOT_MEMORY_START);
		state=CPUState.LOAD_MAR;
		return true;
	}
	
	/**
	 * Check if there is next instruction
	 * @return On case existing next instruction, true is returned, otherwise false is returned.
	 */
	private boolean isInstruction()
	{
		WORD inst=new WORD();
		try {
			inst = memory.load(PC.getLong(),this);			
		} catch (IOException e){
			LOG.severe(e.getMessage());
			LOG.severe("Failed to load Next Instruction");			
		}
		boolean result= (inst.getLong()==END_OF_PROGRAM); // check if next instruction is none
		return !result;
	}
	
	/**
	 * perform appropriate operation considering current state at every clock.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean clock()
	{
		boolean result=true;
		message="";
		switch(state){
		case LOAD_MAR:
			if(!isInstruction())
			{
				// if the status is trapped or machine fault, recover PC from memory
				if(trap>=0)
				{
					long address=0;
					try {
						address = memory.load(TRAP_PC_ADDRESS,this).getLong();
					} catch (IOException e) {
						message="Failed to access memeory\n";
						return false;
					}
					this.restoreRegister();
					this.setPC(address);
					this.trap=-1;
					message="[TRAP] Finished to execute trap code.\nPC = "+address+"\n";
					break;
				}
				else if(this.getMFR().getLong()>0)
				{
					long address=0;
					try {
						address = memory.load(MF_PC_ADDRESS,this).getLong();
					} catch (IOException e) {
						message="Failed to access memeory\n";
						return false;
					}
					this.restoreRegister();
					this.setPC(address);
					this.getMFR().clear();
					message="[FAULT] Finished to execute machine fault code.\nPC = "+address+"\n";
					break;
				}
				state=CPUState.NO_INST;
				return false;
			}
			MAR.copy(PC);
			state=CPUState.LOAD_MBR;
		case LOAD_MBR:
			try {
				MBR.copy(loadMemory(MAR.getLong()));
			} catch (IOException e) {
				LOG.severe("Failed to process LOAD MBR\n");
				return false;
			}
			state=CPUState.LOAD_IR;
			increasePC();		// increase the program counter
		case LOAD_IR:
			IR.copy(MBR);
			message=message+"[FETCH] IR <- MEM[PC], PC <- PC + 1\n";
			state=CPUState.EXECUTE;
			if(Boolean.getBoolean("debug")==false)
				break;
		case EXECUTE:
			String code=Translator.getAsmCode(IR);
			if(Boolean.getBoolean("debug")==true)
				message=String.format("[EXECUTE @%03d] ",getPC().getLong()-1);
			else
				message=message+String.format("[EXECUTE @%03d] ",getPC().getLong()-1);
			if(code!=null)
				message+=code;
			message+="\n";
				
			if(execute()==false)
			{
				//message=message+"Failed to execute code\n";
				state=CPUState.LOAD_MAR;
				return false;
			}
			if(isInputInterrupt()==false) {
				state=CPUState.LOAD_MAR;
				simu.setEnableIn(false);
			}else {
				simu.setEnableIn(true);
			}
			// in case of interrupt, current instruction is executed again
			break;
		case INTERRUPT:
			simu.setEnableIn(true);
			break;
		default:
			state=CPUState.LOAD_MAR;
			break;
		}
		if(simu.isDebug()!=true)
			addMessage(cache.toString()+"\n"+cache.getMessage());
		return result;
	}
	
	/**
	 * Increase program counter
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean increasePC() {
		long result=PC.getLong()+1;
		PC.setLong(result);
		return true;
	}
	
	/**
	 * Execute instructions.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean execute() {
		if(ih.checkOPCode()==false)
		{
			message+=ih.getMessage();
			this.setFault(MF_ILLEGAL_OPERATION);
			return false;
		}
		try {
			ih.execute();
		} catch (IOException e) {
			message=message+e.getMessage();
			LOG.severe(message);
			return false;
		}
		message=message+ih.getMessage();
		return true;
	}
	

	public void setInputInterrupt() { this.state=CPUState.INTERRUPT; }
	public boolean isInputInterrupt() { return this.state==CPUState.INTERRUPT; }
	public void setResume() { this.state=CPUState.EXECUTE; }

	/**
	 * Load instructions from the rom.txt file.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean setBootCode()
	{
		cache.init();
		message="";
		ArrayList<WORD> arrBinCode=ROM.getBinCode();
		if(arrBinCode==null)
		{
			message="Failed to get binary code\n"+ROM.getMessage();
			LOG.warning(message);
			return false;
		}
		
	    try {
			if(memory.storeBootCode(arrBinCode)==false)
			{
				message="Failed to load boot code\n";
				LOG.warning(message);
				return false;
			}
		} catch (IOException e) {
			message="Failed to access boot code\n"+e.getMessage()+"\n";
			LOG.warning(message);
			return false;
		}
	    PC.setLong(Memory.BOOT_MEMORY_START);
	    message=("[LOADED] Boot program\n"
	    		+this.getAllMemory()
	    		+"==> PC = "+PC.getLong()+"\n");
	    state=CPUState.LOAD_MAR;
		return true;
	}
	
	/**
	 * Store binary code in memory .
	 * @param arrBinCode a WORD list storing multiple instructions
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean setUserData(ArrayList<WORD> arrBinCode) {
		cache.clear(Memory.USER_MEMORY_START,memory.getUserProgramEnd()+1);
		message="";
		
	    if(memory.storeUserData(arrBinCode)==false)
	    {
			message="[WARNING] Failed to store user program\n";
	    	LOG.warning(message);
			return false;
		}
	    PC.setLong(memory.getUserProgramLocation());
	    message=("[LOAD] User program\n"
	    		+this.getCodeMemory()
	    		+"PC = "+PC.getLong()+"\n");
	    state=CPUState.LOAD_MAR;
		return true;
	}
	
	/**
	 * Store binary code in memory .
	 * @param arrBinCode a WORD list storing multiple instructions
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean setUserCode(ArrayList<WORD> arrBinCode) {
		cache.clear(Memory.USER_PROGRAM_START,memory.getUserProgramEnd()+1);
		message="";
		
	    if(memory.storeUserCode(arrBinCode)==false)
	    {
			message="[WARNING] Failed to store user program\n";
	    	LOG.warning(message);
			return false;
		}
	    PC.setLong(memory.getUserProgramLocation());
	    message=("[LOAD] User program\n"
	    		+this.getCodeMemory()
	    		+"PC = "+PC.getLong()+"\n");
	    state=CPUState.LOAD_MAR;
		return true;
	}
	
	/**
	 * Convert a list of assembly code to binary code and store in memory .
	 * @param arrAsmCode a string list storing multiple instructions
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean setUserCode(String[] arrAsmCode) {
		message="";
		ArrayList<WORD> arrBinCode=new ArrayList<WORD>();
		for(int i=0; i<arrAsmCode.length;i++) {
			arrAsmCode[i]=arrAsmCode[i].toUpperCase().trim();
			if(arrAsmCode[i].isBlank())	
				continue;
			WORD binCode=Translator.getBinCode(arrAsmCode[i]);
			if(binCode==null) {
				message=Translator.getMessage()+"Failed to parse user instruction : "+arrAsmCode[i]+"\n";
				LOG.warning(message);
				return false;
			}
			arrBinCode.add(binCode);
		}
		
	    return setUserCode(arrBinCode);
	}

	/**
	 * Check if the address is illegal.
	 * @param address target address
	 * @return On case legal, true is returned, otherwise false is returned.
	 */
	private boolean checkAddress(long address)
	{
		if(address<Memory.BOOT_MEMORY_START && address>=0)
		{
			this.addMessage("==> Failed to access address : "+address+"\n");
			this.setFault(MF_ILLEGAL_MEMORY_ACCESS);
			return false;
		}
		else if(address<0 || address>=getMemory().getLength())
		{
			this.addMessage("==> Failed to access address : "+address+"\n");
			this.setFault(MF_ILLEGAL_MEMORY_ADDRESS);
			return false;
		}
		return true;
	}

	/**
	 * Load data from memory or cache given address.
	 * @param address an integer of the memory address the machine wants to access.
	 * @return The data stored in the memory or cache slot.
	 * @throws IOException
	 */
	public WORD loadMemory(long address) throws IOException
	{
		if(checkAddress(address)==false)
			throw new IOException("");
		
		WORD result=null;
		
		result = cache.load(address);
		if(result!=null)
			return result;
		result = memory.load(address,this);
		cache.store(address,result);
		return result;
	}

	/**
	 * Store data into memory, cpu stores the data into cache when the data is stored into memory
	 * @param address	the address of data
	 * @param value		the data
	 * @return On case success, true is returned, otherwise false is returned.
	 * @throws IOException
	 */
	public boolean storeMemory(long address, WORD value) throws IOException
	{
		return storeMemory(address,value,false);
	}
	
	public boolean storeMemory(long address, WORD value, boolean isSystem) throws IOException
	{
		if(checkAddress(address)==false)
			throw new IOException("");
		boolean result=true;
		result= memory.store(address, value,isSystem,this);
		cache.store(address,value);
	    LOG.info("mem["+address+"] = "+value+"\n");
		return result;
	}

	/**
	 * Set output buffer of IO controller
	 * @param devID ID of device
	 * @param out a character to output
	 * @return if success, return true, otherwise return false
	 */
	public boolean setOutputChar(int devID,char out) {
		ioc.appendIOBuffer(devID, out);
		return false;
	}

	/**
	 * Get character from input buffer of IO controller
	 * @param devID ID of device
	 * @return a character gotten from IO controller
	 */
	public char getInputChar(int devID)
	{
		char result=0;
		if(devID==IOC.KEYBOARD && ioc.isIOBuffer(devID)==false)
			setInputInterrupt();
		result=ioc.getIOBuffer(devID);
		return result;
	}

	/**
	 * Activate trap instruction
	 * @param code the code of trap
	 * @return if success, return true, otherwise return false
	 */
	public boolean setTrap(int code) {
		
		if(code < 0 || code > 15) {
			message+=String.format("==> Illegal TRAP code(%d)\n",code); 
			return this.setFault(MF_ILLEGAL_TRAP);
		}
		
		// store current PC to memory[2]
		WORD word = new WORD();
		word.setLong(this.getPC().getLong());
		try {
			memory.store(TRAP_PC_ADDRESS, word,true,this);
		} catch (IOException e) {
			message += e.getMessage();
			return false;
		}

		// store trap code
		String text = String.format("Occured trap code(%d)\n", code) + "\0";
		this.getIOC().appendIOBuffer(30, text);
		long address = 0;
		try {			
			address = memory.load(TRAP_CODE_ADDRESS,this).getLong();
		} catch (IOException e) {
			message += ("==> Failed to access memory "+TRAP_CODE_ADDRESS+"\n");
			return false;
		}

		this.backupRegister();
		
		this.getIX(3).setLong(address);
		message += String.format("[TRAP] Executed trap code(%d)\n==> PC = %d\n", code, address);
		this.setPC(address);

		this.trap = code;
		return true;
	}

	/**
	 * Activate machine fault instruction
	 * @param id the id of machine fault
	 * @return if success, return true, otherwise return false
	 */
	public boolean setFault(int id)
	{
		if(id<MF_ILLEGAL_MEMORY_ACCESS || id>MF_ILLEGAL_MEMORY_ADDRESS)
		{
			message+="==> Wrong machine fault ID "+id+"\n";
			return false;
		}
		String notice=new String();
		if(id==MF_ILLEGAL_MEMORY_ACCESS) notice="Occured machine fault\n=> Illegal Memory Address to Reserved Locations\n";
		else if(id==MF_ILLEGAL_TRAP) notice="Occured machine fault\n=> Illegal TRAP Code\n";
		else if(id==MF_ILLEGAL_OPERATION) notice="Occured machine fault\n=> Illegal Operation Code\n";
		else if(id==MF_ILLEGAL_MEMORY_ADDRESS) notice="Occured machine fault\n=> Illegal Memory Address\n";
		
		// store current PC
		WORD word= new WORD();
		word.setLong(this.getPC().getLong());
		try {
			memory.store(MF_PC_ADDRESS, word,true,this);
		} catch (IOException e) {
			message+=e.getMessage();
			return false;
		}
		
		// store machine fault code
		this.getIOC().appendIOBuffer(31, notice+"\0");
		long address=0;
		try {
			address = memory.load(MF_CODE_ADDRESS,this).getLong();
		} catch (IOException e) {
			message+=("==> Failed to access memory "+MF_CODE_ADDRESS+"\n");
			return false;
		}
		
		this.backupRegister();
		
		this.getIX(3).setLong(address);
		message+=String.format("[FAULT] Executed machine fault code\n==> PC = %d\n",address); 

		this.setPC(address);
		getMFR().clear();
		getMFR().set(id);
		
		
		return true;
	}
	
	/**
	 * Append new message to the original one.
	 * @param message a string message wanted to be appended.
	 */
	public void addMessage(String message)
	{
		this.message=this.message+message;
	}

	public boolean isTerminate() { return state==CPUState.NO_INST; }

	public String getMessage(){ return message;}
	public SignedWORD getGPR(int i) { return GPR[i]; }
	public SignedWORD getIX(int i) { return IX[i]; }
	public GBitSet getPC() { return PC; }
	public boolean setPC(long value) {PC.setLong(value); return true;}
	public GBitSet getCC() { return CC; }
	public WORD getMAR() { return MAR; }
	public WORD getMBR() { return MBR; }
	public WORD getMFR() { return MFR; }	
	public WORD getIR() { return IR; }
	public CPUState getState() { return state; }
	public Memory getMemory() {	return this.memory; }
	public ALU getALU() { return alu;}
	public IOC getIOC() { return ioc;}
	public CISCSimulator getSimulator() {return this.simu;}
	
	
}
