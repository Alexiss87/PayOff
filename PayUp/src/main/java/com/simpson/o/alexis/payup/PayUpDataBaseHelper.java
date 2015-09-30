package com.simpson.o.alexis.payup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.dropbox.sync.android.DbxDatastoreStatus;
import com.simpson.o.alexis.payup.enums.SortOrder;
import com.simpson.o.alexis.payup.cursorwrapper.BorrowerCursor;
import com.simpson.o.alexis.payup.cursorwrapper.LenderCursor;
import com.simpson.o.alexis.payup.storage.AppStorage;
import com.simpson.o.alexis.payup.storage.AppStorageListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alexis on 6/14/2014.
 */
public class PayUpDataBaseHelper extends SQLiteOpenHelper implements AppStorage{

    private static final String TAG = PayUpDataBaseHelper.class.getSimpleName();
    // listeners
    private final List<AppStorageListener> storageListeners = new LinkedList<AppStorageListener>();



    private static final String DB_NAME = "payup.sqlite";
    private static final int VERSION = 2;

    public static final String TABLE_B0RROWERS = "borrowers_table";
    public static final String COLUMN_BORROWER_ID = "_id";
    public static final String COLUMN_BORROWER_NAME = "borrower_name";
    public static final String COLUMN_BORROWER_AMOUNT_BORROWED = "amount_borrowed";
    public static final String COLUMN_BORROWER_AMOUNT_PAID = "amount_paid";
    public static final String COLUMN_BORROWER_INTEREST_RATE = "interest_rate";
    public static final String COLUMN_BORROWER_LOAN_DURATION = "number_of_periods";
    public static final String COLUMN_BORROWER_PAID = "borrower_paid";
    public static final String COLUMN_BORROWER_DATE_BORROWED = "date_borrowed";
    public static final String COLUMN_BORROWER_DATE_DUE = "date_due";
    public static final String COLUMN_BORROWER_PHONE_NUMBER = "borrower_phone_number";
    public static final String COLUMN_BORROWER_CONTACT_URI = "borrower_contact_uri";
    public static final String COLUMN_BORROWER_THUMBNAIL_URI = "borrower_thumbnail_uri";

    public static final String TABLE_lENDERS = "lenders";
    public static final String COLUMN_LENDER_ID = "_id";
    public static final String COLUMN_LENDER_NAME = "lender_name";
    public static final String COLUMN_lENDER_LOAN_AMOUNT = "amount_loaned";
    public static final String COLUMN_LENDER_AMOUNT_PAID = "amount_paid";
    public static final String COLUMN_lENDER_INTEREST_RATE = "interest_rate";
    public static final String COLUMN_LENDER_LOAN_DURATION = "number_of_periods";
    public static final String COLUMN_LENDER_PAID = "lender_paid";
    public static final String COLUMN_LENDER_DATE_LOANED = "date_loaned";
    public static final String COLUMN_LENDER_DATE_DUE = "date_due";
    public static final String COLUMN_LENDER_PHONE_NUMBER = "lender_phone_number";
    public static final String COLUMN_LENDER_CONTACT_URI = "lender_contact_uri";
    public static final String COLUMN_LENDER_THUMBNAIL_URI = "lender_thumbnail_uri";

    private static final String CREATE_DATABASE_TABLE_BORROWERS =
            "create table " + TABLE_B0RROWERS + " ("
                    + COLUMN_BORROWER_ID + " integer primary key autoincrement, "
                    + COLUMN_BORROWER_NAME + " text , "
                    + COLUMN_BORROWER_INTEREST_RATE + " real , "
                    + COLUMN_BORROWER_LOAN_DURATION + " int , "
                    + COLUMN_BORROWER_PAID + " numeric , "
                    + COLUMN_BORROWER_DATE_BORROWED + " integer , "
                    + COLUMN_BORROWER_DATE_DUE + " integer , "
                    + COLUMN_BORROWER_AMOUNT_BORROWED + " real , "
                    + COLUMN_BORROWER_AMOUNT_PAID + " real , "
                    + COLUMN_BORROWER_PHONE_NUMBER + " text , "
                    + COLUMN_BORROWER_CONTACT_URI + " text , "
                    + COLUMN_BORROWER_THUMBNAIL_URI + " text);";

