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
	int counter = 0;
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

	
	private boolean executeIN() throws FileNotFoundException{	
		int devId = address;
		if(devId == 1) {
			System.out.println("Printer cannot input.");
		}else if(devId == 2) {
			//read from reader.txt
			File file = new File("reader.txt");
			Scanner sc = new Scanner(file);
			
			try {
				 char in = sc.next().charAt(counter);
				 counter++;
				 cpu.getGPR(reg).setLong((int) in);
			} catch (IndexOutOfBoundsException e) {
				counter = 0;
				System.out.println("reached the end.");
				
			}
		}else {
			char in = cpu.getInputChar(address);
			cpu.getGPR(reg).setLong(in);
			if(in!=IOC.NONE_INPUT)
				{
					message="[+] Input character is "+in+"\n";
				}
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
			char output = (char) param.getLong(); 
			cpu.setOutputChar(address,output);
		}
		
		return true;
	}
	
	private boolean executeHLT() {
		return true;
	}
	
}
