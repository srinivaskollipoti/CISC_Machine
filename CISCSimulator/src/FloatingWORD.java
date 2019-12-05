
/**
 * @author cozyu
 *
 */
<<<<<<< HEAD
public class FloatingWORD extends GBitSet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final int SIZE=6;
	public FloatingWORD() {
		super(FloatingWORD.SIZE);
		setMinValue((int)(Math.pow(2, FloatingWORD.SIZE-1)*-1));
		setMaxValue((int)(Math.pow(2, FloatingWORD.SIZE)));
	}
	
	public FloatingWORD(java.util.BitSet input)
	{
		super(FloatingWORD.SIZE);
=======
public class FloatingWORD extends WORD {
	static final int SIZE=6;
	public FloatingWORD() {
		super();
		setMinValue((int)(Math.pow(2, FloatingWORD.SIZE-1)*-1));
		setMaxValue((int)(Math.pow(2, FloatingWORD.SIZE)));
	}
	
	public FloatingWORD(java.util.BitSet input)
	{
		super();
>>>>>>> refs/remotes/origin/master
		setMinValue((int)(Math.pow(2, FloatingWORD.SIZE-1)*-1));
		setMaxValue((int)(Math.pow(2, FloatingWORD.SIZE)));
		or(input);
	}
	
	public float getFloat() {
		if(this.isEmpty())
			return 0;
		int exp = 0;
		for(int i = 2 ; i < 7; i++)
	        if(this.get(i))
	            exp |= (1 << i);
		if(this.toLongArray()[1] == 1) exp = -1*exp;
		int mant = 0;
		for(int i = 8 ; i < 15; i++)
	        if(this.get(i))
	            mant |= (1 << i);
		float res = (float)(Math.pow(mant, exp-8));
		res = -1*this.toLongArray()[0]*res;
		return res;
	}
	public long setFloat(float f) {
		if(f < 0) this.set(1);
		else this.set(0);
		
		return 0;
	}
}