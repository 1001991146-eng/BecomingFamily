package com.example.becomingfamily;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConnectivityReceiver extends BroadcastReceiver
{
    // Interface to communicate network status back to the Activity/Fragment
    public static ConnectivityReceiverListener connectivityReceiverListener;
    public ConnectivityReceiver() {
        super();
    }
    /**
     * נקרא כאשר המערכת משדרת שינוי בקישוריות.
     */
    @Override
    public void onReceive(Context context, Intent arg1) {
        // שימוש במתודת העזר הסטטית בתוך ה-Activity המכיל
        boolean isConnected = WeeklyUpdateActivity.isNetworkAvailable(context);

        // עדכון המאזין (ה-Activity) באמצעות הממשק
        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
        }
    }
    /**
     * ממשק למאזינים (ה-Activity הראשי)
     */
    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}