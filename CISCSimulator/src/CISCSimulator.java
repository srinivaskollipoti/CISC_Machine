import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author cozyu
 *
 */
import java.util.logging.SimpleFormatter;
 
/**
 * @author youcao
 * Operates the simulator(basic machine) according to the user input. See below for detailed descriptions. 
 */
public class CISCSimulator {
	final static Logger LOG = Logger.getGlobal();
	private ControlUnit controller;
	private Memory memory;
	private CISCGUI panel;
	private StateType state;
	String message=new String();
	
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
		memory=new Memory();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/**
	 * Print out the register and memory informations on the user interface and set state to ready.
	 * @return A boolean indicating if success. 
	 */
	public boolean initProcessor()
	{
		message="";
		if(!controller.init())
			return false;
		if(!controller.setBootCode())
			return false;
		controller.showRegister();
		controller.showMemory();
		
		state=StateType.READY;
		message=controller.getMessage();
		message=message+"Simulator Initialized\n";
		panel.updateDisplay();
		return true;
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
			/*
		    try        
			{
			    Thread.sleep(100);
			} 
			catch(InterruptedException ex) 
			{
			    Thread.currentThread().interrupt();
			}
			*/
		}while(state==StateType.READY);
		message=buffer.toString();
		return true;
	}


	public boolean isPowerOff()
	{
		return state==StateType.POWEROFF;
	}

	/**
	 * Execute only one more clock from the last step according to the instruction.
	 */
	public void singleStep()
	{
		message="";
		if(isPowerOff()==true)
		{
			message=("Simulator is not turned on, push the IPL button");
			return;
		}
		
		if(controller.isCurrentInstruction()==false)
		{
			initProcessor();
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
			return;
		}
		panel.updateDisplay();
		state=StateType.READY;
		return;
	}
	
	public ControlUnit getCPU()
	{	
		return controller;
	}
	
	public Memory getMemory()
	{
		return memory;
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
		return result;
	}

	public StateType getState() {
		return state;
	}

	public void powerOff() {
		state=StateType.POWEROFF;
	}

	/**
	 * Save the register with given data.
	 */
	public void saveRegister(long R0, long R1,long R2, long R3, 
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
		if(R0!=controller.GPR[0].getLong()) message=message+"R0 ="+R0+"\n";
		controller.GPR[0].setLong(R0);
		if(R1!=controller.GPR[1].getLong()) message=message+"R1 = "+R1+"\n";
		controller.GPR[1].setLong(R1);
		if(R2!=controller.GPR[2].getLong()) message=message+"R2 = "+R2+"\n";
		controller.GPR[2].setLong(R2);
		if(R3!=controller.GPR[3].getLong()) message=message+"R3 = "+R3+"\n";
		controller.GPR[3].setLong(R3);
		if(IX1!=controller.IX[1].getLong()) message=message+"IX1 = "+IX1+"\n";
		controller.IX[1].setLong(IX1);
		if(IX2!=controller.IX[2].getLong()) message=message+"IX2 = "+IX2+"\n";
		controller.IX[2].setLong(IX2);
		if(IX3!=controller.IX[3].getLong()) message=message+"IX3 = "+IX3+"\n";
		controller.IX[3].setLong(IX3);
		if(IR!=controller.IR.getLong()) message=message+"IR = "+IR+"\n";
		controller.IR.setLong(IR);
		if(PC!=controller.PC.getLong()) message=message+"PC = "+PC+"\n";
		controller.PC.setLong(PC);
		if(MAR!=controller.MAR.getLong()) message=message+"MAR = "+MAR+"\n";
		controller.MAR.setLong(MAR);
		if(MBR!=controller.MBR.getLong()) message=message+"MBR = "+MBR+"\n";
		controller.MBR.setLong(MBR);
		if(MFR!=controller.MFR.getLong()) message=message+"MFR = "+MFR+"\n";
		controller.MFR.setLong(MFR);
		if(CC!=controller.CC.getLong()) message=message+"CC = "+CC+"\n";
		controller.CC.setLong(CC);
		}catch(IllegalArgumentException e)
		{
			message="Failed to save user input\n+ "+e.getMessage();
			panel.updateDisplay();
			return;
		}
		message="Saved user input data\n"+message;
		panel.updateDisplay();
	}

	public String getMessage() {
		return message;
	}
}
