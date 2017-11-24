package com.pf.fl.be.datamodel;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;
import com.pf.fl.be.util.Constants;
import com.pf.shared.IndentWriter;

import java.util.List;

import javax.persistence.Embedded;

@Entity
public class FLA_FundPortfolio {
	
	public static final String TYPE_PPM = Constants.ACCOUNT_TYPE_PPM;
	public static final String TYPE_SEB = Constants.ACCOUNT_TYPE_SEB;
	public static final String TYPE_SPP = Constants.ACCOUNT_TYPE_SPP;
	
	@Id public Long mId;
	@Index public String mName; // Required: Non-null and length > 0
	@Index public String mDateYYMMDD_Created; // Required
	@Index public String mDateYYMMDD_Modified; // Required
    @Index public String mType; // Required
    @Unindex @Embedded public List<Ref<FLA_FundInfo>> mFunds;

    /**
     *
     */
	public void dumpInfo(IndentWriter iw) { 
		iw.println("FLA_FundPortfolio: " + mName + ", type: " + mType + ", id: " + mId + ", created: " + mDateYYMMDD_Created + ", modified: " + mDateYYMMDD_Modified);
        if (mFunds.size() > 0) {
            iw.push();
        }
        for (int i=0; i < mFunds.size(); i++) {
            iw.println(mFunds.get(i).toString());
        }
        if (mFunds.size() > 0) {
            iw.pop();
        }
	}

	/**
	 * 
	 */
	public FLA_FundPortfolio setInfo1(
			String name,
            String dateYYMMDD_Created,
            String dateYYMMDD_Modified,
            List<Ref<FLA_FundInfo>> funds) {
        mName = name;
        mDateYYMMDD_Created = dateYYMMDD_Created;
        mDateYYMMDD_Modified = dateYYMMDD_Modified;
        mFunds = funds;
        return this;
	}
	
}


