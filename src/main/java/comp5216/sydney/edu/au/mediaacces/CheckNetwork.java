package comp5216.sydney.edu.au.mediaacces;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckNetwork {
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context) {
        NetworkInfo info=CheckNetwork.getNetworkInfo(context);

        return (info!=null&&info.isConnected());
    }

    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info=CheckNetwork.getNetworkInfo(context);

        return (info!=null&&info.isConnected()&&info.getType()==ConnectivityManager.TYPE_WIFI);
    }
}
