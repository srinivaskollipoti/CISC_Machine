import java.util.Hashtable;

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
	private boolean isReg; 
	private boolean isIReg; 
	private boolean isAddr; 
	private boolean isFlag;		
	private InstType type;		
	
	
	public Instruction(String name,int code,int paramLength,
			boolean isReg,boolean isIReg, boolean isAddr, boolean isFlag,InstType type)
	{
		this.name=name;
		this.code=code;
		this.paramLength=paramLength;
		this.isReg=isReg;
		this.isIReg=isIReg;
		this.isAddr=isAddr;
		this.isFlag=isFlag;
		this.type=type;
	}

	public String getName() { return this.name; }
	public int getCode() { return this.code; }
	public int getParamLength() { return this.paramLength; }
	public boolean isReg() { return this.isReg; }
	public boolean isIReg() { return this.isIReg; }
	public boolean isAddr() { return this.isAddr; }
	public boolean isFlag() { return this.isFlag; }	
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
	static final int TRAP = 036; // (Part 4)

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
			put(LDA,new Instruction("LDA",LDA,3,true,true,true,true,Instruction.InstType.LS_INST));
			put(STR,new Instruction("STR",STR,3,true,true,true,true,Instruction.InstType.LS_INST));
			put(LDR,new Instruction("LDR",LDR,3,true,true,true,true,Instruction.InstType.LS_INST));
			put(LDX,new Instruction("LDX",LDX,2,false,true,true,true,Instruction.InstType.LS_INST));
			put(STX,new Instruction("STX",STX,2,false,true,true,true,Instruction.InstType.LS_INST));
			
			put(AMR,new Instruction("AMR",AMR,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(SMR,new Instruction("SMR",SMR,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(AIR,new Instruction("AIR",AIR,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(SIR,new Instruction("SIR",SIR,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(MLT,new Instruction("MLT",MLT,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(DVD,new Instruction("DVD",DVD,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(TRR,new Instruction("TRR",TRR,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(AND,new Instruction("AND",AND,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(ORR,new Instruction("ORR",ORR,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(NOT,new Instruction("NOT",NOT,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(SRC,new Instruction("SRC",SRC,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(RRC,new Instruction("RRC",RRC,3,true,true,true,true,Instruction.InstType.AL_INST));
			
			put(IN,new Instruction("IN",IN,3,true,true,true,true,Instruction.InstType.IO_MISC_INST));
			put(OUT,new Instruction("OUT",OUT,3,true,true,true,true,Instruction.InstType.IO_MISC_INST));
			put(CHK,new Instruction("CHK",CHK,3,true,true,true,true,Instruction.InstType.IO_MISC_INST));
			put(HLT,new Instruction("HLT",HLT,3,true,true,true,true,Instruction.InstType.IO_MISC_INST));
			put(TRAP,new Instruction("TRAP",TRAP,3,true,true,true,true,Instruction.InstType.IO_MISC_INST));
			
			put(JZ,new Instruction("JZ",JZ,3,true,true,true,true,Instruction.InstType.TRANS_INST)); 	
			put(JZE,new Instruction("JZE",JZE,3,true,true,true,true,Instruction.InstType.TRANS_INST)); 	
			put(JCC,new Instruction("JCC",JCC,3,true,true,true,true,Instruction.InstType.TRANS_INST)); 	
			put(JMA,new Instruction("JMA",JMA,3,true,true,true,true,Instruction.InstType.TRANS_INST)); 	
			put(JSR,new Instruction("JSR",JSR,3,true,true,true,true,Instruction.InstType.TRANS_INST)); 	
			put(RFS,new Instruction("RFS",RFS,3,true,true,true,true,Instruction.InstType.TRANS_INST)); 	
			put(SOB,new Instruction("SOB",SOB,3,true,true,true,true,Instruction.InstType.TRANS_INST)); 	
			put(JGE,new Instruction("JGE",JGE,3,true,true,true,true,Instruction.InstType.TRANS_INST)); 	

			put(FADD,new Instruction("FADD",FADD,3,true,true,true,true,Instruction.InstType.FP_VEC_INST)); 	
			put(FSUB,new Instruction("FSUB",FSUB,3,true,true,true,true,Instruction.InstType.FP_VEC_INST)); 	
			put(VADD,new Instruction("VADD",VADD,3,true,true,true,true,Instruction.InstType.FP_VEC_INST)); 	
			put(VSUB,new Instruction("VSUB",VSUB,3,true,true,true,true,Instruction.InstType.FP_VEC_INST)); 	
			put(CNVRT,new Instruction("CNVRT",CNVRT,3,true,true,true,true,Instruction.InstType.FP_VEC_INST)); 	
			put(LDFR,new Instruction("LDFR",LDFR,3,true,true,true,true,Instruction.InstType.FP_VEC_INST)); 	
			put(STFR,new Instruction("STFR",STFR,3,true,true,true,true,Instruction.InstType.FP_VEC_INST)); 	

			
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
			put("JZE",JZE);
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
