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
			word.clear();
		}
		return true;
	}
	
	public boolean store(int address, WORD input) throws IOException
	{
		return store(address,input,false);
	}

	public boolean store(int address, WORD input, boolean isSystem) throws IOException
	{
		int limitMemoryStart=BOOT_MEMORY_START;
		if(isSystem==false) limitMemoryStart=userMemoryStart;
		if(address>=MAX_MEMORY)
			throw new IOException("Memory violation : access over memory("+address+")");
		else if(address<limitMemoryStart)
			throw new IOException("Memory violation : access system address("+address+")");
		if (input==null)
			throw new IOException("Memory violation : insert null data("+address+")");
		
		memory[address]=input;
		return true;
	}

	
	public boolean storeUserCode(ArrayList<WORD> arrCode)
	{
		int address=userMemoryStart;
		for(WORD code : arrCode) {
			try {
			if(!store(address,code)) return false;
			}catch(IOException e) {
				e.getStackTrace();
				return false;
			}
			address++;
		}
		memory[address++].clear();
		return true;
	}
	
	public boolean storeBootCode(ArrayList<WORD> arrCode) throws IOException
	{
		int address=BOOT_MEMORY_START;
		for(WORD word : arrCode) {
			if(!store(address,word,true)) return false;
			address++;
		}
		memory[address].clear();
		userMemoryStart=address;
		return true;
	}
	
	
	public WORD load(int address) throws IOException
	{
		if(address>MAX_MEMORY || address<BOOT_MEMORY_START)
			throw new IOException("Memory violation");
		return memory[address];
	}
	
	public String getString() 
	{
		StringBuffer buffer=new StringBuffer();
		for(int i =0; i<MAX_MEMORY;i++)
		{
			if(!memory[i].isEmpty())
			{
				String message=String.format("Memory [%03d]  %s (%d)\n", i,memory[i].getString(),memory[i].getLong());
				buffer.append(message);
//				buffer.append("Memory ["+i+"] "+memory[i].getString()+"\n");
			}
		}
		return buffer.toString();
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
	public int getUserMemoryLocation() {
		return userMemoryStart;
	}

}
