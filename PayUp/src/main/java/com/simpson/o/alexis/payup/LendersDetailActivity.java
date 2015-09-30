package com.simpson.o.alexis.payup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by Alexis on 7/1/2014.
 */
public class LendersDetailActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private Fragment createFragment(){

        long id = (long)getIntent().getLongExtra(LendersDetailFragment.EXTRA_LENDER_DETAIL_ID,-1);
        return LendersDetailFragment.newInstance(id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Refactor activity_borrowers_detail to just activity detail
        // and detail container
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
