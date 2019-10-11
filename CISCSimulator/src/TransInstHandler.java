import java.io.IOException;

/**
 * perform Transfer instruction
 */

/**
 * @author cozyu
 * @author youcao
 *
 */
public class TransInstHandler extends InstructionHandler {

	/**
	 * @param cpu
	 */
	public TransInstHandler(CPU cpu) {
		super(cpu);
		// TODO Auto-generated constructor stub
	}

	public boolean execute() throws IOException{
		LOG.info("Execute TRANS Instruction\n");

		parseIR(cpu.getIR());
		switch(getOPCode())
		{
			case InstructionSet.JZ:
				executeJZ();
				break;
				
			case InstructionSet.JNE:
				executeJNE();
				break;
				
			case InstructionSet.JGE:
				executeJGE();
				break;
				
			case InstructionSet.JMA:
				executeJMA();
				break;
				
			case InstructionSet.SOB:
				executeSOB();
				break;
				
			case InstructionSet.JSR:
				executeJSR();
				break;
				
			case InstructionSet.JCC:
				executeJCC();
				break;
				
			case InstructionSet.RFS:
				executeRFS();
				break;
				
			default:
				message="Unknown Instruction(OPCODE): "+ir;
				LOG.warning(message);
				break;
		}
		return true;
	}


	/**
	 * Jump to address if c(r) = 0
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeJZ() throws IOException{	
		int eAddress = getEA();
		WORD param=new WORD();
	    param.copy(cpu.getGPR(reg));
	    if(param.getLong() == 0) cpu.setPC(eAddress);
	    else cpu.increasePC();
		return true;
	}
	// @annie - implement function JNE, JCC, JMA, JSR, RFS, SOB, JGE
	
	/**
	 * Jump to address if c(r) != 0
	 * @return true
	 * @throws IOException
	 */
	private boolean executeJNE() throws IOException {
		int eAddress = getEA();
		WORD param=new WORD();
	    param.copy(cpu.getGPR(reg));
	    if(param.getLong() != 0) cpu.setPC(eAddress);
	    else cpu.increasePC();
		return true;
	}
	
	/**
	 * Jump to address if c(r) is no less than 0
	 * @return true
	 * @throws IOException
	 */
	
	private boolean executeJGE() throws IOException {
		int eAddress = getEA();
		WORD param=new WORD();
	    param.copy(cpu.getGPR(reg));
	    if(param.getLong() >= 0) cpu.setPC(eAddress);
	    else cpu.increasePC();
		return true;
	}
	
	/**
	 * Jump to address anyways
	 * @return
	 * @throws IOException
	 */
	private boolean executeJMA() throws IOException {
		int eAddress = getEAWithoutIX();
		cpu.setPC(eAddress);
		return true;
	}
	
	/**
	 * Subtract one from the c(r) and jump to address if c(r) > 0
	 * @return
	 * @throws IOException
	 */
	private boolean executeSOB() throws IOException {
		int eAddress = getEA();
		WORD param=new WORD();
		param.copy(cpu.getGPR(reg));
		cpu.getGPR(reg).setLong(param.getLong() - 1);
		param.copy(cpu.getGPR(reg));
		if(param.getLong() > 0) cpu.setPC(eAddress);
	    else cpu.increasePC();
		return true;
	}
	
	private boolean executeJSR() throws IOException {
		int eAddress = getEA();
		cpu.getGPR(3).setLong(cpu.getPC().getLong()+1); //set R3 to PC+1
		//cpu.getGPR(0).setLong(arg[0]);
		cpu.setPC(eAddress);
		return true;
	}
	
	/**
	 * jump to address if cc = 1
	 * @return
	 * @throws IOException
	 */
	private boolean executeJCC() throws IOException {
		int eAddress = getEAWithoutIX();
		if(cpu.getCC().getLong() == 1) cpu.setPC(eAddress);
		else cpu.increasePC();
		return true;
	}
	
	/**
	 * return from subroutine
	 * @return
	 * @throws IOException
	 */
	private boolean executeRFS() throws IOException {
		cpu.getGPR(0).setLong(address);
		cpu.setPC(cpu.getGPR(3).getLong());
		return true;
	}
	

}
