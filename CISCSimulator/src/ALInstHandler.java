import java.io.IOException;

/**
 * perform Arithmetic Logical instruction
 */

/**
 * @author cozyu
 *
 */
public class ALInstHandler extends InstructionHandler {

	/**
	 * @param cpu
	 */
	public ALInstHandler(CPU cpu) {
		super(cpu);
		// TODO Auto-generated constructor stub
	}

	public boolean execute() throws IOException{
		parseIR(cpu.getIR());
		switch(getOPCode())
		{
			case InstructionSet.AMR:
				executeAMR();
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
	private boolean executeAMR() {	
		return true;
	}
	// @yeongmok - implement function AMR, SMR, AIR, SIR, MLT, DVD, TRR, AND, ORR, NOT, SRC, RRC	
	
	
}
