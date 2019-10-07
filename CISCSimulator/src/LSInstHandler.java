import java.io.IOException;

/**
 * 
 */

/**
 * @author cozyu
 *
 */
public class LSInstHandler extends InstructionHandler {

	
	/**
	 * @param cpu
	 */
	public LSInstHandler(CPU cpu) {
		super(cpu);
	}
	
	public boolean execute() throws IOException{
		parseIR(cpu.getIR());
		switch(getOPCode())
		{
			case InstructionSet.LDA:
				executeLDA();
				break;
			case InstructionSet.LDR:
				executeLDR();
				break;
			case InstructionSet.LDX:
				executeLDX();
				break;
			case InstructionSet.STR:
				executeSTR();
				break;
			case InstructionSet.STX:
				executeSTX();
				break;
			default:
				message="Unknown Instruction(OPCODE): "+ir;
				LOG.warning(message);
				break;
		}
		return true;
	}
	
	/**
	 * Load register from a given memory address.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeLDR() throws IOException {
		int eAddress=getEA();
		cpu.getGPR(reg).copy(cpu.loadMemory(eAddress));
		return true;
	}
	
	/**
	 * Load register with address
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeLDA() throws IOException {
		int eAddress=getEA();
		cpu.getGPR(reg).setLong(eAddress);
	    return true;
	}
	
	/**
	 * Store Register To Memory.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeSTR() throws IOException {
		int eAddress=getEA();
		WORD param=new WORD();
	    param.copy(cpu.getGPR(reg));
		cpu.storeMemory(eAddress,param);
		return true;
	}
	
	/**
	 * Load Index Register from Memory.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeLDX() throws IOException {
		int eAddress=getEAWithoutIX();
		cpu.getIX(ireg).copy(cpu.loadMemory(eAddress));
		return true;
	}
	
	/**
	 * Store Index Register to Memory.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeSTX() throws IOException {
		int eAddress=getEAWithoutIX();
		WORD param=new WORD();
	    param.copy(cpu.getIX(ireg));
		cpu.storeMemory(eAddress,param);
		return true;
	}	
	
}
