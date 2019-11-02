import java.io.IOException;
import java.io.File; 
import java.io.FileNotFoundException; 
import java.util.Scanner; 



/**
 *  perform IO instruction and Miscellaneous Instruction
 * @author cozyu (Yeongmok You)
 * @author youcao
 */
public class IOInstHandler extends InstructionHandler {
	
	public IOInstHandler(CPU cpu) {
		super(cpu);
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
			char in=cpu.getInputChar(IOC.CARD_READER);
			if(in==IOC.NONE_INPUT)
			{
				//read from reader.txt
				File file = new File("reader.txt");
				Scanner sc = new Scanner(file);
				sc.useDelimiter("\0");
			    String readText=sc.next(); 
			    cpu.getIOC().appendIOBuffer(IOC.CARD_READER, readText);
			    sc.close();
			    in=cpu.getInputChar(IOC.CARD_READER);
			}
			cpu.getGPR(reg).setLong(in);
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
			WORD param=new SignedWORD();
			param.copy(cpu.getGPR(reg));
			char output = (char) param.getLong(); 
			cpu.setOutputChar(address,output);
			message="==> Output character is "+output+"\n";
		}
		
		return true;
	}

	/**
	 * Execute Halt instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeHLT() {
		cpu.getSimulator().setStop();
		return true;
	}
	
}