    private static final String CREATE_DATABASE_TABLE_LENDERS =
            "create table " + TABLE_lENDERS + " ("
                    + COLUMN_LENDER_ID + " integer primary key autoincrement, "
                    + COLUMN_LENDER_NAME + " text , "
                    + COLUMN_lENDER_INTEREST_RATE + " real , "
                    + COLUMN_LENDER_LOAN_DURATION + " int , "
                    + COLUMN_LENDER_PAID + " numeric , "
                    + COLUMN_LENDER_DATE_LOANED + " numeric , "
                    + COLUMN_LENDER_DATE_DUE + " numeric , "
                    + COLUMN_lENDER_LOAN_AMOUNT + " real , "
                    + COLUMN_LENDER_AMOUNT_PAID + " real , "
                    + COLUMN_LENDER_PHONE_NUMBER + " text ,"
                    + COLUMN_LENDER_CONTACT_URI + " text , "
                    + COLUMN_LENDER_THUMBNAIL_URI + " text);";

    private static String borrowerSortString = COLUMN_BORROWER_DATE_DUE + " asc";
    private static String lenderSortString = COLUMN_LENDER_DATE_DUE + " asc";


    public PayUpDataBaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // create the "borrowers" table
        db.execSQL(CREATE_DATABASE_TABLE_BORROWERS);
        // create the "Lenders" table
        db.execSQL(CREATE_DATABASE_TABLE_LENDERS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //database.execSQL("ALTER TABLE " + your_table_name + " ADD COLUMN " + new_col_name + " int");
        if (newVersion > oldVersion) {
            db.execSQL("ALTER TABLE " + TABLE_B0RROWERS + " ADD COLUMN " + COLUMN_BORROWER_AMOUNT_PAID + " real");
            db.execSQL("ALTER TABLE " + TABLE_B0RROWERS + " ADD COLUMN " + COLUMN_BORROWER_LOAN_DURATION + " int");
            db.execSQL("ALTER TABLE " + TABLE_lENDERS + " ADD COLUMN " + COLUMN_LENDER_AMOUNT_PAID + " real");
            db.execSQL("ALTER TABLE " + TABLE_lENDERS + " ADD COLUMN " + COLUMN_LENDER_LOAN_DURATION + " int");
        }
        /*db.execSQL("DROP TABLE IF EXISTS " + TABLE_B0RROWERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_lENDERS);
        // create new tables
        onCreate(db);*/

    }

    public long insertBorrower(Borrower borrower) {

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_BORROWER_NAME, borrower.getName());
        cv.put(COLUMN_BORROWER_AMOUNT_BORROWED, borrower.getLoanAmount());
        cv.put(COLUMN_BORROWER_AMOUNT_PAID, borrower.getAmountPaid());
        cv.put(COLUMN_BORROWER_INTEREST_RATE, borrower.getInterestRate());
        cv.put(COLUMN_BORROWER_LOAN_DURATION, borrower.getLoanDuration());
        cv.put(COLUMN_BORROWER_PAID, borrower.paid());
        cv.put(COLUMN_BORROWER_DATE_BORROWED, borrower.getDateBorrowed().getTimeInMillis());
        cv.put(COLUMN_BORROWER_DATE_DUE, borrower.getDateDue().getTimeInMillis());
        cv.put(COLUMN_BORROWER_PHONE_NUMBER, borrower.getPhoneNumber());
        if (borrower.getContactUri() != null) {
            cv.put(COLUMN_BORROWER_CONTACT_URI, borrower.getContactUri().toString());
        } else {
            cv.put(COLUMN_BORROWER_CONTACT_URI, "Uri null");
        }
        cv.put(COLUMN_BORROWER_THUMBNAIL_URI, borrower.getThumbnailUri());


