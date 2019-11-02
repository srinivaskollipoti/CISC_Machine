import java.util.logging.Logger;

/**
 * 
 */


/**
 * Perform AND, OR, NOT, XOR, add, sub, mul, div and complement, shift operation 
 * @author cozyu (Yeongmok You)
 * @see ALInstHandler  
 */
public class ALU {
	protected final static Logger LOG = Logger.getGlobal();	
	StringBuffer message=new StringBuffer();
	
	private CPU cpu;

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
		message.append(String.format("==> [ALU] %d + %d",i1,i2));		
		short sum=(short) (i1+i2);
		message.append(String.format(" = %d\n",sum));		
		if(i1>0 && i2>0 && sum<0)
			setOverflow(true);
		else if(i1<0 && i2<0 && sum>0)
			setOverflow(true);
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
		message.append(String.format("==> [ALU] %d - %d",i1,i2));		
		int afterSub=i1-i2;
		short sum=(short) afterSub;
		message.append(String.format(" = %d\n",sum));
		if(afterSub>result.maxValue || afterSub<result.minValue)
			setOverflow(true);
		result.setLong(sum);
		return result;
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
		message.append(String.format("==> [ALU] %d * %d",i1,i2));
		int afterMul= i1*i2;
		GBitSet bit32=new GBitSet(32);
		bit32.setSigned(true);
		bit32.setLong(afterMul);
		message.append(String.format(" = %d\n",afterMul));
		result[0].copy(bit32.subSet(WORD.SIZE, WORD.SIZE*2));
		result[1].copy(bit32.subSet(0,WORD.SIZE));
		
		if(i1>0 && i2>0 && afterMul<0)
			setOverflow(true);
		else if(i1<0 && i2<0 && afterMul<0)
			setOverflow(true);
		else if(i1<0 && i2>0 && afterMul>0)
			setOverflow(true);
		else if(i1>0 && i2<0 && afterMul>0)
			setOverflow(true);
		
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
		message.append(String.format("==> [ALU] %d / %d",i1,i2));
		if(i2==0){
			setDivzero(true);
			return null;
		}
		short quotient=(short) (i1/i2);
		short remainer=(short) (i1%i2);
		message.append(String.format(" => Quotient = %d, Remainer = %d\n",quotient,remainer));
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
	
	/**
	 * check two word is same if it is same, set EqualOrNot flag
	 * @param s1 select 1
	 * @param s2 select 2
	 * @return if same, return true, otherwise return false
	 */
	public boolean equal(final WORD s1,final WORD s2) {
		message.setLength(0);
		this.setEqualOrNot(false);
		boolean result=s1.equals(s2);
		if (result==true)
			this.setEqualOrNot(true);
		return result;
	}

	/**
	 * perform NOT operation.
	 * @param s1 select 1
	 * @return result after operation
	 */
	public WORD not(final WORD s1) {
		message.setLength(0);
		WORD result=new SignedWORD(s1);
		result.flip(0,result.length);
		message.append(String.format("==> [ALU] NOT %d = %d\n",s1.getLong(),result.getLong()));		
		return result;
	}	

	/**
	 * perform AND operation between two word,
	 * @param s1 select 1
	 * @param s2 select 2
	 * @return result after operation
	 */
	public WORD and(final WORD s1,final WORD s2) {
		message.setLength(0);
		WORD result=new SignedWORD(s1);
		result.and(s2);
		message.append(String.format("==> [ALU] %d AND %d = %d\n",s1.getLong(),s2.getLong(),result.getLong()));		
		return result;
	}

	/**
	 * perform OR operation between two word,
	 * @param s1 select 1
	 * @param s2 select 2
	 * @return result after operation
	 */
	public WORD or(final WORD s1,final WORD s2) {
		message.setLength(0);
		WORD result=new SignedWORD(s1);
		result.or(s2);
		message.append(String.format("==> [ALU] %d OR %d = %d\n",s1.getLong(),s2.getLong(),result.getLong()));		
		return result;
	}

	/**
	 * perform SHIFT operation on one WORD 
	 * if overflow is occurred, set overflow flag
	 * if underflow is occurred, set underflow flag
	 * @param s1 select 1
	 * @param count number of shift
	 * @param isLeft left(true) or right(false)
	 * @param isArith arithmetic(true) or logical(false)
	 * @return result after operation
	 */
	public WORD shift(final WORD s1,int count, boolean isLeft, boolean isArith) {
		message.setLength(0);
		setOverflow(false);
		setUnderflow(false);

		WORD result=new SignedWORD(s1);
		result.shift(isLeft, count,isArith);
		message.append(String.format("[ALU] SHIFT %d (%s, %s, COUNT=%d)\n==> %d\n",
				s1.getLong(),isLeft?"LEFT":"RIGHT",isArith?"ARITH":"LOGICAL",count,result.getLong()));
		
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
		return result;
	}

	/**
	 * perform ROTATE operation on one WORD
	 * @param s1 select 1
	 * @param count number of shift
	 * @param isLeft left(true) or right(false)
	 * @param isArith arithmetic(true) or logical(false)
	 * @return result after operation
	 */
	public WORD rotate(final WORD s1,int count, boolean isLeft, boolean isArith) {
		message.setLength(0);
		WORD result=new SignedWORD(s1);
		result.rotate(isLeft, count, isArith);
		message.append(String.format("[ALU] ROTATE %d (%s, %s, COUNT=%d)\n==> %d\n",
				s1.getLong(),isLeft?"LEFT":"RIGHT",isArith?"ARITH":"LOGICAL",count,result.getLong()));
		return result;
	}
	
	private void setOverflow(boolean input) {
		//message.setLength(0);
		cpu.getCC().set(0, input);
		if(input)
			message.append("==> Set CC : Overflow\n");
	}

	private void setUnderflow(boolean input) {
		//message.setLength(0);
		cpu.getCC().set(1, input);
		if(input)
			message.append("==> Set CC : Underflow\n");
	}

	private void setDivzero(boolean input) {
		//message.setLength(0);
		cpu.getCC().set(2, input);
		if(input)
			message.append("==> Set CC : Divzero\n");
	}

	private void setEqualOrNot(boolean input) {
		//message.setLength(0);
		cpu.getCC().set(3, input);
		if(input)
			message.append("==> Set CC : EqualOrNot\n");
	}

	public String getMessage() { return message.toString();}
}
