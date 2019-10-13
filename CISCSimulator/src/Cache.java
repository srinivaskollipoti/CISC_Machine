/**
 * perform operation related cache
 */

/**
 * @author srinukollipoti
 *
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.text.html.HTML.Tag;

import java.util.*;


public class Cache {


		
private final LinkedList<WORD> cache;

private final static Logger LOG = Logger.getGlobal();

//private final HashMap<Long, WORD> find;

private final static int DEFAULT_CACHE_SIZE = 16;
private final int maxCacheSize;
private long C_hit = 0, C_miss = 0, totalhits = 0;

public Cache() {
    this.cache = new LinkedList<>();
    //this.find = new HashMap<>();
    this.maxCacheSize = DEFAULT_CACHE_SIZE;
    this.cachestack = new LinkedList<CacheLoad>();
    		
    		
   // cache= new ArrayList();
    //for(int i =0;i<128;i++)
    //	cache.add(cacheline);
    
    //long address = <= from function
    //divide into tag, offset
    //for(int i =0;i<128;i++)
    //	if(tag == cache.get[i].tag)
    //		cache.get[i].getData(offset)
    initCacheline();
    
}

public Cache(int cacheSize) {
    this.cache = new LinkedList<>();
    //this.find = new HashMap<>();
    this.maxCacheSize = cacheSize;
	  
    initCacheline();
	}

	public class CacheLoad {

		//long tag;
		WORD data;
	

		public CacheLoad( WORD data) {
			//this.address = address;
			this.data.copy(data);
		}

		//public long getMemAddress() {
		//	LOG.info( "address in cache"+this.address);
			//return this.address;
	//	}

		//public void setAddress(long address) {
			//this.address = address;
		//}

		public WORD getData() {
			return data;
		}

		public void setData(WORD data) {
			this.data.copy(data);
		}
	}

	LinkedList<CacheLoad> cachestack;
	//LinkedList<CacheLoad> [] arrayCacheline=new LinkedList<CacheLoad>[128];
	ArrayList< LinkedList<CacheLoad> > arrayCacheline= new ArrayList< LinkedList<CacheLoad> >();
	long tagOfCacheline[] =new long[128];

	
	public void initCacheline()
	{
		for(int i =0; i<128; i++)
		{
			arrayCacheline.add(new LinkedList<CacheLoad>());
			tagOfCacheline[i]=0;
		}
	}
	

	public LinkedList<CacheLoad> getCacheLines() {
		return cachestack;
	}
	
	/* check for the data in cache if the data in cache stack is > DEFAULT_CACHE_SIZE
	 * remove it from the stack.(FIFO)
	 */

	public WORD fetch(long address)
	{
		// 10101010 01010101
		// tag 10101010 0101 
		// offset 0101
		
//		0
//		16
//		32
//		48
		
//		if addresss 17
//		base 16
//		offset 2
		
//		if address 35
//		base 32
//		offset 4
		
		long tag = address; // temp value, extract from address or use whole address
		LinkedList<CacheLoad> target = null;
		
		for (int i = 0; i < 128; i++) {
		if (tagOfCacheline[i] == tag)
			target = arrayCacheline.get(i);
		}
		//CacheLoad targetData=target.get(offset);
		CacheLoad targetData=target.get(11);
		return targetData.getData();
	
		
		
	}
	
	
	public void add(long address, WORD value) {
		int tag = 55555; // temp value, extract from address or use whole address
		LinkedList<CacheLoad> target = null;
		for (int i = 0; i < 128; i++) {
			if (tagOfCacheline[i] == tag)
				target = arrayCacheline.get(i);
		}
		
		if (target.size() >= Cache.DEFAULT_CACHE_SIZE) {
			target.removeLast();
		}
		target.addFirst(new CacheLoad(value));

		/*
		if (this.cachestack.size() >= Cache.DEFAULT_CACHE_SIZE) {
			this.cachestack.removeLast();
		}
		this.cachestack.addFirst(new CacheLoad(value));
		*/
	}



	/**
	 * 
	 */
   
	public WORD load(long address) {
		WORD result=null;
		return result;
	}

	public boolean store(long address,WORD input) {
		return true;
	}

}
