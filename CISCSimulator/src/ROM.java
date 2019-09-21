/**
 * 
 */

/**
 * @author cozyu
 *
 */
public class ROM {
	
	//LDA 1,0,8     000011 	01 	00 	0 	01000   => R[1] = 8
	//STR 1,0,13    000010 	01 	00 	0 	01101   => M[13] = 8
	//LDR 2,0,13    000001 	10 	00 	0 	01101   => R[2] = M[13], R[2]=8
	//LDX 1,13      100001 	00 	01 	0 	01101   => X[1] = M[13], X[1]=8
	//STX 1,31 		100010 	00 	01 	0 	11111   => M[31] = X[1], M[31]=8	
	public String getCode() {
		// LDA 1, 0, 31 / 000011 01 00001000
		// STR 1, 0, 20 / 000010 01 00001101
		// LDR 2, 0, 20 / 000001 10 00001101
		// LDX 1, 20    / 100001 00 01001101
		// STX 1, 32    / 100010 00 01011111
		StringBuffer buffer=new StringBuffer();
		buffer.append(InstructionHelper.binToHex("0000110100001000"));
		buffer.append(InstructionHelper.binToHex("0000100100001101"));
		buffer.append(InstructionHelper.binToHex("0000011000001101"));
		buffer.append(InstructionHelper.binToHex("1000010001001101"));
		buffer.append(InstructionHelper.binToHex("1000100001011111"));
		
		
		return buffer.toString();
	}
}
