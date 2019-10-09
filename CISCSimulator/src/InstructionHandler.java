import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author cozyu
 * @author youcao  documented by youcao.
 * A class that process given instructions.
 */
public class InstructionHandler{
	protected final static Logger LOG = Logger.getGlobal();	
	protected WORD ir;
	protected CPU cpu;
	
	private LSInstHandler lsInst;
	private ALInstHandler alInst;
	private IOInstHandler ioInst;
	private TransInstHandler trInst;
	
	protected int opcode=0;
	protected int reg=0;
	protected int ireg=0;
	protected int flag=0;
	protected int address=0;
	
	protected String message=new String();
	
	/**
	 * A constructor to initializes instruction set.
	 * @param cpu to use this class
	 */
	public InstructionHandler(CPU cpu)
	{
		this.cpu=cpu;		
		ir=new WORD();
	}
	
	public void init()
	{
		lsInst=new LSInstHandler(cpu);
		alInst=new ALInstHandler(cpu);
		ioInst=new IOInstHandler(cpu);
		trInst=new TransInstHandler(cpu);
	}
	
	/**
	 * Set up the instruction code by parts from given translated instruction.
	 * @param ir A WORD containing the translated instruction. Format is explained in user guide.
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	protected boolean parseIR(WORD ir)
	{
		this.ir.copy(ir);
        opcode=ir.subSet(10,16).getInt();
		reg=ir.subSet(8,10).getInt();
		ireg=ir.subSet(6,8).getInt();
		flag=ir.subSet(5,6).getInt();
		address=ir.subSet(0,5).getInt();
		return true;
	}
	
	
	/**
	 * Get the effective address without given register
	 * @return an integer for the effective address.
	 */
	protected int getEAWithoutIX() throws IOException
	{
		int eAddress=address;
		if(flag==0)
		{
			eAddress=address;
		}
		else if(flag==1)
		{
			eAddress=cpu.loadMemory(address).getInt();
		}
		else throw new IOException("Wrong indirect flag");
		return eAddress;
		
	}
	
	/**
	 * Get the effective address with given register.
	 * @return An integer of the effective address.
	 */
	protected int getEA() throws IOException
	{
		int eAddress=address;
		if(flag==0)
		{
			if(ireg==0) {
				eAddress=address;
			}
			else{
				eAddress=cpu.getIX(ireg).getInt()+address;
			};			
		}
		else if(flag==1)
		{
			if(ireg==0) {
				eAddress=cpu.loadMemory(address).getInt();
			}else {
				int iAddress=cpu.getIX(ireg).getInt()+address;
				eAddress=cpu.loadMemory(iAddress).getInt();
			}
		}
		else throw new IOException("Wrong indirect flag");
		return eAddress;
	}
	
	
	/**
	 * Execute instructions and print out the information in register and memory
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean execute() throws IOException
	{
		parseIR(cpu.getIR());
		int opcode=this.opcode;
		InstructionHandler handler=null;
		Instruction inst=InstructionSet.getInstruction(opcode);
		if(inst==null) {
			message="Unknown Instruction(OPCODE): "+opcode;
			LOG.warning(message);
			return false;
		}
		switch(inst.getType())
		{
		case LS_INST:
			handler=lsInst;
			break;
		case TRANS_INST:		
			handler=trInst;
			break;
		case AL_INST:	
			handler=alInst;
			break;
		case IO_MISC_INST:	
			handler=ioInst;
			break;
		default:
			break;
		}
		if(handler==null)
		{
			message="Failed to process instruction(OPCODE): "+opcode;
			LOG.warning(message);
			return false;
		}
		handler.execute();
		message=handler.getMessage();
		return true;
	}

	/**
	 * Print out the instruction meaning by parts.
	 */
	public void showInstruction()
	{
		System.out.println("### IR STATUS START ###");
		System.out.println("[IR] "+ir);
        System.out.println("[OPCODE] "+opcode+" [GPR] "+reg+" [XR] "+ireg+" [FLAG] "+flag+" [ADDR] "+address);
		System.out.println("### IR STATUS END ###");		
	}

	/**
	 * Get string indicating current instruction.
	 * @return String indicating current instruction
	 */
	public String getString()
	{
		StringBuffer buffer=new StringBuffer();
		buffer.append("[IR] "+ir.getString());
        buffer.append("\n[OPCODE] "+opcode+" [GPR] "+reg+" [XR] "+ireg+" [FLAG] "+flag+" [ADDR] "+address);
        return buffer.toString();
	}

	public int getOPCode(WORD ir)
	{
		return ir.subSet(10,16).getInt();
	}
	
	public int getOPCode()
	{
		return opcode;
	}
	public int getReg()
	{
		return reg;
		
	}
	public int getIReg()
	{
		return ireg;
	}
	public int getAddress()
	{
		return address;
	}
	public int getFlag()
	{
		return flag;
	}
		
	

