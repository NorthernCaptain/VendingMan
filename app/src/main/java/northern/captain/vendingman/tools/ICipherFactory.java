package northern.captain.vendingman.tools;

public interface ICipherFactory
{
	public static final int MAX_SCHEMAS = 12;
	/**
	 * Creates and returns new Cipher initialized with the given key
	 * @param key
	 * @return
	 */
	public ICipher createCipher(byte[] key);
	
	/**
	 * Return cipher for given schema. If there is no cipher then create new one with the key
	 * and put it into schema
	 * @param schema
	 * @param key
	 * @return
	 */
	public ICipher forSchema(int schema, byte[] key);
	
	/**
	 * Extract schema number from the start chunk
	 * @param chunk
	 * @return
	 */
	public int getChunkSchema(byte[] chunk);
}
