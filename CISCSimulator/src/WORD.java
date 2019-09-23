/**
 * 
 */

/**
 * @author cozyu
 *
 */
public class WORD extends GBitSet {
	private static final int SIZE=16;
	public WORD() {
		super(WORD.SIZE);
	}
	/**
	 * @param subText
	 */
		
	public WORD(String wordString) {
		super(WORD.SIZE);
		
		//System.out.println(wordString);
		//System.exit(1);
		long[] highLong=new long[]{ Long.valueOf(wordString.substring(0,2), 16)  };
		long[] lowLong=new long[]{ Long.valueOf(wordString.substring(2,4), 16) };
		highLong[0]=highLong[0]*256;
		
		or(GBitSet.valueOf(lowLong));
		or(GBitSet.valueOf(highLong));
	}
	
	
}
