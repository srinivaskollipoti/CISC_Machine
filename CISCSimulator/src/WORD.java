/**
 * 
 */

/**
 * @author cozyu
 * A class to implement 16 bits WORD
 */
public class WORD extends GBitSet {
	static final int SIZE=16;
	public WORD() {
		super(WORD.SIZE);
	}
	
	public WORD(java.util.BitSet input)
	{
		super(WORD.SIZE);
		or(input);
	}
}
