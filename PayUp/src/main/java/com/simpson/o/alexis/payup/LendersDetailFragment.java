package com.simpson.o.alexis.payup;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.app.backup.BackupManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.QuickContactBadge;
import android.widget.TimePicker;
import android.widget.Toast;

import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;
import com.doomonafireball.betterpickers.timepicker.TimePickerDialogFragment;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import com.simpson.o.alexis.payup.Utils.DateUtils;
import com.simpson.o.alexis.payup.Utils.Utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Alexis on 6/5/2014.
 */
public class LendersDetailFragment extends Fragment {

    //private CheckBox paidCheckBox;
    private EditText paidEditText;
    private QuickContactBadge mBadge;
    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText amountEditText;
    private EditText interestEditText;
    private EditText dateLoanedEditText;
    private EditText dateDueEditText;
    private EditText amountDueEditText;
    private EditText timePickerEditText;
    private Button selectContactButton;
    private final String type = "Lender";
    static final String EXTRA_LENDER_DETAIL_ID = "com.simpson.o.alexis.payup.LendersDetailFragment";
    private static final String EXTRA_TIME = "lender notification time";
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("EEE MMM dd");
    private SimpleDateFormat mTimeFormat = new SimpleDateFormat("hh:mm:aa");
    private Boolean isNewLender = false;
    private Lender mLender;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_TIME = 2;
    private static final int REQUEST_INTEREST_INFO = 0;
    private long mRowId;
    private Boolean paid = false;
    private Calendar mDateLoaned;
    private Calendar mDateDue;
    private Calendar mTimeDue;
    /////////////////////////////////////////////////
    private int mIdColumn;
    private int mLookUpKeyColumn;
    private Uri mContactUri;
    private int numberOfweeks = 1;
    // The column in which to find the thumbnail ID
    int mThumbnailColumn;
    /*
     * The thumbnail URI, expressed as a String.
     * Contacts Provider stores URIs as String values.
     */
    private String mThumbnailUri;
    private NotificationManager notificationManager;
    private SharedPreferences sharedPrefs;

