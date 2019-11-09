
/**
 * @author cozyu
 *
 */
public class SignedWORD extends WORD {

	/**
	 * @param length
	 */
	public SignedWORD() {
		super();
		setMinValue((int)(Math.pow(2, SignedWORD.SIZE-1)*-1));
		setMaxValue((int)(Math.pow(2, SignedWORD.SIZE-1)-1));
	}
	
	public SignedWORD(WORD word) {
		super();
		setMinValue((int)(Math.pow(2, SignedWORD.SIZE-1)*-1));
		setMaxValue((int)(Math.pow(2, SignedWORD.SIZE-1)-1));
		or(word);
	}
	
	public SignedWORD(java.util.BitSet input)
	{
		super();
		setMinValue((int)(Math.pow(2, SignedWORD.SIZE-1)*-1));
		setMaxValue((int)(Math.pow(2, SignedWORD.SIZE-1)-1));
		or(input);
	}

	public long getLong(){
		return (short)super.getLong();
	}
	

}
