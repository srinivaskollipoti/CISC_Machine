/**
 * 
 */

/**
 * IO Controller
 * @author cao you
 * 
 */
public class IOC {

	public static final int MAX_DEVID=32;
	public static final int KEYBOARD=0;
	public static final int PRINTER=1;
	public static final int CARD_READER=2;

	
	public static final char NONE_INPUT=(char)-1;
	public static final char END_OF_TEXT=(char)0;
	
	private StringBuffer[] ioBuffer;
	CISCSimulator simu;

	/**
	 * 
	 */
	public IOC(CISCSimulator simu) {
		this.simu=simu;
		ioBuffer=new StringBuffer[MAX_DEVID];
		for(int i =0; i<MAX_DEVID;i++)
			ioBuffer[i]=new StringBuffer();
	}
	
	public void init()
	{
		for(int i =0; i<MAX_DEVID;i++)
			ioBuffer[i].setLength(0);
	}
	
	/// IO Controller
	public void appendIOBuffer(int devID,char i) {
		if(devID>MAX_DEVID || devID<KEYBOARD)
			throw new IllegalArgumentException("DevID must be between 0-31");
		ioBuffer[devID].append(i);
	}
	public char getIOBuffer(int devID) {
		if(devID>MAX_DEVID || devID<KEYBOARD)
			throw new IllegalArgumentException("DevID must be between 0-31");
		if(isIOBuffer(devID)==false)
			return NONE_INPUT;
		
		char result= ioBuffer[devID].charAt(0);
		ioBuffer[devID].deleteCharAt(0);
		return result;
	}


	public boolean isIOBuffer(int devID) {
		if(devID>MAX_DEVID || devID<KEYBOARD)
			throw new IllegalArgumentException("DevID must be between 0-31");
		if(ioBuffer[devID].length()==0)
			return false;
		
		return true;
	}

	
	
	// for CISCSimulator, don't use this function except CISCSimulator
	public void appendIOBuffer(int devID,String text) {
		if(devID>MAX_DEVID || devID<KEYBOARD)
			throw new IllegalArgumentException("DevID must be between 0-31");
		ioBuffer[devID].append(text);
	}

	public String getIOBufferString(int devID) {
		if(devID>32 || devID<0)
			throw new IllegalArgumentException("DevID must be between 0-31");
		if(isIOBuffer(devID)==false)
			return "";
		
		String result=ioBuffer[devID].toString();
		ioBuffer[devID].setLength(0);
		return result;
	}

	


}
