import java.io.IOException;
import java.io.File; 
import java.io.FileNotFoundException; 
import java.util.Scanner; 
import java.util.ArrayList;



/**
 *  perform IO instruction and Miscellaneous Instruction
 * @author cozyu (Yeongmok You)
 * @author youcao
 */
public class IOInstHandler extends InstructionHandler {
	
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

	/**
	 * Execute IN instruction, it doesn't support PRINTER
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeIN() throws FileNotFoundException{	
		int devId = address;
		if(devId == IOC.PRINTER) {
			message= "Printer is not input device.\n";
		}else if(devId == IOC.CARD_READER) {
			//read from reader.txt
			File file = new File("reader.txt");
			Scanner sc = new Scanner(file);
			
			try {
				 char in = sc.next().charAt(counter);
				 counter++;
				 cpu.getGPR(reg).setLong((int) in);
			} catch (IndexOutOfBoundsException e) {
				counter = 0;
				LOG.warning("reached the end.");
				
			}
		}else {
			char in = cpu.getInputChar(address);
			if(in!=IOC.NONE_INPUT) 
			{
				cpu.getGPR(reg).setLong(in);
				message="==> Input character is "+in+"\n";
			}
		}
		
		return true;
	}
	
	/**
	 * Execute OUT instruction, it doesn't support KEYBOARD and CARD READER
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeOUT() {	
		long devId = address;
		if(devId == IOC.KEYBOARD || devId == IOC.CARD_READER) {
			message=String.format("Device %d is not output device\n",devId); //keyboard and card reader cannot use out
		}else {
			WORD param=new WORD();
			param.copy(cpu.getGPR(reg));
			char output = (char) param.getLong(); 
			cpu.setOutputChar(address,output);
		}
		
		return true;
	}

	/**
	 * Execute Halt instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeHLT() {
		cpu.getSimulator().setStop();
		//message="[NOTICE] System halted\n";
		return true;
	}
	
}
