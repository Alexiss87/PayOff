package com.simpson.o.alexis.payup.storage;

import com.simpson.o.alexis.payup.Borrower;
import com.simpson.o.alexis.payup.Lender;
import com.simpson.o.alexis.payup.enums.SortOrder;
import com.simpson.o.alexis.payup.cursorwrapper.BorrowerCursor;
import com.simpson.o.alexis.payup.cursorwrapper.LenderCursor;

import java.util.List;

/**
 * Created by alexi_000 on 22/11/2014.
 */
public  class StorageWrapper implements AppStorage {

    private AppStorage target;

    void setTarget(AppStorage target) {
        this.target = target;
    }

    @Override
    public long insertBorrower(Borrower borrower) {
        return target.insertBorrower(borrower);
    }

    @Override
    public BorrowerCursor queryBorrowers() {
        return target.queryBorrowers();
    }

    @Override
    public List<Borrower> getAllBorrowers() {
        return target.getAllBorrowers();
    }

    @Override
    public BorrowerCursor queryBorrowers(String query) {
        return target.queryBorrowers(query);
    }

    @Override
    public BorrowerCursor queryBorrower(long id) {
        return target.queryBorrower(id);
    }

    @Override
    public int updateBorrower(Borrower borrower) {
        return target.updateBorrower(borrower);
    }

    @Override
    public boolean deleteBorrower(long rowId) {
        return target.deleteBorrower(rowId);
    }

    @Override
    public long insertLender(Lender lender) {
        return target.insertLender(lender);
    }

    @Override
    public LenderCursor queryLenders() {
        return target.queryLenders();
    }

    @Override
    public List<Lender> getAllLenders() {
        return target.getAllLenders();
    }

    @Override
    public LenderCursor queryLenders(String query) {
        return target.queryLenders(query);
    }

    @Override
    public LenderCursor queryLender(long id) {
        return target.queryLender(id);
    }

    @Override
    public int updateLender(Lender lender) {
        return target.updateLender(lender);
    }

    @Override
    public boolean deleteLender(long rowId) {
        return target.deleteLender(rowId);
    }

    @Override
    public boolean addStorageListener(AppStorageListener listener) {
        return target.addStorageListener(listener);
    }

    @Override
    public boolean removeStorageListener(AppStorageListener listener) {
        return target.removeStorageListener(listener);
    }

    @Override
    public List<AppStorageListener> detachAllListeners() {
        return detachAllListeners();
    }

    @Override
    public void attachListeners(List<AppStorageListener> listeners) {
        target.attachListeners(listeners);
    }

    @Override
    public void sync() {
        target.sync();
    }

    @Override
    public void clear() {
        target.clear();
    }

    @Override
    public void setSortOrder(SortOrder sortOrder) {
        target.setSortOrder(sortOrder);
    }

    @Override
    public com.dropbox.sync.android.DbxDatastoreStatus getSyncStatus() {
        return target.getSyncStatus();
    }


}
