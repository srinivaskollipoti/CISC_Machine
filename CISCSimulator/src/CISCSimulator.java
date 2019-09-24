import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author cozyu
 *
 */
 
/**
 * @author youcao
 * Operates the simulator(basic machine) according to the user input. See below for detailed descriptions. 
 */
public class CISCSimulator {
	private final static Logger LOG = Logger.getGlobal();
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
		//panel=new FrontPanel(this);
		this.panel=panel;
		controller=new ControlUnit(this);
		state = StateType.POWEROFF;
	}
	
	/**
	 * Print out the register and memory informations on the user interface and set state to ready.
	 * @return A boolean indicating if success. 
	 */
	public boolean initProcessor()
	{
		if(!controller.init())
			return false;
		if(!controller.loadROM())
			return false;
		controller.showRegister();
		controller.showMemory();
		
		state=StateType.READY;
		LOG.info("Simulator Initialized");
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
			buffer.append(controller.getMessage()+"\n\n"+memory.getString()+"\n");
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
		controller.clock();
		message=controller.getMessage()+"\n\n"+memory.getString()+"\n";
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
	 * @return A boolean indicating if success.
	 */
	public boolean setUserCode(String[] arrInst)
	{
		if(isPowerOff()==true)
		{
			message=("Simulator is not turned on, push the IPL button");
			return false;
		}
		return controller.setUserCode(arrInst);
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
			long MAR, long MBR, long MFR) {
		message="";
		if(isPowerOff()==true)
		{
			message="Simulator is not turned on, push the IPL button";
			return;
		}
		controller.GPR[0].setLong(R0);
        controller.GPR[1].setLong(R1);
        controller.GPR[2].setLong(R2);
        controller.GPR[3].setLong(R3);
        controller.IX[1].setLong(IX1);
        controller.IX[2].setLong(IX2);
        controller.IX[3].setLong(IX3);
        controller.IR.setLong(IR);
        controller.PC.setLong(PC);
        controller.MAR.setLong(MAR);
        controller.MBR.setLong(MBR);
        controller.MFR.setLong(MFR);
        controller.CC.setLong(CC);
        message="[CPU] Saved data ";
        panel.updateDisplay();
        
	}

	public String getMessage() {
		return message;
	}
}
