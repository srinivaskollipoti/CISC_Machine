import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
 
/**
 * Operates the simulator(basic machine) according to the user input. See below for detailed descriptions. 
 * @author cozyu (Yeongmok You)
 * @author youcao  documented by youcao.
 */
public class CISCSimulator implements Runnable{
	final static Logger LOG = Logger.getGlobal();
	private CPU cpu;					/// cpu of computer
	private Memory memory;					/// memory of computer
	private CISCGUI panel;					/// panel of computer
	private IOC ioc;						/// io controller of computer
	
	private StateType state;				// state of computer
	private String message=new String();	// state message of computer
	private int mode=0;						// 0: NORMAL, 1: Test Program1, 2: Test Program2
	
	private boolean isNeedReload=false;
	/**
	 * Enum type to distinguish the state of simulator.
	 * POWEROFF = Turned off
	 * READY = Ready
	 * RUNNING = Running
	 * HALT = Halted
	 * TERMINATE = Terminated
	 */
	enum StateType{
		POWEROFF("Power off"), READY("Ready"), RUNNING("Running"), HALT("Halted"), 
		TERMINATE("Terminated");
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
		ioc=new IOC(this);
		this.panel=panel;
		
		// cpu must be initialized late
		cpu=new CPU(this);
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
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean initProcessor()
	{
		message="";
		ioc.init();
		if(!cpu.init())
		{
			message=cpu.getMessage();
			message="[WARNING] Failed to initialize processor\n==> "+message+"\n";
			powerOff();
			return false;
		}
		if(!cpu.setBootCode())
		{
			message=cpu.getMessage();
			message="[WARNING] Failed to load boot program\n==> "+message+"\n";
			powerOff();
			return false;
		}
	
		initStatus();
		
		message+=cpu.getMessage();
		message=message+"[NOTICE] Simulator has been initialized\n";
		panel.updateDisplay();
		
		return true;
	}
	
	public void initStatus() {
		state=StateType.READY;
		isNeedReload=false;
		mode=0;
	}
	
	/**
	 * Set the user code to cpu.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean setUserCode(String[] arrInst)
	{
		if(isPowerOff()==true)
		{
			message=("Simulator is not turned on, push the IPL button\n");
			return false;
		}
		boolean result=cpu.setUserCode(arrInst);
		message=cpu.getMessage();
		if(result==true)
			initStatus();
		return result;
	}


	/**
	 * Turn off the simulator 
	 */
	public void powerOff() {
		setState(StateType.POWEROFF);
		panel.printLog("[NOTICE] Simulator is turned off\n");
	}
	
	/**
	 * Stop the simulator
	 */
	public void setStop()
	{
		setState(StateType.HALT);
	}
	
	/**
	 * Forward user input of console to IO controller
	 * @param text
	 */
	public void inputUserText(String text)
	{
		ioc.appendIOBuffer(0, text);
		cpu.setResume();
	}
	
	
	public boolean reloadROM()
	{
		if(initProcessor()==false)
		{
			LOG.severe("Failed to initialize processor");
			powerOff();
			return false;
		}
		isNeedReload=false;
		return true;
	}
	
