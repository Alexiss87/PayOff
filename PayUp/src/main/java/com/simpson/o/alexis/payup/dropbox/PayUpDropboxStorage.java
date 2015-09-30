package com.simpson.o.alexis.payup.dropbox;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxDatastoreStatus;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.simpson.o.alexis.payup.Borrower;
import com.simpson.o.alexis.payup.Lender;
import com.simpson.o.alexis.payup.PayUpApplication;
import com.simpson.o.alexis.payup.Utils.AppLog;
import com.simpson.o.alexis.payup.enums.SortOrder;
import com.simpson.o.alexis.payup.cursorwrapper.BorrowerCursor;
import com.simpson.o.alexis.payup.cursorwrapper.LenderCursor;
import com.simpson.o.alexis.payup.storage.AppStorage;
import com.simpson.o.alexis.payup.storage.AppStorageListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by alexi_000 on 22/11/2014.
 */
public class PayUpDropboxStorage implements AppStorage {

    private static final String TAG = PayUpDropboxStorage.class.getSimpleName();
    private static final String INVALID_ID = "";

    private static final DbxTable.ResolutionRule RESOLUTION_RULE = DbxTable.ResolutionRule.LOCAL;
    DbxDatastore datastore;

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
    private static final String[] BORROWER_FIELDS =
            {COLUMN_BORROWER_ID,COLUMN_BORROWER_NAME,COLUMN_BORROWER_AMOUNT_BORROWED,COLUMN_BORROWER_AMOUNT_PAID,
                    COLUMN_BORROWER_INTEREST_RATE,COLUMN_BORROWER_LOAN_DURATION,COLUMN_BORROWER_PAID,
            COLUMN_BORROWER_DATE_BORROWED,COLUMN_BORROWER_DATE_DUE,COLUMN_BORROWER_PHONE_NUMBER,
            COLUMN_BORROWER_CONTACT_URI,COLUMN_BORROWER_THUMBNAIL_URI};

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
    private static final String[] LENDERS_FIELDS =
            {COLUMN_LENDER_ID,COLUMN_LENDER_NAME,COLUMN_lENDER_LOAN_AMOUNT,COLUMN_LENDER_AMOUNT_PAID,
                    COLUMN_lENDER_INTEREST_RATE,COLUMN_LENDER_LOAN_DURATION,COLUMN_LENDER_PAID,
                    COLUMN_LENDER_DATE_LOANED,COLUMN_LENDER_DATE_DUE,COLUMN_LENDER_PHONE_NUMBER,
                    COLUMN_LENDER_CONTACT_URI,COLUMN_LENDER_THUMBNAIL_URI};

    private DbxTable borrowersTable;
    private DbxTable lendersTable;
    private static long lastBorrowerId;
    private static long lastLenderId;
    private DbxTable idTable;

    // cache
    private static final int CACHE_BORROWER = 1;
    private static final int CACHE_LENDERS = 2;

    // notes list cache
    private List<Borrower> notesListCache;
    private volatile Serializable notesListCacheLabelId = INVALID_ID;
    private volatile boolean notesListCacheActual = false;

    // note cache
    private Borrower borrowerCach;
    private volatile Serializable noteCacheNoteId = INVALID_ID;
    private volatile boolean noteCacheActual = false;

    // listeners
    private final List<AppStorageListener> storageListeners = new LinkedList<AppStorageListener>();

    //comparators
    private BorrowerComparator borrowerComparator = new BorrowerComparator();
    private LenderComparator lenderComparator = new LenderComparator();

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    private final String KEY_LAST_BORROWERD_ID = "last_borrowed_id";
    private static final String KEY_LAST_LENDER_ID = "last lender_id";


    public PayUpDropboxStorage() {
        try {
            datastore = DbxDatastore.openDefault(DropboxHelper.getAccount());
        } catch (DbxException e) {
            AppLog.e(TAG, "Error opening datastore", e);
            throw new RuntimeException("Error opening datastore");
        }

        syncDatastore();
        initTables();
    }

