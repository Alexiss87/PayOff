package com.simpson.o.alexis.payup.storage;

import com.dropbox.sync.android.DbxDatastore;

/**
 * Created by alexi_000 on 22/11/2014.
 */
public interface AppStorageListener extends DbxDatastore.SyncStatusListener{

    @Override
    void onDatastoreStatusChange(DbxDatastore dbxDatastore);
}
