
/**
 * @author cozyu
 *
 */
public class FloatingWORD extends GBitSet {
	static final int SIZE=6;
	public FloatingWORD() {
		super(FloatingWORD.SIZE);
		setMinValue((int)(Math.pow(2, FloatingWORD.SIZE-1)*-1));
		setMaxValue((int)(Math.pow(2, FloatingWORD.SIZE)));
	}
	
	public FloatingWORD(java.util.BitSet input)
	{
		super(FloatingWORD.SIZE);
		setMinValue((int)(Math.pow(2, FloatingWORD.SIZE-1)*-1));
		setMaxValue((int)(Math.pow(2, FloatingWORD.SIZE)));
		or(input);
	}
	
	public float getFloat() {
		if(this.isEmpty())
			return 0;
		long exp = this.get(2,7).toLongArray()[0];
		if(this.get(1,1).toLongArray()[0] == 1) exp = -1*exp;
		long mant = this.get(8,15).toLongArray()[0];
		float res = (float)(Math.pow(mant, exp-8));
		if(this.toLongArray()[0] == 1) res = -1*res;
		return res;
	}
	public long setFloat(float f) {
		
		return 0;
	}
}