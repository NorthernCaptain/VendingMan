package northern.captain.vendingman.tools;

public interface IKeyStore
{
	
	public byte[] getKey(int idx);
	
	public int getKeyIdx();
	
}
