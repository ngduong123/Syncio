package online.syncio.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeHelper {

    private TimeHelper() {
    }

    public static String getCurrentDateTime() {
        Date currentTime = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(currentTime);
    }
}
