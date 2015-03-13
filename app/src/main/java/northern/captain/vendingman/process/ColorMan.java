package northern.captain.vendingman.process;

import northern.captain.vendingman.AndroidContext;
import northern.captain.vendingman.R;

/**
 * Created by leo on 08.12.14.
 */
public class ColorMan
{
    public static ColorMan instance;

    public static void initialize()
    {
        instance = new ColorMan();
        instance.init(R.array.color_spectrum_arr);
    }

    int[] spectrum;

    public void init(int id)
    {
        spectrum = AndroidContext.mainActivity.getResources().getIntArray(id);
    }

    public int[] getColors(int howMany)
    {
        int[] ret = new int[howMany];

        for(int i=0, j=0; i<howMany;i++)
        {
            ret[i] = spectrum[j++];
            if(j >= spectrum.length)
            {
                j = 0;
            }
        }

        return ret;
    }
}
