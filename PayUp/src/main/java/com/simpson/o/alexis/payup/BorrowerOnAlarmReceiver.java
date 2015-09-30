package com.simpson.o.alexis.payup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by Alexis on 6/27/2014.
 */
public class BorrowerOnAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

       // String type = intent.getStringExtra("TAG");
          // Toast.makeText(context, "Borrower onRecieved called", Toast.LENGTH_SHORT).show();
            long rowid =
                    intent.getExtras().getLong(BorrowersDetailFragment.EXTRA_BORROWER_ID);
            WakeReminderIntentService.acquireStaticLock(context);
            //Toast.makeText(context, "wake lock aquired", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(context, BorrowerReminderService.class);
           // i.putExtra("TAG", type);
            i.putExtra(BorrowersDetailFragment.EXTRA_BORROWER_ID, rowid);
            context.startService(i);
           // Toast.makeText(context, "Borrower service recieved and started", Toast.LENGTH_SHORT).show();

    }
}
