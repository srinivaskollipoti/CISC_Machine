import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

class Instruction
{
	private String name; 
	private int code; 
	private int paramLength; 
	private boolean isReg; 
	private boolean isIReg; 
	private boolean isAddr; 
	private boolean isFlag;		
	
	public Instruction(String name,int code,int paramLength,boolean isReg,boolean isIReg, boolean isAddr, boolean isFlag)
	{
		this.name=name;
		this.code=code;
		this.paramLength=paramLength;
		this.isReg=isReg;
		this.isIReg=isIReg;
		this.isAddr=isAddr;
		this.isFlag=isFlag;
	}

	public String getName() { return this.name; }
	public int getCode() { return this.code; }
	public int getParamLength() { return this.paramLength; }
	public boolean isReg() { return this.isReg; }
	public boolean isIReg() { return this.isIReg; }
	public boolean isAddr() { return this.isAddr; }
	public boolean isFlag() { return this.isFlag; }
	
}

/**
 * @author cozyu
 * A class that process given instructions.
 */
public class InstructionHandler {
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
	
	Hashtable< Integer, Instruction> instSet= new Hashtable< Integer, Instruction>();
	Hashtable< String, Integer> textToCode= new Hashtable< String, Integer>();
	
	private String message=new String();
	/**
	 * A constructor initializes with given controller.
	 * @param controller
	 */
	public InstructionHandler(ControlUnit controller)
	{
		this.controller=controller;
		instSet.put(LDA,new Instruction("LDA",LDA,3,true,true,true,true));
		instSet.put(STR,new Instruction("STR",STR,3,true,true,true,true));
		instSet.put(LDR,new Instruction("LDR",LDR,3,true,true,true,true));
		instSet.put(LDX,new Instruction("LDX",LDX,2,false,true,true,true));
		instSet.put(STX,new Instruction("STX",STX,2,false,true,true,true));
		textToCode.put("LDA",LDA);
		textToCode.put("STR",STR);
		textToCode.put("LDR",LDR);
		textToCode.put("LDX",LDX);
		textToCode.put("STX",STX);

	}
	
	public boolean setIR()
	{
		return setIR(controller.getIR());
	}
	
	/**
	 * Set up the intruction code by parts from given translated instruction.
	 * @param ir A WORD containing the translated instruction. Format is explained in user guide.
	 * @return A boolean indicating if done.
	 */
	public boolean setIR(WORD ir)
	{
		this.ir.copy(ir);
        opcode=ir.subSet(10,16).getInt();
		reg=ir.subSet(8,10).getInt();
		ireg=ir.subSet(6,8).getInt();
		flag=ir.subSet(5,6).getInt();
		addressBit.copy(ir.subSet(0,5));
		address=addressBit.getInt();
		return true;
	}
        
	/**
	 * Load register from a given memory address.
	 */
	//LDR 2,0,13    000001 	10 	00 	0 	01101   => R[2] = M[13], R[2]=8
	public boolean executeLDR() throws IOException {
		int eAddress=getEA();
		controller.GPR[reg].copy(controller.getMemory().load(eAddress));
		return true;
	}

	/**
	 * Load register with address
	 */
	//LDA 1,0,8     000011 	01 	00 	0 	01000   => R[1] = 8
	public boolean executeLDA() throws IOException {
		int eAddress=getEA();
		controller.GPR[reg].setLong(eAddress);
	    return true;
	}
	
	/**
	 * Load Index Register from Memory.
	 */
	//LDX 1,13      100001 	00 	01 	0 	01101   => X[1] = M[13], X[1]=8
	public boolean executeLDX() throws IOException {
		int eAddress=getEAWithouIReg();
		controller.IX[ireg].copy(controller.getMemory().load(eAddress));
		return true;
	}

	/**
	 * Store Register To Memory.
	 */
	//STR 1,0,13    000010 	01 	00 	0 	01101   => M[13] = 8
	public boolean executeSTR() throws IOException {
		int eAddress=getEA();
		WORD param=new WORD();
        param.copy(controller.GPR[reg]);
		controller.getMemory().store(eAddress,param);
		return true;
	}

	/**
	 * Store Index Register to Memory.
	 */
	//STX 1,31 		100010 	00 	01 	0 	11111   => M[31] = X[1], M[31]=8
	public boolean executeSTX() throws IOException {
		int eAddress=getEAWithouIReg();
		WORD param=new WORD();
        param.copy(controller.IX[ireg]);
		controller.getMemory().store(eAddress,param);
		return true;
	}
	
	/**
	 * Get the effective address without given register
	 * @return an integer for the effective address.
	 */
	public int getEAWithouIReg() throws IOException
	{
		int eAddress=address;
		if(flag==0)
		{
			eAddress=address;
		}
		else if(flag==1)
		{
			eAddress=controller.getMemory().load(address).getInt();
		}
		else throw new IOException("Wrong indirect flag");
		return eAddress;
		
	}
	
