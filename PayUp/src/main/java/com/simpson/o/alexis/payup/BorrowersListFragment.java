package com.simpson.o.alexis.payup;
//this is using the custom cursor adapter
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.simpson.o.alexis.payup.Utils.DateUtils;
import com.simpson.o.alexis.payup.Utils.Utils;
import com.simpson.o.alexis.payup.cursorwrapper.BorrowerCursor;
import com.simpson.o.alexis.payup.dialogs.AboutDialog;
import com.simpson.o.alexis.payup.enums.SortOrder;
import com.simpson.o.alexis.payup.loaders.ClientAsycTaskLoader;
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
public class BorrowersListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<? extends Client>>,
        SearchView.OnQueryTextListener,SearchView.OnCloseListener/*AppStorageListener*/ {

    private static final String TAG = "com.simpson.o.alexis.payup.BorrowersListFragment";
    private static final String EXTRA_NEW_BORROWER = "NEW BORROWER";
    private LoanOffice loanOffice;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("EEE MMM dd");
    private static final int REQUEST_NEW_BORROWER = 0;
    private ImageButton emptyButton;
    // The SearchView for doing filtering.
    SearchView mSearchView;
    private static String search_query = null; // holds the current query...
    private static float total=0f;
    private static float principal;
    //private static Borrower borrower;
    private static float count = 0f;

    private Callbacks mCallback;
    private MenuActivity mainActivity;
    private AppStorage storage = LoanOffice.getInstance(getActivity()).getStorage();
    private RecyclerView recList;


    /**
     * Required interface for hosting activities.
     */
    public interface Callbacks {
        void onBorrowerSelected(long id);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        /*// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }*/
        mainActivity = (MenuActivity)activity;
    }


    @Override
    public void onDetach() {
        super.onDetach();
       // mCallback = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.borrowers_pager_title);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        loanOffice = LoanOffice.getInstance(getActivity());
        // initialize the loader to load the list of Borrowers
        getLoaderManager().initLoader(0, null, this);
    }

    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.recycler_list,container, false);

      /*  mListView = (ListView) v.findViewById(android.R.id.list);
        registerForContextMenu(mListView);*/
        recList = (RecyclerView) v.findViewById(R.id.cardList);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        registerForContextMenu(recList);

        return v;
    }



    @Override
    public Loader<List<? extends Client>> onCreateLoader(int id, Bundle args) {
        // we only ever load the borrowers, so assume this is the case
        return new BorrowerListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<? extends Client>> loader, List<? extends Client> data) {
        BorrowerListAdapter adapter = new BorrowerListAdapter(getActivity(), (List<Borrower>) data);

        recList.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<List<? extends Client>> loader) {
        recList.setAdapter(null);

    }

    private static class BorrowerListLoader extends ClientAsycTaskLoader {
        BorrowerCursor mBorrowerCursor;
        private List<Borrower> borrowerList;
        public BorrowerListLoader(Context context) {
            super(context);
        }


        @Override
        protected  List<Borrower> loadData() {
            borrowerList = new ArrayList<Borrower>();
            // query the list of borrowers
            if(!TextUtils.isEmpty(search_query)) {
                mBorrowerCursor = LoanOffice.getInstance(getContext()).getBorrowers(search_query);
            }else {
                mBorrowerCursor = LoanOffice.getInstance(getContext()).getBorrowers();

            }
            for (mBorrowerCursor.moveToFirst(); !mBorrowerCursor.isAfterLast(); mBorrowerCursor.moveToNext()) {
                // The Cursor is now set to the right position
                borrowerList.add(mBorrowerCursor.getBorrower());
            }
            if (mBorrowerCursor != null && !mBorrowerCursor.isClosed()) {
                mBorrowerCursor.close();
            }
            return  borrowerList;
        }
    }



    private class BorrowerListAdapter extends RecyclerView.Adapter<BorrowerListAdapter.ClientViewHolder> {

        public LayoutInflater mInflater;
        public List<Borrower> borrowerList;

        public BorrowerListAdapter(Context context, List<Borrower> borrowers) {
            //super(context, cursor, 0);
          borrowerList = borrowers;

            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getItemCount() {
            return borrowerList.size();
        }

        @Override
        public void onBindViewHolder(ClientViewHolder clientViewHolder, int i) {

            Borrower borrower = borrowerList.get(i);
            clientViewHolder.nameTextView.setText(borrower.getName());
            clientViewHolder.balannceTV.setText(getActivity().getString(R.string.owes_you));

            if (borrower.getContactUri() != null) {
                clientViewHolder.mBadge.assignContactUri(borrower.getContactUri());
            }
            if (borrower.getThumbnailUri() != null ) {
                if ( !borrower.getThumbnailUri().equalsIgnoreCase("Uri null")){
                    clientViewHolder.mBadge.setImageBitmap(Utils.loadContactPhotoThumbnail(borrower.getThumbnailUri()));
                }
            }

            //calculate principal amount 1. amount loaned
            //2. interst and 3. number of periods
            float principal = LoanOffice.getInstance(getActivity()).
                    getAmountDue(borrower.getLoanAmount(),borrower.getInterestRate(),1/*borrower.getLoanPeriod*/);
            //calculate amount due by subtracting what is paid from the principal amount
            float amountDue = principal - borrower.getAmountPaid();

            clientViewHolder.amountTextView.setText("$"+Float.toString(amountDue));

            //remove imagge fom image view
            clientViewHolder.statusImageView.setVisibility(View.GONE);


            if (!borrower.paid() && DateUtils.hasSetTimePassed(borrower.getDateDue())){
                //(((ImageView) view.getTag(R.id.imageView1)).getBackground()== null)
                clientViewHolder.statusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_alert));
                clientViewHolder.statusImageView.setVisibility(View.VISIBLE);

            }
            //remove stike through from textviews
            clientViewHolder.amountTextView.setPaintFlags(clientViewHolder.amountTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            clientViewHolder.balannceTV.setPaintFlags(clientViewHolder.balannceTV.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            clientViewHolder.nameTextView.setPaintFlags(clientViewHolder.nameTextView.getPaintFlags()  & ~Paint.STRIKE_THRU_TEXT_FLAG);

            if ( borrower.paid()){
                clientViewHolder.statusImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                clientViewHolder.statusImageView.setVisibility(View.VISIBLE);
                clientViewHolder.nameTextView.setPaintFlags(clientViewHolder.nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                clientViewHolder.amountTextView.setText("$"+Float.toString(principal));
                clientViewHolder.amountTextView.setPaintFlags(clientViewHolder.amountTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                clientViewHolder.balannceTV.setPaintFlags(clientViewHolder.balannceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

        @Override
        public ClientViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.card_list_item, viewGroup, false);

            return new ClientViewHolder(itemView);
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
                        long id = borrowerList.get(viewPosition).getId();
                        switch (actionId){
                            case ID_DETAILS:
                                Intent intent = new Intent(getActivity(), BorrowersDetailActivity.class);
                                intent.putExtra(BorrowersDetailFragment.EXTRA_BORROWER_ID,id);
                                startActivityForResult(intent, 0);
                                break;
                            case ID_DELETE:
                                loanOffice.removeBorrower(id);
                                // restart the loader to get any new borrower available
                                getLoaderManager().restartLoader(0, null, BorrowersListFragment.this);
                                break;
                            case ID_PAID:
                                createYesNoDialog(id);
                                getLoaderManager().restartLoader(0, null, BorrowersListFragment.this);
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
        super.onResume();
        startListeningStorage();
        getActivity().setTitle(R.string.borrowers_pager_title);
        getLoaderManager().initLoader(0, null, this);

       // loadData();
        /*String strTotal = Float.toString(total/2);
        ((ActionBarActivity)getActivity()).getSupportActionBar().
                setSubtitle(getResources().
                        getString(R.string.action_bar_list_subtitle) + ":" + strTotal);*/
       // getActivity().getActionBar().setSubtitle(R.string.action_bar_list_subtitle + ":" + strTotal);


    }

    @Override
    public void onPause() {
        super.onPause();
        stopListeningStorage();
    }

    private void startListeningStorage() {
     //   storage.addStorageListener(this);
    }

    private void stopListeningStorage() {
     //   storage.removeStorageListener(this);
    }

    /*@Override
    public void onContentChanged() {
        getLoaderManager().restartLoader(0, null, this);;
       // updateUiFromBackgroundThread();
    }*/

    @Override
    public void onDestroyView() {
       // mBorrowersCursor.close();
        super.onDestroyView();
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

        public static SearchView getSearchView(Context context/* String strHint*/) {
            SearchView searchView = new SearchView(context);
           // searchView.setQueryHint(strHint);
            searchView.setFocusable(true);
            searchView.setFocusableInTouchMode(true);
            searchView.requestFocus();
            searchView.requestFocusFromTouch();
            SearchAutoComplete searchText = (SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
            searchText.setTextColor(context.getResources().getColor(android.R.color.white));

            return searchView;
        }


        public static SearchView getSearchView(Context context, int strHintRes) {
            return getSearchView(context);
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_borrower_list,menu);

        /*SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);*/

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

            search_query = null;
            //mListView.clearTextFilter();
        }
        else
        {
           // getActivity().getActionBar().setSubtitle(R.string.action_bar_list_subtitle_search + newText);
            search_query = newText;
           // mListView.setFilterText(newText.toString());
        }
        getLoaderManager().restartLoader(0, null, BorrowersListFragment.this);

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
            case R.id.menu_item_add_borrower:
                Intent intent = new Intent(getActivity(), BorrowersDetailActivity.class);
                intent.putExtra(EXTRA_NEW_BORROWER,true);
                intent.putExtra(BorrowersDetailFragment.EXTRA_BORROWER_ID, -1);
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

    public void updateUI(){
        // restart the loader to get any new borrower available
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //loadData();
        // restart the loader to get any new borrower available
        getLoaderManager().restartLoader(0, null, this);
    }


   /* @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_borrowers_lenders_list_context,menu);

        //menu.setHeaderTitle("Number of items selsected");
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        BorrowerListAdapter adapter = (BorrowerListAdapter)recList.getAdapter();
        long id = adapter.getItemId(position);

        switch (item.getItemId()) {
            case R.id.blist_menu_item_delete:
                loanOffice.removeBorrower(id);
                // restart the loader to get any new borrower available
                getLoaderManager().restartLoader(0, null, this);
                //loadData();
                return true;
            case R.id.blist_menu_item_mark_paid:
                createYesNoDialog(id);
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
                Borrower b =loanOffice.getBorrower(ID);
                float amountDue = b.getLoanAmount();
                b.setAmountPaid(amountDue);
                b.setIsPaid(true);
                loanOffice.updateBorrower(b);
                getLoaderManager().restartLoader(0, null, BorrowersListFragment.this);

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

    public void showOverFlowTutorial(){
        final Toolbar toolbar = new MenuActivity().toolbar;
        final Target viewTarget = new Target() {
            @Override
            public Point getPoint() {
                View navIcon = null;
                for (int i = 0; i < toolbar.getChildCount(); i++)
                {
                    View child = toolbar.getChildAt(i);
                    if (ImageButton.class.isInstance(child))
                    {
                        navIcon = child;
                        break;
                    }
                }

                if (navIcon != null)
                    return new ViewTarget(navIcon).getPoint();
                else
                    return new ViewTarget(toolbar).getPoint();
            }
        };

        new ShowcaseView.Builder(getActivity())
                .setTarget(viewTarget)
                .setContentTitle("Home icon")
                .setContentText("Press to access menu")
                .hideOnTouchOutside()
                .setStyle(R.style.AppBaseTheme)
                .doNotBlockTouches()
                .build();


    }
}