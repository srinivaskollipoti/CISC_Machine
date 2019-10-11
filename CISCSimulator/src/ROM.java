import java.io.*;

/**
 * 
 */

/**
 * @author cozyu
 * @author youcao  documented by youcao.
 * A class that retrieves instructions from rom.txt.
 */
public class ROM {
	
	private String message=new String();
	/*
	 * Read boot program of binary format from rom.txt and translate into assemble code.
	 * @return assemble code of boot program
	 */
	public String[] getCode() {
		// little endian
		// LDA 1, 0, 31 	/ 000011 01 00 0 11111   / 1F0D
		// STR 1, 0, 20 	/ 000010 01 00 0 10100   / 1409  
		// LDR 2, 0, 20 	/ 000001 10 00 0 10100   / 1406
		// LDX 1, 20    	/ 100001 00 01 0 10100   / 5484
		// STX 1, 31    	/ 100010 00 01 0 11111   / 5F88
		// LDR 3, 0, 20, I  / 000001 11 00 1 10100   / 3407
		// STR 2, 0, 10, I  / 000010 10 00 1 01010   / 2A0A
	
		String inputFile="rom.txt";
		StringBuffer buffer=new StringBuffer();
		byte[] allBytes;
		
		InputStream inputStream=null;
		try {
			inputStream = new FileInputStream(inputFile);
			long fileSize = new File(inputFile).length();
	        allBytes = new byte[(int) fileSize];
	        inputStream.read(allBytes);
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	      message="Failed to find rom file("+inputFile+")";
	      return null;
	    } catch (IOException e) {	
	      e.printStackTrace();
	      message="Failed to open rom file("+inputFile+")";
	      return null;
	    } finally {
	    	if(inputStream!=null)
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					message="Failed to close rom file("+inputFile+")";
				}
	    }
		
		for(int i=0;i<allBytes.length/2;i=i+1)
		{	
			byte[] readBytes=new byte[2];
			readBytes[0]=allBytes[i*2];
			readBytes[1]=allBytes[i*2+1];
			WORD ir=new WORD(WORD.valueOf(readBytes));
			String asmCode=Translator.getAsmCode(ir);
			if (asmCode==null)
			{
				message="Failed to parse rom file\n[+] Unknown instruction ["+ir.getString()+"]";
				return null;
			}
			buffer.append(asmCode);
        	buffer.append("\n");
		}		
		return buffer.toString().toUpperCase().split("\n");
	}

	
	/*
	 * Get assemble code of boot program from romasm.txt
	 * @return assemble code of boot program
	 */
	public String[] getCodeFromAsm() {
		
		
		String inputFile="romasm.txt";
		StringBuffer buffer=new StringBuffer();
		
		try {
			File file = new File(inputFile);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
	        while((line = bufReader.readLine()) != null){
	        	buffer.append(line);
	        	buffer.append("\n");
	        }
	        bufReader.close();
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	      return null;
	    } catch (IOException e) {
	      e.printStackTrace();
	      return null;
	    }
		return buffer.toString().toUpperCase().split("\n");
	}
	public String getMessage()
	{
		return message;
	}
}
