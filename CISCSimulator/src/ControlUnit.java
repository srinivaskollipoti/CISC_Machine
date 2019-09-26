import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author cozyu
 * @author youcao  documented by youcao.
 * A class that get data and current states and status of the simulator.
 */
public class ControlUnit {
	
	private final static Logger LOG = Logger.getGlobal();
	private Memory memory;
	private InstructionHandler ih=new InstructionHandler(this);
	
	private CPUState state=CPUState.LOAD_MAR;
	private GBitSet PC=new GBitSet(12);
	private GBitSet GPR[] = new GBitSet[4];
	private GBitSet IX[] = new GBitSet[4];
	private GBitSet CC=new GBitSet(4);
	
	private WORD MBR=new WORD();
	private WORD MAR=new WORD();
	private WORD MFR=new WORD();
	private WORD IR=new WORD();
	private String message=new String();

	enum CPUState
	{
		LOAD_MAR("LOAD MAR"), LOAD_MBR("LOAD MBR"), LOAD_IR("LOAD IR"), 
		NO_INST("NO INSTRUCTION"), EXECUTE("EXECUTE");
		final private String name; 
		private CPUState(String name) 
		{
			this.name=name;
		}
		public String getName() {
			return this.name;
		}	
	}
	
	
	public ControlUnit(CISCSimulator simulator)
	{
		this.memory=simulator.getMemory();
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
	 * Initialize all register and transfer control to boot program.
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
			GPR[i]=new GBitSet(16);
		for(GBitSet register:GPR) register.clear();
		for (int i =0; i<4;i++)
			IX[i]=new GBitSet(16);
		for(GBitSet register:IX) register.clear();
		
		PC.setLong(Memory.BOOT_MEMORY_START);
		state=CPUState.LOAD_MAR;
		return true;
	}
	
	/**
	 * Check if there is next instruction
	 * @return 
	 * true : there is next instruction
	 * false : there is not next instruction
	 */
	private boolean isNextInstruction()
	{
		WORD inst=new WORD();
		try {
			inst = memory.load(PC.getLong());
		} catch (IOException e){
			LOG.severe(e.getMessage());
			LOG.severe("Failed to load Next Instruction");			
		}
		boolean result=inst.isEmpty();	// check if next instruction is none
		return !result;
	}
	
	/**
	 * Check if there is current instruction in IR
	 * @return 
	 * true : there is current instruction
	 * false : there is not current instruction
	 */
	public boolean isCurrentInstruction()
	{
		return state!=CPUState.NO_INST;
	}
	
	/**
	 * A method that set the simulator to proper state, including load_mar, load_mbr,load_ir and execute.
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
				MBR.copy(memory.load(MAR.getInt()));
			} catch (IOException e) {
				LOG.severe("Failed to process LOAD MBR\n");
				return false;
			}
			state=CPUState.LOAD_IR;
			message="[FETCH] MBR <- MEM[MAR]\n";
			break;
		case LOAD_IR:
			IR.copy(MBR);
			increasePC();
			message="[FETCH] IR <- MBR\n";
			state=CPUState.EXECUTE;
			break;
		case EXECUTE:
			message="";
			if(execute()==false)
			{
				message="[ERROR] "+message+"Failed to execute code\n";
				result=false;
			}
			message="[EXECUTE] "+ih.getAsmCode()+"\n"+message;
			state=CPUState.LOAD_MAR;
			break;
		default:
			state=CPUState.LOAD_MAR;
			break;
		}
		return result;
	}
	
	/**
	 * Increase program counter
	 */
	private boolean increasePC() {
		long result=PC.getLong()+1;
		PC.setLong(result);
		return true;
	}
	

	/**
	 * Execute instructions.
	 */
	private boolean execute() {
		ih.showInstruction();
		try {
			ih.execute();
		} catch (IOException e) {
			message=message+e.getMessage();
			LOG.severe(message);
			return false;
		}
		return true;
	}
	
	/**
	 * Load instructions from the rom.txt file.
	 * @return boolean indicating the process is done.
	 */
	public boolean setBootCode()
	{
		message="[LOAD] Boot Program\n";
		ROM rom= new ROM();
		String[] arrAsmCode=rom.getCode();
		if(arrAsmCode==null)
		{
			message="Failed to load boot program\n";
			LOG.warning(message);
			return false;
		}
		ArrayList<WORD> arrBinCode=new ArrayList<WORD>();
		for(String asmCode:arrAsmCode)
		{
			WORD binCode=ih.getBinCode(asmCode);
			if(binCode==null) {
				message="Failed to parse boot program\n"+asmCode;
				LOG.warning(message);
				return false;
			}
			arrBinCode.add(binCode);
		}
		
	    try {
			if(memory.storeBootCode(arrBinCode)==false)
			{
				message="Failed to store boot program\n"+String.join("\n", arrAsmCode);
				LOG.warning(message);
				return false;
			}
		} catch (IOException e) {
			message="Failed to store boot program\n"+String.join("\n", arrAsmCode)+"\n"+e.getMessage();
			LOG.warning(message);
			return false;
		}
	    message=message+String.join("\n",arrAsmCode)+"\n";		
	    PC.setLong(Memory.BOOT_MEMORY_START);
	    message=message+"PC = "+Memory.BOOT_MEMORY_START+"\n";
	    state=CPUState.LOAD_MAR;
		return true;
	}
	
	/**
	 * Convert a list of insturctions to a list of binary codes and print out the memory status.
	 * @param arrAsmCode a string list storing multiple instructions
	 * @return A boolean indicating is done.
	 */
	public boolean setUserCode(String[] arrAsmCode) {
		
		message="";
		ArrayList<WORD> arrBinCode=new ArrayList<WORD>();
		for(int i=0; i<arrAsmCode.length;i++) {
			arrAsmCode[i]=arrAsmCode[i].toUpperCase();
			WORD binCode=ih.getBinCode(arrAsmCode[i]);
			if(binCode==null) {
				message=ih.getMessage()+"Failed to parse user program : "+arrAsmCode[i];
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
	    PC.setLong(memory.getUserMemoryLocation());
	    message=("[LOAD] User program\n"+String.join("\n",arrAsmCode)+"\nPC = "+memory.getUserMemoryLocation()+"\n");
	    state=CPUState.LOAD_MAR;
		return true;
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

	public GBitSet getGPR(int i) { return GPR[i]; }
	public GBitSet getIX(int i) { return IX[i]; }
	public GBitSet getPC() { return PC; }
	public GBitSet getCC() { return CC; }
	public WORD getMAR() { return MAR; }
	public WORD getMBR() { return MBR; }
	public WORD getMFR() { return MFR; }	
	public WORD getIR() { return IR; }
	
	public Memory getMemory() {	return this.memory; }
}
