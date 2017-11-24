package com.pf.fl.be.datamodel;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class FLA_FundCategory {

	@Id public String mKey_CategoryName; // Required, never null
	
	/**
	 * 
	 */
	public static FLA_FundCategory instantiate(String name) {
		FLA_FundCategory r = new FLA_FundCategory();
		r.mKey_CategoryName = name;
		return r;
	}
}
