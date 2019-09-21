import java.util.ArrayList;

/**
 * 
 */

/**
 * @author cozyu
 *
 */
public class InstructionHelper {

	public static boolean Translate(String strCode, ArrayList<WORD> binCode)
	{
		binCode.clear();
		for (int i=0; i<strCode.length()/4; i++)
		{
			String subText=strCode.substring(i*4,i*4+4);
			WORD code=new WORD(subText);
			
			binCode.add(code);
		}
	
		return true;
	}
	
	public static String binToHex(String binText)
	{
		int decimal = Integer.parseInt(binText,2);
		String hexText=Integer.toString(decimal,16);
		int byteNumber= binText.length()/4;
		int padding=byteNumber-hexText.length();
		StringBuffer buffer=new StringBuffer();
		for(int i=0;i<padding;i++)
			buffer.append("0");
		buffer.append(hexText.toUpperCase());
		return buffer.toString();
	}
}
