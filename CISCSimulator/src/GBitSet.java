import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * 
 */

/**
 * @author cozyu
 *
 */
public class GBitSet extends java.util.BitSet {
         private final static Logger LOG = Logger.getGlobal();
	
	public final int length;
	private int minValue=0;
	private int maxValue=0;
	
	public GBitSet(int length)
	{
		super(length);
		this.length=length;
		maxValue=(int)Math.pow(2,length)-1;
	}
	
	public void setMinValue(int minValue)
	{
		this.minValue=minValue;
	}
	
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
	
	public boolean setLong(long number){
		
		if (maxValue<number || number<minValue)
			throw new IllegalArgumentException("Input number("+number+") is out of data range");
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

	public boolean copy(GBitSet input){
		this.clear();
		this.or(input);
		return true;
	}
	
	
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
