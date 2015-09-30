package com.simpson.o.alexis.payup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.appbrain.AppBrain;
import com.dropbox.sync.android.DbxDatastore;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.simpson.o.alexis.payup.Utils.ConnectivityUtils;
import com.simpson.o.alexis.payup.dialogs.DropboxAccountLinkingDialog;
import com.simpson.o.alexis.payup.dropbox.DropboxHelper;
import com.simpson.o.alexis.payup.enums.SortOrder;
import com.simpson.o.alexis.payup.storage.AppStorage;
import com.simpson.o.alexis.payup.storage.AppStorageListener;
import com.simpson.o.alexis.payup.storage.Storage;
import com.simpson.o.alexis.payup.storage.StorageDataTransfer;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;
import com.startapp.android.publish.StartAppAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created by alexi_000 on 14/12/2014.
 */
public class MenuActivity extends ActionBarActivity implements View.OnClickListener,
        AppStorageListener {

    private ResideMenu resideMenu;
    private MenuActivity mContext;
    private ResideMenuItem itemBorrowers;
    private ResideMenuItem itemLenders;
    private ResideMenuItem itemSettings;

    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private static final String  developerID = "102835337";
    private static final String appId = "208586418";
    private StartAppAd startAppAd;
    public Toolbar toolbar ;

    private static final int RESULT_DROPBOX_LINK = DropboxHelper.REQUEST_LINK_TO_DBX;
    private static final AppStorage storage = LoanOffice.getInstance(PayUpApplication.getContext()).getStorage();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            StartAppAd.init(this, developerID, appId);
        }
      //   AppBrain.init(this);
        // StartAppSearch.init(this, "<Your Developer Id>", "<Your App ID>");
        setContentView(R.layout.reside_main);
        toolbar = (Toolbar)findViewById(R.id.app_bar);
      //  toolbar.setLogo(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_navigation_menu);


        startAppAd = new StartAppAd(this);
        mContext = this;
        setUpMenu();
        if( savedInstanceState == null )
            changeFragment(new BorrowersListFragment());
        setDefaultSortOder();
    }

    private void setUpMenu() {

        // attach to current activity;
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
       // resideMenu.setMenuListener(menuListener);
        //valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip.
        resideMenu.setScaleValue(0.6f);

        // create menu items;
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        itemBorrowers = new ResideMenuItem(this,navMenuIcons.getResourceId(0, -1), navMenuTitles[0]);
        itemLenders = new ResideMenuItem(this, navMenuIcons.getResourceId(1, -1),  navMenuTitles[1]);
        itemSettings = new ResideMenuItem(this, navMenuIcons.getResourceId(2, -1), navMenuTitles[2]);

        navMenuIcons.recycle();

        itemBorrowers.setOnClickListener(this);
        itemLenders.setOnClickListener(this);
        itemSettings.setOnClickListener(this);

        resideMenu.addMenuItem(itemBorrowers, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemLenders, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemSettings, ResideMenu.DIRECTION_LEFT);

        // You can disable a direction by setting ->
         resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

       /* findViewById(R.id.title_bar_left_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
        findViewById(R.id.title_bar_right_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_RIGHT);
            }
        });*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {

        if (view == itemBorrowers){
            changeFragment( new BorrowersListFragment());
        }else if (view == itemLenders){
            changeFragment(new LendersListFragment());
        }else if (view == itemSettings){
            changeFragment(new PayUpPreferences());
        }

        resideMenu.closeMenu();
    }

   /* private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
            Toast.makeText(mContext, "Menu is opened!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void closeMenu() {
            Toast.makeText(mContext, "Menu is closed!", Toast.LENGTH_SHORT).show();
        }
    };*/

    private void changeFragment(Fragment targetFragment){
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    // What good method is to access resideMenu？
    public ResideMenu getResideMenu(){
        return resideMenu;
    }

    public void tryLinkDropboxAccount() {
        DropboxHelper.tryLinkAccountFromActivity(this);
    }

    public void performDropboxAction() {
        if (ConnectivityUtils.isNetworkConnected()) {
            if (Storage.getCurrentStorageType() == Storage.Type.Dropbox) {
                storage.sync();
                Toast.makeText(this, R.string.action_dropbox_refresh_toast, Toast.LENGTH_SHORT).show();
                //EventTracker.track(Event.DropboxSyncManual);
            } else {
                final boolean dataTransferStarted = storageTypeChanged();
                if (!dataTransferStarted) {
                    DropboxAccountLinkingDialog.show(getFragmentManager());
                }
            }
        } else {
            Toast.makeText(this, R.string.no_connection_toast, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean storageTypeChanged() {
        boolean changeStorage =
                Storage.getCurrentStorageType() == Storage.Type.Database &&
                        DropboxHelper.hasLinkedAccount();

        if (changeStorage) {
            PayUpApplication.executeInBackground(new Runnable() {
                @Override
                public void run() {
                    StorageDataTransfer.changeStorageType(Storage.Type.Dropbox, true);
                    DropboxHelper.initSynchronization();
                    MenuActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            invalidateOptionsMenu();
                        }
                    });
                }
            });
        }

        return changeStorage;
    }

    private boolean transferData(){
        //if dropbox has synced local and remote databases transfer data
        StorageDataTransfer.transferDataFromSqlToDropbox(true);
        //StorageDataTransfer.changeStorageType(Storage.Type.Dropbox, true);
        return true;
    }

    @Override
    public void onDatastoreStatusChange(DbxDatastore dbxDatastore) {
        // dbxDatastore.close();
        if (Storage.getCurrentStorageType() == Storage.Type.Dropbox){

            if (storage.getSyncStatus().hasIncoming) {
                storage.sync();
                /*PayUpApplication.executeInBackground( new Runnable() {
                    @Override
                    public void run() {
                     // transferData();


                    }
                });*/

            }
        }


    }


    public interface PreferenceActivityCallbacks {
        // ...
        public void onActivityResult(int requestCode, int resultCode, Intent data);
        // ...
    }
    public void setDefaultSortOder(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        SortOrder sortorder = SortOrder.name;
        int order = prefs.getInt("PREFS_KEY_SORT_ORDER", sortorder.ordinal());
        switch (order){
            case 0:
                storage.setSortOrder(com.simpson.o.alexis.payup.enums.SortOrder.name);
                break;
            case 1:
                storage.setSortOrder(com.simpson.o.alexis.payup.enums.SortOrder.dateDue);
                break;
            case 2:
                storage.setSortOrder(com.simpson.o.alexis.payup.enums.SortOrder.dateCreated);
                break;
            default:
                storage.setSortOrder(com.simpson.o.alexis.payup.enums.SortOrder.name);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        switch (requestCode) {
            case RESULT_DROPBOX_LINK:
                DropboxHelper.onAccountLinkActivityResult(this, requestCode, resultCode, data);
                storageTypeChanged();
                break;
        }
        Bundle bundle = data.getExtras();

        if (bundle != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            String ringtoneString = "";

            if (uri != null) {
                ringtoneString = uri.toString();
            }
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPrefs.edit().putString("KEY_RINGTONE_PREFS", ringtoneString).commit();
        }


    }

    @Override
    public void onBackPressed() {
        Random random = new Random();
        int chance = random.nextInt(2);
        if (chance == 0) {
            startAppAd.onBackPressed();
        }else{

          //  AppBrain.getAds().showOfferWall(this);
        }
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        startAppAd.onPause();
        storage.removeStorageListener(this);
        //storage.close();
    }
    @Override
    public void onResume() {
        super.onResume();
        startAppAd.onResume();
        storage.sync();
        storage.addStorageListener(this);
        //mStore = mDatastoreManager.openDefaultDatastore();
        // mStore.addSyncStatusListener(this);
    }


}

