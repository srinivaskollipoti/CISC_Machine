import java.io.IOException;
import java.util.logging.Logger;

/**
 * 
 */

/**
 * @author cozyu
 *
 */
public class InstructionSet {
	private final static Logger LOG = Logger.getGlobal();
		
	static final int LDA=3;
	static final int STR=2;
	static final int LDR=1;
	static final int LDX=33;
	static final int STX=34;
	
	private WORD ir=new WORD();
	private ControlUnit controller;
	
	private int opcode=0;
	private int reg=0;
	private int ireg=0;
	private int flag=0;
	private int address=0;
	
	private GBitSet addressBit=new GBitSet(6);
	
	
	public InstructionSet(ControlUnit controller)
	{
		this.controller=controller;	
		this.ir.copy(controller.getIR());
		opcode=ir.subSet(10,15).getInt();
		reg=ir.subSet(8,9).getInt();
		ireg=ir.subSet(6,7).getInt();
		flag=ir.subSet(5,5).getInt();
		
		addressBit.copy(ir.subSet(0,4));
		address=addressBit.getInt();
	}


	//LDR 2,0,13    000001 	10 	00 	0 	01101   => R[2] = M[13], R[2]=8
	public boolean executeLDR() throws IOException {
		
		controller.GPR[reg].copy(controller.memory.load(address));
		controller.showRegister();
		controller.showMemory();		
		//System.exit(1);
		return true;
	}

	//LDA 1,0,8     000011 	01 	00 	0 	01000   => R[1] = 8
	public boolean executeLDA() {
		controller.GPR[reg].setLong(address);
		controller.showRegister();
		controller.showMemory();		
		return true;
	}
	

	//LDX 1,13      100001 	00 	01 	0 	01101   => X[1] = M[13], X[1]=8
	public boolean executeLDX() {

		return true;
	}

	//STR 1,0,13    000010 	01 	00 	0 	01101   => M[13] = 8
	public boolean executeSTR() throws IOException {
		WORD param=new WORD();
		param.setLong(address);
		controller.memory.store(address,param);
		controller.showRegister();
		controller.showMemory();		
		
		return true;
	}
	

	//STX 1,31 		100010 	00 	01 	0 	11111   => M[31] = X[1], M[31]=8
	public boolean executeSTX() {
		return true;
	}
	
	public boolean execute() throws IOException
	{
		switch(getOPCode())
		{
			case InstructionSet.LDA:
				System.out.println("Execute LDA");
				executeLDA();
				break;
			case InstructionSet.LDR:
				System.out.println("Execute LDR");
				executeLDR();
				
				break;
			case InstructionSet.LDX:
				
				System.out.println("Execute LDX");
				executeLDX();
				
				break;
			case InstructionSet.STR:
				
				System.out.println("Execute STR");
				executeSTR();
				
				break;
			case InstructionSet.STX:
				
				System.out.println("Execute STX");
				executeSTX();
				
				break;
			default:
				LOG.warning("Unknown Instruction");
				break;
		}
		return true;
	}

	
	public void showInstruction()
	{
		System.out.println("### IR STATUS START ###");
		System.out.println("[OPCODE] "+opcode);
		System.out.println("[GPR   ] "+reg);
		System.out.println("[XR    ] "+ireg);
		System.out.println("[FLAG  ] "+flag);
		System.out.println("[ADDR  ] "+address);
		System.out.println("### IR STATUS END ###");
		
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
}
