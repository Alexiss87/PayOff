package com.simpson.o.alexis.payup.storage;


import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxSyncStatus;
import com.simpson.o.alexis.payup.Borrower;
import com.simpson.o.alexis.payup.Lender;
import com.simpson.o.alexis.payup.enums.SortOrder;
import com.simpson.o.alexis.payup.cursorwrapper.BorrowerCursor;
import com.simpson.o.alexis.payup.cursorwrapper.LenderCursor;

import java.util.List;

/**
 * Created by alexi_000 on 22/11/2014.
 */
public interface AppStorage {



//methods for borrowers
    public long insertBorrower(Borrower borrower) ;

    public BorrowerCursor queryBorrowers();

    public List<Borrower> getAllBorrowers();

    public BorrowerCursor queryBorrowers(String query);

    public BorrowerCursor queryBorrower(long id) ;

    public int updateBorrower(Borrower borrower);

    public boolean deleteBorrower(long rowId) ;

//methods for lenders
    public long insertLender(Lender lender);

    public LenderCursor queryLenders() ;

    public List<Lender> getAllLenders();

    public LenderCursor queryLenders(String query) ;

    public LenderCursor queryLender(long id) ;

    public int updateLender(Lender lender) ;

    public boolean deleteLender(long rowId) ;

    // listeners

    public boolean addStorageListener(AppStorageListener listener);
    public boolean removeStorageListener(AppStorageListener listener);
    public List<AppStorageListener> detachAllListeners();
    public void attachListeners(List<AppStorageListener> listeners);

    // synchronization

    public void sync();

    // all data delete

    public void clear();

    public void setSortOrder(SortOrder sortOrder);

    public com.dropbox.sync.android.DbxDatastoreStatus getSyncStatus();
}
