import java.io.IOException;
import java.util.ArrayList;


/**
 * @author cozyu
 * @author youcao  documented by youcao.
 * This class manages memory of the simulator, see details below.
 *
 */

public class Memory {
	private WORD memory[];
	private static final int DEFAULT_MEMORY=2048;
	private static final int MAX_MEMORY=4096;
	private static final int MIN_MEMORY=512;
	private int userMemoryStart;
	
	public static final int BOOT_MEMORY_START=010;
	
	/**
	 * A constructor
	 * Initializes memory with the max size 2048. The memory is an array of type WORD.
	 * Set userMemoryStart to 010.
	 * @throws IOException 
	 */
	public Memory() throws IOException
	{		
		setMemory(DEFAULT_MEMORY);
	}

	/**
	 * Initializes memory with given size.
	 * @return true
	 */

	public boolean setMemory(int size) throws IOException
	{
		memory= new WORD[size];
		if(size<MIN_MEMORY || size>MAX_MEMORY)
		{
			throw new IOException("Invalid memory size");
		}

		for (int i=0; i<size;i++) {
			memory[i]=new WORD();
		}
		userMemoryStart=BOOT_MEMORY_START;	
		return true;
	}
	
	/**
	 * Set every elements in the memory array to null.
	 * @return true
	 */
	public boolean init()
	{
		for(WORD word:memory) {
			word.clear();
		}
		return true;
	}
	
	/**
	 * Store the input data to a memory with specific address.
	 * @param address A integer indicating the memory slot to access.
	 * @param input A WORD argument containing the input data for the memory to store.
	 * @return Boolean indicating if the execution is success.
	 * 
	 */
	public boolean store(int address, WORD input) throws IOException
	{
		return store(address,input,false);
	}

	/**
	 * Store the input data to a memory with specific address.
	 * @param address A integer indicating the memory slot to access.
	 * @param input A WORD argument containing the input data for the memory to store.
	 * @param isSystem A boolean indicating to access boot address.
	 * @return Boolean indicating if the execution is success.
	 * 
	 */
	public boolean store(int address, WORD input, boolean isSystem) throws IOException
	{
		int limitMemoryStart=BOOT_MEMORY_START;
		if(isSystem==false) limitMemoryStart=userMemoryStart;
		if(address>=memory.length)
			throw new IOException("Memory violation : access over memory("+address+")\n");
		else if(address<limitMemoryStart)
			throw new IOException("Memory violation : access system address("+address+")\n");
		if (input==null)
			throw new IOException("Memory violation : insert null data("+address+")\n");
		
		memory[address]=input;
		return true;
	}

	/**
	 * Store a list of user code starting from a specified memory address defined by the user
	 * @param arrCode A WORD array list containing the input code.
	 * @return A boolean indicating if the execution is success.
	 */
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
	
	/**
	 * Store a list of boot code starting from a specified memory address.
	 * @param arrCode A WORD array list containing the input code.
	 * @return A boolean indicating if the execution is success.
	 */
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
	
	/**
	 * Load data from a memory given address.
	 * @param address an integer of the memory address the machine wants to access.
	 * @return The data stored in the memory slot.
	 */
	public WORD load(int address) throws IOException
	{
		if(address>=memory.length || address<BOOT_MEMORY_START)
			throw new IOException("Memory access violation : ["+address+"]\n");
		return memory[address];
	}
	
	public WORD load(long address) throws IOException
	{
		return load((int)address);
	}
	
	/**
	 * Get information of each memory slots, including the address and content.
	 * @return The result in String type.
	 */
	public String getString() 
	{
		StringBuffer buffer=new StringBuffer();
		for(int i =0; i<memory.length;i++)
		{
			if(!memory[i].isEmpty())
			{
				String message=String.format("Memory [%03d]  %s (%d)\n", i,memory[i].getString(),memory[i].getLong());
				buffer.append(message);
			}
		}
		
		return buffer.toString();
	}
	
	/**
	 * Get information of each memory slots, including the address and content.
	 * @return The result in String type.
	 */
	public String toString() 
	{
		StringBuffer buffer=new StringBuffer();
		for(int i =0; i<memory.length;i++)
		{
			if(!memory[i].isEmpty())
				buffer.append("Memory ["+i+"] is "+memory[i]+"\n");
		}
		return buffer.toString();
	}
	

	/**
	 * Get starting location for user program.
	 * @return starting location for user program.
	 */
	public int getUserMemoryLocation() {
		return userMemoryStart;
	}

}
