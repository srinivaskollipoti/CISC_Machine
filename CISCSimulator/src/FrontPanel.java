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
		return true;
	}
}
