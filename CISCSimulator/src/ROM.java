import java.io.*;
import java.util.ArrayList;

/**
 * 
 */

/**
 * @author cozyu
 * @author youcao  documented by youcao.
 * A class that retrieves instructions from rom.txt.
 */
public class ROM {
	
	private static String message=new String();
	
	public static ArrayList<WORD> getBinCode()
	{
		return getBinCode("rom.txt");
	}
	
	public static ArrayList<WORD> getBinCode(String path)
	{
		message="";
		ArrayList<WORD> arrBinCode=new ArrayList<WORD>();
		
		String inputFile=path;
		byte[] allBytes;		
		InputStream inputStream=null;
		try {
			inputStream = new FileInputStream(inputFile);
			long fileSize = new File(inputFile).length();
	        allBytes = new byte[(int) fileSize];
	        inputStream.read(allBytes);
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	      message="Failed to find rom file("+inputFile+")\n";
	      return null;
	    } catch (IOException e) {	
	      e.printStackTrace();
	      message="Failed to open rom file("+inputFile+")\n";
	      return null;
	    } finally {
	    	if(inputStream!=null)
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					message="Failed to close rom file("+inputFile+")\n";
					return null;
				}
	    }
		
		for(int i=0;i<allBytes.length/2;i=i+1)
		{	
			byte[] readBytes=new byte[2];
			readBytes[0]=allBytes[i*2];
			readBytes[1]=allBytes[i*2+1];
			
			WORD binCode=new SignedWORD(GBitSet.valueOf(readBytes));
			long value=binCode.getLong();	
			binCode.setLong(value);   		// for 64bit setting, 
        	arrBinCode.add(binCode);
        	String asmCode=Translator.getAsmCode(binCode);
        	System.out.println(asmCode);
        	System.out.println(binCode);
		}		

		
		return arrBinCode;
	}
		
	
	public static String getMessage()
	{
		return message;
	}
}
