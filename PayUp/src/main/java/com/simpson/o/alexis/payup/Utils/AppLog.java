package com.simpson.o.alexis.payup.Utils;

import android.util.Log;

import com.simpson.o.alexis.payup.BuildConfig;

/**
 * Created by alexi_000 on 22/11/2014.
 */
public class AppLog {

    private static final boolean ENABLED = BuildConfig.DEBUG;

    private AppLog() {
        throw new AssertionError("Instance creation not allowed!");
    }

    public static void d(String tag, String msg) {
        if (ENABLED) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (ENABLED) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        e(tag, msg, null);
    }

    public static void e(String tag, String msg, Throwable e) {
        if (ENABLED) {
            Log.e(tag, msg, e);
        }
    }

    public static void wtf(String tag, String msg, Throwable e) {
        if (ENABLED) {
            Log.wtf(tag, msg, e);
        }
    }

    public static void i(String tag, String msg) {
        if (ENABLED) {
            Log.i(tag, msg);
        }
    }
}
