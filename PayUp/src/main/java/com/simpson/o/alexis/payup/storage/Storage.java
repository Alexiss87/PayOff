package com.simpson.o.alexis.payup.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.simpson.o.alexis.payup.PayUpApplication;
import com.simpson.o.alexis.payup.PayUpDataBaseHelper;
import com.simpson.o.alexis.payup.Utils.AppLog;
import com.simpson.o.alexis.payup.dropbox.PayUpDropboxStorage;

/**
 * Created by alexi_000 on 22/11/2014.
 */
public final class Storage {

    private static final String TAG = Storage.class.getSimpleName();

    private static final StorageWrapper storageWrapper = new StorageWrapper();

    private static final String STORAGE_TYPE = "storage_type";
    public static final Type DEFAULT_STORAGE = Type.Database;
    private static Type currentStorageType = null;
    private static volatile boolean initialized = false;


    public static AppStorage getStorage() {
        checkInit();
        return storageWrapper;
    }

    private static void checkInit() {
        if (!initialized) {
            throw new IllegalStateException("Storage must be initialized before usage!");
        }
    }

    public static Type getCurrentStorageType() {
        return currentStorageType;
    }

    /**
     * Storage initialization
     * @param newStorageType new storage type or null for last used or default storage
     */
    public static void init(Type newStorageType) {
        AppLog.d(TAG, "init() call. Initialized: " + initialized +
                " Current storage: " + String.valueOf(currentStorageType) +
                " New storage: " + String.valueOf(newStorageType));
        if (initialized && currentStorageType == newStorageType) {
            return;
        }

        final Context context = PayUpApplication.getContext();
        final boolean lastUsedOrDefault = newStorageType == null;
        if (lastUsedOrDefault) {
            final SharedPreferences prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            newStorageType = Type.valueOf(prefs.getString(STORAGE_TYPE, DEFAULT_STORAGE.toString()));
        }

        switch (newStorageType) {
            case Database:
                storageWrapper.setTarget(new PayUpDataBaseHelper(PayUpApplication.getContext()));
                break;

            case Dropbox:
                storageWrapper.setTarget(new PayUpDropboxStorage());
                break;

            default:
                throw new IllegalArgumentException("Unknown storage type: "
                        + newStorageType.toString());
        }

        final SharedPreferences.Editor prefsEditor =
                context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit();
        prefsEditor.putString(STORAGE_TYPE, newStorageType.toString());
        prefsEditor.commit();

        currentStorageType = newStorageType;
        initialized = true;
    }
    /**
     * ******************************************
     *
     * Inner classes
     *
     * *******************************************
     */


    public static enum Type {
        Database,
        Dropbox
    }
    }