    public static LendersDetailFragment newInstance(long detailID) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_LENDER_DETAIL_ID, detailID);
        LendersDetailFragment fragment = new LendersDetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.lender_detail_fragment_title);
        setRetainInstance(true);
        notificationManager = (NotificationManager)getActivity().
                getSystemService(Context.NOTIFICATION_SERVICE);

        // check for a lender ID as an argument, and find the borrower
        Bundle args = getArguments();
        if (args != null) {
            mRowId = args.getLong(LendersDetailFragment.EXTRA_LENDER_DETAIL_ID, -1);
            if (mRowId != -1) {
                mLender = LoanOffice.getInstance(getActivity()).getLender(mRowId);

            }
        }
        setHasOptionsMenu(true);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.lenders_detail_fragment, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //if (NavUtils.getParentActivityName(getActivity()) != null) {
          //  getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            // }
        }

        mBadge = (QuickContactBadge) v.findViewById(R.id.quickContactBadge_Lender);
        nameEditText = (EditText) v.findViewById(R.id.lenders_nameEt);
        phoneEditText = (EditText) v.findViewById(R.id.lenders_phone_editText);
        amountEditText = (EditText) v.findViewById(R.id.lenders_amount_editText);
        interestEditText = (EditText) v.findViewById(R.id.lenders_interest_editText);
        dateLoanedEditText = (EditText) v.findViewById(R.id.lenders_dateLoaned_editText);
        dateDueEditText = (EditText) v.findViewById(R.id.lenders_date_due_editText);
        amountDueEditText = (EditText) v.findViewById(R.id.lenders_amount_due_editText);
        timePickerEditText = (EditText) v.findViewById(R.id.lenders_Notification_Time_editText);
        selectContactButton = (Button) v.findViewById(R.id.lenders_selectContactButton);
        paidEditText =(EditText)v.findViewById(R.id.lenders_amount_paid_editText);
        //paidCheckBox = (CheckBox) v.findViewById(R.id.lenders_paid_checkBox);

        if (mLender != null) {
            mDateDue = mLender.getDateDue();
            mTimeDue = mLender.getDateDue();
            paid = mLender.paid();
            notificationManager.cancel((int)mLender.getId());
        } else {
           mTimeDue = Calendar.getInstance();
           mDateDue = Calendar.getInstance();
            /*mTimeDue.set(Calendar.HOUR_OF_DAY, 8);
            mTimeDue.set(Calendar.MINUTE, 00);*/
        }
        // / Force the keyboard to close
       /* InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);*/

        UpdateUI();
        registerListeners();

        return v;
    }


    private void UpdateUI() {

        if (mRowId != -1) {
            nameEditText.setText(mLender.getName());
            phoneEditText.setText(mLender.getPhoneNumber());
            amountEditText.setText(Float.toString(mLender.getLoanAmount()));
            interestEditText.setText(Float.toString(mLender.getInterestRate()));

            if (DateUtils.isToday(mLender.getDateLoaned())){
                dateLoanedEditText.setText(R.string.today);
            }else {
                dateLoanedEditText.setText(mDateFormat.format(mLender.getDateLoaned().getTime()));
            }

            if (DateUtils.isToday(mLender.getDateDue())){
                dateDueEditText.setText(R.string.today);
            }else {
                dateDueEditText.setText(mDateFormat.format(mLender.getDateDue().getTime()));
            }


            timePickerEditText.setText(mTimeFormat.format(mLender.getDateDue().getTime()));
            if (mLender.getContactUri() != null) {
                mBadge.assignContactUri(mLender.getContactUri());
            }
            if (mLender.getThumbnailUri() != null ) {
                if (! mLender.getThumbnailUri().equalsIgnoreCase("Uri null")) {
                    mBadge.setImageBitmap(Utils.loadContactPhotoThumbnail(mLender.getThumbnailUri()));
                }
            }
            paidEditText.setText(Float.toString(mLender.getAmountPaid()));
           /* if (mLender.paid()) {
               // paidCheckBox.setChecked(true);
            } else {
              //  paidCheckBox.setChecked(false);
            }*/
            UpdateBalanceDue();
        } else {

            Calendar c = Calendar.getInstance();
            dateLoanedEditText.setText(R.string.today/*mDateFormat.format(c.getTime())*/);
            dateDueEditText.setText(R.string.today/*mDateFormat.format(c.getTime())*/);
            String timeString = sharedPrefs.getString("KEY_NOTIFICATION_TIME", "08:00 AM");
            timePickerEditText.setText(time24to12(timeString));
            Date inDate = toDate(timeString);
            mTimeDue.setTime(inDate);
            mDateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
            mDateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));
        }
    }

    private void UpdateBalanceDue() {
        //calculate principal amount 1. amount loaned
        //2. interst and 3. number of periods
        float principal = LoanOffice.getInstance(getActivity()).
                getAmountDue(mLender.getLoanAmount(),mLender.getInterestRate(),numberOfweeks);
        //calculate amount due by subtracting what is paid from the principal amount
        float amountDue = principal - mLender.getAmountPaid();

        if (amountDue <= 0){
            paid = true;
        }else {
            paid = false;
        }
        amountDueEditText.setText("$"+Float.toString(amountDue));

    }


    public static String time24to12(String inTime) {
        Date inDate = toDate(inTime);
        if(inDate != null) {
            java.text.DateFormat outTimeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return outTimeFormat.format(inDate);
        } else {
            return inTime;
        }
    }

    public static Date toDate(String inTime) {
        try {
            java.text.DateFormat inTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return inTimeFormat.parse(inTime);
        } catch(ParseException e) {
            return null;
        }
    }

    public boolean hasSetTimePassed() {
        long prevEventTime = System.currentTimeMillis();
        if (mDateDue == null ) {
            throw new IllegalArgumentException("The date must not be null");
        }

        if (prevEventTime >= mDateDue.getTimeInMillis()){
            return true;
        }else{
            return false;
        }

    }

    private void registerListeners() {


        timePickerEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = Integer.parseInt(sharedPrefs.getString("KEY_PICKER_TYPE","2"));
                if (type == 2 ) {
                    showBetterPickerDialog();
                }else if (type == 1) {
                    showTimePickerDialog();
                }
            }
        });

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mLender!= null){mLender.setName(s.toString());}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (mLender!= null) { mLender.setPhoneNumber(s.toString());}
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        paidEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mLender!=null) {
                    String temp = s.toString();
                    if (temp == null || temp.isEmpty()) {
                        mLender.setAmountPaid(0.00f);
                    } else {

                        mLender.setAmountPaid(Float.parseFloat(temp));
                    }
                    UpdateBalanceDue();
                }else{
//                    //update amount paid and balance textfields without lender object
                    String temp = s.toString();
                    float paidAmount ;
                    float interestRate;
                    float loanAmount;
                    //get interest from edittext
                    if (interestEditText.getText().toString().length() == 0) {
                        interestRate = 0.00f;
                    }else{interestRate = Float.parseFloat(interestEditText.getText().toString());
                    }
                   //get amount paid from paid editeditex
                    if (temp == null || temp.isEmpty()) {paidAmount = 0.00f;
                    } else {paidAmount =  Float.parseFloat(temp);
                    }
                    //get amount loaned from amount editText
                    if (amountEditText.getText().toString().length() == 0){
                        loanAmount = 0.00f;
                    }else{loanAmount = Float.parseFloat(amountEditText.getText().toString()); }

                    //calculate principal amount 1. amount loaned
                    //2. interst and 3. number of periods
                    float principal = LoanOffice.getInstance(getActivity()).
                            getAmountDue(loanAmount,interestRate,numberOfweeks);
                    //calculate amount due by subtracting what is paid from the principal amount
                    float amountDue = principal - paidAmount;

                    if (amountDue <= 0){
                        paid = true;
                    }else {
                        paid = false;
                    }
                    amountDueEditText.setText("$"+Float.toString(amountDue));


                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        paidEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //can't remeber why i did this but it throws an exception????
               // if (Float.parseFloat(paidEditText.getText().toString()) == 0.0f) {paidEditText.setText("");}

            }
        });

        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mLender!= null) {
                    String temp = s.toString();
                    if (temp == null || temp.isEmpty()) {
                        mLender.setLoanAmount(0.00f);
                    } else {

                        mLender.setLoanAmount(Float.parseFloat(temp));
                    }
                    UpdateBalanceDue();
                }else{
                    //update balance textfield without lender object
                    String temp = s.toString();
                    float amount ;
                    float interestRate;
                    float paidAmount;

                    if (interestEditText.getText().toString().length() == 0) {
                        interestRate = 0.00f;
                    }else{
                        interestRate = Float.parseFloat(interestEditText.getText().toString());
                    }

                    if (temp == null || temp.isEmpty()) {
                        amount = 0.00f;
                    } else {

                        amount =  Float.parseFloat(temp);
                    }
                    //get amount paid from edittexxt
                    if (paidEditText.getText().toString().length() == 0) {
                        paidAmount = 0.00f;
                    }else{
                        paidAmount = Float.parseFloat(paidEditText.getText().toString());
                    }

                    //calculate principal amount 1. amount loaned
                    //2. interst and 3. number of periods
                    float principal = LoanOffice.getInstance(getActivity()).
                            getAmountDue(amount,interestRate,numberOfweeks);
                    //calculate amount due by subtracting what is paid from the principal amount
                    float amountDue = principal - paidAmount;

                    /*float principal = amount - paidAmount;
                    float amountDue = LoanOffice.getInstance(getActivity()).
                            getAmountDue(principal,interestRate,numberOfweeks);//number of periods*/

                    amountDueEditText.setText("$"+Float.toString(amountDue));
                    if (amountDue <= 0){
                        paid = true;
                    }else {
                        paid = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        interestEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mLender!= null) {
                    String temp = s.toString();
                    if (temp == null || temp.isEmpty()) {
                        mLender.setInterestRate(0f);
                    } else {

                        mLender.setInterestRate(Float.parseFloat(temp));
                    }
                    UpdateBalanceDue();
                }else {
                    //update balance textfield without lender object
                    String temp = s.toString();
                    float amountBorrowed ;
                    float interestRate;
                    float paidAmount;

                    if(amountEditText.getText().toString().length() == 0){
                        amountBorrowed = 0.00f;
                    }else {
                        amountBorrowed = Float.parseFloat(amountEditText.getText().toString());
                    }


                    if (temp == null || temp.isEmpty()) {
                        interestRate = 0.00f;
                    } else {

                        interestRate =  Float.parseFloat(temp);
                    }
                    //get amount paid from edittexxt
                    if (paidEditText.getText().toString().length() == 0) {
                        paidAmount = 0.00f;
                    }else{
                        paidAmount = Float.parseFloat(paidEditText.getText().toString());
                    }
                    //calculate principal amount 1. amount loaned
                    //2. interst and 3. number of periods
                    float principal = LoanOffice.getInstance(getActivity()).
                            getAmountDue(amountBorrowed,interestRate,numberOfweeks);
                    //calculate amount due by subtracting what is paid from the principal amount
                    float amountDue = principal - paidAmount;
                    amountDueEditText.setText("$"+Float.toString(amountDue));

                    if (amountDue <= 0){
                        paid = true;
                    }else {
                        paid = false;
                    }
                }
            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        interestEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Float interestRate ;
                if (interestEditText.getText().toString().length() == 0) {
                    interestRate = 0.00f;
                }else{
                    interestRate = Float.parseFloat(interestEditText.getText().toString());
                }
                FragmentManager fm = getActivity()
                        .getSupportFragmentManager();
                InterestDialogFragment dialog = InterestDialogFragment
                        .newInstance(interestRate,numberOfweeks);
                dialog.setTargetFragment(LendersDetailFragment.this, REQUEST_INTEREST_INFO);
                dialog.show(fm,"payUp.INTEREST_RATE");
            }
        });

        dateLoanedEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Resources r = getResources();
                if (mRowId!= -1) {
                    mDateLoaned = mLender.getDateLoaned();
                }else{
                    mDateLoaned = Calendar.getInstance();
                }

                final CaldroidFragment dialogCaldroidFragment = CaldroidFragment.
                        newInstance(r.getString(R.string.calendar_title_dateBorrowed), (mDateLoaned.get(Calendar.MONTH))+1,
                                mDateLoaned.get(Calendar.YEAR));
                dialogCaldroidFragment.setBackgroundResourceForDate(R.color.holo_green_light,
                        mDateLoaned.getTime());

                dialogCaldroidFragment.setCaldroidListener(new CaldroidListener() {
                    @Override
                    public void onSelectDate(Date date, View view) {
                        mDateLoaned.setTime(date);

                        if (DateUtils.isToday(mDateLoaned)){
                            dateLoanedEditText.setText(R.string.today);
                        }else{
                            dateLoanedEditText.setText(mDateFormat.format(mDateLoaned.getTime()));
                        }
                        //dateLoanedEditText.setText(mDateFormat.format(mDateLoaned.getTime()));
                        dialogCaldroidFragment.dismiss();

                    }
                });

                dialogCaldroidFragment.show(getFragmentManager(),"TAG");


            }
        });
        dateDueEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources r = getResources();
                if (mRowId!= -1) {
                    mDateDue = mLender.getDateDue();
                }else {
                    mDateDue= Calendar.getInstance();
                }

                final CaldroidFragment dialogCaldroidFragment = CaldroidFragment.
                        newInstance(r.getString(R.string.calendar_title_dateDue), (mDateDue.get(Calendar.MONTH))+1,
                                mDateDue.get(Calendar.YEAR));

                dialogCaldroidFragment.setBackgroundResourceForDate(R.color.holo_red_light,
                        mDateDue.getTime());
            //    dateDueEditText.setTextColor(r.getColor(R.color.holo_red_light));

                dialogCaldroidFragment.setCaldroidListener(new CaldroidListener() {
                    @Override
                    public void onSelectDate(Date date, View view) {

                        mDateDue.setTime(date);
                        if (DateUtils.isToday(mDateDue)){
                            dateDueEditText.setText(R.string.today);
                        }else{
                            dateDueEditText.setText(mDateFormat.format(mDateDue.getTime()));
                        }
                       // dateDueEditText.setText(mDateFormat.format(mDateDue.getTime()));
                        mDateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
                        mDateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));
                        timePickerEditText.setText(mTimeFormat.format(mDateDue.getTime()));
                        if (mLender !=null) {
                            Toast.makeText(getActivity(), R.string.time_reset, Toast.LENGTH_SHORT).show();
                        }
                        dialogCaldroidFragment.dismiss();

                    }
                });

               // ReminderService.setReminder(getActivity(),mBorrower,mBorrower.getDateDue());

                dialogCaldroidFragment.show(getFragmentManager(),"TAG");

            }
        });

        selectContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Retrieve contact information
                Intent i = new Intent(Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, REQUEST_CONTACT);
            }
        });

    }

    private void saveState() {


        if (mRowId == -1) {
            String name = nameEditText.getText().toString();
            String phoneNumber = phoneEditText.getText().toString();
            float interestRate;
            float amountLoaned;
            float amountPaid;
            Calendar dateBorrowed;
            Calendar dateDue;


            if (interestEditText.getText().toString().length() == 0) {
                interestRate = 0.00f;
            }else{
                interestRate = Float.parseFloat(interestEditText.getText().toString());
            }

            if(amountEditText.getText().toString().length() == 0){
                amountLoaned = 0.00f;
            }else {
                amountLoaned = Float.parseFloat(amountEditText.getText().toString());
            }

            if(paidEditText.getText().toString().length() == 0){
                amountPaid = 0.00f;
            }else {
                amountPaid = Float.parseFloat(paidEditText.getText().toString());
            }

           /* if (amountDueEditText.getText().toString().length() ==0){
                amountDue = 0.00f;
            }else {

                float interest = amountLoaned *(interestRate/100);
                amountDue=(interest+amountLoaned);
                // amountDue = Float.parseFloat(amountDueEditText.getText().toString());*//*
            }*/

            if(mDateLoaned ==null){
                dateBorrowed = Calendar.getInstance();
            }else{dateBorrowed = mDateLoaned;}

            if (mDateDue == null){
                dateDue = Calendar.getInstance();
                dateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
                dateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));
            } else{
               mDateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
               mDateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));
                dateDue = mDateDue;
            }

            Toast.makeText(getActivity(),R.string.lender_saved ,Toast.LENGTH_SHORT).
                    show();

            long id = LoanOffice.getInstance(getActivity()).addLender(name,
                    phoneNumber,
                    interestRate,
                    numberOfweeks,
                    amountLoaned,
                    amountPaid,
                    dateBorrowed,
                    dateDue,
                    paid, mContactUri, mThumbnailUri);

            if (id > 0) {
                mRowId = id;
            }
            if (paid == false|| !hasSetTimePassed()) {
                new ReminderManager(getActivity()).setReminder(mRowId, dateDue, type);
            }

        } else {
            Toast.makeText(getActivity(),R.string.lender_updated ,Toast.LENGTH_SHORT).show();
            /// Lender should not be null at this point

            Calendar dateDue = Calendar.getInstance();
           if (mDateDue == null){
               // this should never be called too lazy to double check
                dateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
                dateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));
            }
            if (mDateDue != null){
                mDateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
                mDateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));
                dateDue = mDateDue;
            }
            mLender.setIsPaid(paid);
            mLender.setDateDue(dateDue);

            LoanOffice.getInstance(getActivity()).updateLender(mLender);
            if (mLender.paid() == false ||!hasSetTimePassed() ) {
                new ReminderManager(getActivity()).setReminder(mRowId, mLender.getDateDue(), type);
            }
        }
        BackupManager.dataChanged("com.simpson.o.alexis.payup");

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent i = getActivity().getIntent();
        isNewLender = i.getBooleanExtra("NEW LENDER",false);

        if (isNewLender) {
            // BEGIN_INCLUDE (inflate_set_custom_view)
            // Inflate a "Done/Cancel" custom action bar view.
            final LayoutInflater inflater = (LayoutInflater) ((ActionBarActivity) getActivity())
                    .getSupportActionBar().getThemedContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final View customActionBarView = inflater.inflate(
                    R.layout.actionbar_custom_view_done_discard, null);
            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // "Done"
                            //save if notification time has'nt already passed
                           /* float amountDue;
                            if (amountDueEditText.getText().toString().length() ==0){
                                amountDue = 0.0f;
                            }else {
                                String temp = amountDueEditText.getText().toString();
                                temp = temp.substring(temp.indexOf("$")+1);
                                amountDue = Float.parseFloat(temp);
                            }*/
                            if (!hasSetTimePassed() /*|| amountDue <=0*/) {
                                saveState();
                            /*if (NavUtils.getParentActivityName(getActivity()) != null){
                                NavUtils.navigateUpFromSameTask(getActivity());
                            }*/
                                getActivity().finish();
                            }else{
                                //show Toast
                                Toast.makeText(getActivity(),R.string.upcoming_time
                                        ,Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
            customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // "Cancel"
                            /*if (NavUtils.getParentActivityName(getActivity()) != null){
                                NavUtils.navigateUpFromSameTask(getActivity());
                            }*/
                           getActivity().finish();
                        }
                    }
            );

            // Show the custom action bar view and hide the normal Home icon and title.
            final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayOptions(
                    ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                            | ActionBar.DISPLAY_SHOW_TITLE
            );

            actionBar.setCustomView(customActionBarView,
                    new ActionBar.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT)
            );
            actionBar.setDisplayHomeAsUpEnabled(false);
            // END_INCLUDE (inflate_set_custom_view)
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode != Activity.RESULT_OK)return;
        if ( requestCode == REQUEST_TIME){
            mTimeDue = (Calendar)data.getSerializableExtra(EXTRA_TIME);
            // update lender and time editText

            timePickerEditText.setText(mTimeFormat.format(mTimeDue.getTime()));
        }
 ///*****************************************************************************
        if (requestCode == REQUEST_INTEREST_INFO){
            numberOfweeks = data.getIntExtra(InterestDialogFragment.EXTRA_LOANPERIOD,1);
            float interest = data.getFloatExtra(InterestDialogFragment.EXTRA_INTEREST,0.0f);

            if (mLender !=null){
                mLender.setInterestRate(interest);
                interestEditText.setText(Float.toString(interest));
                UpdateBalanceDue();
            }else{
                interestEditText.setText(Float.toString(interest));
            }

        }
