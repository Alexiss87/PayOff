package com.simpson.o.alexis.payup.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.simpson.o.alexis.payup.PayUpApplication;

/**
 * Created by alexi_000 on 25/11/2014.
 */
public class ConnectivityUtils {

    private ConnectivityUtils() {
        throw new AssertionError("Instance creation not allowed!");
    }

    public static boolean isNetworkConnected() {
        final ConnectivityManager cm = (ConnectivityManager) PayUpApplication.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

}
