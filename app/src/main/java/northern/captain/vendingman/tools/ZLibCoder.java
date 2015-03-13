package northern.captain.vendingman.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZLibCoder
{
	public static byte[] encode(byte[] src)
	{
		Deflater deflater = new Deflater();
		 
		/*
		* Set the input of compressor using,
		*
		* setInput(byte[] b)
		* method of Deflater class.
		*/
		 
		deflater.setInput(src);
		 
		/*
		* We are done with the input, so say finish using
		*
		* void finish()
		* method of Deflater class.
		*
		* It ends the compression with the current contents of
		* the input.
		*/
		 
		deflater.finish();
		 
		/*
		* At this point, we are done with the input.
		* Now we will have to create another byte array which can
		* hold the compressed bytes.
		*/
		 
		ByteArrayOutputStream bos = new ByteArrayOutputStream(src.length);
		 
		byte[] buffer = new byte[1024];
		 
		
		buffer[0] = (byte)(src.length & 0x7f);
		buffer[1] = (byte)((src.length >> 7) & 0x7f);
		buffer[2] = (byte)((src.length >> 14) & 0x7f);
		
		bos.write(buffer,0,3);
		/*
		* Use
		*
		* boolean finished()
		* method of Deflater class to determine whether
		* end of compressed data output stream reached.
		*
		*/
		while(!deflater.finished())
		{
		/*
		* use
		* int deflate(byte[] buffer)
		* method to fill the buffer with the compressed data.
		*
		* This method returns actual number of bytes compressed.
		*/
		 
			int bytesCompressed = deflater.deflate(buffer);
			bos.write(buffer,0,bytesCompressed);
		}
		 
		try
		{
		//close the output stream
			bos.close();
		}
		catch(IOException ioe)
		{
			System.out.println("Error while closing the stream : " + ioe);
		}
		 
		//get the compressed byte array from output stream
		byte[] compressedArray = bos.toByteArray();
		return compressedArray;
	}
	
	public static byte[] decode(byte[] src)
	{
		int olen = 0;
		
		olen |= src[2];
		olen <<=7;
		olen |= src[1];
		olen <<=7;
		olen |= src[0];
				
		Inflater inflater = new Inflater();
		
		inflater.setInput(src, 3, src.length - 3);
		
		byte[] outbuf = new byte[olen];
		
		int ret = 0;
		try
		{
			ret = inflater.inflate(outbuf);
		}
		catch(DataFormatException ex)
		{
			
		}
		inflater.end();
		
		return outbuf;
	}
}
