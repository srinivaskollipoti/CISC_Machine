import java.io.IOException;

/**
 *  perform IO instruction and Miscellaneous Instruction
 */

/**
 * @author cozyu
 *
 */
public class IOInstHandler extends InstructionHandler {

	/**
	 * @param cpu
	 */
	public IOInstHandler(CPU cpu) {
		super(cpu);
		// TODO Auto-generated constructor stub
	}
	
	public boolean execute() throws IOException{
		LOG.info("Execute Io Instruction\n");

		parseIR(cpu.getIR());
		switch(getOPCode())
		{
			
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
	private boolean executeIN() {	
		return true;
	}
	// @annie - implement function HLT, IN, OUT


}
