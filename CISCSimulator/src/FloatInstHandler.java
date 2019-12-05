import java.io.IOException;

/**
 * perform floating points and vector operations instruction
 */

/**
 * @author cozyu
 * @author youcao
 *
 */
public class FloatInstHandler extends InstructionHandler {

	/**
	 * @param cpu
	 */
	public FloatInstHandler(CPU cpu) {
		super(cpu);
		// TODO Auto-generated constructor stub
	}

	public boolean execute() throws IOException{
		LOG.info("Execute TRANS Instruction\n");
		message="";
		parseIR(cpu.getIR());
		switch(getOPCode())
		{
			case InstructionSet.FADD:
				executeFADD();
				break;
				
			case InstructionSet.FSUB:
				executeFSUB();
				break;
				
			case InstructionSet.VADD:
				executeVADD();
				break;
				
			case InstructionSet.VSUB:
				executeVSUB();
				break;
				
			case InstructionSet.CNVRT:
				executeCNVRT();
				break;
				
			case InstructionSet.LDFR:
				executeLDFR();
				break;
				
			case InstructionSet.STFR:
				executeSTFR();
				break;
				
			default:
				message="Unknown Instruction(OPCODE): "+ir;
				LOG.warning(message);
				break;
		}
		return true;
	}

	/**
	 * Add c(EA) to the floating-point register
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	private boolean executeFADD() throws IOException{	
		int eAddress=getEA();

		FloatingWORD param=new FloatingWORD();
		param.setLong(cpu.loadMemory(eAddress).getLong()+cpu.getFR(reg).getLong());
		if(param.getLong() > 64) {
			message+="==> FR"+reg+"+"+cpu.loadMemory(eAddress)+"  = OVERFLOW "+"\n";
			return true;
		}
		cpu.getFR(reg).copy(param);
		message+="==> FR"+reg+"+"+cpu.loadMemory(eAddress)+" = "+cpu.getFR(reg).getLong()+"\n";
	    return true;
	}
	
	/**
	 * Subtract c(EA) from the floating-point register
	 * @return true
	 * @throws IOException
	 */
	private boolean executeFSUB() throws IOException {
		int eAddress=getEA();
		FloatingWORD param=new FloatingWORD();
		
		param.setLong(cpu.loadMemory(eAddress).getLong()-cpu.getFR(reg).getLong());
		if(param.getLong() < -63) {
			message+="==> FR"+reg+"+"+cpu.loadMemory(eAddress)+"  = OVERFLOW "+"\n";
			return true;
		}
		cpu.getFR(reg).copy(param);
		message+="==> FR"+reg+"+"+cpu.loadMemory(eAddress)+" = "+cpu.getFR(reg).getLong()+"\n";
	    return true;
		
	}
	
	/**
	 * Add vectors in EA and EA+1
	 * @return true
	 * @throws IOException
	 */
	
	private boolean executeVADD() throws IOException {
		int eAddress = getEA();
		int eAddress1 = getEA()+1;
		FloatingWORD param=new FloatingWORD();
		float size = cpu.getFR(reg).getFloat();
		
		
		for (int i=0; i<size; i++) {
			
			long V1 = cpu.loadMemory(eAddress).getLong()+i;
			long V2 = cpu.loadMemory(eAddress1).getLong()+i;
			
			long result = V1+V2;
			V1++;
			V2++;
			//cpu.storeMemory(eAddress,result);			
			
		}
		message+="==> VADD"+reg+"+"+cpu.loadMemory(eAddress)+""+cpu.loadMemory(eAddress);
	    return true;
	}
	
	/**
	 * Subtract vector in EA+1 from EA
	 * @return
	 * @throws IOException
	 */
	private boolean executeVSUB() throws IOException {
		
		int eAddress = getEA();
		int eAddress1 = getEA()+1;
		
		FloatingWORD param=new FloatingWORD();
		
		float size = cpu.getFR(reg).getFloat();
		
		
		
		for (int i=0; i<size; i++) {
			
			long V1 = cpu.loadMemory(eAddress).getLong()+i;
			long V2 = cpu.loadMemory(eAddress1).getLong()+i;
			long result = V1-V2;
			V1++;
			V2++;
		    //cpu.storeMemory(eAddress,result);
		
		}
		message+="==> VADD"+reg+"+"+cpu.loadMemory(eAddress)+""+cpu.loadMemory(eAddress);
		return true;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean executeCNVRT() throws IOException {
		
	    return true;
	}
	
	/**
	 * Load floating register from memory, fr0 = c(EA), fr1 = c(EA+1)
	 * @return
	 * @throws IOException
	 */
	private boolean executeLDFR() throws IOException {
		int eAddress=getEA();
		WORD param1=new FloatingWORD();
		WORD param2=new FloatingWORD();
		param1.copy(cpu.loadMemory(eAddress));
		param2.copy(cpu.loadMemory(eAddress));
		cpu.getFR(0).setLong(param1.getLong());
		cpu.getFR(1).setLong(param2.getLong());
		message+="==> FR0"+" = mem["+eAddress+"] = "+cpu.getFR(0).getLong()+"\n";
		message+="==> FR1"+" = mem["+eAddress+1+"] = "+cpu.getFR(1).getLong()+"\n";
		return true;
	}
	
	/**
	 * Store floating register to memory, c(EA) = fr0, c(EA+1) = fr1
	 * @return
	 * @throws IOException
	 */
	private boolean executeSTFR() throws IOException {
		int eAddress=getEA();
		FloatingWORD param1=new FloatingWORD();
		FloatingWORD param2=new FloatingWORD();
	    param1.copy(cpu.getFR(0));
	    param2.copy(cpu.getFR(1));
		cpu.storeMemory(eAddress,param1);
		cpu.storeMemory(eAddress+1,param2);
		message+="==>  mem["+eAddress+"] = "+param1.getLong()+"\n";
		message+="==>  mem["+eAddress+1+"] = "+param2.getLong()+"\n";
		return true;
	}

	

}
