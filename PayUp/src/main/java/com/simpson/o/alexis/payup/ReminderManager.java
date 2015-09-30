package com.simpson.o.alexis.payup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

/**
 * Created by Alexis on 6/27/2014.
 */
public class ReminderManager {
    private static final int REMINDER_INTERVAL = 1000 * 60*1;
    private Context mContext;
    private AlarmManager mAlarmManager;
    public ReminderManager(Context context) {
        mContext = context;
        mAlarmManager =
                (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }
    public void setReminder(Long taskId, Calendar when, String type) {

        if (type == "Borrower") {
            Intent i = new Intent(mContext, BorrowerOnAlarmReceiver.class);
           // i.putExtra("TAG", type);
            i.putExtra(BorrowersDetailFragment.EXTRA_BORROWER_ID, (long) taskId);
            PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(),
                        pi);
            }else {
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(),
                        pi);
            }

        }else if (type == "Lender"){
            Intent i = new Intent(mContext, LenderOnAlarmReceiver.class);
           // i.putExtra("TAG", type);
            i.putExtra(LendersDetailFragment.EXTRA_LENDER_DETAIL_ID, (long) taskId);
            PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(),
                        pi);
            }else {
                mAlarmManager.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(),
                        pi);
            }


        }
    }
}
