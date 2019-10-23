import java.util.logging.Logger;
/**
 * 
 */

/**
 * @author cozyu(Yeongmok You)
 * implement bit operation for the simulator
 * all registers and memory implemented based on GBitSet
 */
public class GBitSet extends java.util.BitSet {

	protected final static Logger LOG = Logger.getGlobal();
	
	public final int length;
	protected int minValue=0;
	protected int maxValue=0;
	
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
	
	public void setSigned(boolean isSigned)
	{
		if(isSigned==true)
		{
			setMinValue((int)(Math.pow(2, length-1)*-1));
			setMaxValue((int)(Math.pow(2, length-1)-1));
		}else {
			maxValue=(int)Math.pow(2,length)-1;
		}
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
		maxValue=(int)Math.pow(2,length-1)-1;
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
	 * Set maximum value of a bit set.
	 * @param minValue minimum value.
	 */
	public void setMaxValue(int maxValue)
	{
		this.maxValue=maxValue;
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
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean setLong(long number){
		
		if (maxValue<number || number<minValue)
			throw new IllegalArgumentException("Input number("+number+") is out of data range\n");
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
		return (int)input;	
	}

	/**
	 * Copy bit set from specified GBitSet 
	 * @return On case success, true is returned, otherwise false is returned.
	 */
	public boolean copy(GBitSet input){
		this.clear();
		this.or(input);
		return true;
	}

	/**
	 * Perform ROTATE operation
	 * @param isLeft	left(true) or right(false)
	 * @param count		the number of move
	 * @param isArith	arithmetic(true) or logical(false)
	 */
	public void rotate(boolean isLeft,int count, boolean isArith )
	{
		GBitSet copy;
		count=count%length;
		if(count<0)
			count=length+count;

		if(isLeft)
		{
			int from=length-count;
			int end=length;
			if(isArith==true) {
				from--;
				end--;
			}	
			copy=subSet(from, end);
			shift(isLeft,count,isArith);
			for(int i=count-1;i>=0;i--)
			{
				set(i,copy.get(i));
			}
		}else {
			copy=subSet(0, count);
			shift(isLeft,count,isArith);
			
			int from=length-1-count;
			int end=length-1;
			if(isArith==true)
			{
				from--;
				end--;
			}
			count--;
			for(int i=end;i>from;i--)
			{
				set(i,copy.get(count--));
			}			
		}
	}
	
	/**
	 * Perform SHIFT operation
	 * @param isLeft	left(true) or right(false)
	 * @param count		the number of move
	 * @param isArith	arithmetic(true) or logical(false)
	 */
	public void shift(boolean isLeft,int count, boolean isArith )
	{
		if(count>length || count<0)
			throw new IllegalArgumentException("Count must be in range of length : count("+count+")\n");

		boolean MSB=get(length-1);
		if(isLeft) {
			int end=length-1;
			if(isArith) {
				end--;
			}
			for(int i=end;i>=count;i--)
			{
				set(i,get(i-count));
			}
			for(int i=count-1;i>=0;i--)
			{
				set(i,false);
			}
		}else {
			for(int i=count;i<length;i++)
			{
				set(i-count,get(i));
			}
			boolean fill=false;
			// if the mode is arithmetic, extend the MSB bit
			if(isArith) fill=MSB;
			for(int i=length-1;i>length-1-count;i--)
			{		
				set(i,fill);
			}
		}
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