	/**
	 * Get the effective address with given register.
	 * @return An integer of the effective address.
	 */
	public int getEA() throws IOException
	{
		int eAddress=address;
		if(flag==0)
		{
			if(ireg==0) {
				eAddress=address;
			}
			else{
				eAddress=controller.IX[ireg].getInt()+address;
			};			
		}
		else if(flag==1)
		{
			if(ireg==0) {
				eAddress=controller.getMemory().load(address).getInt();
			}else {
				int iAddress=controller.IX[ireg].getInt()+address;
				eAddress=controller.getMemory().load(iAddress).getInt();
			}
		}
		else throw new IOException("Wrong indirect flag");
		return eAddress;
	}
	
	
	/**
	 * Execute instructions and print out the information in register and memory
	 * @return a boolean indicating it is done.
	 */
	public boolean execute() throws IOException
	{
		setIR();
		switch(getOPCode())
		{
			case InstructionHandler.LDA:
				executeLDA();
                break;
			case InstructionHandler.LDR:
				executeLDR();
				break;
			case InstructionHandler.LDX:
				executeLDX();
				break;
			case InstructionHandler.STR:
				executeSTR();
				break;
			case InstructionHandler.STX:
				executeSTX();
				break;
			default:
				LOG.warning("Unknown Instruction(OPCODE): "+ir);
				break;
		}
		controller.showRegister();
		controller.showMemory();		
		return true;
	}

	/**
	 * Print out the instruction meaning by parts.
	 */
	public void showInstruction()
	{
		System.out.println("### IR STATUS START ###");
		System.out.println("[IR    ] "+ir);
        System.out.println("[OPCODE] "+opcode+" [GPR] "+reg+" [XR] "+ireg+" [FLAG] "+flag+" [ADDR] "+address);
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
	public int getFlag()
	{
		return flag;
	}
	
	/**
	 * Store the input instruction by parts to buffer given the asmCode.
	 * @asmCode A WORD storing each parts of the input instructions.
	 * @return A string of the result.
	 */
	public String getAsmCode(WORD asmCode)
	{
		StringBuffer buffer=new StringBuffer();
		
		InstructionHandler temp=new InstructionHandler(controller);
		temp.setIR(asmCode);
		Instruction inst=instSet.get(opcode);
		buffer.append(inst.getName());
		if(inst.isReg()==true)
			buffer.append(getReg());
		if(inst.isIReg()==true)
			buffer.append(getIReg());
		if(inst.isFlag()==true)
			buffer.append(getFlag());
		if(inst.isAddr()==true)
			buffer.append(getAddress());
		
		return buffer.toString();
	}

	/**
	 * Slice the input instruction by parts
	 * @return A string of the result.
	 */
	public String getAsmCode()
	{
		StringBuffer buffer=new StringBuffer();
		Instruction inst=instSet.get(opcode);
		buffer.append(inst.getName()+" ");
		if(inst.isReg()==true)
			buffer.append(getReg()+",");
		if(inst.isIReg()==true)
			buffer.append(getIReg()+",");
		if(inst.isAddr()==true)
			buffer.append(getAddress()+",");
		buffer.setLength(buffer.length()-1);
		if(inst.isFlag()==true && flag==1)
			buffer.append("[,I]");
		
		return buffer.toString();
	}
	
	/**
	 * Convert the input instruction into a binary code.
	 * @return the binary code in WORD format.
	 */
	public WORD getBinCode(String assemCode)
	{
		message="";
		WORD result=new WORD();
		int opcode=0;
		int reg=0;
		int ireg=0;
		int flag=0;
		int address=0;
		
		assemCode=assemCode.trim();
		String[] arrStr=assemCode.split(" ",2);
		arrStr[0]=arrStr[0].toUpperCase();
		Integer code=textToCode.get(arrStr[0]);
		if(code==null) {
			message="Unsupported Operation Text: "+arrStr[0]+"\n";
			LOG.warning(message);
			return null;
		}
		opcode=code;
		Instruction inst=instSet.get(opcode);
		if(inst==null) {
			message="Unsupported Operation Code : "+code+"\n";
			LOG.warning(message);
			return null;
		}
		
		if(arrStr.length<2)
		{
			message=arrStr[0]+" accepts "+inst.getParamLength()+" parameters.\nInput parameters are not matched - "+assemCode+"\n";
			LOG.warning(message);
			return null;
		}
		
		String lastParam=arrStr[1];
		if(lastParam.endsWith("[,I]"))
		{
			arrStr[1]=lastParam.substring(0,lastParam.length()-4);
			flag=1;
		}
		
		String[] arrParam=arrStr[1].split(",",3);
		int paramLength=arrParam.length;
		if(paramLength!=inst.getParamLength())
		{
			message=arrStr[0]+" accepts "+inst.getParamLength()+" parameters.\nInput parameters are not matched - "+assemCode+"\n";
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
			message="Parameter must be number : "+assemCode+"\n";
			LOG.warning(message);			
			return null;
		}
			
		System.out.println(assemCode);
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
			message=e.getMessage()+" : "+assemCode+"\n";
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
	public String getMessage()
	{
		return message;
	}
}
