package com.simpson.o.alexis.payup.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.simpson.o.alexis.payup.R;
import com.simpson.o.alexis.payup.Utils.Utils;

import java.util.List;

/**
 * Created by alexi_000 on 25/11/2014.
 */
public class AboutDialog extends DialogFragment {

        private static final String FRAGMENT_TAG = AboutDialog.class.getSimpleName();

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.app_name)
                .setView(createView())
                .setNegativeButton(R.string.common_close, null)
                .create();
    }

    public View createView() {
        final Context context = getActivity();
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.about_dialog, null);

        // version
        final String appVersion = Utils.getVersionName();
        ((TextView) view.findViewById(R.id.version))
                .setText(context.getString(R.string.about_version, appVersion));

        // google play
        view.findViewById(R.id.google_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGooglePlay();
            }
        });


        // email
        view.findViewById(R.id.contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });

        return view;
    }

    private void openGooglePlay() {
        /*final Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=" + getActivity().getPackageName()));
        startActivity(browserIntent);*/
        if (isApplicationIstalledByPackageName("com.slideme.sam.manager")) {
            // sam is installed, search SAM for bundleid;
            // Create your market link intent to launch
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //replace with your SlideME username
            intent.setData(Uri.parse("sam://details?id="+getActivity().getPackageName()));
            startActivity(intent);
        } else {
            //sam is not installed, go to application details on slideme.org
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //replace below url with the link to the application on slideme.org
            intent.setData(Uri.parse("http://slideme.org/app/com.simpson.o.alexis.payup"));
            startActivity(intent);
        }
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


    private void sendFeedback() {
        final Context context = getActivity();
        final Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", "alexis.o.simspson@gmail.com", null));
        final String subject = context.getString(R.string.app_name) + " feedback";
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, getTextForFeedback(context));
        startActivity(Intent.createChooser(emailIntent, context.getString(R.string.about_contact)));
    }

    private String getTextForFeedback(Context context) {
        return context.getString(R.string.about_contact_do_not_remove) + Utils.getDeviceInformation();
    }

    public static void show(FragmentManager fm) {
        final AboutDialog dialog = new AboutDialog();
        dialog.show(fm, FRAGMENT_TAG);
    }

}
