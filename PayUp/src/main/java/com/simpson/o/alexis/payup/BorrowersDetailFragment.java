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
import android.support.v7.widget.Toolbar;
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

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.simpson.o.alexis.payup.Utils.DateUtils;
import com.simpson.o.alexis.payup.Utils.Utils;

/**
 * Created by Alexis on 5/31/2014.
 */
public class BorrowersDetailFragment extends Fragment {

   // private CheckBox paidCheckBox;
    private EditText paidEditText;
    private final String TAG="BORROWERS_DETAIL_FRAGMENT";
    private final String type = "Borrower";
    private QuickContactBadge mBadge;
    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText amountEditText;
    private EditText interestEditText;
    private EditText dateBorrowedEditText;
    private EditText dateDueEditText;
    private EditText amountDueEditText;
    private EditText timePickerEditText;
    private Button selectContactButton;
    static final String EXTRA_BORROWER_ID ="com.simpson.o.alexis.payup.BorrowersDetailFragment";
    private static final String EXTRA_TIME = "borrower notification time";
    private long mRowId;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("EEE MMM dd");
    private SimpleDateFormat mTimeFormat = new SimpleDateFormat("hh:mm:aa");
    private Boolean isNewBorrower =false;
    private Boolean paid = false;
    private Borrower mBorrower;
    private Calendar mDateBorrowed;
    private Calendar mDateDue;
    private Calendar mTimeDue;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_TIME = 2;
    private static final int REQUEST_INTEREST_INFO = 0;
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
    private SharedPreferences sharedPrefs;
    private NotificationManager notificationManager;

    private Toolbar toolBar;



    public static BorrowersDetailFragment newInstance(long detailID ) {
        Bundle args = new Bundle();
        args.putLong(EXTRA_BORROWER_ID,detailID);
        BorrowersDetailFragment fragment = new BorrowersDetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /*public void loadData(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.borrower_detail_fragment_title);
        setRetainInstance(true);
        notificationManager = (NotificationManager)getActivity().
                getSystemService(Context.NOTIFICATION_SERVICE);

        // check for a Borrower ID as an argument, and find the borrower
        Bundle args = getArguments();
        if (args != null) {
            mRowId= args.getLong(BorrowersDetailFragment.EXTRA_BORROWER_ID, -1);

            if (mRowId != -1) {
                mBorrower = LoanOffice.getInstance(getActivity()).getBorrower(mRowId);

            }
        }
        setHasOptionsMenu(true);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.borrowers_detail_fragment,container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
           // if (NavUtils.getParentActivityName(getActivity()) != null) {
             //   getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
           // }
        }

        mBadge= (QuickContactBadge)v.findViewById(R.id.quickContactBadge_borrower);
        nameEditText = (EditText)v.findViewById(R.id.borrowers_nameEt);
        phoneEditText =(EditText)v.findViewById(R.id.borrowers_phone_editText);
        amountEditText =(EditText)v.findViewById(R.id.borrowers_amount_editText);
        interestEditText = (EditText)v.findViewById(R.id.borrowers_interest_editText);
        dateBorrowedEditText = (EditText)v.findViewById(R.id.borrowers_dateBorrowed_editText);
        dateDueEditText = (EditText)v.findViewById(R.id.borrowers_date_due_editText);
        amountDueEditText = (EditText)v.findViewById(R.id.borrowers_amount_due_editText);
        timePickerEditText = (EditText)v.findViewById(R.id.borrowers_Notification_Time_editText);
        selectContactButton = (Button)v.findViewById(R.id.borrowers_selectContactButton);
        paidEditText =(EditText)v.findViewById(R.id.borrowers_amount_paid_editText);
       // paidCheckBox = (CheckBox)v.findViewById(R.id.borrowers_paid_checkBox);


        if(mBorrower!= null ){
            mTimeDue = mBorrower.getDateDue();
            mDateDue = mBorrower.getDateDue();
            paid = mBorrower.paid();
            notificationManager.cancel((int)mBorrower.getId());
            /*Toast.makeText(getActivity(),Float.toString(mBorrower.getLoanAmount()),Toast.LENGTH_SHORT);
            Toast.makeText(getActivity(),mBorrower.paid().toString(),Toast.LENGTH_SHORT);*/

        }else {
            mTimeDue = Calendar.getInstance();
            mDateDue = Calendar.getInstance();

        }
        // / Force the keyboard to close
      /*  InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);*/
        updateUI();
        registerListeners();


