import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
/**
 * @author cozyu
 *
 */
 
/**
 * @author youcao
 * Operates the simulator(basic machine) according to the user input. See below for detailed descriptions. 
 */
public class CISCSimulator {
	final static Logger LOG = Logger.getGlobal();
	private ControlUnit controller;			// cpu of computer
	private Memory memory;					// memory of comuter
	private CISCGUI panel;					// panel of computer
	private StateType state;				// state of computer
	private String message=new String();	// state message of computer
	
	enum StateType{
		POWEROFF("Power off"), READY("Ready"), RUNNING("Running"), HALT("Halted"), TERMINATE("Terminated");
		final private String name; 
		private StateType(String name) 
		{
			this.name=name;
		}
		public String getName() {
			return this.name;
		}
	}	
	
	/**
	 * A constructor that create a simulator given a GUI panel with state on POWEROFF.
	 * @param panel A graphic user interface.
	 */
	public CISCSimulator(CISCGUI panel){
		try {
			memory=new Memory();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		controller=new ControlUnit(this);
		this.panel=panel;
		state = StateType.POWEROFF;
		try {
			System.setProperty("java.util.logging.SimpleFormatter.format",
		       "[%1$tm/%1$te %1$tT] [%4$-7s] %5$s %n");
			FileHandler fh = new FileHandler("log.txt",true);
			LOG.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Initialize all register and memory and load boot program from ROM and transfers control to the boot program and set state to ready.
	 * @return A boolean indicating if success. 
	 */
	public boolean initProcessor()
	{
		message="";
		if(!controller.init())
		{
			message=controller.getMessage();
			message="[WARNING] Failed to initialize processor\n+"+message;
			state=StateType.POWEROFF;
			return false;
		}
		if(!controller.setBootCode())
		{
			message=controller.getMessage();
			message="[WARNING] Failed to load boot program\n+"+message;
			//state=StateType.POWEROFF;
			//return false;
		}
		controller.showRegister();
		controller.showMemory();
		
		state=StateType.READY;
		message=controller.getMessage();
		message=message+"Simulator Initialized\n";
		panel.updateDisplay();
		return true;
	}

	/**
	 * Turn off the simulator 
	 */
	public void powerOff() {
		state=StateType.POWEROFF;
	}

	/**
	 * Execute the whole process for the input instruction and print out memory information when done.
	 * @return A boolean indicating if the process is success.
	 */
	public boolean run()
	{
		StringBuffer buffer=new StringBuffer();

		if(isPowerOff()==true)
		{
			message=("Simulator is not turned on, push the IPL button");
			return false;
		}
		
		do{
			singleStep();
			buffer.append(message+"\n");
		}while(state==StateType.READY);
		message=buffer.toString();
		return true;
	}

	/**
	 * Execute only one more clock from the last step according to the instruction.
	 */
	public void singleStep()
	{
		message="";
		if(isPowerOff()==true)
		{
			message=("Simulator is not turned on, push the IPL button\n");
			return;
		}
		
		// if current program is terminated, initialize simulator.
		if(state==StateType.TERMINATE)
		{
			if(initProcessor()==false)
			{
				LOG.severe("Failed to initialize processor");
				return;
			}
		}
		
		state=StateType.RUNNING;
		boolean result=controller.clock();
		message=message+controller.getMessage();
		if(result==true)
			message=message+memory.getString();
		if(controller.isCurrentInstruction()==false)
		{
			state=StateType.TERMINATE;
			LOG.warning("No more instruction");	
		}else {
			state=StateType.READY;
		}
		panel.updateDisplay();
		return;
	}
	
	/**
	 * Load the register with given data.
	 */
	public void loadRegister(long R0, long R1,long R2, long R3, 
			long IX1, long IX2, long IX3, 
			long IR, long PC, long CC, 
			long MAR, long MBR, long MFR) 
	{
		message="";
		if(isPowerOff()==true)
		{
			message="Simulator is not turned on, push the IPL button";
			return;
		}
		try {
		if(R0!=controller.getGPR(0).getLong()) message=message+"R0 ="+R0+"\n";
		controller.getGPR(0).setLong(R0);
		if(R1!=controller.getGPR(1).getLong()) message=message+"R1 = "+R1+"\n";
		controller.getGPR(1).setLong(R1);
		if(R2!=controller.getGPR(2).getLong()) message=message+"R2 = "+R2+"\n";
		controller.getGPR(2).setLong(R2);
		if(R3!=controller.getGPR(3).getLong()) message=message+"R3 = "+R3+"\n";
		controller.getGPR(3).setLong(R3);
		if(IX1!=controller.getIX(1).getLong()) message=message+"IX1 = "+IX1+"\n";
		controller.getIX(1).setLong(IX1);
		if(IX2!=controller.getIX(2).getLong()) message=message+"IX2 = "+IX2+"\n";
		controller.getIX(2).setLong(IX2);
		if(IX3!=controller.getIX(3).getLong()) message=message+"IX3 = "+IX3+"\n";
		controller.getIX(3).setLong(IX3);
		if(IR!=controller.getIR().getLong()) message=message+"IR = "+IR+"\n";
		controller.getIR().setLong(IR);
		if(PC!=controller.getPC().getLong()) message=message+"PC = "+PC+"\n";
		controller.getPC().setLong(PC);
		if(MAR!=controller.getMAR().getLong()) message=message+"MAR = "+MAR+"\n";
		controller.getMAR().setLong(MAR);
		if(MBR!=controller.getMBR().getLong()) message=message+"MBR = "+MBR+"\n";
		controller.getMBR().setLong(MBR);
		if(MFR!=controller.getMFR().getLong()) message=message+"MFR = "+MFR+"\n";
		controller.getMFR().setLong(MFR);
		if(CC!=controller.getCC().getLong()) message=message+"CC = "+CC+"\n";
		controller.getCC().setLong(CC);
		}catch(IllegalArgumentException e)
		{
			message="Failed to save user input\n+ "+e.getMessage();
			panel.updateDisplay();
			return;
		}
		message="Loaded register from user input\n"+message;
		panel.updateDisplay();
	}

		
	/**
	 * Set the user code to controller.
	 * @return A boolean indicating if success.C
	 */
	public boolean setUserCode(String[] arrInst)
	{
		if(isPowerOff()==true)
		{
			message=("Simulator is not turned on, push the IPL button");
			return false;
		}
		boolean result=controller.setUserCode(arrInst);
		message=controller.getMessage();
		if(result==true)
			state=StateType.READY;
		return result;
	}
	
	/**
	 * check if the simulator is turned off
	 * @return A boolean indicating if the simulator is turned off.
	 */
	public boolean isPowerOff()
	{
		return state==StateType.POWEROFF;
	}
	
	public ControlUnit getCPU()
	{	
		return controller;
	}
	
	public Memory getMemory()
	{
		return memory;
	}
	
	public StateType getState() {
		return state;
	}

	public String getMessage() {
		return message;
	}
}
