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
import java.util.*;


public class Cache {


		
private final LinkedList<WORD> cache;

private final HashMap<Long, WORD> find;

private final static int DEFAULT_CACHE_SIZE = 16;
private final int maxCacheSize;
private long C_hit = 0, C_miss = 0, totalhits = 0;

public Cache() {
    this.cache = new LinkedList<>();
    this.find = new HashMap<>();
    this.maxCacheSize = DEFAULT_CACHE_SIZE;
    this.cachestack = new LinkedList<CacheLoad>();
}

public Cache(int cacheSize) {
    this.cache = new LinkedList<>();
    this.find = new HashMap<>();
    this.maxCacheSize = cacheSize;
	  

	}

	public class CacheLoad {

		long address;
		WORD data;
	

		public CacheLoad(long address, WORD data) {
			this.address = address;
			this.data = data;
		}

		public long getMemAddress() {
			return this.address;
		}

		public void setAddress(long address) {
			this.address = address;
		}

		public WORD getData() {
			return data;
		}

		public void setData(WORD data) {
			this.data = data;
		}
	}

	LinkedList<CacheLoad> cachestack;



	public LinkedList<CacheLoad> getCacheLines() {
		return cachestack;
	}

	public void add(long address, WORD data) {
		if (this.cachestack.size() >= Cache.DEFAULT_CACHE_SIZE) {
			this.cachestack.removeLast();
		}
		this.cachestack.addFirst(new CacheLoad(address, data));
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