        return getWritableDatabase().insert(TABLE_B0RROWERS, null, cv);
    }

    public long insertLender(Lender lender) {

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LENDER_NAME, lender.getName());
        cv.put(COLUMN_lENDER_LOAN_AMOUNT, lender.getLoanAmount());
        cv.put(COLUMN_LENDER_AMOUNT_PAID, lender.getAmountPaid());
        cv.put(COLUMN_lENDER_INTEREST_RATE, lender.getInterestRate());
        cv.put(COLUMN_LENDER_LOAN_DURATION, lender.getLoanDuration());
        cv.put(COLUMN_LENDER_PAID, lender.paid());
        cv.put(COLUMN_LENDER_DATE_LOANED, lender.getDateLoaned().getTimeInMillis());
        cv.put(COLUMN_LENDER_DATE_DUE, lender.getDateDue().getTimeInMillis());
        cv.put(COLUMN_LENDER_PHONE_NUMBER, lender.getPhoneNumber());

        if (lender.getContactUri() != null) {
            cv.put(COLUMN_LENDER_CONTACT_URI, lender.getContactUri().toString());
        } else {
            cv.put(COLUMN_LENDER_CONTACT_URI, "Uri null");
        }
        cv.put(COLUMN_LENDER_THUMBNAIL_URI, lender.getThumbnailUri());

        return getWritableDatabase().insert(TABLE_lENDERS, null, cv);

    }

    public BorrowerCursor queryBorrowers() {
        // equivalent to "select * from run order by date due asc"
        Cursor wrapped = getReadableDatabase().query(TABLE_B0RROWERS,
                null, null, null, null, null, borrowerSortString);
        //db.close();
        return new BorrowerCursor(wrapped);
    }

    @Override
    public List<Borrower> getAllBorrowers() {
        List<Borrower> borrowersList = new ArrayList<Borrower>();

        Cursor cursor = getReadableDatabase().query(TABLE_B0RROWERS,
                null, null, null, null, null, borrowerSortString);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Borrower borrower = new Borrower();
                borrower.setId(cursor.getLong(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_ID)));
                borrower.setLoanAmount(cursor.getFloat(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_AMOUNT_BORROWED)));
                borrower.setInterestRate(cursor.getFloat(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_INTEREST_RATE)));
                borrower.setName(cursor.getString(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_NAME)));
                borrower.setIsPaid((cursor.getInt(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_PAID)) != 0));
                borrower.setPhoneNumber(cursor.getString(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_PHONE_NUMBER)));
                borrower.setThumbnailUri(cursor.getString(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_THUMBNAIL_URI)));
                borrower.setContactUri(Uri.parse(cursor.getString(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_CONTACT_URI))));

                if (!cursor.isNull((cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_AMOUNT_PAID)))) {
                    borrower.setAmountPaid(cursor.getFloat(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_AMOUNT_PAID)));
                }
                if (cursor.isNull((cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_LOAN_DURATION)))) {
                    borrower.setLoanDuration(cursor.getInt(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_LOAN_DURATION)));
                }

                borrower.setDateBorrowed(getDateBorrowed(cursor));
                borrower.setDateDue(getDateDue(cursor));

                borrowersList.add(borrower);

            } while (cursor.moveToNext());
        }


        // return contact list
        return borrowersList;
    }
    public Calendar getDateBorrowed(Cursor cursor) {
        Calendar calendar = Calendar.getInstance();
        Date dateBorrowed = new Date(cursor.getLong(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_DATE_BORROWED)));
        calendar.setTime(dateBorrowed);
        return calendar;
    }

    public Calendar getDateDue(Cursor cursor) {
        Calendar calendar2 = Calendar.getInstance();
        Date dateDue = new Date(cursor.getLong(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_BORROWER_DATE_DUE)));
        calendar2.setTime(dateDue);
        return calendar2;
    }

    public BorrowerCursor queryBorrowers(String query) {
        // equivalent to "select * from run order by date due asc"
        Cursor wrapped = getReadableDatabase().query(TABLE_B0RROWERS,
                null, // return all coumns
                COLUMN_BORROWER_NAME + " LIKE ?", //look for rows with query name
                new String[]{query + "%"},//with this value
                null,
                null,
                borrowerSortString);//sort by due date
        //db.close();
        return new BorrowerCursor(wrapped);
    }

    public BorrowerCursor queryBorrower(long id) {

        Cursor wrapped = getReadableDatabase().query(TABLE_B0RROWERS,
                null, // all columns
                COLUMN_BORROWER_ID + " = ?", // look for a ID
                new String[]{String.valueOf(id)}, // with this value
                null, // group by
                null, // order by
                null, // having
                "1"); // limit 1 row

        return new BorrowerCursor(wrapped);
    }

    public LenderCursor queryLenders() {
        // equivalent to "select * from run order by DATE Due asc"
        Cursor wrapped = getReadableDatabase().query(TABLE_lENDERS,
                null, null, null, null, null, lenderSortString);

        return new LenderCursor(wrapped);
    }

    @Override
    public List<Lender> getAllLenders() {
        List<Lender> lenderList = new ArrayList<Lender>();
        Cursor cursor = getReadableDatabase().query(TABLE_lENDERS,
                null, null, null, null, null, lenderSortString);
        if (cursor.moveToFirst()) {
            do {
                Lender lender = new Lender();
                lender.setId(cursor.getLong(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_ID)));
                lender.setLoanAmount(cursor.getFloat(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_lENDER_LOAN_AMOUNT)));
                lender.setInterestRate(cursor.getFloat(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_lENDER_INTEREST_RATE)));
                lender.setName(cursor.getString(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_NAME)));
                lender.setIsPaid((cursor.getInt(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_PAID)) != 0));
                lender.setPhoneNumber(cursor.getString(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_PHONE_NUMBER)));
                lender.setThumbnailUri(cursor.getString(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_THUMBNAIL_URI)));
                lender.setContactUri(Uri.parse(cursor.getString(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_CONTACT_URI))));

                if (cursor.isNull((cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_AMOUNT_PAID)))) {
                    lender.setAmountPaid(cursor.getFloat(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_AMOUNT_PAID)));
                }

                if (cursor.isNull((cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_LOAN_DURATION)))) {
                    lender.setLoanDuration(cursor.getInt(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_LOAN_DURATION)));
                }

                lender.setDateLoaned(getDateLoaned(cursor));
                lender.setDateDue(getLenderDateDue(cursor));
                lenderList.add(lender);
            }while (cursor.moveToNext());

        }
        return lenderList;
    }
    public Calendar getDateLoaned(Cursor cursor) {
        Calendar calendar = Calendar.getInstance();
        Date dateBorrowed = new Date(cursor.getLong(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_DATE_LOANED)));
        calendar.setTime(dateBorrowed);
        return calendar;
    }

    public Calendar getLenderDateDue(Cursor cursor) {
        Calendar calendar2 = Calendar.getInstance();
        Date dateDue = new Date(cursor.getLong(cursor.getColumnIndex(PayUpDataBaseHelper.COLUMN_LENDER_DATE_DUE)));
        calendar2.setTime(dateDue);
        return calendar2;
    }

    public LenderCursor queryLenders(String query) {
        // equivalent to "select * from run order by DATE Due asc"

        Cursor wrapped = getReadableDatabase().query(TABLE_lENDERS,
                null, // return all coumns
                COLUMN_LENDER_NAME + " LIKE ?", //look for rows with query name
                new String[]{query + "%"},//with this value
                null,
                null,
                lenderSortString);//sort by due date

        return new LenderCursor(wrapped);
    }

    public LenderCursor queryLender(long id) {

        Cursor wrapped = getReadableDatabase().query(TABLE_lENDERS,
                null, // all columns
                COLUMN_LENDER_ID + " = ?", // look for a run ID
                new String[]{String.valueOf(id)}, // with this value
                null, // group by
                null, // order by
                null, // having
                "1"); // limit 1 row

        return new LenderCursor(wrapped);
    }


    public int updateBorrower(Borrower borrower) {

        ContentValues args = new ContentValues();
        args.put(COLUMN_BORROWER_NAME, borrower.getName());
        args.put(COLUMN_BORROWER_PHONE_NUMBER, borrower.getPhoneNumber());
        args.put(COLUMN_BORROWER_AMOUNT_BORROWED, borrower.getLoanAmount());
        args.put(COLUMN_BORROWER_AMOUNT_PAID, borrower.getAmountPaid());
        args.put(COLUMN_BORROWER_DATE_BORROWED, borrower.getDateBorrowed().getTimeInMillis());
        args.put(COLUMN_BORROWER_DATE_DUE, borrower.getDateDue().getTimeInMillis());
        args.put(COLUMN_BORROWER_INTEREST_RATE, borrower.getInterestRate());
        args.put(COLUMN_BORROWER_LOAN_DURATION, borrower.getLoanDuration());
        args.put(COLUMN_BORROWER_PAID, borrower.paid() == true ? 1 : 0);
        if (borrower.getContactUri() != null) {
            args.put(COLUMN_BORROWER_CONTACT_URI, borrower.getContactUri().toString());
        } else {
            args.put(COLUMN_BORROWER_CONTACT_URI, "Uri null");
        }
        args.put(COLUMN_BORROWER_THUMBNAIL_URI, borrower.getThumbnailUri());

        return getWritableDatabase().update(TABLE_B0RROWERS,
                args,
                COLUMN_BORROWER_ID + "=" + borrower.getId(),
                null);
        // new String[]{String.valueOf(borrower.getId())});
    }

    public int updateLender(Lender lender) {

        ContentValues args = new ContentValues();
        args.put(COLUMN_LENDER_NAME, lender.getName());
        args.put(COLUMN_LENDER_PHONE_NUMBER, lender.getPhoneNumber());
        args.put(COLUMN_lENDER_LOAN_AMOUNT, lender.getLoanAmount());
        args.put(COLUMN_LENDER_AMOUNT_PAID, lender.getAmountPaid());
        args.put(COLUMN_LENDER_DATE_LOANED, lender.getDateLoaned().getTimeInMillis());
        args.put(COLUMN_LENDER_DATE_DUE, lender.getDateDue().getTimeInMillis());
        args.put(COLUMN_lENDER_INTEREST_RATE, lender.getInterestRate());
        args.put(COLUMN_LENDER_LOAN_DURATION, lender.getLoanDuration());
        args.put(COLUMN_LENDER_PAID, lender.paid() == true ? 1 : 0);
        if (lender.getContactUri() != null) {
            args.put(COLUMN_LENDER_CONTACT_URI, lender.getContactUri().toString());
        } else {
            args.put(COLUMN_LENDER_CONTACT_URI, "Uri null");
        }
        args.put(COLUMN_LENDER_THUMBNAIL_URI, lender.getThumbnailUri());

        return getWritableDatabase().update(TABLE_lENDERS,
                args,
                COLUMN_LENDER_ID + "=" + lender.getId(),
                null);
        // new String[]{String.valueOf(lender.getId())});
    }

    public boolean deleteBorrower(long rowId) {


        boolean bool = getWritableDatabase().delete(TABLE_B0RROWERS,
                COLUMN_BORROWER_ID
                        + "=" + rowId, null
        ) > 0;

        return bool;
    }

    public boolean deleteLender(long rowId) {


        boolean bool = getWritableDatabase().delete(TABLE_lENDERS,
                COLUMN_LENDER_ID
                        + "=" + rowId, null
        ) > 0;

        return bool;
    }

    // Listeners

    private void notifyListeners() {
        PayUpApplication.executeInBackground(new Runnable() {
            @Override
            public void run() {
                synchronized (storageListeners) {
                    for (AppStorageListener listener : storageListeners) {
                        //listener.onContentChanged();
                    }
                }
            }
        });
    }

    @Override
    public boolean addStorageListener(AppStorageListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        synchronized (storageListeners) {
            return storageListeners.add(listener);
        }
    }

    @Override
    public boolean removeStorageListener(AppStorageListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        synchronized (storageListeners) {
            return storageListeners.remove(listener);
        }
    }

    @Override
    public List<AppStorageListener> detachAllListeners() {
        synchronized (storageListeners){
            final List<AppStorageListener> listeners =
                    new LinkedList<AppStorageListener>(storageListeners);
            storageListeners.clear();
            return listeners;
        }
    }

    @Override
    public void attachListeners(List<AppStorageListener> listeners) {
        synchronized (storageListeners) {
            storageListeners.addAll(listeners);
        }
        notifyListeners();
    }

    @Override
    public void sync() {

        //do nothing
    }

    @Override
    public void clear() {

        //delete all data
       getWritableDatabase().delete(TABLE_B0RROWERS, null, null ) ;
       getWritableDatabase().delete(TABLE_lENDERS,null, null) ;
    }

    @Override
    public void setSortOrder(SortOrder sortOrder) {

        switch (sortOrder){
            case name:
                borrowerSortString = COLUMN_BORROWER_NAME + " asc";
                lenderSortString = COLUMN_LENDER_NAME + " asc";
                break;
            case dateDue:
               borrowerSortString = COLUMN_BORROWER_DATE_DUE + " asc";
               lenderSortString = COLUMN_LENDER_DATE_DUE + " asc";
               break;
            case dateCreated:
                borrowerSortString =COLUMN_BORROWER_DATE_BORROWED + " asc";
                lenderSortString = COLUMN_LENDER_DATE_LOANED + " asc";
                break;
            default:
                borrowerSortString = COLUMN_BORROWER_DATE_DUE + " asc";
                lenderSortString = COLUMN_LENDER_NAME + " asc";
        }

    }

    @Override
    public DbxDatastoreStatus getSyncStatus() {
        return null;
    }


}