	/**
	 * get assemble code from current instruction
	 * @return An assemble code string.
	 */
	public String getAsmCode()
	{	
		return getAsmCode(this.ir);
	}
	
	
	/**
	 * get assemble code from specific instruction
	 * @return An assemble code string.
	 */
	public static String getAsmCode(WORD ir)
	{
		int opcode=ir.subSet(10,16).getInt();
        int reg=ir.subSet(8,10).getInt();
        int ireg=ir.subSet(6,8).getInt();
        int flag=ir.subSet(5,6).getInt();
        int address=ir.subSet(0,5).getInt();
        
		StringBuffer buffer=new StringBuffer();
		Instruction inst=InstructionSet.getInstruction(opcode);
		if (inst==null) return null;
		buffer.append(inst.getName()+" ");
		if(inst.isReg()==true)
			buffer.append(reg+",");
		if(inst.isIReg()==true)
			buffer.append(ireg+",");
		if(inst.isAddr()==true)
			buffer.append(address+",");
		buffer.setLength(buffer.length()-1);
		if(inst.isFlag()==true && flag==1)
			buffer.append(",I");
		
		return buffer.toString();
	}
	
	
	/**
	 * Convert the input instruction into a machine code.
	 * @return the machine code in WORD format.
	 */
	public WORD getBinCode(String asmCode)
	{
		message="";
		
		WORD result=new WORD();
		
		int opcode=0;
		int reg=0;
		int ireg=0;
		int flag=0;
		int address=0;
		
		asmCode=asmCode.trim().toUpperCase();
		String[] arrStr=asmCode.split(" ",2);
		Instruction inst=InstructionSet.getInstruction(arrStr[0]);
		if(inst==null) {
			message="Unsupported opcode : "+arrStr[0]+")\n";
			LOG.warning(message);
			return null;
		}
		opcode=inst.getCode();
		
		if(arrStr.length<2)
		{
			message=arrStr[0]+" accepts "+inst.getParamLength()+" parameters.\nInput parameters are not matched - "+asmCode+"\n";
			LOG.warning(message);
			return null;
		}
		
		String lastParam=arrStr[1];
		if(lastParam.endsWith(",I"))
		{
			arrStr[1]=lastParam.substring(0,lastParam.length()-2);
			flag=1;
		}
		
		String[] arrParam=arrStr[1].split(",",3);
		int paramLength=arrParam.length;
		if(paramLength!=inst.getParamLength())
		{
			message=arrStr[0]+" accepts "+inst.getParamLength()+" parameters.\nInput parameters are not matched - "+asmCode+"\n";
			LOG.warning(message);			
			return null;
		}
		for(int i=0;i<paramLength;i++)
			arrParam[i]=arrParam[i].trim();
		
		try {
		if(inst.isReg()==false)
		{
			
			ireg=Integer.valueOf(arrParam[0]);
			address=Integer.valueOf(arrParam[1]);
		}else {
			reg=Integer.valueOf(arrParam[0]);
			ireg=Integer.valueOf(arrParam[1]);
			address=Integer.valueOf(arrParam[2]);
		}
		}catch(java.lang.NumberFormatException e)
		{
			message="Parameter must be number : "+asmCode+"\n";
			LOG.warning(message);			
			return null;
		}
		// IX range limitation for LDX, STX
		if(opcode==InstructionSet.LDX || opcode==InstructionSet.STX) 
		{
			if(ireg<1 || ireg>3)
			{
				message="Index Register must be between 1-3 : "+asmCode+"\n";
				LOG.warning(message);			
				return null;
			}
		}
			
		GBitSet bitOP=new GBitSet(6);
		GBitSet bitReg=new GBitSet(2);
		GBitSet bitIReg=new GBitSet(2);
		GBitSet bitAddress=new GBitSet(5);
		try {
			bitOP.setLong(opcode);	
			bitReg.setLong(reg);
			bitIReg.setLong(ireg);
			bitAddress.setLong(address);
		}catch(IllegalArgumentException e)
		{
			message=e.getMessage()+" : "+asmCode+"\n";
			LOG.warning(message);			
			return null;
		}

		result.setLong(address);	
		
		int index=15;
		for(int i=bitOP.length-1;i>=0;i--)
		{
			if (bitOP.get(i)==true) 
				result.set(index);
			index--;
		}
		for(int i=bitReg.length-1;i>=0;i--)
		{
			if (bitReg.get(i)==true) 
				result.set(index);
			index--;
		}
		
		for(int i=bitIReg.length-1;i>=0;i--)
		{
			if (bitIReg.get(i)==true) 
				result.set(index);
			index--;
		}

		if(flag==1)
		{
			result.set(index);	
		}
		index--;
			
		return result;
	}
	
	/**
	 * Return result string of operation.
	 * @return result string of operation.
	 */
	public String getMessage()
	{
		return message;
	}
}
