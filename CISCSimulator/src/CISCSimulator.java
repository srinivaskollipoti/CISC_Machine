import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author cozyu
 *
 */

	
 
public class CISCSimulator {
	private final static Logger LOG = Logger.getGlobal();
	private ControlUnit controller;
	private Memory memory;
	private FrontPanel panel;
	private StateType state;

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
	
	public CISCSimulator(){
		memory=new Memory();
		panel=new FrontPanel(this);
		controller=new ControlUnit(this);
		state = StateType.POWEROFF;
	}
	
	private boolean initProcessor()
	{
		if(!controller.init())
			return false;
		if(!controller.loadROM())
			return false;
		controller.showRegister();
		controller.showMemory();
		
		state=StateType.READY;
		LOG.info("Simulator Ready");
		panel.updateDisplay();
		return true;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		///LOG.setLevel(Level.WARN);
		CISCSimulator simu=new CISCSimulator(); 		
		boolean resultInit=simu.initProcessor();
		if(!resultInit) {
			LOG.severe("Failed to init processor");
			return;
		}
		simu.run();
	}

	public boolean run()
	{
		state=StateType.RUNNING;
		while(state==StateType.RUNNING){
			if(singleStep()==false)
			{
				LOG.info("TERMINATED");
			}
			
			try        
			{
			    Thread.sleep(100);
			} 
			catch(InterruptedException ex) 
			{
			    Thread.currentThread().interrupt();
			}
		}
		return true;
	}

	
	public boolean singleStep()
	{
		boolean result=controller.clock();
		if(!result) {
			state=StateType.TERMINATE;
			LOG.warning("Failed to run singleStep()");
			panel.updateDisplay();
			return false;
		}
		panel.updateDisplay();
		return true;
		
	}
	public ControlUnit getCPU()
	{
		//System.out.println("GET CPU");
		return controller;
	}
	
	public Memory getMemory()
	{
		//System.out.println("GET Memory");
		return memory;
	}
	//a=getCPU();ControlUnit
	//a.getMemory();
	//a.getRegister("something");
}
