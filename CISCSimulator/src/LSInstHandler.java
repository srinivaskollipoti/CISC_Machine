import java.io.IOException;


/**
 * Perform load and store instruction
 * @author cozyu
 *
 */
public class LSInstHandler extends InstructionHandler {

	
	public LSInstHandler(CPU cpu) {
		super(cpu);
	}
	
	public boolean execute() throws IOException{
		LOG.info("Execute LS Instruction\n");
		
		message="";
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
		message="==> R"+reg+" = mem["+eAddress+"] = "+cpu.getGPR(reg).getLong()+"\n";
		return true;
	}
	
	/**
	 * Load register with address
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeLDA() throws IOException {
		int eAddress=getEA();
		cpu.getGPR(reg).setLong(eAddress);
		message="==> R"+reg+" = "+eAddress+"\n";
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
		message="==>  mem["+eAddress+"] = "+param.getLong()+"\n";
		return true;
	}
	
	/**
	 * Load Index Register from Memory.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeLDX() throws IOException {
		if(ireg<1 || ireg>3)
			throw new IllegalArgumentException("Index Register must be between 1 and 3\n");
		
		int eAddress=getEAWithoutIX();
		cpu.getIX(ireg).copy(cpu.loadMemory(eAddress));
		message="==> IX"+ireg+" = mem["+eAddress+"] = "+cpu.getIX(ireg).getLong()+"\n";
		return true;
	}
	
	/**
	 * Store Index Register to Memory.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeSTX() throws IOException {
		if(ireg<1 || ireg>3)
			throw new IllegalArgumentException("Index Register must be between 1 and 3\n");
		
		int eAddress=getEAWithoutIX();
		WORD param=new WORD();
	    param.copy(cpu.getIX(ireg));
		cpu.storeMemory(eAddress,param);
		message="==> mem["+eAddress+"] = "+param.getLong()+"\n";
		return true;
	}	
	
}
