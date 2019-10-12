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
	int counter = 0;
	int d = 'Z';
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
	private boolean executeIN() throws FileNotFoundException{	
		long devId = address;
		if(devId == 0) {
			cpu.getGPR(reg).setLong(content);
		}else if(devId == 1) {
			System.out.println("Printer cannot input.");
		}else if(devId == 2) {
			//read from reader.txt
			File file = new File("reader.txt");
			Scanner sc = new Scanner(file);
			
			try {
				 char in = sc.next().charAt(counter);
				 counter++;
				 content = (int) in;
				 cpu.getGPR(reg).setLong(content);
			} catch (IndexOutOfBoundsException e) {
				counter = 0;
				System.out.println("reached the end.");
				
			}
		}else {
			//content load from register
			cpu.getGPR(reg).setLong(content);
		}
		
		return true;
	}
	
	private boolean executeOUT() {	
		long devId = address;
		if(devId == 0 || devId == 2) {
			System.out.println("This device cannot output");; //keyboard and card reader cannot use out
		}else {
			WORD param=new WORD();
			param.copy(cpu.getGPR(reg));
			content = param.getLong(); 
			//Assign the content to device.
			//System.out.println(Long.toString(content));
		}
		
		return true;
	}
	

}
