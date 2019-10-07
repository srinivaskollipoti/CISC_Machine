import java.util.Hashtable;

/**
 * 
 */

/**
 * @author cozyu
 *
 */

/**
 * @author cozyu
 * @author youcao  documented by youcao.
 * Control Unit
 * A class to define given instructions.
 */

class Instruction
{
	enum InstType{
		LS_INST,TRANS_INST,AL_INST,IO_INST,UNKNOWN
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


class InstructionSet
{
	static final int LDA=003;
	static final int STR=002;
	static final int LDR=001;
	static final int LDX=041;
	static final int STX=042;
	static final int AMR=004;
	static final int JZ =010;	// add instruction step1
	
	
	public static final Hashtable< Integer, Instruction> instSet;
	static{
		instSet =   new Hashtable<Integer, Instruction>() {{
			put(LDA,new Instruction("LDA",LDA,3,true,true,true,true,Instruction.InstType.LS_INST));
			put(STR,new Instruction("STR",STR,3,true,true,true,true,Instruction.InstType.LS_INST));
			put(LDR,new Instruction("LDR",LDR,3,true,true,true,true,Instruction.InstType.LS_INST));
			put(LDX,new Instruction("LDX",LDX,2,false,true,true,true,Instruction.InstType.LS_INST));
			put(STX,new Instruction("STX",STX,2,false,true,true,true,Instruction.InstType.LS_INST));
			put(AMR,new Instruction("AMR",AMR,3,true,true,true,true,Instruction.InstType.AL_INST));
			put(JZ,new Instruction("JZ",JZ,3,true,true,true,true,Instruction.InstType.TRANS_INST)); // add instruction step2	
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
			put("JZ",JZ); // add instruction step3

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
