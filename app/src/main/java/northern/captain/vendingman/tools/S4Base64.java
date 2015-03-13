package northern.captain.vendingman.tools;

import java.util.Random;

public class S4Base64
{

    protected static final char[] ALPHABET = "eajb12zR59h3-EVZrYxdoFU_8OLNkIQs6cSvG7i0mCPTypfDJXuKHgWlnwtMAq4B".toCharArray();

    protected static int[]  toInt   = new int[128];

    static {
        for(int i=0; i< ALPHABET.length; i++){
            toInt[ALPHABET[i]]= i;
        }
    }

    static Random rnd = new Random();
    
    /**
     * Translates the specified byte array into Base64 string.
     *
     * @param buf the byte array (not null)
     * @return the translated Base64 string (not null)
     */
    public static String encode(byte[] buf){
        int size = buf.length;
        char[] ar = new char[((size + 2) / 3) * 4 +1];
        int rv = rnd.nextInt(64);        
        ar[0] = ALPHABET[rv];
        int a = 1;
        int i=0;
        while(i < size){
            byte b0 = (byte)(buf[i++] ^ rv);
            byte b1 = (i < size) ? (byte)(buf[i++] ^ rv) : 0;
            byte b2 = (i < size) ? (byte)(buf[i++] ^ rv) : 0;

            int mask = 0x3F;
            ar[a++] = ALPHABET[(b0 >> 2) & mask];
            ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
            ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
            ar[a++] = ALPHABET[b2 & mask];
        }
        switch(size % 3){
            case 1: ar[--a]  = '=';
            case 2: ar[--a]  = '=';
        }
        return new String(ar);
    }

    /**
     * Translates the specified Base64 string into a byte array.
     *
     * @param s the Base64 string (not null)
     * @return the byte array (not null)
     */
    public static byte[] decode(String s){
        int delta = s.endsWith( "==" ) ? 2 : s.endsWith( "=" ) ? 1 : 0;
        int len = s.length() -1;
        byte[] buffer = new byte[len*3/4 - delta];
        int mask = 0xFF;
        int index = 0;
        int rv = toInt[s.charAt(0)];
        for(int i=1; i<= len; i+=4){
            int c0 = toInt[s.charAt( i + 0 )];
            int c1 = toInt[s.charAt( i + 1)];
            buffer[index++]= (byte)((((c0 << 2) | (c1 >> 4)) ^ rv) & mask);
            if(index >= buffer.length){
                return buffer;
            }
            int c2 = toInt[s.charAt( i + 2)];
            buffer[index++]= (byte)((((c1 << 4) | (c2 >> 2)) ^ rv) & mask);
            if(index >= buffer.length){
                return buffer;
            }
            int c3 = toInt[s.charAt( i + 3 )];
            buffer[index++]= (byte)((((c2 << 6) | c3) ^ rv) & mask);
        }
        return buffer;
    } 	
}
