import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;

/**
 * 
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
 *
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
        
	//LDR 2,0,13    000001 	10 	00 	0 	01101   => R[2] = M[13], R[2]=8
	public boolean executeLDR() throws IOException {
		int eAddress=getEA();
		controller.GPR[reg].copy(controller.memory.load(eAddress));
		return true;
	}

	//LDA 1,0,8     000011 	01 	00 	0 	01000   => R[1] = 8
	public boolean executeLDA() throws IOException {
		int eAddress=getEA();
		controller.GPR[reg].setLong(eAddress);
	    return true;
	}
	

	//LDX 1,13      100001 	00 	01 	0 	01101   => X[1] = M[13], X[1]=8
	public boolean executeLDX() throws IOException {
		int eAddress=getEAWithouIReg();
		controller.IX[ireg].copy(controller.memory.load(eAddress));
		return true;
	}

	//STR 1,0,13    000010 	01 	00 	0 	01101   => M[13] = 8
	public boolean executeSTR() throws IOException {
		int eAddress=getEA();
		WORD param=new WORD();
        param.copy(controller.GPR[reg]);
		controller.memory.store(eAddress,param);
		return true;
	}

	//STX 1,31 		100010 	00 	01 	0 	11111   => M[31] = X[1], M[31]=8
	public boolean executeSTX() throws IOException {
		int eAddress=getEAWithouIReg();
		WORD param=new WORD();
        param.copy(controller.IX[ireg]);
		controller.memory.store(eAddress,param);
		return true;
	}
	
	public int getEAWithouIReg() throws IOException
	{
		int eAddress=address;
		if(flag==0)
		{
			eAddress=address;
		}
		else if(flag==1)
		{
			eAddress=controller.memory.load(address).getInt();
		}
		else throw new IOException("Wrong indirect flag");
		return eAddress;
		
	}
	
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
				eAddress=controller.memory.load(address).getInt();
			}else {
				int iAddress=controller.IX[ireg].getInt()+address;
				eAddress=controller.memory.load(iAddress).getInt();
			}
		}
		else throw new IOException("Wrong indirect flag");
		return eAddress;
	}
	
	
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
	
	public WORD getBinCode(String assemCode)
	{
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
			LOG.warning("Unknown OPTEXT : "+assemCode);
			return null;
		}
		opcode=code;
		Instruction inst=instSet.get(opcode);
		if(inst==null) {
			LOG.warning("Unknown OPSET : "+assemCode);
			return null;
		}
		
		if(arrStr.length<2)
		{
			LOG.warning("No Parameter : "+assemCode);
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
			LOG.warning("Wrong Parameter Length : "+assemCode);
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
			e.getStackTrace();
			LOG.warning("Wrong Parameter : "+assemCode);
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
			LOG.warning("Wrong Parameter : "+assemCode);
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
	
}
