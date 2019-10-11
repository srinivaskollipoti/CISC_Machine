import java.io.IOException;
import java.util.ArrayList;

//import CPU.CPUState;

/**
 *  perform IO instruction and Miscellaneous Instruction
 */

/**
 * @author cozyu
 * @author youcao
 */
public class IOInstHandler extends InstructionHandler {

	/**
	 * @param cpu
	 */
	long content;
	public IOInstHandler(CPU cpu) {
		super(cpu);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Convert the string into Long type for storage
	 * @param text The input got from the input textarea in GUI
	 * @return
	 */
	public boolean setInputCode(String text) {
		String input = text;
		content = (int) input.charAt(0);
		
		return true;
	}
	
	public boolean execute() throws IOException{
		parseIR(cpu.getIR());
		switch(getOPCode())
		{
			case InstructionSet.IN:
				executeIN();
				break;
				
			case InstructionSet.OUT:
				executeOUT();
				break;
				
			case InstructionSet.HLT:
				executeHLT();
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
		long devId = address;
		cpu.getGPR(reg).setLong(content);
		return true;
	}
	
	private boolean executeOUT() {	
		long devId = address;
		WORD param=new WORD();
		param.copy(cpu.getGPR(reg));
		content = param.getLong(); //use content.toString() to convert the content into string to show on device
		return true;
	}
	
	private boolean executeHLT() {	
		
		return true;
	}
	// @annie - implement function HLT, IN, OUT


}
