import java.util.logging.Logger;

/**
 * 
 */


/**
 * Perform AND, OR, NOT, XOR, add, sub, mul, div and complement, shift operation 
 * @author cozyu
 * @param 
 * input : GPR, IR, MBR, PC  
 */
public class ALU {
	protected final static Logger LOG = Logger.getGlobal();	

	StringBuffer message=new StringBuffer();
	
	private CPU cpu;
	// acc
	// temp register
	
	/**
	 * 
	 */
	public ALU(CPU cpu) {
		this.cpu=cpu;
	}
	
	/**
	 * add two word and if overflow is occurred, set overflow flag
	 * @param s1 select 1
	 * @param s2 select 2
	 * @return result of s1 plus s2
	 */
	public WORD add(WORD s1, WORD s2) {
		message.setLength(0);
		setOverflow(false);
		WORD result=new SignedWORD();
		short i1=(short) s1.getLong();
		short i2=(short) s2.getLong();
		message.append(String.format("[ALU] %d + %d\n",i1,i2));		
		short sum=(short) (i1+i2);
		if(i1>0 && i2>0 && sum<0)
			setOverflow(true);
		else if(i1<0 && i2<0 && sum>0)
			setOverflow(true);
		message.append(String.format("[+] Result = %d\n",sum));
		result.setLong(sum);
		return result;
	}

	
	
	/**
	 * subtract two word and if overflow is occurred, set overflow flag
	 * @param s1 select 1
	 * @param s2 select 2
	 * @return result of s1 minus s2.
	 */
	public WORD sub(final WORD s1,final WORD s2) {
		message.setLength(0);
		setOverflow(false);
		WORD result=new SignedWORD();
		short i1=(short) s1.getLong();
		short i2=(short) s2.getLong();
		message.append(String.format("[ALU] %d - %d\n",i1,i2));		
		int afterSub=i1-i2;
		if(afterSub>result.maxValue || afterSub<result.minValue)
			setOverflow(true);
		short sum=(short) afterSub;
		message.append(String.format("[+] Result = %d\n",sum));
		result.setLong(sum);
		return result;
//		return add(s1,complement(s2));
	}

	
	/**
	 * multiply two word and if overflow is occurred, set overflow flag
	 * @param s1 select 1
	 * @param s2 select 2
	 * @return value to multiply s1 and s2
	 */
	public WORD[] mul(final WORD s1,final WORD s2) {
		message.setLength(0);
		setOverflow(false);
		WORD[] result=new SignedWORD[2];
		for(int i=0;i<2;i++) {
			result[i]=new SignedWORD();
		}
		short i1=(short) s1.getLong();
		short i2=(short) s2.getLong();
		message.append(String.format("[ALU] %d * %d\n",i1,i2));
		int afterMul= i1*i2;
		
		if(i1>0 && i2>0 && afterMul<0)
			setOverflow(true);
		else if(i1<0 && i2<0 && afterMul<0)
			setOverflow(true);
		else if(i1<0 && i2>0 && afterMul>0)
			setOverflow(true);
		else if(i1>0 && i2<0 && afterMul>0)
			setOverflow(true);
		
		GBitSet bit32=new GBitSet(32);
		bit32.setLong(afterMul);
		message.append(String.format("[+] Result = %d\n",afterMul));
		result[0].copy(bit32.subSet(WORD.SIZE, WORD.SIZE*2));
		result[1].copy(bit32.subSet(0,WORD.SIZE));
		return result;
	}
	
