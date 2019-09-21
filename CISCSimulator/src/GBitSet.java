import java.util.BitSet;

/**
 * 
 */

/**
 * @author cozyu
 *
 */
public class GBitSet extends java.util.BitSet {

	int length;
	public GBitSet(int bitSet)
	{
		super(bitSet);
		length=bitSet;
	}
	
	public GBitSet(BitSet input)
	{
		super(input.size());
		System.out.println(input.toByteArray());
		or(input);
	}
	
	public GBitSet subSet(int from,int to)
	{
		GBitSet subSet= new GBitSet(to-from+1);
		subSet.or(get(from, to));
		return subSet;
	}
	
	public boolean setLong(long number){
		
		if (Math.pow(2,length)<=number || number<0)
			throw new IllegalArgumentException("long number is out of range: "+number);
		this.clear();
		long[] temp = new long[1];
		temp[0]=number;
		this.or(GBitSet.valueOf(temp));
		
		return true;
	}
	
	public long getLong(){
		if(this.isEmpty())
			return 0;
		long temp[]=this.toLongArray();
		return temp[0];	
	}
	
	public int getInt(){
		return (int)getLong();	
	}

	public boolean copy(GBitSet input){
		this.clear();
		this.or(input);
		return true;
	}
	
	
	public String toString()
	{
		//System.out.println(super.toString());
		StringBuffer buffer=new StringBuffer();
		for (int i=length-1; i>=0;i--)
		{
			if(get(i)) buffer.append("1");
			else buffer.append("0");
			
			if(i%8==0) buffer.append(" ");
		}
		buffer.append("=> "+getInt());
		return buffer.toString();	
	}
	
}
