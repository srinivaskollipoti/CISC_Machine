import java.util.logging.Logger;
/**
 * 
 */

/**
 * @author cozyu
 * implement bit operation for the simulator
 * all registers and memory implemented based on GBitSet
 */
public class GBitSet extends java.util.BitSet {
         private final static Logger LOG = Logger.getGlobal();
	
	public final int length;
	private int minValue=0;
	private int maxValue=0;
	
	/**
	 * A constructor that create a bit set given length.
	 * @param length length of bit set.
	 */
	public GBitSet(int length)
	{
		super(length);
		this.length=length;
		maxValue=(int)Math.pow(2,length)-1;
	}

	/**
	 * A constructor that create a bit set given BitSet and length.
	 * @param input input BitSet
	 * @param length length of BitSet.
	 */
	public GBitSet(java.util.BitSet input,int length)
	{
		super(length);
		this.length=length;
		maxValue=(int)Math.pow(2,length)-1;
		or(input);
	}
	

	/**
	 * Set minimum value of a bit set.
	 * @param minValue minimum value.
	 */
	public void setMinValue(int minValue)
	{
		this.minValue=minValue;
	}

	/**
	 * Return a subset of this bit set.
	 * @param from the beginning index, inclusive.
	 * @param to the ending index, exclusive.
	 * @return the specified subset
	 */
	public GBitSet subSet(int from,int to)
	{
		if(to<=from)
		{
			LOG.warning("Wrong Parameter in subStr() ["+from+" ~ "+to+"]");
			return null;
		}
		GBitSet subSet= new GBitSet(to-from);
		subSet.or(get(from, to));
		return subSet;
	}

	/**
	 * Sets bit set to specified long value 
	 * @param number a long value.
	 * @return On case success, true is retured, otherwise false is returned.
	 */
	public boolean setLong(long number){
		
		if (maxValue<number || number<minValue)
			throw new IllegalArgumentException("Input number("+number+") is out of data range");
		this.clear();
		long[] temp = new long[1];
		temp[0]=number;
		this.or(GBitSet.valueOf(temp));
		
		return true;
	}
	
	/**
	 * Get long value from bit set 
	 * @return A long indicating bit set. 
	 */
	public long getLong(){
		if(this.isEmpty())
			return 0;
		long temp[]=this.toLongArray();
		return temp[0];	
	}

	/**
	 * Get int value from bit set 
	 * @return A int indicating bit set. 
	 */
	public int getInt(){
		long input=getLong();
		int result=(int)input;
		double checkOverflow=result*input;
		if(checkOverflow<0)
		{
			LOG.severe("Buffer overflow");
			result=-1;
		}
		return result;	
	}

	/**
	 * Copy bit set from specified GBitSet 
	 * @return On case success, true is retured, otherwise false is returned.
	 */
	public boolean copy(GBitSet input){
		this.clear();
		this.or(input);
		return true;
	}
	
	/**
	 * Get binary string from bit set  
	 * @return A binary string 
	 */
	public String getString()
	{
		StringBuffer buffer=new StringBuffer();
		for (int i=length-1; i>=0;i--)
		{
			if(get(i)) buffer.append("1");
			else buffer.append("0");
			if(i%8==0) buffer.append(" ");
		}
		return buffer.toString();	
	}

	/**
	 * Get binary string and long value from bit set   
	 * @return A binary string and long value
	 */
	public String toString()
	{
		return getString()+"=> "+getLong();	
	}	
}