    private void syncDatastore() {
        try {
            datastore.sync();
        } catch (DbxException e) {
            AppLog.e(TAG, "syncDatastore()", e);
            throw new RuntimeException();
        }
    }
    @Override
    public void sync() {
        // Cache invalidation, listeners notification. Sync also called from here
        //onStorageContentChanged(CACHE_NOTE | CACHE_NOTES_LIST |CACHE_LABELS_LIST);
        try {
            datastore.sync();
        } catch (DbxException e) {
            e.printStackTrace();
            AppLog.e(TAG, "sync()", e);
        }
    }

    public void initTables() {
        borrowersTable = datastore.getTable(TABLE_B0RROWERS);
        lendersTable = datastore.getTable(TABLE_B0RROWERS);

        for (String field : BORROWER_FIELDS) {
            borrowersTable.setResolutionRule(field, RESOLUTION_RULE);
        }
        for (String field : LENDERS_FIELDS) {
            lendersTable.setResolutionRule(field, RESOLUTION_RULE);
        }
        pref =  PreferenceManager.getDefaultSharedPreferences(PayUpApplication.getContext());
        lastBorrowerId = pref.getLong(KEY_LAST_BORROWERD_ID,-1);
        lastLenderId = pref.getLong(KEY_LAST_LENDER_ID,-1);
    }


    @Override
    public long insertBorrower(Borrower borrower) {

        long _id = -1 ;
        if (borrower.getId() != -1){
            _id = borrower.getId();
            lastBorrowerId = borrower.getId();
            saveLastBorrowedID();
        }else{
            lastBorrowerId++;
            _id = lastBorrowerId;
            saveLastBorrowedID();
        }


         DbxRecord temp = borrowersTable.insert()
                .set(COLUMN_BORROWER_ID,_id).
        set(COLUMN_BORROWER_NAME, borrower.getName()).
        set(COLUMN_BORROWER_AMOUNT_BORROWED, borrower.getLoanAmount()).
        set(COLUMN_BORROWER_AMOUNT_PAID, borrower.getAmountPaid()).
        set(COLUMN_BORROWER_INTEREST_RATE, borrower.getInterestRate()).
        set(COLUMN_BORROWER_LOAN_DURATION, borrower.getLoanDuration()).
        set(COLUMN_BORROWER_PAID, borrower.paid()).
        set(COLUMN_BORROWER_DATE_BORROWED, borrower.getDateBorrowed().getTimeInMillis()).
        set(COLUMN_BORROWER_DATE_DUE, borrower.getDateDue().getTimeInMillis()).
        set(COLUMN_BORROWER_PHONE_NUMBER, borrower.getPhoneNumber()).
        set(COLUMN_BORROWER_CONTACT_URI, (borrower.getContactUri()!=null)?
                borrower.getContactUri().toString():"Uri null").
        set(COLUMN_BORROWER_THUMBNAIL_URI, borrower.getThumbnailUri());
        ///gonna have to get last id before insertin
      //  DbxRecord idRecord = idTable.insert().set("LAST_BORROWER_ID",lastBorrowerId);


       sync();
       // onStorageContentChanged(CACHE_NOTES_LIST);
        return temp.getLong(COLUMN_BORROWER_ID);

    }

    private void saveLastBorrowedID() {
        editor = pref.edit();
        editor.putLong(KEY_LAST_BORROWERD_ID,lastBorrowerId);
    }

