package comp5216.sydney.edu.au.mediaacces;

import android.icu.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

public class CheckTime {
    public static boolean checkBackupTime(int backupHour) {
        int hour = Integer.parseInt(new SimpleDateFormat("HH",
                Locale.getDefault()).format(new Date()));
        return hour == backupHour;
    }

    public static boolean checkResetTime(int resetHour) {
        int hour = Integer.parseInt(new SimpleDateFormat("HH",
                Locale.getDefault()).format(new Date()));
        return hour != resetHour;
    }
}
