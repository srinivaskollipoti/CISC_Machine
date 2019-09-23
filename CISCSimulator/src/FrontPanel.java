import java.io.IOException;

/**
 * 
 */

/**
 * @author cozyu
 *
 */
public class FrontPanel {
	CISCSimulator simulator;
	public FrontPanel() {
	}
	
	public FrontPanel(CISCSimulator simulator) {
		this.simulator=simulator;
	}



	public boolean updateDisplay()
	{
		ControlUnit cpu=simulator.getCPU();
		Memory memory=simulator.getMemory();
		/*
		cpu.PC.setLong(55L);
		
		cpu.MBR.setLong
		//cpu.PC.copy();
		WORD input=new WORD();
		try {
			memory.store(55,input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			memory.load(55);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return true;
	}
	public boolean test()
	{
		Memory memory=simulator.getMemory();
		int target=55;
		//memory[55].store("FF");
		return true;
	}
}
