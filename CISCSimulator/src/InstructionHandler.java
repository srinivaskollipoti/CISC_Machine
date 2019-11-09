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
	protected int reg=0;		// register
	protected int ireg=0;		// inded register
	protected int flag=0;
	protected int address=0;
	
	protected int al=0;			// 0 is arithmetic, 0 is logical
	protected int lr=0;			// 0 is right, 1 is left 
	protected int rx=0;			// rx
	protected int ry=0;			// ry
	protected int count=0;		// count
	protected int trapcode=0;	// trapcode
	
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
		rx=reg=ir.subSet(8,10).getInt();
		ry=ireg=ir.subSet(6,8).getInt();
		flag=ir.subSet(5,6).getInt();
		address=ir.subSet(0,5).getInt();
		
		al=ir.subSet(7,8).getInt();
		lr=ir.subSet(6,7).getInt();
		trapcode=count=ir.subSet(0,4).getInt();
		
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
	 * Check if the opcode is valid
	 * @return On case valid, true is returned, otherwise false is returned.
	 */
	public boolean checkOPCode()
	{
        opcode=cpu.getIR().subSet(10,16).getInt();
		Instruction inst=InstructionSet.getInstruction(opcode);
		if(inst==null)
		{
			message="Unknown Instruction(OPCODE:"+opcode+")\n";
			return false;
		}
		return true;
	}
	
	/**
	 * Execute instructions and print out the information in register and memory
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean execute() throws IOException
	{
		parseIR(cpu.getIR());
		showInstruction();
		int opcode=this.opcode;
		InstructionHandler handler=null;
		Instruction inst=InstructionSet.getInstruction(opcode);
		if(inst==null) {			
			message="Unknown Instruction(OPCODE:"+opcode+")\n";
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
			message="Failed to process instruction(OPCODE:"+opcode+")\n";
			LOG.warning(message);
			return false;
		}
		try {
			handler.execute();
		}catch(IllegalArgumentException e)
		{
			message="[ERROR] "+e.getMessage();
			LOG.warning(message);
			return false;
		}
		message=handler.getMessage();
		return true;
	}

	/**
	 * Print out the instruction meaning by parts.
	 */
	public void showInstruction()
	{
		LOG.info("[Instruction] "+Translator.getAsmCode(ir)+"\n");
	}

	/**
	 * Get string indicating current instruction.
	 * @return String indicating current instruction
	 */
	public String getString()
	{
		StringBuffer buffer=new StringBuffer();
		buffer.append("[IR] "+ir.getString());
        buffer.append("\n[OPCODE] "+opcode+" [GPR] "+reg+" [XR] "+ireg+" [FLAG] "+flag+" [ADDR] "+address+"\n");
        return buffer.toString();
	}
	
	public static int getOPCode(WORD ir){return ir.subSet(10,16).getInt();}
	public int getOPCode(){ return opcode;}
	public int getReg(){ return reg;}
	public int getIReg(){ return ireg;}
	public int getAddress(){ return address;}
	public int getFlag(){ return flag;}
	
	/**
	 * Return result string of operation.
	 * @return result string of operation.
	 */
	public String getMessage(){ return message;}
}
