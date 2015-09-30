package com.simpson.o.alexis.payup.dropbox;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.simpson.o.alexis.payup.PayUpApplication;
import com.simpson.o.alexis.payup.R;
import com.simpson.o.alexis.payup.Utils.AppLog;
import com.simpson.o.alexis.payup.storage.Storage;

/**
 * Created by alexi_000 on 22/11/2014.
 */
public class DropboxHelper {


    public static final int REQUEST_LINK_TO_DBX = 4;
    private static final String APP_KEY = "eb79z72xdjxzob7";
    private static final String APP_SECRET = "ar56lnvkg6qyt2t";

    private static DbxAccountManager accountManager = null;
    private static DbxAccount account = null;

    private static ConnectivityReceiver connectivityReceiver;


    // call from activity
    public static void tryLinkAccountFromActivity(Activity accountLinkActivity) {
        initAccountManagerIfNeeded(accountLinkActivity.getApplicationContext());

        if (accountManager.hasLinkedAccount()) {
            account = accountManager.getLinkedAccount();
            Toast.makeText(accountLinkActivity, R.string.action_dropbox_already_linked_toast, Toast.LENGTH_LONG).show();
        } else {
            accountManager.startLink(accountLinkActivity, REQUEST_LINK_TO_DBX);
          //  EventTracker.track(Event.DropboxLinkAttempt);
        }
    }

    // call from activity's onActivityResult()
    public static void onAccountLinkActivityResult(Activity accountLinkActivity, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
                account = accountManager.getLinkedAccount();
                Toast.makeText(accountLinkActivity, R.string.action_dropbox_link_succeded_toast, Toast.LENGTH_LONG).show();
               // EventTracker.track(Event.DropboxLinkSuccess);
            } else {
                Toast.makeText(accountLinkActivity, R.string.action_dropbox_link_failed_toast, Toast.LENGTH_LONG).show();
                //EventTracker.track(Event.DropboxLinkFail);
            }
        }
    }

    private static void initAccountManagerIfNeeded(Context context) {
        if (accountManager == null) {
            accountManager = DbxAccountManager.getInstance(context, APP_KEY, APP_SECRET);
        }
    }

    public static synchronized boolean hasLinkedAccount() {
        if (account != null) {
            return true;
        } else {
            if (PayUpApplication.getContext() == null){
                Log.e("DropBoxHelper", "Context is null");
            }
            initAccountManagerIfNeeded(PayUpApplication.getContext());
            return accountManager.hasLinkedAccount();
        }
    }

    public static synchronized DbxAccount getAccount() {
        if (account == null) {
            initAccountManagerIfNeeded(PayUpApplication.getContext());
            if (accountManager.hasLinkedAccount()) {
                account = accountManager.getLinkedAccount();
            }
        }
        return account;
    }

    public static void initSynchronization() {
        if (connectivityReceiver == null &&
                Storage.getCurrentStorageType() == Storage.Type.Dropbox &&
                hasLinkedAccount()) {

            connectivityReceiver = new ConnectivityReceiver();
            PayUpApplication.getContext().registerReceiver(connectivityReceiver,
                    new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    public static void disableSynchronization() {
        if (connectivityReceiver != null) {
            PayUpApplication.getContext().unregisterReceiver(connectivityReceiver);
            connectivityReceiver = null;
        }
    }


    /**
     * ***************** Inner classes *******************
     */

    private static final class ConnectivityReceiver extends BroadcastReceiver {

        private static final int AUTO_SYNC_INTERVAL_MILLIS = 1000;
        private static final String LAST_AUTO_SYNC_TIME = "last_auto_sync_time";

        private final SharedPreferences sharedPreferences;

        private ConnectivityReceiver() {
            sharedPreferences = PayUpApplication.getContext()
                    .getSharedPreferences(ConnectivityReceiver.class.getSimpleName(), Activity.MODE_PRIVATE);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork != null && activeNetwork.isConnected()) {
                if (isSyncIntervalExceeded()) {
                    Storage.getStorage().sync();
                    updateLastSyncTime();
                    //EventTracker.track(Event.DropboxSyncAuto);
                }
            }
        }

        private void updateLastSyncTime() {
            sharedPreferences.edit().putLong(LAST_AUTO_SYNC_TIME, System.currentTimeMillis()).apply();
        }

        private boolean isSyncIntervalExceeded() {
            return System.currentTimeMillis() - sharedPreferences.getLong(LAST_AUTO_SYNC_TIME, 0)
                    >= AUTO_SYNC_INTERVAL_MILLIS;
        }
    }
}
