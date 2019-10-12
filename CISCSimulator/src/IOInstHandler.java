import java.io.IOException;
import java.io.File; 
import java.io.FileNotFoundException; 
import java.util.Scanner; 
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
		LOG.info("Execute Io Instruction\n");

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
	private boolean executeIN() throws FileNotFoundException, NumberFormatException{	
		long devId = address;
		if(devId == 0) {
			cpu.getGPR(reg).setLong(content);
		}else if(devId == 1) {
			return false;
		}else if(devId == 2) {
			//read from reader.txt
			File file = new File("reader.txt");
			Scanner sc = new Scanner(file);
			sc.useDelimiter("\\Z");
		    String in = sc.nextLine();
		    content = Long.parseLong(in);
		    cpu.getGPR(reg).setLong(content);
		}else {
			//content load from register
			cpu.getGPR(reg).setLong(content);
		}
		
		return true;
	}
	
	private boolean executeOUT() {	
		long devId = address;
		if(devId == 0 || devId == 2) {
			return false; //keyboard and card reader cannot use out
		}
		
		WORD param=new WORD();
		param.copy(cpu.getGPR(reg));
		//use content.toString() to convert the content into string to show on device
		content = param.getLong(); 
		//System.out.println(Long.toString(content));
		return true;
	}
	
	private boolean executeHLT() {	
		// HLT: stops running and resume running if other instructions are made
		return true;
	}
	// @annie - implement function HLT, IN, OUT


}
