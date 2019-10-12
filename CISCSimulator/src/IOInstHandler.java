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
		message="";
		parseIR(cpu.getIR());
		switch(getOPCode())
		{
			case InstructionSet.IN:
				executeIN();
				break;
			case InstructionSet.OUT:
				executeOUT();
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
	private boolean executeIN() {	
		char input=cpu.getInputChar(address);
		if(input!=IOC.NONE_INPUT)
		{
			message="[+] Input character is "+input+"\n";
		}
		return true;
	}
	
	private boolean executeOUT() {	
		char output='1';
		cpu.setOutputChar(address,output);
		return true;
	}

}
