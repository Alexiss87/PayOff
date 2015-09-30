package com.simpson.o.alexis.payup;

import android.app.Application;
import android.content.Context;

import com.simpson.o.alexis.payup.Utils.AppLog;
import com.simpson.o.alexis.payup.dropbox.DropboxHelper;
import com.simpson.o.alexis.payup.storage.Storage;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by alexi_000 on 22/11/2014.
 */
public class PayUpApplication extends Application {

    private static final String TAG = PayUpApplication.class.getSimpleName();
    private static Context context;

    private static final int NON_CORE_THREADS_KEEP_ALIVE_TIME_SECONDS = 1;
    private static ThreadPoolExecutor executor;

    @Override
    public void onCreate() {
        super.onCreate();
        AppLog.d(TAG, "onCreate() call");

        context = getApplicationContext();
        initThreadPool();
      //  EventTracker.setEnabled(false);
        initStorage();
    }

    private void initStorage() {
        Storage.init(null);
        DropboxHelper.initSynchronization();
    }

    private void initThreadPool() {
        final int processors = Runtime.getRuntime().availableProcessors();
        AppLog.d(TAG, "Detected " + processors + " processors. Creating thread pool...");
        executor = new ThreadPoolExecutor(processors,
                processors,
                NON_CORE_THREADS_KEEP_ALIVE_TIME_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

    }

    @Override
    public void onTerminate() {
        AppLog.d(TAG, "onTerminate() call");
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
        super.onTerminate();
    }


    public static Context getContext() {
        return context;
    }

    public static void executeInBackground(Runnable task) {
        executor.execute(task);
     }
}
