package com.simpson.o.alexis.payup;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.simpson.o.alexis.payup.Utils.DateUtils;
import com.simpson.o.alexis.payup.Utils.Utils;
import com.simpson.o.alexis.payup.cursorwrapper.LenderCursor;
import com.simpson.o.alexis.payup.dialogs.AboutDialog;
import com.simpson.o.alexis.payup.enums.SortOrder;
import com.simpson.o.alexis.payup.loaders.ClientAsycTaskLoader;
import com.simpson.o.alexis.payup.loaders.SQLiteCursorLoader;
import com.simpson.o.alexis.payup.model.Client;
import com.simpson.o.alexis.payup.quickaction.ActionItem;
import com.simpson.o.alexis.payup.quickaction.QuickAction;
import com.simpson.o.alexis.payup.storage.AppStorage;
import com.simpson.o.alexis.payup.storage.Storage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Alexis on 5/29/2014.
 */
public class LendersListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<? extends Client>>,SearchView.OnQueryTextListener,SearchView.OnCloseListener  {

    private static final String EXTRA_NEW_LENDER ="NEW LENDER";
    private static final String TAG = "com.simpson.o.alexis.payup.LendersListFragment";

    private LoanOffice loanOffice;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("EEE MMM dd");
    private ImageButton emptyButton;
    // The SearchView for doing filtering.
    SearchView mSearchView;
    private static String search_query = null; // holds the current query...
    private MenuActivity mainActivity;
    private AppStorage storage = LoanOffice.getInstance(getActivity()).getStorage();
    private RecyclerView recList;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MenuActivity)activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.lenders_pager_title);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        loanOffice = LoanOffice.getInstance(getActivity());
        getLoaderManager().initLoader(0, null, this);
    }


    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       // View v = inflater.inflate(R.layout.lenders_frgment_list,container, false);
        View v = inflater.inflate(R.layout.recycler_list,container, false);

        /*mListView = (ListView) v.findViewById(android.R.id.list);
        registerForContextMenu(mListView);*/
        recList = (RecyclerView) v.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        registerForContextMenu(recList);

       /* emptyButton = (ImageButton)v.findViewById(R.id.lenders_emptyViewImage);
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LendersDetailActivity.class);
                intent.putExtra(LendersDetailFragment.EXTRA_LENDER_DETAIL_ID,-1);
                intent.putExtra(EXTRA_NEW_LENDER,true);
                startActivityForResult(intent,0);
            }
        });*/


        return v;
    }

   /* @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Intent intent = new Intent(getActivity(), LendersDetailActivity.class);
        intent.putExtra(LendersDetailFragment.EXTRA_LENDER_DETAIL_ID,id);
        startActivityForResult(intent,0);

    }*/

    @Override
    public Loader<List<? extends Client>> onCreateLoader(int id, Bundle args) {
        // we only ever load the lenders, so assume this is the case
        return new LendersListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<? extends Client>> loader, List<? extends Client> data) {
        LenderListsAdapter adapter = new LenderListsAdapter(getActivity(), (List<Lender>) data);

        recList.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<List<? extends Client>> loader) {
        recList.setAdapter(null);
    }



    private static class LendersListCursorLoader extends SQLiteCursorLoader {

        public LendersListCursorLoader(Context context) {
            super(context);
        }

        @Override
        protected Cursor loadCursor() {
            // query the list of borrowers
            if(!TextUtils.isEmpty(search_query)){
                return LoanOffice.getInstance(getContext()).getLenders(search_query);
            }else {
                return LoanOffice.getInstance(getContext()).getLenders();
            }

        }

    }

    private static class LendersListLoader extends ClientAsycTaskLoader {
        LenderCursor mLenderCursor;
        private List<Lender> lenderList;
        public LendersListLoader(Context context) {
            super(context);
        }

        @Override
        protected  List<Lender> loadData() {
            lenderList = new ArrayList<Lender>();
            // query the list of lenders
            if(!TextUtils.isEmpty(search_query)) {
                mLenderCursor = LoanOffice.getInstance(getContext()).getLenders(search_query);
            }else {
                mLenderCursor = LoanOffice.getInstance(getContext()).getLenders();

            }
            for (mLenderCursor.moveToFirst(); !mLenderCursor.isAfterLast(); mLenderCursor.moveToNext()) {
                // The Cursor is now set to the right position
                lenderList.add(mLenderCursor.getLender());
            }
            if (mLenderCursor != null && !mLenderCursor.isClosed()) {
                mLenderCursor.close();
            }
            return  lenderList;
        }
    }


    private class LenderListsAdapter extends RecyclerView.Adapter<LenderListsAdapter.ClientViewHolder> {
        private List<Lender> lendersList;
        private LayoutInflater mInflater;

        public LenderListsAdapter(Context ctx,List<Lender> lendersList){
            this.lendersList = lendersList;
            this.mInflater = LayoutInflater.from(ctx);
        }


        @Override
        public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.card_list_item, parent, false);

            return new ClientViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ClientViewHolder holder, int position) {

            // get the run for the current row
            Lender l = lendersList.get(position);
            /*nameTextView = (TextView) view.findViewById(R.id.list_item_nameTV);
            amountTextView = (TextView) view.findViewById(R.id.list_item_amountTV);
            statusImageView = (ImageView) view.findViewById(R.id.status_imageView);
            balannceTV = (TextView)view.findViewById(R.id.list_item_balanceTV);
            mBadge = (QuickContactBadge)view.findViewById(R.id.listContactBadge);*/
            holder.nameTextView.setText(l.getName());
            holder.balannceTV.setText(getActivity().getString(R.string.You_owe));



            if (l.getContactUri() != null) {
                holder.mBadge.assignContactUri(l.getContactUri());
            }
            if (l.getThumbnailUri() != null ) {
                if (!l.getThumbnailUri().equalsIgnoreCase("Uri null")){
                    holder.mBadge.setImageBitmap(Utils.loadContactPhotoThumbnail(l.getThumbnailUri()));
                }

            }

            //calculate principal amount 1. amount loaned
            //2. interst and 3. number of periods
            float principal = LoanOffice.getInstance(getActivity()).
                    getAmountDue(l.getLoanAmount(),l.getInterestRate(),1/*l.getLoanPeriod*/);
            //calculate amount due by subtracting what is paid from the principal amount
            float amountDue = principal - l.getAmountPaid();
            holder.amountTextView.setText("$"+Float.toString(amountDue));

            holder.statusImageView.setVisibility(View.GONE);

            if (!l.paid() && DateUtils.hasSetTimePassed(l.getDateDue())){
                holder.statusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_alert));
                holder.statusImageView.setVisibility(View.VISIBLE);

            }

            holder.nameTextView.setPaintFlags(holder.nameTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.amountTextView.setPaintFlags(holder.amountTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.balannceTV.setPaintFlags(holder.balannceTV.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

            if (l.paid()){
                holder.statusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                holder.statusImageView.setVisibility(View.VISIBLE);
                holder.nameTextView.setPaintFlags(holder.nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.amountTextView.setText("$"+Float.toString(principal));
                holder.amountTextView.setPaintFlags(holder.amountTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.balannceTV.setPaintFlags(holder.balannceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

        }

        @Override
        public int getItemCount() {
            return lendersList.size();
        }

        public class ClientViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
        {
            protected ImageView statusImageView;
            protected TextView nameTextView;
            protected TextView amountTextView;
            protected TextView balannceTV;
            protected QuickContactBadge mBadge;

            private static final int ID_DETAILS = 1;
            private static final int ID_DELETE = 2;
            private static final int ID_PAID = 3;
            final QuickAction mQuickAction  = new QuickAction(getActivity());

            private int viewPosition;

            public ClientViewHolder(View view) {
                super(view);
                nameTextView = (TextView) view.findViewById(R.id.list_item_nameTV);
                amountTextView = (TextView) view.findViewById(R.id.list_item_amountTV);
                statusImageView = (ImageView) view.findViewById(R.id.status_imageView);
                balannceTV = (TextView) view.findViewById(R.id.list_item_balanceTV);
                mBadge = (QuickContactBadge)view.findViewById(R.id.listContactBadge);
                view.setOnClickListener(this);



                ActionItem addItem      = new ActionItem(ID_DETAILS, mainActivity.getString(R.string.action_menu_details),
                        getResources().getDrawable(R.drawable.ic_action_action_account_box));
                ActionItem acceptItem   = new ActionItem(ID_DELETE, mainActivity.getString(R.string.action_menu_delete),
                        getResources().getDrawable(R.drawable.ic_action_action_delete));
                ActionItem uploadItem   = new ActionItem(ID_PAID, mainActivity.getString(R.string.action_menu_paid),
                        getResources().getDrawable(R.drawable.ic_action_icon_paid));

                //use setSticky(true) to disable QuickAction dialog being dismissed after an item is clicked
                uploadItem.setSticky(true);



                mQuickAction.addActionItem(addItem);
                mQuickAction.addActionItem(acceptItem);
                mQuickAction.addActionItem(uploadItem);

                //setup the action item click listener
                mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                    @Override
                    public void onItemClick(QuickAction quickAction, int pos, int actionId) {
                        ActionItem actionItem = quickAction.getActionItem(pos);
                        long id = lendersList.get(viewPosition).getId();
                        switch (actionId){
                            case ID_DETAILS:
                                Intent intent = new Intent(getActivity(), LendersDetailActivity.class);
                                intent.putExtra(LendersDetailFragment.EXTRA_LENDER_DETAIL_ID,id);
                                startActivityForResult(intent, 0);
                                break;
                            case ID_DELETE:
                                loanOffice.removeLender(id);
                                // restart the loader to get any new borrower available
                                getLoaderManager().restartLoader(0, null, LendersListFragment.this);
                                break;
                            case ID_PAID:
                                createYesNoDialog(id);
                                getLoaderManager().restartLoader(0, null, LendersListFragment.this);
                                mQuickAction.dismiss();
                                break;
                        }
                    }
                });

                mQuickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        // Toast.makeText(getApplicationContext(), "Ups..dismissed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onClick(View v) {
                viewPosition = recList.getChildPosition(v);
                mQuickAction.show(v);
                mQuickAction.setAnimStyle(QuickAction.ANIM_GROW_FROM_CENTER);
            }

        }



    }




    @Override
    public void onResume() {
        getActivity().setTitle(R.string.lenders_pager_title);
        getLoaderManager().initLoader(0, null, this);
        super.onResume();

    }

    public static class MySearchView extends SearchView {
        public MySearchView(Context context) {
            super(context);
        }

        // The normal SearchView doesn't clear its search text when
        // collapsed, so we will do this for it.
        @Override
        public void onActionViewCollapsed() {
            setQuery("", false);
            super.onActionViewCollapsed();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_lenders_list,menu);

       /* SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(this);*/

        // Place an action bar item for searching.
        MenuItem item = menu.add(getResources().getString(R.string.search_title));
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        mSearchView = new MySearchView(getActivity());
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setIconifiedByDefault(true);
        SearchView.SearchAutoComplete searchText = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);
        searchText.setTextColor(getActivity().getResources().getColor(android.R.color.white));
        item.setActionView(mSearchView);
        inflateSortMenu(menu);
        updateDropboxActionTitle(menu);

    }
    private void inflateSortMenu(Menu menu) {
        final int order = 3;
        final SubMenu sortMenu =
                menu.addSubMenu(Menu.NONE, Menu.NONE, order, R.string.action_sort);
        getActivity().getMenuInflater().inflate(R.menu.main_sort_menu, sortMenu);
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        // this is your adapter that will be filtered
        if (TextUtils.isEmpty(newText))
        {
           // getActivity().getActionBar().setSubtitle(R.string.action_bar_list_subtitle);
            search_query = null;
            //mListView.clearTextFilter();
        }
        else
        {
           // getActivity().getActionBar().setSubtitle(R.string.action_bar_list_subtitle_search + newText);
            search_query = newText;
            // mListView.setFilterText(newText.toString());
        }
        getLoaderManager().restartLoader(0, null, LendersListFragment.this);

        return true;
    }

    @Override
    public boolean onClose() {

        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // TODO Auto-generated method stub
        return false;
    }
    private void updateDropboxActionTitle(Menu menu) {
        final MenuItem dropboxItem = menu.findItem(R.id.action_dropbox);
        if (dropboxItem !=  null) {
            if (Storage.getCurrentStorageType() == Storage.Type.Dropbox) {
                dropboxItem.setTitle(R.string.action_dropbox_refresh);
            } else {
                dropboxItem.setTitle(R.string.action_dropbox_link);
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.menu_item_add_Lender:
                Intent intent = new Intent(getActivity(), LendersDetailActivity.class);
                intent.putExtra(LendersDetailFragment.EXTRA_LENDER_DETAIL_ID,-1);
                intent.putExtra(EXTRA_NEW_LENDER,true);
                startActivityForResult(intent,0);
                return true;
            // sort menu
            case R.id.sort_by_name:
                setSortingOrder(SortOrder.name);
                return true;
            case R.id.sort_by_due_date:
                setSortingOrder(SortOrder.dateDue);
                return true;
            case R.id.sort_by_date_loaned_or_borrowed:
                setSortingOrder(SortOrder.dateCreated);
                return true;
            case R.id.action_dropbox:
                mainActivity.performDropboxAction();
                return true;

            case R.id.action_about:
                AboutDialog.show(getFragmentManager());
               // EventTracker.track(Event.AboutOpening);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void setSortingOrder(SortOrder order) {
        storage.setSortOrder(order);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("PREFS_KEY_SORT_ORDER", order.ordinal());
        editor.apply();

        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // restart the loader to get any new borrower available
        getLoaderManager().restartLoader(0, null, this);
    }

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_borrowers_lenders_list_context, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        LenderCursorAdapter adapter = (LenderCursorAdapter)getListAdapter();
        long id  = adapter.getItemId(position);
        switch (item.getItemId()) {
            case R.id.blist_menu_item_delete:
                LoanOffice.getInstance(getActivity()).removeLender(id);
                getLoaderManager().restartLoader(0, null, this);
                return true;
            case R.id.blist_menu_item_mark_paid:
                createYesNoDialog(id);
                return true;
        }
        return super.onContextItemSelected(item);
    }*/

    public void createYesNoDialog(final long ID){
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

// 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle(mainActivity.getString(R.string.mark_as_paid));
        // Add the buttons
        builder.setPositiveButton(mainActivity.getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Lender l =loanOffice.getLender(ID);
                float amountDue = l.getLoanAmount();
                l.setAmountPaid(amountDue);
                l.setIsPaid(true);
                loanOffice.updateLender(l);
                getLoaderManager().restartLoader(0, null, LendersListFragment.this);

            }
        });
        builder.setNegativeButton(mainActivity.getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

// 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}




