package com.simpson.o.alexis.payup;


import android.content.Context;
import android.net.Uri;

import com.simpson.o.alexis.payup.cursorwrapper.BorrowerCursor;
import com.simpson.o.alexis.payup.cursorwrapper.LenderCursor;
import com.simpson.o.alexis.payup.storage.AppStorage;
import com.simpson.o.alexis.payup.storage.Storage;

import java.util.Calendar;

/**
 * Created by Alexis on 5/30/2014.
 */
public class LoanOffice {

    private Context mAppContext;
    private static LoanOffice sLoanOffice;
   // private PayUpDataBaseHelper mDbHelper;
    private static final AppStorage dataBaseStorage = Storage.getStorage();
    private final static String TAG = "LOANOFFICE";


    private LoanOffice(Context mAppContext){
        this.mAppContext = mAppContext;
       // mDbHelper = new PayUpDataBaseHelper(mAppContext);


    }

    public static LoanOffice getInstance(Context c) {
        if (sLoanOffice == null) {
            sLoanOffice = new LoanOffice(c.getApplicationContext());
        }
        return sLoanOffice;
    }

    public static AppStorage getStorage(){
        return dataBaseStorage;
    }

    public Borrower getBorrower(long id) {
        Borrower b = null;
        BorrowerCursor cursor = dataBaseStorage.queryBorrower(id);
        cursor.moveToFirst();
        // if we got a row, get a borrower
        if (!cursor.isAfterLast())
            b = cursor.getBorrower();
        cursor.close();
        return b;
    }


    public Lender getLender(long id) {
        Lender l = null;
        LenderCursor cursor = dataBaseStorage.queryLender(id);
        cursor.moveToFirst();
        // if we got a row, get a borrower
        if (!cursor.isAfterLast()){
            l = cursor.getLender();
        }
        cursor.close();
        return l;
    }

    public BorrowerCursor getBorrowers(){

        return  dataBaseStorage.queryBorrowers();
    }

    public BorrowerCursor getBorrowers(String query){

        return  dataBaseStorage.queryBorrowers(query);
    }

    public LenderCursor getLenders(){
        return dataBaseStorage.queryLenders();
    }
    public LenderCursor getLenders(String query){
        return dataBaseStorage.queryLenders(query);
    }
    public Borrower addBorrower(){

        Borrower b = new Borrower();
        b.setId(dataBaseStorage.insertBorrower(b));
        return b;
    }
    public Lender addLender(){
        Lender l = new Lender();
        l.setId(dataBaseStorage.insertLender(l));
        return l;
    }

    public long addBorrower(String name,
                            String phoneNumber,
                            float interestRate,
                            int numbOfPeriods,
                            float amountBorrowed,
                            float amountPaid,
                            Calendar dateBorrowed,
                            Calendar dateDue,
                            Boolean paid,
                            Uri contactUri,
                            String thumbnailUri){

        Borrower b = new Borrower();
        b.setName(name);
        b.setPhoneNumber(phoneNumber);
        b.setInterestRate(interestRate);
        b.setLoanDuration(numbOfPeriods);
        b.setLoanAmount(amountBorrowed);
        b.setAmountPaid(amountPaid);
        //b.setAmountDue(amountDue);
        b.setDateBorrowed(dateBorrowed);
        b.setDateDue(dateDue);
        b.setIsPaid(paid);
        b.setThumbnailUri(thumbnailUri);
        b.setContactUri(contactUri);
        return (dataBaseStorage.insertBorrower(b));

    }

    public long addLender(String name,
                          String phoneNumber,
                            float interestRate,
                            int numbOfPeriods,
                            float amountLoaned,
                            float amountPaid,
                            Calendar dateLoaned,
                            Calendar dateDue,
                            Boolean paid,
                            Uri contactUri,
                            String thumbnailUri){

        Lender l = new Lender();
        l.setName(name);
        l.setPhoneNumber(phoneNumber);
        l.setInterestRate(interestRate);
        l.setLoanDuration(numbOfPeriods);
        l.setLoanAmount(amountLoaned);
        l.setAmountPaid(amountPaid);
        l.setDateLoaned(dateLoaned);
        l.setDateDue(dateDue);
        l.setIsPaid(paid);
        l.setContactUri(contactUri);
        l.setThumbnailUri(thumbnailUri);
        return (dataBaseStorage.insertLender(l));
    }

    public int updateBorrower(Borrower borrower){
        return dataBaseStorage.updateBorrower(borrower);
    }
    public int updateLender(Lender lender){
        return dataBaseStorage.updateLender(lender);
    }

    public boolean removeBorrower(long id) {
        return dataBaseStorage.deleteBorrower(id);
    }

    public boolean removeLender(long id) {
        return dataBaseStorage.deleteLender(id);
    }

    /*public float getAmountDue(float loanAmount,float interestRate){

        float interest = loanAmount *(interestRate/100);
        return (interest+loanAmount);
    }*/

    public float getAmountDue(float loanAmount,float interestRate,int time){
        float interest = simpleInterest(loanAmount, interestRate, time);

        return (interest+loanAmount);
    }

    private float simpleInterest(float principle,float rate,int time){
        float interest = (principle*rate*time)/100;
        return interest;
    }

}