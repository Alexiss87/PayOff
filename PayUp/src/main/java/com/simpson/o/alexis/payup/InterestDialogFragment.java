package com.simpson.o.alexis.payup;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Alexis on 8/13/2014.
 */
public class InterestDialogFragment extends DialogFragment {
    public static final String EXTRA_INTEREST = "payup.INTEREST";
    public static final String EXTRA_LOANPERIOD = "criminalintent.LOANPERIOD";
    private float mInterest;
    private int mNumbOfperiods;

    public static InterestDialogFragment newInstance(float interest,int loanPeriod) {
        Bundle args = new Bundle();
        args.putFloat(EXTRA_INTEREST, interest);
        args.putInt(EXTRA_LOANPERIOD, loanPeriod);

        InterestDialogFragment fragment = new InterestDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mInterest = getArguments().getFloat(EXTRA_INTEREST);
        mNumbOfperiods = getArguments().getInt(EXTRA_LOANPERIOD);

        View v = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_interest, null);

        final EditText interestEditText = (EditText)v.findViewById(R.id.dialog_interest_editText);
        final EditText durationEditText = (EditText)v.findViewById(R.id.dialog_numb_of_weeks_Et);

        interestEditText.setText(Float.toString(mInterest));
        durationEditText.setText(Integer.toString(mNumbOfperiods));

        interestEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Float.parseFloat(interestEditText.getText().toString()) == 0.0f) {interestEditText.setText("");}

            }
        });



//R.string.inetrest_picker_title
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.interest_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (interestEditText.getText().toString().length() == 0) {
                            mInterest = 0.0f;
                        } else {
                            mInterest = Float.parseFloat(interestEditText.getText().toString());
                        }
                        if (durationEditText.getText().toString().length() == 0){
                            mNumbOfperiods =1;
                        }else {
                        mNumbOfperiods = Integer.parseInt(durationEditText.getText().toString());
                        }
                        sendResult(Activity.RESULT_OK);
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .create();
    }
    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;

        Intent i = new Intent();
        i.putExtra(EXTRA_INTEREST, mInterest);
        i.putExtra(EXTRA_LOANPERIOD,mNumbOfperiods);

        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, i);
    }
}


