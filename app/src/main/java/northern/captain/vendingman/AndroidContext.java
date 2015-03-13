package northern.captain.vendingman;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import northern.captain.vendingman.entities.User;

/**
 * Created by leo on 14.11.14.
 */
public class AndroidContext
{
    public static final int VERSION=100;

    public static DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
    public static DateFormat timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
    public static DateFormat monthFormat = new SimpleDateFormat("MMM");
    public static DateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS"); //2014-11-01 00:00:00.000000

    public static int colorMoneyPlus;
    public static int colorMoneyMinus;

    public static MainActivity mainActivity;

    public static void setMainActivity(MainActivity activity)
    {
        mainActivity = activity;
        colorMoneyMinus = activity.getResources().getColor(R.color.md_red_600);
        colorMoneyPlus = activity.getResources().getColor(R.color.md_green_600);
    }

    public static void clearMainRefs()
    {
//        mainActivity = null;
    }

    public static User ourUser;
}