	/**
	 * divide two word and if divzero is occurred, divzero flag
	 * @param s1 select 1
	 * @param s2 select 2
	 * @return the quotient and remainder of s1 divided by s2.
	 */
	public WORD[] div(final WORD s1,final WORD s2) {
		message.setLength(0);
		setDivzero(false);
		WORD[] result=new SignedWORD[2];
		for(int i=0;i<2;i++) {
			result[i]=new SignedWORD();
		}
		short i1=(short) s1.getLong();
		short i2=(short) s2.getLong();
		message.append(String.format("[ALU] %d / %d\n",i1,i2));
		if(i2==0){
			setDivzero(true);
			return null;
		}
		short quotient=(short) (i1/i2);
		short remainer=(short) (i1%i2);
		message.append(String.format("[+] Quotient = %d, Remainer = %d\n",quotient,remainer));
		result[0].setLong(quotient);
		result[1].setLong(remainer);
		
		return result;
	}

	/**
	 * calculate 2's complement
	 * @param s1	input word
	 * @return 2's complement
	 */
	public WORD complement(final WORD s1) {
		message.setLength(0);
		// positive number range is one less than negative number range
		if(s1.getLong()==s1.minValue)
			setOverflow(true);
			//throw new IllegalArgumentException("Failed to calculate 2's complement for "+s1);
		WORD comp=new SignedWORD(s1);
		comp.flip(0,comp.length);
		short sum=(short) ((short)comp.getLong()+1);
		comp.setLong(sum);
		return comp;
	}
	
	public boolean equal(final WORD s1,final WORD s2) {
		message.setLength(0);
		this.setEqualOrNot(false);
		boolean result=s1.equals(s2);
		if (result==true)
			this.setEqualOrNot(true);
		return result;
	}

	public WORD not(final WORD s1) {
		message.setLength(0);
		WORD result=new SignedWORD(s1);
		result.flip(0,result.length);
		return result;
	}	

	public WORD and(final WORD s1,final WORD s2) {
		message.setLength(0);
		WORD result=new SignedWORD(s1);
		result.and(s2);
		return result;
	}

	public WORD or(final WORD s1,final WORD s2) {
		message.setLength(0);
		WORD result=new SignedWORD(s1);
		result.or(s2);
		return result;
	}

	public WORD shift(final WORD s1,int count, boolean isLeft, boolean isArith) {
		message.setLength(0);
		setOverflow(false);
		setUnderflow(false);

		LOG.info(String.format("SHIFT LEFT=%b, ARITH=%b, COUNT=%d\n",isLeft,isArith,count));
		LOG.info(s1.toString());
		WORD result=new SignedWORD(s1);
		result.shift(isLeft, count,isArith);
		
		if(isLeft) {
			int end=s1.length;
			if(isArith == true) end--;
			int check=0;
			// in case of overflow of negative number, number of 1 is same with count
			if(s1.get(WORD.SIZE-1)==true)	check=count;
			if(s1.subSet(end-count, end).cardinality()!=check)
				setOverflow(true);
			
		}else {
			if(s1.subSet(0, count).isEmpty()==false)
				setUnderflow(true);
		}

		LOG.info(result.toString());
		return result;
	}

	public WORD rotate(final WORD s1,int count, boolean isLeft, boolean isArith) {
		message.setLength(0);
		LOG.info(String.format("ROTETE LEFT=%b, ARITH=%b, COUNT=%d\n",isLeft,isArith,count));
		LOG.info(s1.toString());
		WORD result=new SignedWORD(s1);
		result.rotate(isLeft, count, isArith);
		LOG.info(result.toString());
		return result;
	}
	
	private void setOverflow(boolean input) {
		cpu.getCC().set(0, input);
		if(input)
			message.append("[+] Set Overflow\n");
	}

	private void setUnderflow(boolean input) {
		cpu.getCC().set(1, input);
		if(input)
			message.append("[+] Set Underflow\n");
	}

	private void setDivzero(boolean input) {
		cpu.getCC().set(2, input);
		if(input)
			message.append("[+] Set Divzero\n");
	}

	private void setEqualOrNot(boolean input) {
		cpu.getCC().set(3, input);
		if(input)
			message.append("[+] Set EqualOrNot\n");
	}

	public String getMessage() { return message.toString();}
}
