/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package northern.captain.vendingman.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author leo
 */
public class Encodeco
{
    public static final String gameUuid = "C8B167AB-AB9C-9E2B-ED2C-E5851A1E9779";
    private static byte[] encPattern;

    static
    {
        try
        {
            encPattern = gameUuid.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex)
        {
        }
    }

	private static byte[] simpleCrypt(byte[] buf)
	{
		int eidx = 0;
		for(int i=0; i<buf.length; i++)
		{
			buf[i] ^= encPattern[eidx++];
			if(eidx >= encPattern.length)
				eidx = 0;
		}
		return buf;		
	}
	
	private static byte[] simpleDecrypt(byte[] buf)
	{
		return simpleCrypt(buf);
	}
	
	private static byte[] writeGZIP(byte[] buf)
	{
		GZIPOutputStream zos = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			zos = new GZIPOutputStream(new BufferedOutputStream(bos));
			zos.write(buf);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if(zos != null)
					zos.close();
			}
			catch (IOException ex)
			{
			}
		}
		
		return bos.toByteArray();
	}
	
	private static byte[] readGZIP(byte[] buf)
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(buf);
		GZIPInputStream zis = null;

		StringBuilder builder = new StringBuilder();
		
		try
		{
			zis = new GZIPInputStream(new BufferedInputStream(bis));
			Reader reader = new InputStreamReader(zis, "UTF-8");
			try 
			{
				char[] tmp = new char[1024];
				int l;
				while((l = reader.read(tmp)) != -1) 
				{
					builder.append(tmp, 0, l);
				}
			} finally 
			{
				reader.close();
			}
		}
		catch (IOException ex1)
		{
			ex1.printStackTrace();
		}

		try
		{
			buf = builder.toString().getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException ex1)
		{
		}
		
		return buf;
	}

	
	public static String encode(String toEncode)
	{
		try
		{
			byte[] buf = toEncode.getBytes("UTF-8");

			buf = simpleCrypt(buf);
			
			buf = writeGZIP(buf);
			
			return MyBase64.encode(buf);
		}
		catch (UnsupportedEncodingException ex)
		{
		}
		
		return toEncode;
	}

	public static String decode(String toDecode)
	{
		byte[] buf = MyBase64.decode(toDecode);

		buf = readGZIP(buf);
		
		buf = simpleDecrypt(buf);

		try
		{
			return new String(buf, "UTF-8");
		}
		catch (UnsupportedEncodingException ex)
		{
			return toDecode;
		}
	}

	public static String encode2(String toEncode)
	{
		try
		{
			byte[] buf = toEncode.getBytes("UTF-8");

			buf = writeGZIP(buf);

			buf = simpleCrypt(buf);
						
			return MyBase64.encode(buf);
		}
		catch (UnsupportedEncodingException ex)
		{
		}
		
		return toEncode;
	}

	public static String decode2(String toDecode)
	{
		byte[] buf = MyBase64.decode(toDecode);

		buf = simpleDecrypt(buf);

		buf = readGZIP(buf);
		
		try
		{
			return new String(buf, "UTF-8");
		}
		catch (UnsupportedEncodingException ex)
		{
			return toDecode;
		}
	}

	public static String encode3(String toEncode)
	{
		try
		{
			byte[] buf = toEncode.getBytes("UTF-8");

			buf = simpleCrypt(buf);
						
			return MyBase64.encode(buf);
		}
		catch (UnsupportedEncodingException ex)
		{
		}
		
		return toEncode;
	}

	public static String decode3(String toDecode)
	{
		byte[] buf = MyBase64.decode(toDecode);

		buf = simpleDecrypt(buf);

		try
		{
			return new String(buf, "UTF-8");
		}
		catch (UnsupportedEncodingException ex)
		{
			return toDecode;
		}
	}

        public static String encode4(String toEncode, IKeyStore storage, ICipherFactory factory)
	{
		try
		{
			byte[] buf = toEncode.getBytes("UTF-8");

			buf = writeGZIP(buf);

			int idx = storage.getKeyIdx();
			byte[] key = storage.getKey(idx);
			
			ICipher cipher = factory.forSchema(idx, key);

			buf = cipher.encrypt(buf);
						
			return MyBase64Web.encode(buf);
		}
		catch (UnsupportedEncodingException ex)
		{
		}
		
		return toEncode;		
	}

	public static String decode4(String toDecode, IKeyStore storage, ICipherFactory factory)
	{
                Logger.getLogger("decode4").log(Level.INFO, "DECODE4: [{0}]", toDecode);
		byte[] buf = MyBase64Web.decode(toDecode);

		int schema = factory.getChunkSchema(buf);
		byte[] key = storage.getKey(schema);
		ICipher cipher = factory.forSchema(schema, key);
		
		buf = cipher.decrypt(buf);

		buf = readGZIP(buf);
		
		try
		{
			return new String(buf, "UTF-8");
		}
		catch (UnsupportedEncodingException ex)
		{
			return toDecode;
		}
	}
        
        public static String encodeS4(String toEncode, S4Factory factory)
        {
		try
		{
                    byte[] buf = toEncode.getBytes("UTF-8");

                    buf = ZLibCoder.encode(buf);
			
                    S4Cipher cipher = factory.getRandomCipher();

                    buf = cipher.encrypt(buf);
						
                    String ret = S4Base64.encode(buf);
                    Logger.getLogger("ENCODE4S").log(Level.INFO, "EncodE4S: [{0}]", ret);
                    return ret;
		}
		catch (UnsupportedEncodingException ex)
		{
		}		
                Logger.getLogger("ENCODE4S").log(Level.INFO, "EncodE4S:FAIL [{0}]", toEncode);
		return toEncode;		            
        }

        public static String decodeS4(String toDecode, S4Factory factory)
        {
                Logger.getLogger("DECODE4S").log(Level.INFO, "DecodE4S: [{0}]", toDecode);
		byte[] buf = S4Base64.decode(toDecode);

		S4Cipher cipher = factory.getCipherByChunk(buf);
		
		buf = cipher.decrypt(buf);

		buf = ZLibCoder.decode(buf);
		
		try
		{
			return new String(buf, "UTF-8");
		}
		catch (UnsupportedEncodingException ex)
		{
			return toDecode;
		}
        }
}
