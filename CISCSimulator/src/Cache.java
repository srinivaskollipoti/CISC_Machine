/**
 * perform operation related cache
 * @author srinukollipoti
 * @author cozyu (Yeongmok You)
 */

import java.util.logging.Logger;
import java.util.*;

/**
 * Implement cacheline
 * @author cozyu(Yeongmok You)
 *
 */
class CacheLine {	

	public static final int MAX_DATA=16;
	private long tag;
	private WORD[] data;
	public CacheLine(long tag) {		
		data=new SignedWORD[MAX_DATA];
		this.tag=tag;
	}

	public void setTag(long tag) { this.tag = tag; }
	public long getTag() { return this.tag; }

	/**
	 * get data from the index
	 * @param index the location of data
	 * @return data of the index
	 */
	public WORD getData(int index) {
		if(index>MAX_DATA || index<0)
			throw new IllegalArgumentException("Index must be between 0 and "+MAX_DATA);
		return data[index];
	}

	/**
	 * set data into the index
	 * @param index the location of data
	 * @param input the data to store
	 * @return if success, return true, otherwise return false
	 */
	public boolean setData(int index,WORD input) {
		if(index>MAX_DATA || index<0)
			throw new IllegalArgumentException("Index must be between 0 and "+MAX_DATA);
		if(data[index]==null)
			data[index]=new SignedWORD();
		
		data[index].copy(input);
		return true;
	}
}

/**
 * Implement Cache
 * The simulator has 2048 WORD memory, and has the cache of same size.
 * Size of cache line is 16 WORD.
 * The number of cacheline is 128.
 * The replacement of cache is operated in accordance with FIFO policy.
 * The tag is 12bit and the bytes is 4bit
 * @author cozyu
 *
 */
public class Cache {
	
	private final static Logger LOG = Logger.getGlobal();


	private String message="";
	private final LinkedList<CacheLine> cache;		// the list of cacheline
	private final static int MAX_CACHE_LINE = 128;	// maximum of cacheline
	private long hit = 0, miss = 0, access = 0;

	public Cache() {
		this.cache = new LinkedList<CacheLine>();
	}
	
	public double getHitRate() {
		if(access==0) return 0;
		return ((double)hit)/access*100;
	}
	public double getMissRate() {
		if(access==0) return 0;
		return ((double)miss)/access*100;
	}
	public double getTotalAccess() {return access;}

	public String toString() {
		return String.format("[Cache] Hit Rate : %02.1f%%\n==> Hit(%06d), Miss (%06d)",
				getHitRate(), hit, miss);
	}
	
	public void init()
	{
		hit=0; miss=0; access=0;
		cache.clear();
	}
	
	
	/***
	 * load data from cache
	 * @param address the address of memory
	 * @return data matched the address
	 * @author cozyu(Yeongmok You)
	 */
	public WORD load(long address) 
	{	
		//message="";
		access++;
		WORD word=new WORD();
		word.setLong(address);
		long tag=word.subSet(4, WORD.SIZE).getLong();
		long bytes=word.subSet(0, 4).getLong();

		CacheLine line= getCacheLine(tag);
		if(line==null)
			line=addCacheLine(tag);
		WORD data=line.getData((int)bytes);
		if(data==null)
		{
			miss++;
			message+=String.format("==> Address %4d is missed in cache\n", address);
		}
		else {
			hit++;
			//message=String.format("address %4d is hit in cache\n", address);
		}
		//LOG.info(message);
		return data;
	}

	/***
	 * store data into cache
	 * @param address the address of memory
	 * @param input the data to store
	 * @return if succeed, return true, otherwise return false
	 * @author cozyu(Yeongmok You)
	 */
	public boolean store(long address,WORD input) {
		WORD wordAddr=new WORD();
		wordAddr.setLong(address);
		long tag=wordAddr.subSet(4, WORD.SIZE).getLong();
		long bytes=wordAddr.subSet(0, 4).getLong();
		CacheLine line= getCacheLine(tag);
		if(line==null)
			line=addCacheLine(tag);
		return line.setData((int)bytes, input);
	}


	/***
	 * get cache line matched the tag
	 * @param tag tag to find
	 * @return matched cacheline
	 * @author cozyu(Yeongmok You)
	 */

	public CacheLine getCacheLine(long tag) {
		for(CacheLine line:cache) {
			if(line.getTag()==tag)
				return line;
		}
		return null;
	}

	/***
	 * add cache line
	 * @param tag the tag of cache line
	 * @return added line
	 * @author cozyu(Yeongmok You)
	 */
	public CacheLine addCacheLine(long tag)
	{
		// FIFO
		if(cache.size()==MAX_CACHE_LINE)
			cache.removeLast();
		CacheLine line=new CacheLine(tag);
		cache.addFirst(line);
		return line;	
	}
	
	public String getMessage()
	{
		String temp = new String(message);
		message="";
		return temp;
	}
	
	public String seekMessage()
	{
		return message;
	}




}
