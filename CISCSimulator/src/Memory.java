import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author cozyu
 *
 */
public class Memory {
	private final static Logger LOG = Logger.getGlobal();
	private WORD memory[];
	private static int userMemoryStart;
	
	public static final int MAX_MEMORY=2048;
	public static final int BOOT_MEMORY_START=010;
	
	public Memory()
	{		
		memory= new WORD[MAX_MEMORY];

		for (int i=0; i<MAX_MEMORY;i++) {
			memory[i]=new WORD();
		}
		userMemoryStart=BOOT_MEMORY_START;
	}
	
	public boolean init()
	{
		for(WORD word:memory) {
			if(word!=null)word.clear();
		}
		return true;
	}
	
	public boolean store(int address, WORD input) throws IOException
	{
		if(address>MAX_MEMORY || address<BOOT_MEMORY_START)
			throw new IOException("Memory violation");
		memory[address]=input;
		return true;
	}
	
	public boolean store(int address, ArrayList<WORD> input) throws IOException
	{
		for(WORD word : input) {
			if(!store(address,word)) return false;
			address++;
		}
		return true;
	}
	
	public WORD load(int address) throws IOException
	{
		if(address>MAX_MEMORY || address<BOOT_MEMORY_START)
			throw new IOException("Memory violation");
		return memory[address];
	}
	
	
	public String toString() 
	{
		StringBuffer buffer=new StringBuffer();
		for(int i =0; i<MAX_MEMORY;i++)
		{
			if(!memory[i].isEmpty())
				buffer.append("Memory ["+i+"] is "+memory[i]+"\n");
		}
		return buffer.toString();
	}

	/**
	 * @package : /Memory.java
	 * @author : cozyu  
	 * @date : 2019. 9. 20.
	 * @param i
	 * Memory
	 */
	public void setUserMemoryLocation(int offset) {
		userMemoryStart=BOOT_MEMORY_START+offset;
		// TODO Auto-generated method stub
		
	}

}
