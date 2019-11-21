
/**
 * @author cozyu
 *
 */
public class FloatingWORD extends WORD {

	/**
	 * @param length
	 */
	public FloatingWORD() {
		super();
		setMinValue((int)(Math.pow(2, FloatingWORD.SIZE-1)*-1));
		setMaxValue((int)(Math.pow(2, FloatingWORD.SIZE-1)-1));
	}
	
	public FloatingWORD(WORD word) {
		super();
		setMinValue((int)(Math.pow(2, FloatingWORD.SIZE-1)*-1));
		setMaxValue((int)(Math.pow(2, FloatingWORD.SIZE-1)-1));
		or(word);
	}
	
	public FloatingWORD(java.util.BitSet input)
	{
		super();
		setMinValue((int)(Math.pow(2, FloatingWORD.SIZE-1)*-1));
		setMaxValue((int)(Math.pow(2, FloatingWORD.SIZE-1)-1));
		or(input);
	}

	public long getLong(){
		return (short)super.getLong();
	}
	

}
