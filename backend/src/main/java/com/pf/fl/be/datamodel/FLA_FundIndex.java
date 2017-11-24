package com.pf.fl.be.datamodel;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Embedded;

@Entity
public class FLA_FundIndex {

	@Id public String mKey_IndexName;

    @Unindex @Embedded public List<FLA_FundIndexDPDay> mDPDays = new ArrayList<>();

    public String getLastestOneLine() throws Exception {
        StringBuffer strb = new StringBuffer();
        if (mDPDays == null || mDPDays.size() == 0) {
            strb.append("NULL");
        } else {
            FLA_FundIndexDPDay dpDay = mDPDays.get(mDPDays.size()-1);
            strb.append(dpDay.toSingleLineString());
        }
        strb.append("," + mKey_IndexName);
        return strb.toString();
    }

    public boolean add(String dpDay, FLA_FundInfo fi) throws Exception {
        Collections.sort(mDPDays, FLA_FundIndexDPDay.COMPARATOR_DATE);

        FLA_FundDPDay finfoDPDay = null;
        List<FLA_FundDPDay> dpDays = fi.mDPDays;
        for (int i=dpDays.size()-1; i >= 0; i--) {
            FLA_FundDPDay finfoDPDayTmp = dpDays.get(i);
            if (finfoDPDayTmp.mDateYYMMDD.equals(dpDay) && finfoDPDayTmp.mR1w != null) {
                finfoDPDay = finfoDPDayTmp;
                break;
            }
        }
        if (finfoDPDay == null) {
            return false;
        }

        FLA_FundIndexDPDay findexDPDay = null;
        for (int i=mDPDays.size()-1; i >= 0; i--) {
            FLA_FundIndexDPDay fiDPDayTmp = mDPDays.get(i);
            if (fiDPDayTmp.mDateYYMMDD.equals(dpDay)) {
                findexDPDay = fiDPDayTmp;
                break;
            }
        }

        if (findexDPDay == null) {
            findexDPDay = new FLA_FundIndexDPDay();
            findexDPDay.mDateYYMMDD = dpDay;
            mDPDays.add(findexDPDay);
        }

        boolean result = findexDPDay.add(fi.mId, finfoDPDay);
        Collections.sort(mDPDays, FLA_FundIndexDPDay.COMPARATOR_DATE);
        return result;
    }
	
	public static FLA_FundIndex instantiate(String name) {
		FLA_FundIndex r = new FLA_FundIndex();
		r.mKey_IndexName = name;
		return r;
    }
}
