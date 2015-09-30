package com.simpson.o.alexis.payup.loaders;

import android.content.Context;
import com.simpson.o.alexis.payup.Borrower;
import com.simpson.o.alexis.payup.LoanOffice;

public class BorrowerLoader extends DataLoader<Borrower> {
    private long mBorrowerId;

    public BorrowerLoader(Context context, long BorrowerId) {
        super(context);
        mBorrowerId = BorrowerId;
    }

    @Override
    public Borrower loadInBackground() {
        return LoanOffice.getInstance(getContext()).getBorrower(mBorrowerId);
    }
}