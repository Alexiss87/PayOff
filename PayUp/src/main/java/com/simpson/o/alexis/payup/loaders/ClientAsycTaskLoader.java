package com.simpson.o.alexis.payup.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.simpson.o.alexis.payup.Borrower;
import com.simpson.o.alexis.payup.model.Client;

import java.util.List;

/**
 * Created by alexi_000 on 17/12/2014.
 */
public abstract class ClientAsycTaskLoader extends AsyncTaskLoader<List<? extends Client>> {

    private List<? extends Client> clients ;

    public ClientAsycTaskLoader(Context context){
        super(context);

    }
    protected abstract<T extends Client> List<T> loadData();
    @Override
    public List<? extends Client> loadInBackground() {
        return loadData();
    }

    @Override
    protected void onStartLoading() {
        //make sure we have content to deliver
        if (clients !=null)
            deliverResult(clients);
        //otherwise if something has changed or first time
        if (takeContentChanged() || clients ==null)
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStartLoading();
        //clear refference to object
        //it's necessary to allow GC to collect the object
        //to avoid memory leaking
        clients = null;
    }
}
