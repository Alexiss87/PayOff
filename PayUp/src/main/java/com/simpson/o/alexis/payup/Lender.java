package com.simpson.o.alexis.payup;

import android.net.Uri;

import com.simpson.o.alexis.payup.model.Client;

import java.io.Serializable;
import java.util.Calendar;


/**
 * Created by Alexis on 5/30/2014.
 */
public class Lender extends Client implements Serializable {

    private String name;
    private long id;
    private float loanAmount;
    private float amountPaid;
    private boolean isPaid = false;
    private Calendar dateDue;
    private String phoneNumber;
    private Calendar dateLoaned;
    private float interestRate;
    private Uri contactUri;
    private String thumbnailUri;

    private int loanDuration ;

    public Lender(){
        id = -1;
        dateLoaned = Calendar.getInstance();
        dateDue = Calendar.getInstance();
        amountPaid = 0.0f;
        loanDuration = 1;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public float getLoanAmount(){
        return loanAmount;
    }
    public void setLoanAmount(float loanAmount){
        this.loanAmount = loanAmount;
    }

    public float getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(float amountPaid) {
        this.amountPaid = amountPaid;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public Calendar getDateLoaned(){
        return dateLoaned;
    }

    public void setDateLoaned(Calendar dateLoaned){
        this.dateLoaned = dateLoaned;
    }

    public Calendar getDateDue(){return dateDue;}

    public void setDateDue(Calendar dateDue){
        this.dateDue = dateDue;
    }

    public Boolean paid(){
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid){
        this.isPaid = isPaid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setInterestRate(float interestRate) {
        this.interestRate = interestRate;
    }

    public float getInterestRate() {
        return interestRate;
    }

    public Uri getContactUri() {
        return contactUri;
    }

    public void setContactUri(Uri contactUri) {
        this.contactUri = contactUri;
    }

    public String getThumbnailUri() {
        return thumbnailUri;
    }

    public void setThumbnailUri(String thumbnailUri) {
        this.thumbnailUri = thumbnailUri;
    }
    public int getLoanDuration() {
        return loanDuration;
    }

    public void setLoanDuration(int loanDuration) {
        this.loanDuration = loanDuration;
    }
}
