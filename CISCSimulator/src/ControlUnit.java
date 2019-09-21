import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 */

/**
 * @author cozyu
 *
 */
public class ControlUnit {
	
	private final static Logger LOG = Logger.getGlobal();
	CISCSimulator simulator;
	Memory memory;
	
	CPUState state=CPUState.LOAD_MAR;
	GBitSet PC=new GBitSet(12);
	GBitSet GPR[] = new GBitSet[4];
	GBitSet IX[] = new GBitSet[4];
	GBitSet CC=new GBitSet(4);
	
	WORD MBR=new WORD();
	WORD MAR=new WORD();
	WORD MFR=new WORD();
	WORD IR=new WORD();
	
	
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
	
	public boolean showMemory()
	{
		System.out.println("### MEMORY STATUS START ###\n");
		System.out.println(memory);
		System.out.println("### MEMORY STATUS END ###\n");

		return true;
	}
	
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
		return true;
	}
	
	public boolean isNextInstruction()
	{
		WORD inst=new WORD();
		try {
			inst = memory.load(PC.getInt());
		} catch (IOException e) {
			e.printStackTrace();
			LOG.severe("Failed to load Next Instruction");			
		}
		boolean result=inst.isEmpty();
		return !result;
	}
	
	public boolean clock()
	{
		switch(state){
		case LOAD_MAR:
			if(!isNextInstruction())
			{
				state=CPUState.NO_INST;
				return false;
			}
			MAR.copy(PC);
			state=CPUState.LOAD_MBR;
			break;
		case LOAD_MBR:
			try {
				MBR.copy(memory.load(MAR.getInt()));
			} catch (IOException e) {
				LOG.severe("Failed to process LOAD MBR");
				return false;
			}
			state=CPUState.LOAD_IR;
			break;
		case LOAD_IR:
			IR.copy(MBR);
			increasePC();
			state=CPUState.EXECUTE;
			break;
		case EXECUTE:
			execute();
			state=CPUState.LOAD_MAR;
			break;
		}
		//showRegister();
		return true;
	}

	/**
	 * @package : /ControlUnit.java
	 * @author : cozyu  
	 * @date : 2019. 9. 20.
	 * @return
	 * ControlUnit
	 */
	public boolean loadROM() {
		
		ROM rom= new ROM();
		String romCode=rom.getCode();
		
		ArrayList<WORD> bootCode = new ArrayList<WORD>();
		InstructionHelper.Translate(romCode, bootCode);
		try {
			memory.store(Memory.BOOT_MEMORY_START, bootCode);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		memory.setUserMemoryLocation(bootCode.size());
		PC.setLong(Memory.BOOT_MEMORY_START);
		return true;
	}
	
	public boolean increasePC() {
		long result=PC.getLong()+1;
		PC.setLong(result);
		return true;
	}
	

	public boolean execute() {
		InstructionSet ih=new InstructionSet(this);
		ih.showInstruction();
		try {
			ih.execute();
		} catch (IOException e) {
			e.printStackTrace();
			LOG.severe("Failed to execute");
			return false;
		}
		return true;
	}

}
