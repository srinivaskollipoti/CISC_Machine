import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

/**
 * @author cozyu
 * @author youcao  documented by youcao.
 * A class that define given instructions.
 */

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
	private WORD ir=new WORD();
	private ControlUnit controller;
	
	private int opcode=0;
	private int reg=0;
	private int ireg=0;
	private int flag=0;
	private int address=0;
	
	static final int LDA=3;
	static final int STR=2;
	static final int LDR=1;
	static final int LDX=33;
	static final int STX=34;
	private Hashtable< Integer, Instruction> instSet= new Hashtable< Integer, Instruction>();
	private Hashtable< String, Integer> textToCode= new Hashtable< String, Integer>();
	
	private String message=new String();
	
	/**
	 * A constructor to initializes instruction set.
	 * @param controller to use this class
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
	
	/**
	 * Set up the instruction code by parts from given translated instruction.
	 * @param ir A WORD containing the translated instruction. Format is explained in user guide.
	 * @return On case success, true is retured, otherwise false is returned.
	 */
	private boolean parseIR(WORD ir)
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
	 * Load register from a given memory address.
	 * @return On case success, true is retured, otherwise false is returned.
	 */
	private boolean executeLDR() throws IOException {
		int eAddress=getEA();
		controller.getGPR(reg).copy(controller.getMemory().load(eAddress));
		return true;
	}

	/**
	 * Load register with address
	 * @return On case success, true is retured, otherwise false is returned.
	 */
	private boolean executeLDA() throws IOException {
		int eAddress=getEA();
		controller.getGPR(reg).setLong(eAddress);
	    return true;
	}

	/**
	 * Store Register To Memory.
	 * @return On case success, true is retured, otherwise false is returned.
	 */
	private boolean executeSTR() throws IOException {
		int eAddress=getEA();
		WORD param=new WORD();
        param.copy(controller.getGPR(reg));
		controller.getMemory().store(eAddress,param);
		return true;
	}

	/**
	 * Load Index Register from Memory.
	 * @return On case success, true is retured, otherwise false is returned.
	 */
	private boolean executeLDX() throws IOException {
		int eAddress=getEAWithoutIX();
		controller.getIX(ireg).copy(controller.getMemory().load(eAddress));
		return true;
	}

	/**
	 * Store Index Register to Memory.
	 * @return On case success, true is retured, otherwise false is returned.
	 */
	private boolean executeSTX() throws IOException {
		int eAddress=getEAWithoutIX();
		WORD param=new WORD();
        param.copy(controller.getIX(ireg));
		controller.getMemory().store(eAddress,param);
		return true;
	}
	
	/**
	 * Get the effective address without given register
	 * @return an integer for the effective address.
	 */
	private int getEAWithoutIX() throws IOException
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
	private int getEA() throws IOException
	{
		int eAddress=address;
		if(flag==0)
		{
			if(ireg==0) {
				eAddress=address;
			}
			else{
				eAddress=controller.getIX(ireg).getInt()+address;
			};			
		}
		else if(flag==1)
		{
			if(ireg==0) {
				eAddress=controller.getMemory().load(address).getInt();
			}else {
				int iAddress=controller.getIX(ireg).getInt()+address;
				eAddress=controller.getMemory().load(iAddress).getInt();
			}
		}
		else throw new IOException("Wrong indirect flag");
		return eAddress;
	}
	
	
	/**
	 * Execute instructions and print out the information in register and memory
	 * @return On case success, true is retured, otherwise false is returned.
	 */
	public boolean execute() throws IOException
	{
		parseIR(controller.getIR());
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
		//controller.showRegister();
		//controller.showMemory();		
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
	 * get assemble code from current instruction
	 * @return A assemble code string.
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
		Integer code=textToCode.get(arrStr[0]);
		if(code==null) {
			message="Unsupported opcode: "+arrStr[0]+"\n";
			LOG.warning(message);
			return null;
		}
		opcode=code;
		Instruction inst=instSet.get(opcode);
		if(inst==null) {
			message="Unsupported opcode : "+arrStr[0]+"("+code+")\n";
			LOG.warning(message);
			return null;
		}
		
		if(arrStr.length<2)
		{
			message=arrStr[0]+" accepts "+inst.getParamLength()+" parameters.\nInput parameters are not matched - "+asmCode+"\n";
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
		if(opcode==33 || opcode==34) 
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
