package com.simpson.o.alexis.payup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.simpson.o.alexis.payup.cursorwrapper.BorrowerCursor;
import com.simpson.o.alexis.payup.cursorwrapper.LenderCursor;

import java.util.Calendar;


/**
 * Created by Alexis on 6/27/2014.
 */
public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ReminderManager reminderMgr = new ReminderManager(context);

        BorrowerCursor borrowerCursor =
                LoanOffice.getInstance(context).getBorrowers();

        LenderCursor lenderCursor = LoanOffice.getInstance(context).getLenders();


        if(borrowerCursor != null) {
            borrowerCursor.moveToFirst();


            while(borrowerCursor.isAfterLast() == false) {
                Borrower b = borrowerCursor.getBorrower();

                if (b.paid()==false && hasSetTimePassed(b.getDateDue())) {
                    /*Log.d("OnBootReceiver", "Adding alarm for borrower from boot.");
                    Log.d("OnBootReceiver", "Row Id Column Index - " + b.getId());*/
                    Long rowId = b.getId();
                    reminderMgr.setReminder(rowId, b.getDateDue(), "Borrower");
                }
                borrowerCursor.moveToNext();
            }

        }
        borrowerCursor.close();

        if(lenderCursor != null) {
            lenderCursor.moveToFirst();

        while(lenderCursor.isAfterLast() == false) {
            Lender l = lenderCursor.getLender();

            if (l.paid()== false && !hasSetTimePassed(l.getDateDue())) {
                /*Log.d("OnBootReceiver", "Adding alarm for lender from boot.");
                Log.d("OnBootReceiver", "Row Id Column Index - "+ l.getId());*/
                Long rowId = l.getId();
                reminderMgr.setReminder(rowId, l.getDateDue(), "Lender");
            }
            lenderCursor.moveToNext();
        }

    }
    lenderCursor.close();


    }

    public boolean hasSetTimePassed(Calendar setDate) {
        long prevEventTime = System.currentTimeMillis();
        if (setDate == null ) {
            throw new IllegalArgumentException("The date must not be null");
        }

        if (prevEventTime >= setDate.getTimeInMillis()){
            return true;
        }else{
            return false;
        }

    }
}
