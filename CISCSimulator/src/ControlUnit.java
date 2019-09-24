import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cozyu
 * A class that get data and current states and status of the simulator.
 */
public class ControlUnit {
	
	private final static Logger LOG = Logger.getGlobal();
	private CISCSimulator simulator;
	private Memory memory;
	
	CPUState state=CPUState.LOAD_MAR;
	GBitSet PC=new GBitSet(12);
	GBitSet GPR[] = new GBitSet[4];
	GBitSet IX[] = new GBitSet[4];
	GBitSet CC=new GBitSet(4);
	
	WORD MBR=new WORD();
	WORD MAR=new WORD();
	WORD MFR=new WORD();
	WORD IR=new WORD();
	InstructionHandler ih=new InstructionHandler(this);
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
	
	public WORD getIR() {
		return IR;
	}
	
	public ControlUnit(CISCSimulator simulator)
	{
		this.simulator=simulator;
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
	 * Initializes the machine.
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
	
	private boolean isNextInstruction()
	{
		WORD inst=new WORD();
		try {
			inst = memory.load(PC.getInt());
		} catch (IOException e){
			LOG.severe(e.getMessage());
			LOG.severe("Failed to load Next Instruction");			
		}
		boolean result=inst.isEmpty();	// check if next instruction is none
		return !result;
	}
	
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

	public String getMessage()
	{
		return message;
	}
	
	public void addMessage(String message)
	{
		this.message+=this.message+message;
	}

	
	/**
	 * @package : /ControlUnit.java
	 * @author : cozyu  
	 * @date : 2019. 9. 20.
	 * @return
	 * ControlUnit
	 */
	/*
	public boolean loadROM() {
		
		ROM rom= new ROM();
		String romCode=rom.getCode();
		
		ArrayList<WORD> bootCode = new ArrayList<WORD>();
		InstructionHelper.Translate(romCode, bootCode);
		try {
			memory.storeBootCode(bootCode);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		PC.setLong(Memory.BOOT_MEMORY_START);
		state=CPUState.LOAD_MAR;
		return true;
	}
	*/
	
	
	/**
	 * Execute a list of instructions.
	 * @return a boolean indicating is done. 
	 */
/*
	public boolean executeInstruction(String[] arrAsmCode) {
		ArrayList<WORD> arrBinCode = new ArrayList<WORD>();
		for (String asmCode:arrAsmCode)
		{
			WORD binCode=ih.getBinCode(asmCode);
			if(binCode==null)
			{
				LOG.warning("Wrong assemble code : "+asmCode);;
				continue;
			}
			arrBinCode.add(binCode);
		}
		return true;
	}
*/
	
	public boolean increasePC() {
		long result=PC.getLong()+1;
		PC.setLong(result);
		return true;
	}
	

	/**
	 * Print out instructions.
	 */
	public boolean execute() {
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
		message="[LOAD] Boot Code\n";
		ROM rom= new ROM();
		String[] arrAsmCode=rom.getCode();
		ArrayList<WORD> arrBinCode=new ArrayList<WORD>();
		for(String asmCode:arrAsmCode)
		{
			WORD binCode=ih.getBinCode(asmCode);
			if(binCode==null) {
				message="Failed to parse boot code\n"+asmCode;
				LOG.warning(message);
				return false;
			}
			arrBinCode.add(binCode);
		}
		
	    try {
			if(memory.storeBootCode(arrBinCode)==false)
			{
				message="Failed to store boot code\n"+String.join("\n", arrAsmCode);
				LOG.warning(message);
				return false;
			}
		} catch (IOException e) {
			message="Failed to store boot code\n"+String.join("\n", arrAsmCode)+"\n"+e.getMessage();
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
		for(String asmCode:arrAsmCode)
		{
			WORD binCode=ih.getBinCode(asmCode);
			if(binCode==null) {
				message=ih.getMessage()+"Failed to parse user code : "+asmCode;
				LOG.warning(message);
				return false;
			}
			arrBinCode.add(binCode);
		}
		
	    if(memory.storeUserCode(arrBinCode)==false)
	    {
			message="Failed to store user code\n"+String.join("\n", arrAsmCode);
	    	LOG.warning(message);
			return false;
		}
	    PC.setLong(memory.getUserMemoryLocation());
	    message=("[LOAD] User Code\n"+String.join("\n",arrAsmCode)+"\nPC = "+memory.getUserMemoryLocation()+"\n");
	    state=CPUState.LOAD_MAR;
		return true;
	}

	public Memory getMemory() {
		return this.memory;
	}
}