        return v;
    }

    private void updateUI() {

        if(mRowId !=-1) {
            nameEditText.setText(mBorrower.getName());
            phoneEditText.setText(mBorrower.getPhoneNumber());
            amountEditText.setText(Float.toString(mBorrower.getLoanAmount()));
            interestEditText.setText(Float.toString(mBorrower.getInterestRate()));

            if (DateUtils.isToday(mBorrower.getDateBorrowed())){
                dateBorrowedEditText.setText(R.string.today);
            }else{
                dateBorrowedEditText.setText(mDateFormat.format(mBorrower.getDateBorrowed().getTime()));
            }

            if (DateUtils.isToday(mBorrower.getDateDue())){
                dateDueEditText.setText(R.string.today);
            }else{
                dateDueEditText.setText(mDateFormat.format(mBorrower.getDateDue().getTime()));
            }

            timePickerEditText.setText(mTimeFormat.format(mBorrower.getDateDue().getTime()));
            if (mBorrower.getContactUri() != null) {
                mBadge.assignContactUri(mBorrower.getContactUri());
            }
            if (mBorrower.getThumbnailUri() != null ) {
                if ( !mBorrower.getThumbnailUri().equalsIgnoreCase("Uri null")){
                    mBadge.setImageBitmap(Utils.loadContactPhotoThumbnail(mBorrower.getThumbnailUri()));
                }
            }
            paidEditText.setText(Float.toString(mBorrower.getAmountPaid()));
            /*if (mBorrower.paid()) {
               // paidCheckBox.setChecked(true);
            }else {
               // paidCheckBox.setChecked(false);
            }*/
            UpdateBalanceDue();
        }else{

            dateBorrowedEditText.setText(R.string.today);
            dateDueEditText.setText(R.string.today);
            String timeString = sharedPrefs.getString("KEY_NOTIFICATION_TIME", "08:00 AM");
            timePickerEditText.setText(time24to12(timeString));
            Date inDate = toDate(timeString);
            mTimeDue.setTime(inDate);
            mDateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
            mDateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));

        }


    }
    private void UpdateBalanceDue(){

        //calculate principal amount 1. amount loaned
        //2. interst and 3. number of periods
        float principal = LoanOffice.getInstance(getActivity()).
                getAmountDue(mBorrower.getLoanAmount(),mBorrower.getInterestRate(),numberOfweeks);
        //calculate amount due by subtracting what is paid from the principal amount
        float amountDue = principal - mBorrower.getAmountPaid();

        /*float principal = mBorrower.getLoanAmount() - mBorrower.getAmountPaid();
        float amountDue = LoanOffice.getInstance(getActivity()).
                getAmountDue(principal,mBorrower.getInterestRate(),numberOfweeks);//number of periods*/

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

    private void saveState() {

        if (mRowId == -1) {
            String name = nameEditText.getText().toString();
            String phoneNumber = phoneEditText.getText().toString();
            float interestRate;
            float amountBorrowed;
            float amountPaid;
            Calendar dateBorrowed;
            Calendar dateDue;


            if (interestEditText.getText().toString().length() == 0) {
                interestRate = 0.00f;
            }else{
                interestRate = Float.parseFloat(interestEditText.getText().toString());
            }

            if(amountEditText.getText().toString().length() == 0){
                amountBorrowed = 0.00f;
            }else {
                amountBorrowed = Float.parseFloat(amountEditText.getText().toString());
            }

            if(paidEditText.getText().toString().length() == 0){
                amountPaid = 0.00f;
            }else {
                amountPaid = Float.parseFloat(paidEditText.getText().toString());
            }

            /*if (amountDueEditText.getText().toString().length() ==0){
                amountDue = 0.00f;
            }else {

                float principle = amountBorrowed - amountPaid;
                 amountDue = LoanOffice.getInstance(getActivity()).
                        getAmountDue(principle,interestRate,1);
                // float interest = amountBorrowed *(interestRate/100);
               // amountDue=(interest+amountBorrowed);
              // amountDue = Float.parseFloat(amountDueEditText.getText().toString());*//*
            }*/

            if(mDateBorrowed ==null){
                dateBorrowed = Calendar.getInstance();
            }else{dateBorrowed = mDateBorrowed;}


            if (mDateDue == null){
                //set to current date but time to timepickers times
                dateDue = Calendar.getInstance();
                dateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
                dateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));
            } else{
                //set to time to picker time
                mDateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
                mDateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));
                dateDue = mDateDue;
            }

            Toast.makeText(getActivity(),R.string.borrower_saved ,Toast.LENGTH_SHORT).
                    show();

            //add borrower to database
                long id = LoanOffice.getInstance(getActivity()).addBorrower(name,
                        phoneNumber,
                        interestRate,
                        numberOfweeks,
                        amountBorrowed,
                        amountPaid,
                        dateBorrowed,
                        dateDue,
                        paid,
                        mContactUri,
                        mThumbnailUri);
                if (id > 0) {
                    mRowId = id;
                }
                if (paid==false|| !hasSetTimePassed() ) {
                    new ReminderManager(getActivity()).setReminder(mRowId, dateDue, type);
                }

        } else {

            //update borrower

            Toast.makeText(getActivity(),R.string.borrower_updated ,Toast.LENGTH_SHORT).show();

            Calendar dateDue = Calendar.getInstance();
            if (mDateDue == null){
                // this should never be called
                dateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
                dateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));
            }
            if (mDateDue != null){
                mDateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
                mDateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));
                dateDue = mDateDue;
            }
            mBorrower.setIsPaid(paid);
            mBorrower.setDateDue(dateDue);

            LoanOffice.getInstance(getActivity()).updateBorrower(mBorrower);
            if (mBorrower.paid() == false || !hasSetTimePassed()) {
                new ReminderManager(getActivity()).setReminder(mRowId, mBorrower.getDateDue(), type);
            }

        }
        BackupManager.dataChanged("com.simpson.o.alexis.payup");

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
              if(mBorrower!= null) { mBorrower.setName(s.toString());}
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
              if (mBorrower!=null){  mBorrower.setPhoneNumber(s.toString());}
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
                if(mBorrower!=null) {
                    String temp = s.toString();
                    if (temp == null || temp.isEmpty()) {
                        mBorrower.setAmountPaid(0.00f);
                    } else {

                        mBorrower.setAmountPaid(Float.parseFloat(temp));
                    }
                    UpdateBalanceDue();
                }else{
                    //update amount paid and balance textfields without lender object
                    String temp = s.toString();
                    float paidAmount ;
                    float interestRate;
                    float loanAmount;
                    //get interest from edittext
                    if (interestEditText.getText().toString().length() == 0) {
                        interestRate = 0.00f;
                    }else{interestRate = Float.parseFloat(interestEditText.getText().toString());
                    }
                    //get amount paid from paid editText
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

                    /*float principal = loanAmount - paidAmount;
                    float amountDue = LoanOffice.getInstance(getActivity()).
                            getAmountDue(principal,interestRate,numberOfweeks);//number of periods*/

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
                //this throws an exception
                //  if (Float.parseFloat(paidEditText.getText().toString()) == 0.0f){paidEditText.setText("0");}

            }
        });

        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mBorrower!=null) {
                    String temp = s.toString();
                    if (temp == null || temp.isEmpty()) {
                        mBorrower.setLoanAmount(0.00f);
                    } else {

                        mBorrower.setLoanAmount(Float.parseFloat(temp));
                    }
                    UpdateBalanceDue();
                }else{
                    //update balance textfield without borrower object
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
                if (mBorrower !=null) {
                    String temp = s.toString();
                    if (temp == null || temp.isEmpty()) {
                        mBorrower.setInterestRate(0.00f);
                    } else {

                        mBorrower.setInterestRate(Float.parseFloat(temp));
                    }
                    UpdateBalanceDue();
                }else{
                    //update balance textfield without borrower object
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

                    /*float principal = amountBorrowed - paidAmount;
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
                dialog.setTargetFragment(BorrowersDetailFragment.this, REQUEST_INTEREST_INFO);
                dialog.show(fm,"payUp.INTEREST_RATE");
            }
        });



        dateBorrowedEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Resources r = getResources();
                if (mRowId!= -1) {
                    mDateBorrowed = mBorrower.getDateBorrowed();
                }else{
                    mDateBorrowed = Calendar.getInstance();
                }

                final CaldroidFragment dialogCaldroidFragment = CaldroidFragment.
                        newInstance(r.getString(R.string.calendar_title_dateBorrowed), (mDateBorrowed.get(Calendar.MONTH))+1,
                                mDateBorrowed.get(Calendar.YEAR));
                dialogCaldroidFragment.setBackgroundResourceForDate(R.color.holo_green_light,
                        mDateBorrowed.getTime());
              //  dateBorrowedEditText.setTextColor(r.getColor(R.color.holo_green_light));

                dialogCaldroidFragment.setCaldroidListener(new CaldroidListener() {
                    @Override
                    public void onSelectDate(Date date, View view) {
                        mDateBorrowed.setTime(date);
                        if (DateUtils.isToday(mDateBorrowed/*.getTime()*/)){
                            dateBorrowedEditText.setText(R.string.today);
                        }else{
                            dateBorrowedEditText.setText(mDateFormat.format(mDateBorrowed.getTime()));
                        }
                        //dateBorrowedEditText.setText(mDateFormat.format(mDateBorrowed.getTime()));
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
                    mDateDue = mBorrower.getDateDue();
                }else{
                    mDateDue= Calendar.getInstance();
                }

                final CaldroidFragment dialogCaldroidFragment = CaldroidFragment.
                        newInstance(r.getString(R.string.calendar_title_dateDue), (mDateDue.get(Calendar.MONTH))+1,
                                mDateDue.get(Calendar.YEAR));

                dialogCaldroidFragment.setBackgroundResourceForDate(R.color.holo_red_light,
                        mDateDue.getTime());
               // dateDueEditText.setTextColor(r.getColor(R.color.holo_red_light));

                dialogCaldroidFragment.setCaldroidListener(new CaldroidListener() {
                    @Override
                    public void onSelectDate(Date date, View view) {
                        mDateDue.setTime(date);

                        if (DateUtils.isToday(mDateDue)){
                            dateDueEditText.setText(R.string.today);
                        }else{
                            dateDueEditText.setText(mDateFormat.format(mDateDue.getTime()));
                        }
                       // dateDueEditText.setText(mDateFormat.format( mDateDue.getTime()));
                        mDateDue.set(Calendar.HOUR_OF_DAY,mTimeDue.get(Calendar.HOUR_OF_DAY));
                        mDateDue.set(Calendar.MINUTE,mTimeDue.get(Calendar.MINUTE));
                        timePickerEditText.setText(mTimeFormat.format(mDateDue.getTime()));
                        if (mBorrower !=null) {
                            Toast.makeText(getActivity(), R.string.time_reset, Toast.LENGTH_SHORT).show();
                        }
                        dialogCaldroidFragment.dismiss();

                    }
                });

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent i = getActivity().getIntent();
        isNewBorrower = i.getBooleanExtra("NEW BORROWER",false);

        if (isNewBorrower) {
            setHasOptionsMenu(false);

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
                           /* float amountDue;
                            if (amountDueEditText.getText().toString().length() ==0){
                                amountDue = 0.0f;
                            }else {
                                String temp = amountDueEditText.getText().toString();
                                temp = temp.substring(temp.indexOf("$")+1);
                                amountDue = Float.parseFloat(temp);
                            }*/
                            if (!hasSetTimePassed() /*|| amountDue <=0*/ ) {

                                saveState();
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

                            //do nothing
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
            mTimeDue= (Calendar)data.getSerializableExtra(EXTRA_TIME);
            // update lender and time editText

            timePickerEditText.setText(mTimeFormat.format(mTimeDue.getTime()));
        }
 //******************************************************************************
        if (requestCode == REQUEST_INTEREST_INFO){
            numberOfweeks = data.getIntExtra(InterestDialogFragment.EXTRA_LOANPERIOD,1);
            float interest = data.getFloatExtra(InterestDialogFragment.EXTRA_INTEREST,0.0f);

            if (mBorrower !=null){
                mBorrower.setInterestRate(interest);
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
            if (mBorrower!=null) {
                mBorrower.setContactUri(mContactUri);
            }

            mBadge.assignContactUri(mContactUri);


/***************************************************************************************************
             * Gets the photo thumbnail column index if
             * platform version >= Honeycomb
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mThumbnailColumn =
                        c.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
                // Otherwise, sets the thumbnail column to the _ID column
            } else {
                mThumbnailColumn = mIdColumn;
            }
            /*
             * Assuming the current Cursor position is the contact you want,
             * gets the thumbnail ID
             */
            mThumbnailUri = c.getString(mThumbnailColumn);
            if (mBorrower!=null) {
                mBorrower.setThumbnailUri(mThumbnailUri);
            }


            if (mThumbnailUri!= null) {
            /*
             * Decodes the thumbnail file to a Bitmap.
            */
                Bitmap mThumbnail =
                        Utils.loadContactPhotoThumbnail(mThumbnailUri);
            /*
             * Sets the image in the QuickContactBadge
             * QuickContactBadge inherits from ImageView, so
             */
               mBadge.setImageBitmap(mThumbnail);
            }
//**************************************************************************************************
            ///set name of borrower and number

            int nameColumn = c.getColumnIndex((Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB)?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY:
                    ContactsContract.Contacts.DISPLAY_NAME) ;
            String name = c.getString(nameColumn);
            if (mBorrower == null){
            nameEditText.setText(name);
            }else{
                mBorrower.setName(name);
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

                if (mBorrower == null){
                    phoneEditText.setText(phone);
                }else{
                    mBorrower.setPhoneNumber(phone);
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
        if (isNewBorrower){
            return;
        }
        if (!isNewBorrower) {
            inflater.inflate(R.menu.fragment_borrower_lender_detail, menu);
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //loop borrower and delete any borrower without a name
                if (nameEditText.length() ==0){
                    LoanOffice.getInstance(getActivity()).removeBorrower(mBorrower.getId());
                }
               // saveState();
                /*if (NavUtils.getParentActivityName(getActivity()) != null){
                    NavUtils.navigateUpFromSameTask(getActivity());
                }*/

                getActivity().finish();
                return true;

            case R.id.menu_item_delete:
                LoanOffice.getInstance(getActivity()).removeBorrower(mBorrower.getId());
                /*if (NavUtils.getParentActivityName(getActivity()) != null){
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
                if (!hasSetTimePassed() || mBorrower.paid() || amountDue <=0) {
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
        newFragment.setTargetFragment(BorrowersDetailFragment.this,REQUEST_TIME);
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
        }); //add here any listener implementing TimePickerDialogHandler


        btp.show();
    }
   /* private class BorrowerLoaderCallbacks implements LoaderCallbacks<Borrower> {

        @Override
        public Loader<Borrower> onCreateLoader(int id, Bundle args) {
            return new BorrowerLoader(getActivity(), args.getLong(EXTRA_BORROWER_ID));
        }

        @Override
        public void onLoadFinished(Loader<Borrower> loader, Borrower borrower) {
            mBorrower = borrower;
            mRowId = mBorrower.getId();
            updateUI();
        }

        @Override
        public void onLoaderReset(Loader<Borrower> loader) {
            // do nothing
        }
    }*/

}