//*******************************************************************************

        if (requestCode == REQUEST_CONTACT){
            ContentResolver cr = getActivity().getContentResolver();
            Uri contactUri = data.getData();
            //specify fields which you want your query to
            //return values for
            String[] queryFields = new String[]{
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER,
                    (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB)?
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY:
                            ContactsContract.Contacts.DISPLAY_NAME,
                    (Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB)?
                            ContactsContract.Contacts.PHOTO_FILE_ID:
                            ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI

            };
            //perform query
            Cursor c = getActivity().getContentResolver().
                    query(contactUri,queryFields,null, null,null);

            //double check if cursor has results
            if (c.getCount() == 0){
                c.close();
                return;
            }

            //pull out the first column of the first row of data
            //that is the contact name
            c.moveToFirst();
            String id = c.getString(c
                    .getColumnIndex(ContactsContract.Contacts._ID));

            // get id index
            mIdColumn = c.getColumnIndex(ContactsContract.Contacts._ID);
            //get the lookup key
            mLookUpKeyColumn = c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
            // gets a content uri for the contact
            mContactUri= ContactsContract.Contacts.getLookupUri(c.getLong(mIdColumn),
                    c.getString(mLookUpKeyColumn));
            if (mLender!=null) {
                mLender.setContactUri(mContactUri);
            }

            mBadge.assignContactUri(mContactUri);


//***************************************************************************************************

 /* Gets the photo thumbnail column index if
 * platform version >= Honeycomb
 */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mThumbnailColumn =
                        c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
                // Otherwise, sets the thumbnail column to the _ID column
            } else {
                mThumbnailColumn = mIdColumn;
            }

             // Assuming the current Cursor position is the contact you want,
             // gets the thumbnail ID

            mThumbnailUri = c.getString(mThumbnailColumn);
            if (mLender!=null) {
                mLender.setThumbnailUri(mThumbnailUri);
            }
            if (mThumbnailUri!= null) {

             // Decodes the thumbnail file to a Bitmap.

                Bitmap mThumbnail =
                        Utils.loadContactPhotoThumbnail(mThumbnailUri);

              //Sets the image in the QuickContactBadge
             // QuickContactBadge inherits from ImageView, so

                mBadge.setImageBitmap(mThumbnail);
            }
