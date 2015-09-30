package com.simpson.o.alexis.payup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Alexis on 7/16/2014.
 */
public class LenderOnAlarmReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {

        //String type = intent.getStringExtra("TAG");
        long rowid =
                intent.getExtras().getLong(LendersDetailFragment.EXTRA_LENDER_DETAIL_ID);
        WakeReminderIntentService.acquireStaticLock(context);
        Intent i = new Intent(context, LenderReminderService.class);
       // i.putExtra("TAG", type);
        i.putExtra(LendersDetailFragment.EXTRA_LENDER_DETAIL_ID, rowid);
        context.startService(i);
    }

 }
