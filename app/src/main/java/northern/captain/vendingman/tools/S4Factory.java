package northern.captain.vendingman.tools;

public class S4Factory
{
	private static S4Factory singleton;
	
	public static S4Factory instance()
	{
		if(singleton == null)
		{
			singleton = new S4Factory(S4FarmKey.key, S4FarmKey.MAX_KEYS);
		}
		return singleton;
	}
	
	private S4Cipher[] ciphers;
	
	public S4Factory(int[] keys, int totalkeys)
	{
		ciphers = new S4Cipher[totalkeys];
		int key[] = new int[32];
		int idx = 0;
		int row = 0;
		for(int i=0;i<keys.length;i++)
		{
			if(keys[i]==0)
			{
				ciphers[row] = new S4Cipher(key, idx, row);
				row++;
				idx = 0;
			} else
				key[idx++] = keys[i] & 0x3f;
		}
	}
		
	public S4Cipher getCipherByChunk(byte[] chunk)
	{
		int idx = S4Cipher.getChunkSchema(chunk);
		if(idx < 0 || idx>=ciphers.length)
			return null;
		return ciphers[idx];
	}
	
	
	public S4Cipher getRandomCipher()
	{
		return ciphers[S4Base64.rnd.nextInt(ciphers.length)];
	}
}