//**************************************************************************************************
            ///set name of lender and number

            int nameColumn = c.getColumnIndex((Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB)?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY:
                    ContactsContract.Contacts.DISPLAY_NAME) ;
            String name = c.getString(nameColumn);
            if (mLender == null){
                nameEditText.setText(name);
            }else{
                mLender.setName(name);
                nameEditText.setText(name);
            }
            if (Integer.parseInt(c.getString(
                    c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[]{id}, null);

                if (pCur.getCount() == 0){
                    pCur.close();
                    return;
                }
                pCur.moveToFirst();
                String phone = pCur
                        .getString(pCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if (mLender == null){
                    phoneEditText.setText(phone);
                }else{
                    mLender.setPhoneNumber(phone);
                    phoneEditText.setText(phone);
                }
                pCur.close();

            }

            c.close();
        }
    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isNewLender){
            return;
        }else {
            inflater.inflate(R.menu.fragment_borrower_lender_detail, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //loop lender and delete any borrower without a name
                if (nameEditText.length() ==0){
                    LoanOffice.getInstance(getActivity()).removeLender(mLender.getId());
                }
               // saveState();
                /*if (NavUtils.getParentActivityName(getActivity()) != null){
                    NavUtils.navigateUpFromSameTask(getActivity());
                }*/
                getActivity().finish();
                return true;

            case R.id.menu_item_delete:
                LoanOffice.getInstance(getActivity()).removeLender(mLender.getId());
               /* if (NavUtils.getParentActivityName(getActivity()) != null){
                    NavUtils.navigateUpFromSameTask(getActivity());
                }*/
                getActivity().finish();
                return true;

            case R.id.menu_item_save:

                float amountDue;
                if (amountDueEditText.getText().toString().length() ==0){
                    amountDue = 0.0f;
                }else {
                    String temp = amountDueEditText.getText().toString();
                    temp = temp.substring(temp.indexOf("$")+1);
                    amountDue = Float.parseFloat(temp);
                }

                if (!hasSetTimePassed()|| mLender.paid() || amountDue <=0) {
                    saveState();
                    getActivity().finish();
                }else{
                    Toast.makeText(getActivity(),R.string.upcoming_time
                            ,Toast.LENGTH_SHORT).show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        private Calendar c;

        public static TimePickerFragment newDialog (Calendar calendar){

            Bundle b = new Bundle();
            b.putSerializable("DATE",calendar);
            TimePickerFragment picker = new TimePickerFragment();
            picker.setArguments(b);

            return picker;

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            // final Calendar c = Calendar.getInstance();
            Bundle b  = getArguments();
            c =(Calendar) b.get("DATE");
            if (c ==null){c = Calendar.getInstance();}
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user

            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE,minute);

            if (getTargetFragment() == null)
                return;
            Intent i = new Intent();
            i.putExtra(EXTRA_TIME, c);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
        }

    }

    public void showTimePickerDialog(/*View v*/) {

        DialogFragment newFragment = TimePickerFragment.newDialog(mDateDue);
        newFragment.setTargetFragment(LendersDetailFragment.this,REQUEST_TIME);
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    public void showBetterPickerDialog(){

        TimePickerBuilder btp = new TimePickerBuilder()
                .setFragmentManager(getFragmentManager())
                .setStyleResId(R.style.BetterPickersDialogFragment_Light);
        btp.addTimeSetListener(new TimePickerDialogFragment.TimePickerDialogHandler() {
            @Override
            public void onDialogTimeSet(int hourOfDay, int minute) {

                mDateDue.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mDateDue.set(Calendar.MINUTE,minute);
                mTimeDue.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mTimeDue.set(Calendar.MINUTE,minute);
                // update lender and time editText

                timePickerEditText.setText(mTimeFormat.format(mTimeDue.getTime()));

            }

            @Override
            public void onDialogCancel() {

            }
        });


        btp.show();
    }
}
