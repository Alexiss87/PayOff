package com.simpson.o.alexis.payup.cursorwrapper;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

import com.simpson.o.alexis.payup.Lender;
import com.simpson.o.alexis.payup.PayUpDataBaseHelper;

import java.util.Calendar;
import java.util.Date;

/**
* Created by alexi_000 on 22/11/2014.
*/
public class LenderCursor extends CursorWrapper {
    /*
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public LenderCursor(Cursor cursor) {
        super(cursor);
    }

    /*
     * Returns a Run object configured for the current row, or null if the current row is invalid.
     */

    public Lender getLender() {
        if (isBeforeFirst() || isAfterLast())
            return null;
        Lender lender = new Lender();
        lender.setId(getLong(getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_ID)));
        lender.setLoanAmount(getFloat(getColumnIndex(PayUpDataBaseHelper.COLUMN_lENDER_LOAN_AMOUNT)));
        lender.setInterestRate(getFloat(getColumnIndex(PayUpDataBaseHelper.COLUMN_lENDER_INTEREST_RATE)));
        lender.setName(getString(getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_NAME)));
        lender.setIsPaid((getInt(getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_PAID)) != 0));
        lender.setPhoneNumber(getString(getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_PHONE_NUMBER)));
        lender.setThumbnailUri(getString(getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_THUMBNAIL_URI)));
        lender.setContactUri(Uri.parse(getString(getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_CONTACT_URI))));

        if (!getWrappedCursor().isNull((getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_AMOUNT_PAID)))) {
            lender.setAmountPaid(getFloat(getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_AMOUNT_PAID)));
        }

        if (!getWrappedCursor().isNull((getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_LOAN_DURATION)))) {
            lender.setLoanDuration(getInt(getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_LOAN_DURATION)));
        }

        lender.setDateLoaned(getDateLoaned());
        lender.setDateDue(getDateDue());

        return lender;
    }

    public Calendar getDateLoaned() {
        Calendar calendar = Calendar.getInstance();
        Date dateBorrowed = new Date(getLong(getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_DATE_LOANED)));
        calendar.setTime(dateBorrowed);
        return calendar;
    }

    public Calendar getDateDue() {
        Calendar calendar2 = Calendar.getInstance();
        Date dateDue = new Date(getLong(getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_DATE_DUE)));
        calendar2.setTime(dateDue);
        return calendar2;
    }


}
