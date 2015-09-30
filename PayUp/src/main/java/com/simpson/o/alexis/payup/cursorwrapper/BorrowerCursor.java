package com.simpson.o.alexis.payup.cursorwrapper;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

import com.simpson.o.alexis.payup.Borrower;
import com.simpson.o.alexis.payup.PayUpDataBaseHelper;

import java.util.Calendar;
import java.util.Date;

/**
 * A convenience class to wrap a cursor that returns rows from the "Borrowers" table.
 * The  getBorrower()} method will give you a Run instance representing the current row.
 */

public class BorrowerCursor extends CursorWrapper {
    /*
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public BorrowerCursor(Cursor cursor) {
        super(cursor);
    }

    /*
     * Returns a borrower object configured for the current row, or null if the current row is invalid.
     */

    public Borrower getBorrower() {
        if (isBeforeFirst() || isAfterLast())
            return null;
        Borrower borrower = new Borrower();
        borrower.setId(getLong(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_ID)));
        borrower.setLoanAmount(getFloat(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_AMOUNT_BORROWED)));
        borrower.setInterestRate(getFloat(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_INTEREST_RATE)));
        borrower.setName(getString(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_NAME)));
        borrower.setIsPaid((getInt(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_PAID)) != 0));
        borrower.setPhoneNumber(getString(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_PHONE_NUMBER)));
        borrower.setThumbnailUri(getString(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_THUMBNAIL_URI)));
        borrower.setContactUri(Uri.parse(getString(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_CONTACT_URI))));

        if (!getWrappedCursor().isNull((getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_AMOUNT_PAID)))) {
            borrower.setAmountPaid(getFloat(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_AMOUNT_PAID)));
        }
        if (!getWrappedCursor().isNull((getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_LOAN_DURATION)))) {
            borrower.setLoanDuration(getInt(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_LOAN_DURATION)));
        }

        borrower.setDateBorrowed(getDateBorrowed());
        borrower.setDateDue(getDateDue());


        return borrower;
    }

    public Calendar getDateBorrowed() {
        Calendar calendar = Calendar.getInstance();
        Date dateBorrowed = new Date(getLong(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_DATE_BORROWED)));
        calendar.setTime(dateBorrowed);
        return calendar;
    }

    public Calendar getDateDue() {
        Calendar calendar2 = Calendar.getInstance();
        Date dateDue = new Date(getLong(getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_DATE_DUE)));
        calendar2.setTime(dateDue);
        return calendar2;
    }
}