	/**
	 * Execute the whole process for the input instruction.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public void run() 
	{
		StringBuffer buffer=new StringBuffer();

		if(isPowerOff()==true)
		{
			message=("Simulator is not turned on, push the IPL button");
			return;
		}
		
		if(isNeedReload==true)
		{
			reloadROM();
		}
		
		setState(StateType.RUNNING);
		while(checkRun()==true)
		{	
			// handle the input interrupt
			long sleep=4;
			if(cpu.isInputInterrupt()==true)
			{	
				panel.setEnableIn(true);
				panel.printLog("Waiting user input for IN instruction..\n");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}else {
				panel.setEnableIn(false);
			}
			
			// perform singlestep
			singleStep();

			// handle io buffer for printer 
			while(ioc.isIOBuffer(IOC.PRINTER)==true)
			{
				String output=Character.toString(ioc.getIOBuffer(IOC.PRINTER));
				panel.printScreen(output);
			}

			// sleep for clock time
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}

		// handle the halt and terminate
		if(isHalt()==true)
		{
			buffer.append(memory.getString());
			buffer.append("[NOTICE] System is halted.\n==> Press the Run button to resume.\n\n");
		}
		
		message=buffer.toString();	
		panel.updateDisplay();
		return;
	}

	/**
	 * Execute only one more clock from the last step according to the instruction.
	 * if there is no more instruction, change the state into TERMINATE
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean singleStep()
	{
		message="";
		StringBuffer buffer=new StringBuffer();
		if(isPowerOff()==true)
		{
			message=("Simulator is not turned on, push the IPL button\n");
			return false;
		}
				
		// if current program is terminated, initialize simulator.
		if(isNeedReload==true)
			reloadROM();
		
		setState(StateType.RUNNING);
		cpu.clock();
		buffer.append(cpu.getMessage());
		if(cpu.isTerminate()==true)
		{
			setState(StateType.TERMINATE);
			isNeedReload=true;
			buffer.append(memory.getString());
			buffer.append("[NOTICE] System is terminated\n\n");
		}
		message=buffer.toString();
		panel.updateDisplay();
		return true;
	}
	
	/**
	 * Load the register with given data.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean loadRegister(long R0, long R1,long R2, long R3, 
			long IX1, long IX2, long IX3, 
			long IR, long PC, long CC, 
			long MAR, long MBR, long MFR) 
	{
		message="";
		if(isPowerOff()==true)
		{
			message="Simulator is not turned on, push the IPL button";
			return false;
		}
		try {
		if(R0!=cpu.getGPR(0).getLong()) message=message+"R0 ="+R0+"\n";
		cpu.getGPR(0).setLong(R0);
		if(R1!=cpu.getGPR(1).getLong()) message=message+"R1 = "+R1+"\n";
		cpu.getGPR(1).setLong(R1);
		if(R2!=cpu.getGPR(2).getLong()) message=message+"R2 = "+R2+"\n";
		cpu.getGPR(2).setLong(R2);
		if(R3!=cpu.getGPR(3).getLong()) message=message+"R3 = "+R3+"\n";
		cpu.getGPR(3).setLong(R3);
		if(IX1!=cpu.getIX(1).getLong()) message=message+"IX1 = "+IX1+"\n";
		cpu.getIX(1).setLong(IX1);
		if(IX2!=cpu.getIX(2).getLong()) message=message+"IX2 = "+IX2+"\n";
		cpu.getIX(2).setLong(IX2);
		if(IX3!=cpu.getIX(3).getLong()) message=message+"IX3 = "+IX3+"\n";
		cpu.getIX(3).setLong(IX3);
		if(IR!=cpu.getIR().getLong()) message=message+"IR = "+IR+"\n";
		cpu.getIR().setLong(IR);
		if(PC!=cpu.getPC().getLong()) message=message+"PC = "+PC+"\n";
		cpu.getPC().setLong(PC);
		if(MAR!=cpu.getMAR().getLong()) message=message+"MAR = "+MAR+"\n";
		cpu.getMAR().setLong(MAR);
		if(MBR!=cpu.getMBR().getLong()) message=message+"MBR = "+MBR+"\n";
		cpu.getMBR().setLong(MBR);
		if(MFR!=cpu.getMFR().getLong()) message=message+"MFR = "+MFR+"\n";
		cpu.getMFR().setLong(MFR);
		if(CC!=cpu.getCC().getLong()) message=message+"CC = "+CC+"\n";
		cpu.getCC().setLong(CC);
		}catch(IllegalArgumentException e)
		{
			message="Failed to load user input\n==> "+e.getMessage()+"\n";
			return false;
		}
		if(message.isBlank()==false)
			message="==> Loaded register from user input\n"+message;
		else
			message="==> There is no change\n"+message;
		return true;
	}


	/**
	 * Load test program1
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean loadTestProgram1()
	{
		message="[LOADED] Test Program 1\n";
		// read test program file
		ArrayList<WORD> arrBinCode=ROM.getBinCode("test1.txt");
		if(arrBinCode==null)
		{
			message="[WARNING] Failed to load test program 1\n"+ROM.getMessage();
			LOG.warning(message);
			return false;
		}
		
		// change assembly code from binary code
		String[] arrAsmCode= new String[arrBinCode.size()];
		for (int i=0;i<arrBinCode.size();i++)
		{	
			String asmCode=Translator.getAsmCode(arrBinCode.get(i));
			if(asmCode!=null)
				arrAsmCode[i]=asmCode;
			else {
				LOG.warning("ERROR\n");
			}
		}
		
		// input user code to simulator
		boolean result=setUserCode(arrAsmCode);
		if(result) mode=1;
		panel.updateDisplay();
		return result;
	}
	
	public boolean checkRun() {
		if(cpu.isTerminate()==true )
			state=StateType.TERMINATE;
		return state==StateType.RUNNING; 
	}
	
	/**
	 * check if the simulator is turned off
	 * @return A boolean indicating if the simulator is turned off.
	 */
	public boolean isPowerOff(){ return state==StateType.POWEROFF; }
	public boolean isTerminate() { return state==StateType.TERMINATE; }
	public boolean isHalt() { return state==StateType.HALT; }
	
	
	public CPU getCPU(){ return cpu; }
	public Memory getMemory(){ return memory;}
	public IOC getIOC(){ return ioc;}
	public StateType getState() { return state; }
	public void setState(StateType state){ this.state=state;}
	public String getMessage() { return message; }

	/**
	 * Get phase of the test program, it check the flag in the memory
	 * @return
	 */
	public int getPhase() {
		int result=0;
		try {
			result= (int)getMemory().load(550, getCPU()).getLong();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public int getMode() { return mode;}
}
