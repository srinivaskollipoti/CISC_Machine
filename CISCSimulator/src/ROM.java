import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 */

/**
 * @author cozyu
 * @author youcao  documented by youcao.
 * A class that retrieves instructions from rom.txt.
 */
public class ROM {
	
	/*
	 * Get assemble code of boot program from rom.txt
	 * @return assemble code of boot program
	 */
	public String[] getCode() {
		// LDA 1, 0, 31 / 000011 01 00001000
		// STR 1, 0, 20 / 000010 01 00001101
		// LDR 2, 0, 20 / 000001 10 00001101
		// LDX 1, 20    / 100001 00 01001101
		// STX 1, 32    / 100010 00 01011111
	
		StringBuffer buffer=new StringBuffer();
		
		try {
			File file = new File("rom.txt");
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
}
