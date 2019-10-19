import java.io.IOException;

/**
 * perform Arithmetic Logical instruction
 */

/**
 * @author cozyu
 *
 */
public class ALInstHandler extends InstructionHandler {

	ALU alu;
	/**
	 * @param cpu
	 */
	public ALInstHandler(CPU cpu) {
		super(cpu);
		alu=cpu.getALU();
	}

	public boolean execute() throws IOException{
		message="";
		LOG.info("Execute AL Instruction\n");
		parseIR(cpu.getIR());
		switch(getOPCode())
		{
			case InstructionSet.AMR:
				executeAMR();
				break;
			case InstructionSet.SMR:
				executeSMR();
				break;
			case InstructionSet.AIR:
				executeAIR();
				break;
			case InstructionSet.SIR:
				executeSIR();
				break;
			case InstructionSet.MLT:
				executeMLT();
				break;
			case InstructionSet.DVD:
				executeDVD();
				break;
			case InstructionSet.TRR:
				executeTRR();
				break;
			case InstructionSet.AND:
				executeAND();
				break;
			case InstructionSet.ORR:
				executeORR();
				break;
			case InstructionSet.NOT:
				executeNOT();
				break;
			case InstructionSet.SRC:
				executeSRC();
				break;
			case InstructionSet.RRC:
				executeRRC();
				break;
			default:
				message="Unknown Instruction(OPCODE): "+ir;
				LOG.warning(message);
				break;
		}
		return true;
	}

	private void checkRegIReg()
	{
		if(reg<0 || reg>3)
			throw new IllegalArgumentException("Register must be between 0 and 3\n");
		if(ireg<0 || ireg>3)
			throw new IllegalArgumentException("Index Register must be between 0 and 3\n");
	}
	/**
	 * Execute AMR instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 * @throws IOException 
	 */
	private boolean executeAMR() throws IOException{
		checkRegIReg();
		int eAddress=getEA();
		WORD result=alu.add(cpu.getGPR(reg),cpu.loadMemory(eAddress));
		message=alu.getMessage();
		cpu.getGPR(reg).copy(result);
		return true;
	}

	/**
	 * Execute SMR instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 * @throws IOException 
	 */
	private boolean executeSMR() throws IOException {	
		checkRegIReg();
		int eAddress=getEA();
		WORD result=alu.sub(cpu.getGPR(reg),cpu.loadMemory(eAddress));
		message=alu.getMessage();
		cpu.getGPR(reg).copy(result);
		return true;
	}
	
	/**
	 * Execute AIR instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeAIR() {	
		if(reg<0 || reg>3)
			throw new IllegalArgumentException("Register must be between 0 and 3\n");
		if (address==0)
			return true;
		WORD immed=new WORD();
		immed.setLong(address);
		WORD result=alu.add(cpu.getGPR(reg),immed);
		message=alu.getMessage();
		cpu.getGPR(reg).copy(result);
		return true;
	}
	
	/**
	 * Execute SIR instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeSIR() {	
		if(reg<0 || reg>3)
			throw new IllegalArgumentException("Register must be between 0 and 3\n");
		if (address==0)
			return true;
		WORD immed=new WORD();
		immed.setLong(address);
		WORD result=alu.sub(cpu.getGPR(reg),immed);
		message=alu.getMessage();
		cpu.getGPR(reg).copy(result);
		return true;
	}
	
	/**
	 * Execute MLT instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeMLT() {	
		if(rx!=0 && rx!=2)
			throw new IllegalArgumentException("rx must be 0 or 2\n");
		if(ry!=0 && ry!=2)
			throw new IllegalArgumentException("ry must be 0 or 2\n");
		WORD[] result=alu.mul(cpu.getGPR(rx),cpu.getGPR(ry));
		message=alu.getMessage();
		cpu.getGPR(rx).copy(result[0]);
		cpu.getGPR(rx+1).copy(result[1]);
		return true;
	}
	
	/**
	 * Execute DVD instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeDVD() {	
		if(rx!=0 && rx!=2)
			throw new IllegalArgumentException("rx must be 0 or 2\n");
		if(ry!=0 && ry!=2)
			throw new IllegalArgumentException("ry must be 0 or 2\n");
		WORD[] result=alu.div(cpu.getGPR(rx),cpu.getGPR(ry));
		message=alu.getMessage();
		if(result==null) return false;

		cpu.getGPR(rx).copy(result[0]);
		cpu.getGPR(rx+1).copy(result[1]);
		return true;
	}
	
	/**
	 * Execute TRR instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeTRR() {	
		if(rx<0 && rx>3)
			throw new IllegalArgumentException("rx must be between 0 and 3\n");
		if(ry<0 && ry>3)
			throw new IllegalArgumentException("ry must be between 0 and 3\n");
		alu.equal(cpu.getGPR(rx), cpu.getGPR(ry));
		message=alu.getMessage();
		return true;
	}
	
	/**
	 * Execute AND instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeAND() {	
		if(rx<0 && rx>3)
			throw new IllegalArgumentException("rx must be between 0 and 3\n");
		if(ry<0 && ry>3)
			throw new IllegalArgumentException("ry must be between 0 and 3\n");
		
		WORD result=alu.and(cpu.getGPR(rx), cpu.getGPR(ry));
		message=alu.getMessage();
		cpu.getGPR(rx).copy(result);
		return true;
	}
	
	/**
	 * Execute ORR instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeORR() {	
		if(rx<0 && rx>3)
			throw new IllegalArgumentException("rx must be between 0 and 3\n");
		if(ry<0 && ry>3)
			throw new IllegalArgumentException("ry must be between 0 and 3\n");
		
		WORD result=alu.or(cpu.getGPR(rx), cpu.getGPR(ry));
		message=alu.getMessage();
		cpu.getGPR(rx).copy(result);
		return true;
	}
	
	/**
	 * Execute NOT instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeNOT() {	
		if(rx<0 && rx>3)
			throw new IllegalArgumentException("rx must be between 0 and 3\n");
		
		WORD result=alu.not(cpu.getGPR(rx));
		message=alu.getMessage();
		cpu.getGPR(rx).copy(result);
		return true;
	}
	
	/**
	 * Execute SRC instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeSRC() {
		if(reg<0 && reg>3)
			throw new IllegalArgumentException("reg must be between 0 and 3\n");
		WORD result=alu.shift(cpu.getGPR(reg), count, lr==1, al==1);
		cpu.getGPR(reg).copy(result);
		return true;
	}
	
	/**
	 * Execute RRC instruction
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeRRC() {	
		if(reg<0 && reg>3)
			throw new IllegalArgumentException("reg must be between 0 and 3\n");
		WORD result=alu.rotate(cpu.getGPR(reg), count, lr==1, al==1);
		cpu.getGPR(reg).copy(result);
		return true;
	}
	
	
}
