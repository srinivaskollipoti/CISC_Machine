import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;



/**
 * @author cozyu
 * @author youcao  documented by youcao.
 * cpu of simulator
 * it executes instruction and has registers for instructions.
 * also it controls memory loading and saving.
 */
public class CPU {
	
	public final static int END_OF_CODE=-1;
	private final static Logger LOG = Logger.getGlobal();
	private Memory memory;
	private IOC ioc;
	private CISCSimulator simu;
	
	private Cache cache;
	private CPUState state=CPUState.LOAD_MAR; 	
	// Need to consider Instruction Cycle Code for pipeline  
	// IF-ID-EX-MEM-WB
	
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
		
	}
	
	/**
	 * Print out memory status.
	 */
	public boolean showMemory()
	{
		System.out.println("### MEMORY STATUS START ###\n");
		System.out.println(memory);
		System.out.println("### MEMORY STATUS END ###\n");
		return true;
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
	private boolean isNextInstruction()
	{
		WORD inst=new WORD();
		try {
			inst = loadMemory(PC.getLong());
		} catch (IOException e){
			LOG.severe(e.getMessage());
			LOG.severe("Failed to load Next Instruction");			
		}
		boolean result= (inst.getLong()==END_OF_CODE); // check if next instruction is none
		//boolean result=inst.isEmpty();	
		return !result;
	}
	
	/**
	 * Check if there is current instruction in IR
	 * @return On case existing current instruction, true is returned, otherwise false is returned.
	 */
	public boolean isCurrentInstruction()
	{
		return state!=CPUState.NO_INST;
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
			if(!isNextInstruction())
			{
				message="[TERMINATED] End of instruction\n";
				state=CPUState.NO_INST;
				return false;
			}
			MAR.copy(PC);
			state=CPUState.LOAD_MBR;
			message="[FETCH] MAR <- PC\n";
			break;
		case LOAD_MBR:
			try {
				MBR.copy(loadMemory(MAR.getLong()));
			} catch (IOException e) {
				LOG.severe("Failed to process LOAD MBR\n");
				return false;
			}
			state=CPUState.LOAD_IR;
			increasePC();		// increase the program counter
			message="[FETCH] MBR <- MEM[MAR]\n";
			break;
		case LOAD_IR:
			IR.copy(MBR);
			message="[FETCH] IR <- MBR\n";
			state=CPUState.EXECUTE;
			break;
		case EXECUTE:
			message="";
			if(execute()==false)
			{
				message="[ERROR] Failed to execute code - "+message;
				result=false;
			}
			if(isInterrupt()==false) {
				message = String.format("[EXECUTE @%03d] %s\n%s" , getPC().getLong()-1,
						Translator.getAsmCode(IR), message.toString());
				state=CPUState.LOAD_MAR;				
			}
			// in case of interrupt, current instruction is executed again
			break;
		default:
			state=CPUState.LOAD_MAR;
			break;
		}
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
		try {
			ih.execute();
		} catch (IOException e) {
			message=message+e.getMessage();
			LOG.severe(message);
			return false;
		}
		message=ih.getMessage();
		return true;
	}
	

	public void setInterrupt() { this.state=CPUState.INTERRUPT; }
	public boolean isInterrupt() { return this.state==CPUState.INTERRUPT; }
	public void setResume() { this.state=CPUState.EXECUTE; }

	/**
	 * Load instructions from the rom.txt file.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean setBootCode()
	{
		message="[LOAD] Boot Program\n";
		ROM rom= new ROM();
		ArrayList<WORD> arrBinCode=rom.getBinCode();
		if(arrBinCode==null)
		{
			message="[WARNING] Failed to load boot program\n"+rom.getMessage();
			LOG.warning(message);
			return false;
		}
		
	    try {
			if(memory.storeBootCode(arrBinCode)==false)
			{
				message="[WARNING] Failed to store boot program\n";
				LOG.warning(message);
				return false;
			}
		} catch (IOException e) {
			message="[WARNING] Failed to store boot program\n"+e.getMessage()+"\n";
			LOG.warning(message);
			return false;
		}
	    message=message+getMemory().getString();
	    //message=message+"==> Loading Complete\n";
	    //message=message+String.join("\n",arrAsmCode)+"\n";		
	    PC.setLong(Memory.BOOT_MEMORY_START);
	    //message=message+"PC = "+Memory.BOOT_MEMORY_START+"\n";
	    state=CPUState.LOAD_MAR;
		return true;
	}
	
	/**
	 * Convert a list of instructions to a list of binary codes and print out the memory status.
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
		
	    if(memory.storeUserCode(arrBinCode)==false)
	    {
			message="Failed to store user program\n"+String.join("\n", arrAsmCode);
	    	LOG.warning(message);
			return false;
		}
	    PC.setLong(memory.getUserCodeLocation());
	    message=("[LOAD] User program\n"+String.join("\n",arrAsmCode)+"\nPC = "+memory.getUserCodeLocation()+"\n"+memory.getString()+"\n");
	    state=CPUState.LOAD_MAR;
		return true;
	}
	

	/**
	 * Srinivas implements L1, L2 cache 
	 * 
	 * Load data from memory or cache given address.
	 * @param address an integer of the memory address the machine wants to access.
	 * @return The data stored in the memory or cache slot.
	 * @throws IOException
	 */
	public WORD loadMemory(long address) throws IOException
	{
		WORD result=null;
		
		// need to implement to load cache
		//result = cache.load(address);
		//if(result!=null)
		//	return result;
		
		result = memory.load(address,this);
		//cache.store(result);
		return result;
	}
	
	/**
	 * 
	 * fetch a word from cache. If the word is not in cache, fetch it from
	 * memory, then store it into cache.
	 * 
	 * @param address
	 * @return
	 */
	
	public WORD fetchFromCache(long address) throws IOException {
		for (Cache.CacheLoad lines : cache.getCacheLines()) { // check every block
														// whether the tag is
														// already exist
			if (address == lines.getMemAddress()) {
				return lines.getData(); // tag exist, return the data of the
										// block
			}
		}
		// tag not exist
		WORD value = loadMemory(address);
		System.out.println(value);
		cache.add(address, value);
		return value;
	}

	/**
	 * 
	 * store into cache with replacement. Also store into memory simultaneously.
	 * 
	 * @param address
	 * @param value
	 */
	public void storeIntoCache(long address, WORD value) {
	
		LOG.info(" "+address+" "+value);
		
		try {
			storeMemory(address, value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Cache.CacheLoad lines : cache.getCacheLines()) { // check every block the
														// tag is already exist
			if (address == lines.getMemAddress()) {
				lines.setData(value); // replace the block
				return;
			}
		}
		// tag not exist
		cache.add(address, value);
	}
	/**
	 * Srinivas implements L1, L2 cache 
	 * 
	 * Store the input data to memory or cache with specific address.
	 * @param address A integer indicating the memory slot to access.
	 * @param value A WORD argument containing the input data for the memory to store.
	 * @return On case success, true is returned, otherwise false is returned.
	 * @throws IOException
	 */
	public boolean storeMemory(long address, WORD value) throws IOException
	{
		// need to implement to store cache and synchronize between cache and memory
	    //cache.store(address,value);
	    //storeIntoCache(address,value);
		LOG.info("mem["+address+"] = "+value+"\n");
		boolean result=true;
		result= memory.store(address, value,this);
		if(address==424)
		{
			LOG.info("Hello");
		}
		return result;
	}

	/**
	 * @param reg
	 * @return
	 */
	public boolean setOutputChar(int devID,char out) {
		ioc.appendIOBuffer(devID, out);
		return false;
	}
	
	public char getInputChar(int devID)
	{
		char result=0;
		if(devID==IOC.KEYBOARD && ioc.isIOBuffer(devID)==false)
			setInterrupt();
		result=ioc.getIOBuffer(devID);
		return result;
	}
	
	/**
	 * Append new message to the original one.
	 * @param message a string message wanted to be appended.
	 */
	public void addMessage(String message)
	{
		this.message+=this.message+message;
	}

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
	public boolean isExecute() { return state==CPUState.LOAD_MAR || state==CPUState.NO_INST; }
	
	public Memory getMemory() {	return this.memory; }
	public ALU getALU() { return alu;}
	public CISCSimulator getSimulator() {return this.simu;}

}
