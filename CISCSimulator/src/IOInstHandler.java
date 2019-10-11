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
	
	public boolean setInputCode(String[] arrAsmCode) {
		InstructionHandler ih = new InstructionHandler(cpu);
		message="";
		for(int i=0; i<arrAsmCode.length;i++) {
			arrAsmCode[i]=arrAsmCode[i].toUpperCase();
			WORD binCode=ih.getBinCode(arrAsmCode[i]);
			if(binCode==null) {
				message=ih.getMessage()+"Failed to parse user program : "+arrAsmCode[i];
				LOG.warning(message);
				return false;
			}
			content = binCode.getLong();
		}
		
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
