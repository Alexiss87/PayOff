package com.simpson.o.alexis.payup;

import android.content.pm.PackageInfo;
import android.os.Bundle;
//import android.preference.PreferenceFragment;
import android.support.v4.preference.PreferenceFragment;

import java.util.List;


public class PayUpPreferences extends PreferenceFragment implements MenuActivity.PreferenceActivityCallbacks {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.payup_preferences);
        getActivity().setTitle(R.string.action_settings);


      /*  Preference myPref = findPreference( "Rate app for support" );
        myPref.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick( Preference pref )
            {
                // Run your custom method

                if(isApplicationIstalledByPackageName ("com.slideme.sam.manager")){
                    // sam is installed, search SAM for bundleid;
                    // Create your market link intent to launch
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    //replace with your SlideME username
                    intent.setData(Uri.parse("sam://search?pub:{AlexisSimpson}"));
                    startActivity(intent);
                } else {
                    //sam is not installed, go to application details on slideme.org
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    //replace below url with the link to the application on slideme.org
                    intent.setData(Uri.parse("http://slideme.org/app/com.simpson.o.alexis.payup"));
                    startActivity(intent);
                }

                return true;
            }
        } );*/


    }


    public boolean isApplicationIstalledByPackageName(String packageName) {
        List<PackageInfo> packages =getActivity().getPackageManager().getInstalledPackages(0);
        if (packages != null && packageName != null) {
            for (PackageInfo packageInfo : packages) {
                if (packageName.equals(packageInfo.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
