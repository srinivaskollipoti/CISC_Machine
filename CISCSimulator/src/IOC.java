import java.io.File;

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
	public static final String CARD_READER_FILE="reader.txt";

	
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
	
	/**
	 * Append a character into IO buffer
	 * @param devID device ID
	 * @param a character to append
	 */
	public void appendIOBuffer(int devID,char i) {
		if(devID>MAX_DEVID || devID<KEYBOARD)
			throw new IllegalArgumentException("DevID must be between 0-31");
		ioBuffer[devID].append(i);
	}
	
	/**
	 * Get a character from IO buffer
	 * @param devID device ID
	 * @return a character gotten from the IO buffer
	 */
	public char getIOBuffer(int devID) {
		if(devID>MAX_DEVID || devID<KEYBOARD)
			throw new IllegalArgumentException("DevID must be between 0-31");
		if(isIOBuffer(devID)==false)
			return NONE_INPUT;
		
		char result= ioBuffer[devID].charAt(0);
		ioBuffer[devID].deleteCharAt(0);
		return result;
	}

	/**
	 * Check if IO buffer is empty
	 * @param devID device iD
	 * @return if empty, return false, otherwise return true
	 */
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

	/**
	 * Return name of device
	 * @param devId
	 * @return name of device
	 */
	public String getName(int devId) {
		String result= "Device #"+Integer.toString(devId);
		switch(devId)
		{
		case KEYBOARD:
			result="Keyboard";
			break;
		case PRINTER:
			result="Printer";
			break;
		case CARD_READER:
			result="Card Reader";
			break;
		}
		
		return result;
	}
	
	/**
	 * Return status of device
	 * @param devId
	 * @return status of device
	 */
	public boolean getStatus(int devId) {
		boolean status=true;
		if(devId==CARD_READER)
		{
			File f = new File(CARD_READER_FILE); 
			status= f.exists();
		}
		return status;
	}

	


}
