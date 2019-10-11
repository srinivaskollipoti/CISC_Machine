import java.io.IOException;

/**
 * perform Transfer instruction
 */

/**
 * @author cozyu
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
			default:
				message="Unknown Instruction(OPCODE): "+ir;
				LOG.warning(message);
				break;
		}
		return true;
	}


	/**
	 * 
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeJZ() {	
		return true;
	}
	// @annie - implement function JNE, JCC, JMA, JSR, RFS, SOB, JGE
	

}
