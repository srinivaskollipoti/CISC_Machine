import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * @author cozyu
 * @author youcao  documented by youcao.
 * This class manages memory of the simulator, see details below.
 *
 */

public class Memory {
	private SignedWORD memory[];
	private boolean	isMemorys[];
	
	private int user_program_end=0;
	private static final int DEFAULT_MEMORY=2048;
	private static final int MAX_MEMORY=4096;
	private static final int MIN_MEMORY=2048;
	
	public static final int BOOT_MEMORY_START=010;
	public static final int USER_MEMORY_START=400;
	public static final int USER_PROGRAM_START=1000;
	
	
	/**
	 * A constructor
	 * Initializes memory with the max size 2048. The memory is an array of type WORD.
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
		memory= new SignedWORD[size];
		isMemorys=new boolean[size];
		if(size<MIN_MEMORY || size>MAX_MEMORY)
		{
			throw new IOException("Invalid memory size");
		}

		for (int i=0; i<size;i++) {
			memory[i]=new SignedWORD();
			
		}
		//userMemoryStart=BOOT_MEMORY_START;	
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
		Arrays.fill(isMemorys, false);
		
		return true;
	}
	
	/**
	 * Store the input data to a memory with specific address.
	 * @param address A integer indicating the memory slot to access.
	 * @param input A WORD argument containing the input data for the memory to store.
	 * @return Boolean indicating if the execution is success.
	 * 
	 */
	private boolean store(int address, WORD input) throws IOException
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
	private boolean store(int address, WORD input, boolean isSystem) throws IOException
	{
		int limitMemoryStart=BOOT_MEMORY_START;
		if(isSystem==false) limitMemoryStart=USER_MEMORY_START;
		if(address>=memory.length)
			throw new IOException("Memory violation\n==> Access over memory("+address+")\n");
		else if(address<0)
			throw new IOException("Memory violation\n==> Access invalid address("+address+")\n");	
		else if(address<limitMemoryStart)
			throw new IOException("Memory violation\n==> Access into system address("+address+")\n");
		if (input==null)
			throw new IOException("Memory violation\n==> Insert empty data("+address+")\n");
		
		memory[address].copy(input);
		isMemorys[address]=true;
		return true;
	}

	
	/**
	 * Load data from a memory given address.
	 * @param address an integer of the memory address the machine wants to access.
	 * @return The data stored in the memory slot.
	 */
	private WORD load(int address) throws IOException
	{
		if(address<BOOT_MEMORY_START)
			throw new IOException("Memory violation\n==> System address : "+address+"\n");
		if(address>=memory.length)
			throw new IOException("Memory violation\n==> Out of memory range : "+address+"\n");

		return memory[address];
	}
	
	/**
	 * Load data from a memory given address.
	 * Don't use this function and use loadMemory() of CPU class
	 * @param address an integer of the memory address the machine wants to access.
	 * @return The data stored in the memory slot.
	 */	
	public WORD load(long address,CPU cpu) throws IOException
	{
		if(cpu.getMemory()!=this)
			return null;
		return load((int)address);
	}
	
	/**
	 * Store the input data to a memory with specific address.
	 * Don't use this function and use storeMemory() of CPU class
	 * @param address A integer indicating the memory slot to access.
	 * @param input A WORD argument containing the input data for the memory to store.
	 * @return Boolean indicating if the execution is success.
	 * 
	 */
	public boolean store(long address, WORD input,boolean isSystem, CPU cpu) throws IOException
	{
		return store((int)address,input,isSystem);
	}
	
	/**
	 * Store a list of user code starting from a specified memory address defined by the user
	 * @param arrCode A WORD array list containing the input code.
	 * @return A boolean indicating if the execution is success.
	 */
	public boolean storeUserCode(ArrayList<WORD> arrCode)
	{
		int address=USER_PROGRAM_START;
		for(WORD code : arrCode) {
			try {
			if(!store(address,code)) return false;
			}catch(IOException e) {
				e.getStackTrace();
				return false;
			}
			address++;
		}
		memory[address].setLong(CPU.END_OF_PROGRAM);
		isMemorys[address]=true;
		user_program_end=address;
		//System.out.println(this);
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
		memory[address].setLong(CPU.END_OF_PROGRAM);
		isMemorys[address]=true;
		return true;
	}
	
	/**
	 * Get information of each memory slots, including the address and content.
	 * @return The result in String type.
	 */
	public String getString() 
	{
		StringBuffer buffer=new StringBuffer();
		
		buffer.append("### MEMORY STATUS START ###\n");
		for(int i =0; i<memory.length;i++)
		{
			if(i>=1000||i<400)
				continue;
			if(isEmpty(i))
			{
				//String message = String.format("Memory [%04d]  %s (%02X%02X) (%d)\n", i, memory[i].getString(),
				//		  memory[i].getLong()&0x00FF,(memory[i].getLong()&0xFF00)>>>8,memory[i].getLong());
				String message = String.format("Memory [%04d]  %s (%d)\n", i, memory[i].getString(),
						memory[i].getLong());
				buffer.append(message);
			}
		}
		buffer.append("### MEMORY STATUS END   ###\n");
		
		
		return buffer.toString();
	}
	public String toString()
	{
		StringBuffer buffer=new StringBuffer();
		for(int i =0; i<memory.length;i++)
		{
			if(isEmpty(i))
			{
				String message = String.format("Memory [%04d]  %s (%02X%02X) (%d)\n", i, memory[i].getString(),
						  memory[i].getLong()&0x00FF,(memory[i].getLong()&0xFF00)>>>8,memory[i].getLong());
				buffer.append(message);
			}
		}
		return buffer.toString();
		
	}
	
	public boolean isEmpty(long address)
	{
		return isMemorys[(int)address];
	}
	

	/**
	 * Get starting location for user program.
	 * @return starting location for user program.
	 */
	public int getUserProgramLocation() {
		return USER_PROGRAM_START;
	}

}
