package northern.captain.vendingman.tools;

public interface ICipher
{
	/**
	 * Encrypt given byte sequence. Can return more bytes than original buffer
	 * @param from
	 * @return encrypted byte sequence.
	 */
	public byte[] encrypt(byte[] from);
	
	/**
	 * Decrypt encrypted buffer
	 * @param enc
	 * @return decrypted buffer
	 */
	public byte[] decrypt(byte[] enc);
}