    @Override
    public BorrowerCursor queryBorrowers() {

        try {
            DbxTable.QueryResult results = borrowersTable.query();
            MatrixCursor matrixCursor = new MatrixCursor(BORROWER_FIELDS);
            List<DbxRecord> listResults = results.asList();
            Collections.sort(listResults,borrowerComparator);
            for (DbxRecord dbxRecord : listResults) {
                //loop and create each matrixCursor
                matrixCursor.addRow(new Object[]{
                        dbxRecord.getLong(COLUMN_BORROWER_ID),
                        dbxRecord.getString(COLUMN_BORROWER_NAME),
                        dbxRecord.getDouble(COLUMN_BORROWER_AMOUNT_BORROWED),
                        dbxRecord.getDouble(COLUMN_BORROWER_AMOUNT_PAID),
                        dbxRecord.getDouble(COLUMN_BORROWER_INTEREST_RATE),
                        (int)dbxRecord.getLong(COLUMN_BORROWER_LOAN_DURATION),
                        dbxRecord.getBoolean(COLUMN_BORROWER_PAID),
                        dbxRecord.getLong(COLUMN_BORROWER_DATE_BORROWED),
                        dbxRecord.getLong(COLUMN_BORROWER_DATE_DUE),
                        dbxRecord.getString(COLUMN_BORROWER_PHONE_NUMBER),
                        dbxRecord.getString(COLUMN_BORROWER_CONTACT_URI),
                        dbxRecord.getString(COLUMN_BORROWER_THUMBNAIL_URI)});

            }
            return new BorrowerCursor(matrixCursor);
        } catch (DbxException e) {
            e.printStackTrace();
            return null;
        }




    }

    @Override
    public List<Borrower> getAllBorrowers() {
        return null;
    }

    @Override
    public BorrowerCursor queryBorrowers(String query) {

        if (!query.isEmpty()) {
            query.trim().toLowerCase();
            DbxTable.QueryResult results = null;
            String title;
            try {
                List<DbxRecord> listResults = new ArrayList<DbxRecord>();
                results = borrowersTable.query();
                if (results != null) {
                    for (DbxRecord record : results) {
                        title = record.getString(COLUMN_BORROWER_NAME);
                        if (title.contains(query)) {
                            listResults.add(record);
                        }

                    }

                    MatrixCursor matrixCursor = new MatrixCursor(BORROWER_FIELDS);
                    Collections.sort(listResults, borrowerComparator);
                    for (DbxRecord dbxRecord : listResults) {
                        //loop and create each matrixCursor
                        matrixCursor.addRow(new Object[]{
                                dbxRecord.getLong(COLUMN_BORROWER_ID),
                                dbxRecord.getString(COLUMN_BORROWER_NAME),
                                dbxRecord.getDouble(COLUMN_BORROWER_AMOUNT_BORROWED),
                                dbxRecord.getDouble(COLUMN_BORROWER_AMOUNT_PAID),
                                dbxRecord.getDouble(COLUMN_BORROWER_INTEREST_RATE),
                                (int) dbxRecord.getLong(COLUMN_BORROWER_LOAN_DURATION),
                                dbxRecord.getBoolean(COLUMN_BORROWER_PAID),
                                dbxRecord.getLong(COLUMN_BORROWER_DATE_BORROWED),
                                dbxRecord.getLong(COLUMN_BORROWER_DATE_DUE),
                                dbxRecord.getString(COLUMN_BORROWER_PHONE_NUMBER),
                                dbxRecord.getString(COLUMN_BORROWER_CONTACT_URI),
                                dbxRecord.getString(COLUMN_BORROWER_THUMBNAIL_URI)});

                    }

                    return new BorrowerCursor(matrixCursor);
                } else {
                    //return an empty cursor
                    return new BorrowerCursor(new MatrixCursor(BORROWER_FIELDS));
                }
            } catch (DbxException e) {
                e.printStackTrace();
                return null;
            }
        }
        //return an empty cursor
        return new BorrowerCursor(new MatrixCursor(BORROWER_FIELDS));
    }

    @Override
    public BorrowerCursor queryBorrower(long id) {

        DbxFields queryParams = new DbxFields().set(COLUMN_BORROWER_ID, id);
        DbxTable.QueryResult results = null;
        try {
            results = borrowersTable.query(queryParams);
        } catch (DbxException e) {
            e.printStackTrace();
        }
       // int size = results.count();
        DbxRecord firstResult = results.iterator().next();

        return new BorrowerCursor(createCursorFromRecord(firstResult));
    }

