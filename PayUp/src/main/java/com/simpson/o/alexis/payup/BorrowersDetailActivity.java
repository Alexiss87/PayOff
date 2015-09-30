package com.simpson.o.alexis.payup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by Alexis on 6/19/2014.
 */
public class BorrowersDetailActivity extends ActionBarActivity {

    /*Developer ID 102835337
    App ID 208586418*/

   private Toolbar toolbar;
   private Fragment createFragment(){

       long id = getIntent().getLongExtra(BorrowersDetailFragment.EXTRA_BORROWER_ID,-1);
       return BorrowersDetailFragment.newInstance(id);
   }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.detail_container);

        if (fragment == null) {
            fragment = createFragment();
            manager.beginTransaction()
                    .add(R.id.detail_container, fragment)
                    .commit();
        }
    }

}
