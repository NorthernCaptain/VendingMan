package northern.captain.vendingman.tools;

public class S4Cipher
{
	private static final int MAXCOLS=256;
	public int index;
	public byte[][] table;
	public byte[][] detable;
	int maxrows;
	
	public S4Cipher(int[] key, int maxkeys, int idx)
	{
		byte[] shiftBuf = new byte[MAXCOLS];
		index = idx;
		table = new byte[maxkeys][];
		detable = new byte[maxkeys][];
		maxrows = maxkeys;
		for(int i=0;i<maxrows;i++)
		{
			table[i] = new byte[MAXCOLS];
			for(int j=0;j<MAXCOLS;j++)
			{
				table[i][j]=(byte)j;
			}
			
			rShiftBytes(table[i], 0, MAXCOLS, key[i], shiftBuf);
			detable[i] = new byte[MAXCOLS];
			for(int j=0;j<MAXCOLS;j++)
			{
				detable[i][0xff & table[i][j]] = (byte)j;
			}
		}
	}

	private void rShiftBytes(byte[] ar, int startIdx, int len, int shift, byte[] shiftBuf)
	{
		if(len <= 0)
			return;
		
		shift %= len;
		if(shift == 0)
			return;
				
		System.arraycopy(ar, startIdx + len - shift, shiftBuf, 0, shift);
		System.arraycopy(ar, startIdx, ar, startIdx+shift, len - shift);
		System.arraycopy(shiftBuf, 0, ar, startIdx, shift);
	}
	
	private void lShiftBytes(byte[] ar, int startIdx, int len, int shift, byte[] shiftBuf)
	{
		if(len <= 0)
			return;
		
		shift %= len;
		if(shift == 0)
			return;
		
		System.arraycopy(ar, startIdx, shiftBuf, 0, shift);
		System.arraycopy(ar, startIdx + shift, ar, startIdx, len - shift);
		System.arraycopy(shiftBuf, 0, ar, startIdx + len - shift, shift);
	}

	private static final int SHIFT_CHUNK = 31;
	/**
	 * Encrypt given byte sequence. Can return more bytes than original buffer
	 * @param srcbuf
	 * @return encrypted byte sequence.
	 */
	public byte[] encrypt(byte[] srcbuf)
	{
		byte[] dstbuf = new byte[srcbuf.length+1];
		dstbuf[0] = (byte)((index + SHIFT_CHUNK + 2) & 0x3f);
		for(int i=0;i<srcbuf.length;i++)
		{
			byte ch = srcbuf[i];
			if(i>0)
				ch ^= dstbuf[i];

			int row = i % maxrows;
			dstbuf[i+1] = table[row][(int)(0xff & ch)];			
		}
		
		byte[] tmpbuf = new byte[SHIFT_CHUNK+1];
		int len = srcbuf.length / SHIFT_CHUNK;
		for(int i=0;i<len;i++)
			rShiftBytes(dstbuf, 1+i*SHIFT_CHUNK, SHIFT_CHUNK, (i+1)*(index+1),tmpbuf);
		return dstbuf;
	}
	
	/**
	 * Decrypt encrypted buffer
	 * @param srcbuf
	 * @return decrypted buffer
	 */
	public byte[] decrypt(byte[] srcbuf)
	{
		int len = (srcbuf.length-1) / SHIFT_CHUNK;

		byte[] tmpbuf = new byte[SHIFT_CHUNK+1];
		for(int i=0;i<len;i++)
			lShiftBytes(srcbuf, 1+i*SHIFT_CHUNK, SHIFT_CHUNK, (i+1)*(index+1),tmpbuf);

		int row;

		byte prev = 0;
		len = srcbuf.length -1;
		byte[] dstbuf = new byte[len];
		for(int i = 0;i<len;i++)
		{
			byte ch = srcbuf[i+1];
			row = i % maxrows;
			ch = detable[row][(int)(0xff & ch)];

			if(i>0)
				ch ^= prev;
			prev = srcbuf[i+1];
			dstbuf[i] = ch;
		}
		
		return dstbuf;
	}
	
	public static int getChunkSchema(byte[] chunk)
	{
		return (int)(chunk[0] & 0x3f) - 2 - SHIFT_CHUNK;
	}	
}