    private MatrixCursor createCursorFromRecord(DbxRecord dbxRecord) {

        MatrixCursor matrixCursor = new MatrixCursor(BORROWER_FIELDS);
        //for (int i=0;i<count;i++) {
            matrixCursor.addRow(new Object[]{
                    dbxRecord.getLong(COLUMN_BORROWER_ID),
                    dbxRecord.getString(COLUMN_BORROWER_NAME),
                    dbxRecord.getDouble(COLUMN_BORROWER_AMOUNT_BORROWED),
                    dbxRecord.getDouble(COLUMN_BORROWER_AMOUNT_PAID),
                    dbxRecord.getDouble(COLUMN_BORROWER_INTEREST_RATE),
                    (int) dbxRecord.getLong(COLUMN_BORROWER_LOAN_DURATION),
                    dbxRecord.getBoolean(COLUMN_BORROWER_PAID),
                    dbxRecord.getLong(COLUMN_BORROWER_DATE_BORROWED),
                    dbxRecord.getLong(COLUMN_BORROWER_DATE_DUE),
                    dbxRecord.getString(COLUMN_BORROWER_PHONE_NUMBER),
                    dbxRecord.getString(COLUMN_BORROWER_CONTACT_URI),
                    dbxRecord.getString(COLUMN_BORROWER_THUMBNAIL_URI)});
      //  }

        return matrixCursor;

    }

    @Override
    public int updateBorrower(Borrower borrower) {

        DbxFields queryParams = new DbxFields().set(COLUMN_BORROWER_ID, borrower.getId());

        DbxTable.QueryResult results = null;
        try {
            results = borrowersTable.query(queryParams);
            DbxRecord firstResult = results.iterator().next();
           firstResult = borrowersTable.insert().
                set(COLUMN_BORROWER_NAME, borrower.getName()).
                set(COLUMN_BORROWER_AMOUNT_BORROWED, borrower.getLoanAmount()).
                set(COLUMN_BORROWER_AMOUNT_PAID, borrower.getAmountPaid()).
                set(COLUMN_BORROWER_INTEREST_RATE, borrower.getInterestRate()).
                set(COLUMN_BORROWER_LOAN_DURATION, borrower.getLoanDuration()).
                set(COLUMN_BORROWER_PAID, borrower.paid()).
                set(COLUMN_BORROWER_DATE_BORROWED, borrower.getDateBorrowed().getTimeInMillis()).
                set(COLUMN_BORROWER_DATE_DUE, borrower.getDateDue().getTimeInMillis()).
                set(COLUMN_BORROWER_PHONE_NUMBER, borrower.getPhoneNumber()).
                set(COLUMN_BORROWER_CONTACT_URI, (borrower.getContactUri() != null) ?
                        borrower.getContactUri().toString() : "Uri null").
                set(COLUMN_BORROWER_THUMBNAIL_URI, borrower.getThumbnailUri());
            sync();
            return 1;
        } catch (DbxException e) {
            e.printStackTrace();
            return 0;
        }

    }

