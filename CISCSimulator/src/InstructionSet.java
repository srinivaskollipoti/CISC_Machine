import java.util.Hashtable;
import java.util.logging.Logger;


class Translator
{
	protected final static Logger LOG = Logger.getGlobal();	
	protected static String message=new String();
	
	public static String getMessage() {return message;}
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
        int al=ir.subSet(7,8).getInt();
		int lr=ir.subSet(6,7).getInt();
		int count=ir.subSet(12,15).getInt();
		
		StringBuffer buffer=new StringBuffer();
		Instruction inst = InstructionSet.getInstruction(opcode);
		if (inst == null)
			return null;
		buffer.append(inst.getName()+" ");		
		if (inst.isReg() == true)
			buffer.append(reg + ",");
		if (inst.isIReg() == true)
			buffer.append(ireg + ",");
		if (inst.isAddr() == true)
			buffer.append(address + ",");
		
		if (inst.isCount() == true)
			buffer.append(count + ",");
		if (inst.isLR() == true)
			buffer.append(lr + ",");
		if (inst.isAL() == true)
			buffer.append(al + ",");
		
		buffer.setLength(buffer.length() - 1);
		if (inst.isFlag() == true && flag == 1)
			buffer.append(",I");

		return buffer.toString();
		
	}
	
	
	/**
	 * Convert the input instruction into a machine code.
	 * @return the machine code in WORD format.
	 */
	public static WORD getBinCode(String asmCode)
	{
		message="";
		
		WORD result=new WORD();
		
		int opcode=0;
		int reg=0;
		int ireg=0;
		int flag=0;
		int address=0;
		
		int count=0;
		int lr=0;
		int al=0;
		
		asmCode=asmCode.trim().toUpperCase();
		String[] arrStr=asmCode.split(" ",2);
		Instruction inst=InstructionSet.getInstruction(arrStr[0]);
		if(inst==null) {
			message="Unsupported opcode : "+arrStr[0]+"\n";
			LOG.warning(message);
			return null;
		}
		opcode=inst.getCode();
		
		if(inst.getParamLength()>0)
		{
			if (arrStr.length < 2) {
				message = arrStr[0] + " requires " + inst.getParamLength()
						+ " parameters.\nInput parameters are not matched - " + asmCode + "\n";
				LOG.warning(message);
				return null;
			}
			String lastParam=arrStr[1];
			if(lastParam.endsWith(",I"))
			{
				if(inst.isFlag()==false)
				{
					message = arrStr[0] + " doesn't support I flag.\nInput parameters are not matched - " + asmCode + "\n";
					LOG.warning(message);
					return null;
				}
				arrStr[1]=lastParam.substring(0,lastParam.length()-2);
				flag=1;
			}
			
			String[] arrParam=arrStr[1].split(",");
			int paramLength=arrParam.length;
			if (paramLength != inst.getParamLength()) {
				message = arrStr[0] + " requires " + inst.getParamLength()
						+ " parameters.\nInput parameters are not matched - " + asmCode + "\n";
				LOG.warning(message);
				return null;
			}
			for(int i=0;i<paramLength;i++)
				arrParam[i]=arrParam[i].trim();
			
			int index=0;
			try {
				if(inst.isReg()==true) reg=Integer.valueOf(arrParam[index++]);
				if(inst.isIReg()==true) ireg=Integer.valueOf(arrParam[index++]);
				if(inst.isAddr()==true) address=Integer.valueOf(arrParam[index++]);
				if(inst.isCount()==true) count=Integer.valueOf(arrParam[index++]);
				if(inst.isLR()==true) lr=Integer.valueOf(arrParam[index++]);
				if(inst.isAL()==true) al=Integer.valueOf(arrParam[index++]);
			}catch(java.lang.NumberFormatException e)
			{
				message="Parameter must be number : "+asmCode+"\n";
				LOG.warning(message);			
				return null;
			}
			
			// parameter validation
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
		}
		
		// set default value
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
		
		if(inst.isCount()==true) bitAddress.setLong(count);
		if(inst.isLR()==true) bitIReg.set(0,lr);
		if(inst.isAL()==true) bitIReg.set(1,al);
		
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

/**
 * @author cozyu
 * @author youcao  documented by youcao.
 * Define instruction structure.
 */
class Instruction
{
	enum InstType{
		LS_INST,TRANS_INST,AL_INST,IO_MISC_INST,FP_VEC_INST, UNKNOWN
	};
	
	private String name; 
	private int code; 
	private int paramLength; 
	private boolean isReg; 		// first register  
	private boolean isIReg; 	// index register or second register
	private boolean isAddr; 	// address
	private boolean isFlag;		// indirect flag
	private boolean isAL;		// arithmetic or logical flag
	private boolean isLR;		// left or right flag
	private boolean isCount;	// count
	private InstType type;		// instruction type
	
	
	public Instruction(String name,int code,int paramLength,
			boolean isReg,boolean isIReg, boolean isAddr, 
			boolean isCount, boolean isLR,  boolean isAL, boolean isFlag, InstType type)
	{
		this.name=name;
		this.code=code;
		this.paramLength=paramLength;
		this.isReg=isReg;		
		this.isIReg=isIReg;
		this.isAddr=isAddr;
		this.isFlag=isFlag;
		this.isAL=isAL;
		this.isLR=isLR;
		this.isCount=isCount;
		this.type=type;
	}

	
	public String getName() { return this.name; }
	public int getCode() { return this.code; }
	public int getParamLength() { return this.paramLength; }
	public boolean isReg() { return this.isReg; }
	public boolean isIReg() { return this.isIReg; }
	public boolean isAddr() { return this.isAddr; }
	public boolean isFlag() { return this.isFlag; }
	public boolean isCount() { return this.isCount; }	
	public boolean isAL() { return this.isAL; }	
	public boolean isLR() { return this.isLR; }	
	
	public InstType getType() { return this.type; }	
}

/**
 * @author cozyu
 * Define CISC instruction set
 */
class InstructionSet
{
	// Load and store instruction
	static final int LDA = 003;
	static final int STR = 002;
	static final int LDR = 001;
	static final int LDX = 041;
	static final int STX = 042;

	// Arithmetic and logical instruction
	static final int AMR = 004;
	static final int SMR = 005;
	static final int AIR = 006;
	static final int SIR = 007;
	static final int MLT = 020;
	static final int DVD = 021;
	static final int TRR = 022;
	static final int AND = 023;
	static final int ORR = 024;
	static final int NOT = 025;
	static final int SRC = 031;
	static final int RRC = 032;

	// Input and output operation
	static final int IN = 061;
	static final int OUT = 062;
	static final int CHK = 063;

	// Miscellaneous Instructions
	static final int HLT = 000;
	static final int TRAP = 030; // (Part 4)

	// Transfer Instructions
	static final int JZ = 010;
	static final int JNE = 011;
	static final int JCC = 012;
	static final int JMA = 013;
	static final int JSR = 014;
	static final int RFS = 015;
	static final int SOB = 016;
	static final int JGE = 017;

	// Floating Point Instructions/Vector Operations (Part 4)
	static final int FADD = 033;
	static final int FSUB = 034;
	static final int VADD = 035;
	static final int VSUB = 036;
	static final int CNVRT = 037;
	static final int LDFR = 050;
	static final int STFR = 051;

	
	public static final Hashtable< Integer, Instruction> instSet;
	static{
		instSet =   new Hashtable<Integer, Instruction>() {{
			put(LDA,new Instruction("LDA",LDA,3,true,true,true,false,false,false,true,Instruction.InstType.LS_INST));
			put(STR,new Instruction("STR",STR,3,true,true,true,false,false,false,true,Instruction.InstType.LS_INST));
			put(LDR,new Instruction("LDR",LDR,3,true,true,true,false,false,false,true,Instruction.InstType.LS_INST));
			put(LDX,new Instruction("LDX",LDX,2,false,true,true,false,false,false,true,Instruction.InstType.LS_INST));
			put(STX,new Instruction("STX",STX,2,false,true,true,false,false,false,true,Instruction.InstType.LS_INST));

			put(AMR,new Instruction("AMR",AMR,3,true,true,true,false,false,false,true,Instruction.InstType.AL_INST));
			put(SMR,new Instruction("SMR",SMR,3,true,true,true,false,false,false,true,Instruction.InstType.AL_INST));
			put(AIR,new Instruction("AIR",AIR,2,true,false,true,false,false,false,false,Instruction.InstType.AL_INST));
			put(SIR,new Instruction("SIR",SIR,2,true,false,true,false,false,false,false,Instruction.InstType.AL_INST));
			put(MLT,new Instruction("MLT",MLT,2,true,true,false,false,false,false,false,Instruction.InstType.AL_INST));
			put(DVD,new Instruction("DVD",DVD,2,true,true,false,false,false,false,false,Instruction.InstType.AL_INST));
			put(TRR,new Instruction("TRR",TRR,2,true,true,false,false,false,false,false,Instruction.InstType.AL_INST));
			put(AND,new Instruction("AND",AND,2,true,true,false,false,false,false,false,Instruction.InstType.AL_INST));
			put(ORR,new Instruction("ORR",ORR,2,true,true,false,false,false,false,false,Instruction.InstType.AL_INST));
			put(NOT,new Instruction("NOT",NOT,1,true,false,false,false,false,false,false,Instruction.InstType.AL_INST));
			put(SRC,new Instruction("SRC",SRC,4,true,false,false,true,true,true,false,Instruction.InstType.AL_INST));
			put(RRC,new Instruction("RRC",RRC,4,true,false,false,true,true,true,false,Instruction.InstType.AL_INST));

			put(IN,new Instruction("IN",IN,2,true,false,true,false,false,false,false,Instruction.InstType.IO_MISC_INST));
			put(OUT,new Instruction("OUT",OUT,2,true,false,true,false,false,false,false,Instruction.InstType.IO_MISC_INST));
			put(CHK,new Instruction("CHK",CHK,2,true,false,true,false,false,false,false,Instruction.InstType.IO_MISC_INST));
			put(HLT,new Instruction("HLT",HLT,0,false,false,false,false,false,false,false,Instruction.InstType.IO_MISC_INST));
			put(TRAP,new Instruction("TRAP",TRAP,1,false,false,false,true,false,false,false,Instruction.InstType.IO_MISC_INST));

			put(JZ,new Instruction("JZ",JZ,3,true,true,true,false,false,false,true,Instruction.InstType.TRANS_INST)); 
			put(JNE,new Instruction("JNE",JNE,3,true,true,true,false,false,false,true,Instruction.InstType.TRANS_INST)); 
			put(JCC,new Instruction("JCC",JCC,3,true,true,true,false,false,false,true,Instruction.InstType.TRANS_INST)); 
			put(JMA,new Instruction("JMA",JMA,2,false,true,true,false,false,false,true,Instruction.InstType.TRANS_INST)); 
			put(JSR,new Instruction("JSR",JSR,2,false,true,true,false,false,false,true,Instruction.InstType.TRANS_INST)); 
			put(RFS,new Instruction("RFS",RFS,1,false,false,true,false,false,false,false,Instruction.InstType.TRANS_INST)); 
			put(SOB,new Instruction("SOB",SOB,3,true,true,true,false,false,false,true,Instruction.InstType.TRANS_INST)); 
			put(JGE,new Instruction("JGE",JGE,3,true,true,true,false,false,false,true,Instruction.InstType.TRANS_INST)); 

			put(FADD,new Instruction("FADD",FADD,3,true,true,true,false,false,false,true,Instruction.InstType.FP_VEC_INST)); 
			put(FSUB,new Instruction("FSUB",FSUB,3,true,true,true,false,false,false,true,Instruction.InstType.FP_VEC_INST)); 
			put(VADD,new Instruction("VADD",VADD,3,true,true,true,false,false,false,true,Instruction.InstType.FP_VEC_INST)); 
			put(VSUB,new Instruction("VSUB",VSUB,3,true,true,true,false,false,false,true,Instruction.InstType.FP_VEC_INST)); 
			put(CNVRT,new Instruction("CNVRT",CNVRT,3,true,true,true,false,false,false,true,Instruction.InstType.FP_VEC_INST)); 
			put(LDFR,new Instruction("LDFR",LDFR,3,true,true,true,false,false,false,true,Instruction.InstType.FP_VEC_INST)); 
			put(STFR,new Instruction("STFR",STFR,3,true,true,true,false,false,false,true,Instruction.InstType.FP_VEC_INST)); 
		}};
	}

	public final static Hashtable< String, Integer> textToCode;
	
	static{
		textToCode =   new Hashtable<String, Integer>() {{
			put("LDA",LDA);
			put("STR",STR);
			put("LDR",LDR);
			put("LDX",LDX);
			put("STX",STX);
			put("AMR",AMR);
			put("SMR",SMR);
			put("AIR",AIR);
			put("SIR",SIR);
			put("MLT",MLT);
			put("DVD",DVD);
			put("TRR",TRR);
			put("AND",AND);
			put("ORR",ORR);
			put("NOT",NOT);
			put("SRC",SRC);
			put("RRC",RRC);
			put("IN",IN);
			put("OUT",OUT);
			put("CHK",CHK);
			put("HLT",HLT);
			put("TRAP",TRAP);
			put("JZ",JZ);
			put("JNE",JNE);
			put("JCC",JCC);
			put("JMA",JMA);
			put("JSR",JSR);
			put("RFS",RFS);
			put("SOB",SOB);
			put("JGE",JGE);
			put("FADD",FADD);
			put("FSUB",FSUB);
			put("VADD",VADD);
			put("VSUB",VSUB);
			put("CNVRT",CNVRT);
			put("LDFR",LDFR);
			put("STFR",STFR);
		}};
	}
	
	/**
	 * @param opcode
	 * @return
	 */
	public static Instruction getInstruction(int opcode) {
		return instSet.get(opcode);
	}
	public static Instruction getInstruction(String opcode) {
		Integer code=textToCode.get(opcode.trim());
		if(code==null) return null;
		return getInstruction(code);
	}

}
