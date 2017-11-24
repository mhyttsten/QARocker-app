package com.pf.fl.be.datamodel;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class FLA_Currency {

	@Id public String mKey_CurrencyName; // Required, never null

	/**
	 * 
	 */
	public static FLA_Currency instantiate(String name) {
		FLA_Currency r = new FLA_Currency();
		r.mKey_CurrencyName = name;
		return r;
	}
}