    @Override
    public boolean deleteBorrower(long rowId) {
        DbxFields queryParams = new DbxFields().set(COLUMN_BORROWER_ID, rowId);

        DbxTable.QueryResult results = null;
        try {
            results = borrowersTable.query(queryParams);
            DbxRecord firstResult = results.iterator().next();
            firstResult.deleteRecord();
            sync();
            return true;
        } catch (DbxException e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public long insertLender(Lender lender) {

        long _id ;
        if (lender.getId() != -1){
            _id = lender.getId();
            lastLenderId = lender.getId();
            saveLastLenderID();
        }else{
            lastLenderId++;
            _id = lastLenderId ;
            saveLastLenderID();
        }

        DbxRecord temp = lendersTable.insert()
                .set(COLUMN_LENDER_ID,_id).
                        set(COLUMN_LENDER_NAME, lender.getName()).
                        set(COLUMN_lENDER_LOAN_AMOUNT, lender.getLoanAmount()).
                        set(COLUMN_LENDER_AMOUNT_PAID, lender.getAmountPaid()).
                        set(COLUMN_lENDER_INTEREST_RATE, lender.getInterestRate()).
                        set(COLUMN_LENDER_LOAN_DURATION, lender.getLoanDuration()).
                        set(COLUMN_LENDER_PAID, lender.paid()).
                        set(COLUMN_LENDER_DATE_LOANED, lender.getDateLoaned().getTimeInMillis()).
                        set(COLUMN_LENDER_DATE_DUE, lender.getDateDue().getTimeInMillis()).
                        set(COLUMN_LENDER_PHONE_NUMBER, lender.getPhoneNumber()).
                        set(COLUMN_LENDER_CONTACT_URI, (lender.getContactUri()!=null)?
                                lender.getContactUri().toString():"Uri null").
                        set(COLUMN_LENDER_THUMBNAIL_URI, lender.getThumbnailUri());
        ///gonna have to get last id before insertin
        //  DbxRecord idRecord = idTable.insert().set("LAST_BORROWER_ID",lastBorrowerId);


        sync();
        // onStorageContentChanged(CACHE_NOTES_LIST);
        return temp.getLong(COLUMN_LENDER_ID);
    }

    private void saveLastLenderID() {
        editor = pref.edit();
        editor.putLong(KEY_LAST_LENDER_ID,lastLenderId);
    }

    @Override
    public LenderCursor queryLenders() {
        try {
            DbxTable.QueryResult results = lendersTable.query();
            MatrixCursor matrixCursor = new MatrixCursor(LENDERS_FIELDS);
            List<DbxRecord> listResults = results.asList();
            Collections.sort(listResults,lenderComparator);
            for (DbxRecord dbxRecord : listResults) {
                //loop and create each matrixCursor
                matrixCursor.addRow(new Object[]{
                        dbxRecord.getLong(COLUMN_LENDER_ID),
                        dbxRecord.getString(COLUMN_LENDER_NAME),
                        dbxRecord.getDouble(COLUMN_lENDER_LOAN_AMOUNT),
                        dbxRecord.getDouble(COLUMN_LENDER_AMOUNT_PAID),
                        dbxRecord.getDouble(COLUMN_lENDER_INTEREST_RATE),
                        (int) dbxRecord.getLong(COLUMN_LENDER_LOAN_DURATION),
                        dbxRecord.getBoolean(COLUMN_LENDER_PAID),
                        dbxRecord.getLong(COLUMN_LENDER_DATE_LOANED),
                        dbxRecord.getLong(COLUMN_LENDER_DATE_DUE),
                        dbxRecord.getString(COLUMN_LENDER_PHONE_NUMBER),
                        dbxRecord.getString(COLUMN_LENDER_CONTACT_URI),
                        dbxRecord.getString(COLUMN_LENDER_THUMBNAIL_URI)});

            }
            return new LenderCursor(matrixCursor);
        } catch (DbxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Lender> getAllLenders() {
        return null;
    }

    @Override
    public LenderCursor queryLenders(String query) {
        if (!query.isEmpty()) {
            query.trim().toLowerCase();
            DbxTable.QueryResult results = null;
            String title;
            try {
                List<DbxRecord> listResults = new ArrayList<DbxRecord>();
                results = lendersTable.query();
                if (results != null) {
                    for (DbxRecord record : results) {
                        title = record.getString(COLUMN_LENDER_NAME);
                        if (title.contains(query)) {
                            listResults.add(record);
                        }

                    }
                    MatrixCursor matrixCursor = new MatrixCursor(LENDERS_FIELDS);
                    Collections.sort(listResults, lenderComparator);
                    for (DbxRecord dbxRecord : listResults) {
                        //loop and create each matrixCursor
                        matrixCursor.addRow(new Object[]{
                                dbxRecord.getLong(COLUMN_LENDER_ID),
                                dbxRecord.getString(COLUMN_LENDER_NAME),
                                dbxRecord.getDouble(COLUMN_lENDER_LOAN_AMOUNT),
                                dbxRecord.getDouble(COLUMN_LENDER_AMOUNT_PAID),
                                dbxRecord.getDouble(COLUMN_lENDER_INTEREST_RATE),
                                (int) dbxRecord.getLong(COLUMN_LENDER_LOAN_DURATION),
                                dbxRecord.getBoolean(COLUMN_LENDER_PAID),
                                dbxRecord.getLong(COLUMN_LENDER_DATE_LOANED),
                                dbxRecord.getLong(COLUMN_LENDER_DATE_DUE),
                                dbxRecord.getString(COLUMN_LENDER_PHONE_NUMBER),
                                dbxRecord.getString(COLUMN_LENDER_CONTACT_URI),
                                dbxRecord.getString(COLUMN_LENDER_THUMBNAIL_URI)});

                    }

                    return new LenderCursor(matrixCursor);
                }
            } catch (DbxException e) {
                e.printStackTrace();
                AppLog.e(TAG,"Error querying lenders using string",e);
                return null;
            }
        }
        return new LenderCursor(new MatrixCursor(LENDERS_FIELDS));
    }

    @Override
    public LenderCursor queryLender(long id) {
        DbxFields queryParams = new DbxFields().set(COLUMN_LENDER_ID,id);
        DbxTable.QueryResult results = null;
        try {
            results = lendersTable.query(queryParams);
            MatrixCursor matrixCursor = new MatrixCursor(LENDERS_FIELDS);
            for (DbxRecord dbxRecord : results) {
                //loop and create each matrixCursor
                matrixCursor.addRow(new Object[]{
                        dbxRecord.getLong(COLUMN_LENDER_ID),
                        dbxRecord.getString(COLUMN_LENDER_NAME),
                        dbxRecord.getDouble(COLUMN_lENDER_LOAN_AMOUNT),
                        dbxRecord.getDouble(COLUMN_LENDER_AMOUNT_PAID),
                        dbxRecord.getDouble(COLUMN_lENDER_INTEREST_RATE),
                        (int) dbxRecord.getLong(COLUMN_LENDER_LOAN_DURATION),
                        dbxRecord.getBoolean(COLUMN_LENDER_PAID),
                        dbxRecord.getLong(COLUMN_LENDER_DATE_LOANED),
                        dbxRecord.getLong(COLUMN_LENDER_DATE_DUE),
                        dbxRecord.getString(COLUMN_LENDER_PHONE_NUMBER),
                        dbxRecord.getString(COLUMN_LENDER_CONTACT_URI),
                        dbxRecord.getString(COLUMN_LENDER_THUMBNAIL_URI)});

            }

            return new LenderCursor(matrixCursor);
        } catch (DbxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int updateLender(Lender lender) {

        DbxFields queryParams = new DbxFields().set(COLUMN_BORROWER_ID, lender.getId());

        DbxTable.QueryResult results = null;

        try {
            results = lendersTable.query(queryParams);
        } catch (DbxException e) {
            e.printStackTrace();
            return 0;
        }
        DbxRecord firstResult = results.iterator().next();
        firstResult = lendersTable.insert().
                        set(COLUMN_LENDER_NAME, lender.getName()).
                        set(COLUMN_lENDER_LOAN_AMOUNT, lender.getLoanAmount()).
                        set(COLUMN_LENDER_AMOUNT_PAID, lender.getAmountPaid()).
                        set(COLUMN_lENDER_INTEREST_RATE, lender.getInterestRate()).
                        set(COLUMN_LENDER_LOAN_DURATION, lender.getLoanDuration()).
                        set(COLUMN_LENDER_PAID, lender.paid()).
                        set(COLUMN_LENDER_DATE_LOANED, lender.getDateLoaned().getTimeInMillis()).
                        set(COLUMN_LENDER_DATE_DUE, lender.getDateDue().getTimeInMillis()).
                        set(COLUMN_LENDER_PHONE_NUMBER, lender.getPhoneNumber()).
                        set(COLUMN_LENDER_CONTACT_URI, (lender.getContactUri()!=null)?
                                lender.getContactUri().toString():"Uri null").
                        set(COLUMN_LENDER_THUMBNAIL_URI, lender.getThumbnailUri());
        ///gonna have to get last id before insertin
        //  DbxRecord idRecord = idTable.insert().set("LAST_BORROWER_ID",lastBorrowerId);


        sync();
        return 1;
    }

    @Override
    public boolean deleteLender(long rowId) {
        DbxFields queryParams = new DbxFields().set(COLUMN_LENDER_ID, rowId);

        DbxTable.QueryResult results = null;
        try {
            results = lendersTable.query(queryParams);
            DbxRecord firstResult = results.iterator().next();
            firstResult.deleteRecord();
            sync();
            return true;
        } catch (DbxException e) {
            e.printStackTrace();
            return false;
        }
    }



    // Listeners

    private void notifyListeners() {
       /* PayUpApplication.executeInBackground(new Runnable() {
            @Override
            public void run() {
                synchronized (storageListeners) {
                    for (AppStorageListener listener : storageListeners) {
                        listener.onContentChanged();
                    }
                }
            }
        });*/
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
            //storageListeners.clear();
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
    public void clear() {

        final DbxTable[] allTables = {borrowersTable,lendersTable};

        for (DbxTable table : allTables) {
            final DbxTable.QueryResult allTableRecords;
            try {
                allTableRecords = table.query();
            } catch (DbxException e) {
                AppLog.e(TAG, "clear()", e);
                throw new RuntimeException();
            }

            for (DbxRecord record : allTableRecords) {
                record.deleteRecord();
            }
        }
        try {
            datastore.sync();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        // onStorageContentChanged(CACHE_NOTE | CACHE_NOTES_LIST | CACHE_LABELS_LIST);

    }

    @Override
    public void setSortOrder(SortOrder sortOrder) {
        lenderComparator.setSortOrder(sortOrder);
        borrowerComparator.setSortOrder(sortOrder);

    }

    @Override
    public DbxDatastoreStatus getSyncStatus() {
        return null;
    }




    //************************************************
    private class BorrowerComparator implements Comparator<DbxRecord>{

        private SortOrder order;


        public BorrowerComparator(){
            order = SortOrder.dateDue;
        }

        public void setSortOrder(SortOrder order){
            this.order = order;
        }
        public SortOrder getSortOrder(){
            return this.order;
        }
        @Override
        public int compare(DbxRecord lhs, DbxRecord rhs) {

            switch(order){
                case name:
                   return lhs.getString(COLUMN_BORROWER_NAME).compareToIgnoreCase
                           (rhs.getString(COLUMN_BORROWER_NAME));

                case dateDue:
                  return  new Date(lhs.getLong(COLUMN_BORROWER_DATE_DUE)).compareTo
                          (new Date(rhs.getLong(COLUMN_BORROWER_DATE_DUE)));

                case dateCreated:
                   return new Date(lhs.getLong(COLUMN_BORROWER_DATE_BORROWED)).compareTo
                           (new Date(rhs.getLong(COLUMN_BORROWER_DATE_BORROWED)));
                default:
                    throw new IllegalArgumentException("Unknown sort order type: " + order.toString());
            }

        }

    }

    //************************************************************
    private class LenderComparator implements Comparator<DbxRecord> {

        private SortOrder order;


        public LenderComparator(){
            order = SortOrder.dateDue;
        }

        public void setSortOrder(SortOrder order){
            this.order = order;
        }
        public SortOrder getSortOrder(){
            return this.order;
        }
        public int compare(DbxRecord lhs, DbxRecord rhs) {

            switch(order){
                case name:
                    return lhs.getString(COLUMN_LENDER_NAME).compareToIgnoreCase
                            (rhs.getString(COLUMN_LENDER_NAME));

                case dateDue:
                    return  new Date(lhs.getLong(COLUMN_LENDER_DATE_DUE)).compareTo
                            (new Date(rhs.getLong(COLUMN_LENDER_DATE_DUE)));

                case dateCreated:
                    return new Date(lhs.getLong(COLUMN_LENDER_DATE_LOANED)).compareTo
                            (new Date(rhs.getLong(COLUMN_LENDER_DATE_LOANED)));
                default:
                    throw new IllegalArgumentException("Unknown sort order type: " + order.toString());
            }

        }


    }


}
