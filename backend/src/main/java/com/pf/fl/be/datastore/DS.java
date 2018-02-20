package com.pf.fl.be.datastore;

import com.pf.fl.be.datamodel.*;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.pf.fl.be.util.EE;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class DS {
	private static final Logger log = Logger.getLogger(DS.class.getName());
	private static final String TAG = MM.getClassName(DS.class.getName());
		
	public static void initialize() {
		ObjectifyService.register(FLA_Currency.class);
		ObjectifyService.register(FLA_FundCategory.class);
		ObjectifyService.register(FLA_FundIndex.class);
		ObjectifyService.register(FLA_FundInfo.class);

      ObjectifyService.register(FLA_FundPortfolio.class);
      ObjectifyService.register(FLA_Cache.class);
//		ObjectifyService.register(FLA_WorkOrder.class);
//		ObjectifyService.register(FLA_Report_Entity_Raw.class);
    }

	/**
	 * 
	 */
	public static Ref<FLA_FundCategory> insertOrUpdateFundCategory(String categoryName) {
		Key<FLA_FundCategory> key = null;
		FLA_FundCategory f = ofy().load().type(FLA_FundCategory.class).id(categoryName).now();
		if (f == null) {
			key = ofy().save().entity(FLA_FundCategory.instantiate(categoryName)).now();
		} else {
			key = Key.create(FLA_FundCategory.class, categoryName);
		}

		return Ref.create(key);
	}
	
	/**
	 * 
	 */
	public static Ref<FLA_FundIndex> insertOrUpdateFundIndex(String index) {
		Key<FLA_FundIndex> key = null;
		FLA_FundIndex f = ofy().load().type(FLA_FundIndex.class).id(index).now();
		if (f == null) {
			key = ofy().save().entity(FLA_FundIndex.instantiate(index)).now();
		} else {
			key = Key.create(FLA_FundIndex.class, index);
		}
		
		return Ref.create(key);
	}

	/**
	 * 
	 */
	public static Ref<FLA_Currency> insertOrUpdateCurrency(String currency) {
		Key<FLA_Currency> key = null;
		FLA_Currency f = ofy().load().type(FLA_Currency.class).id(currency).now();
		if (f == null) {
			key = ofy().save().entity(FLA_Currency.instantiate(currency)).now();
		} else {
			key = Key.create(FLA_Currency.class, currency);
		}
		
		return Ref.create(key);
	}

	/**
	 * 
	 */
	public static FLA_FundInfo getFundInfoByURL(String url) {
		FLA_FundInfo fundInfo = ofy().load().type(FLA_FundInfo.class).filter("mURL", url).first().now();
		return fundInfo;
	}

    /**
     *
     */
    public static FLA_FundInfo getFundInfoByTypeAndURL(String type, String url) {
        Iterator<FLA_FundInfo> fundInfos = ofy().load().type(FLA_FundInfo.class).filter("mURL", url).iterator();
        while (fundInfos.hasNext()) {
            FLA_FundInfo fi = fundInfos.next();
            if (type.equals(fi.mType)) {
                return fi;
            }
        }
        return null;
    }

    /**
     *
     */
    public static FLA_FundInfo getFundInfoByTypeAndName(String type, String name) {
        Iterator<FLA_FundInfo> fundInfos = ofy().load().type(FLA_FundInfo.class).filter("mName", name).iterator();
        while (fundInfos.hasNext()) {
            FLA_FundInfo fi = fundInfos.next();
            if (type.equals(fi.mType)) {
                return fi;
            }
        }
        return null;
    }

	/**
	 * 
	 */
	public static void deleteFundInfo(FLA_FundInfo fiE) throws Exception {
		ofy().delete().entity(fiE).now();
		EE.getEE().dinfo(log, TAG, "DS.deleteFundInfo: " + fiE.mType + "." + fiE.mName + " [" + fiE.mId + "]");
	}
	
	/**
	 * 
	 */
	public static Ref<FLA_FundInfo> saveFundInfoAndUpdateFundIndex(
			IndentWriter iwArg,
			FLA_FundInfo fi) throws Exception {
		if (iwArg == null) {
			iwArg = new IndentWriter();
		}

		Ref<FLA_FundInfo> fundInfoRef = null;
		if (fi.mId == null) {
			IndentWriter iw = new IndentWriter();
			iw.println("*** NEW FLA_FundInfo will be inserted, are you sure you know what you are doing");
			iw.push();
			fi.dumpInfo(iw);
			iw.pop();
			Key<FLA_FundInfo> fundInfoKey = ofy().save().entity(fi).now();
			fundInfoRef = Ref.create(fundInfoKey);
			iw.println("...key became: " + fundInfoKey.getId());

			iw.println("...");
			iw.println("Stack trace");
			iw.println(MM.getStackTraceString(new Exception()));
			iw.println("...");
			iw.println("Details");
			iw.println(iwArg.getString());
			EE.getEE().dsevere(log, TAG, iw.getString());
		} else {
//			EE.getEE().dinfo(log, TAG, "Now saving fund: " + fi.getTypeAndName());
			Key<FLA_FundInfo> fundInfoKey = ofy().save().entity(fi).now();
//			EE.getEE().dinfo(log, TAG, "...saved");
			fundInfoRef = Ref.create(fundInfoKey);
//			EE.getEE().dinfo(log, TAG, "...key created");
		}

        FLA_FundInfo finfo = fundInfoRef.get();

        if (finfo.mIndexCompare != null) {
            FLA_FundIndex findex = finfo.mIndexCompare.get();
            if (findex != null) {
                List<FLA_FundDPDay> dpDayList = finfo.mDPDays;
                for (int i=0; i < dpDayList.size(); i++) {
                    findex.add(dpDayList.get(i).mDateYYMMDD, finfo);
                }
//				EE.getEE().dinfo(log, TAG, "Now saving index: " + findex.mKey_IndexName + ", for fund: " + fi.getTypeAndName());
                ofy().save().entity(findex).now();
//				EE.getEE().dinfo(log, TAG, "...saved");
            }
        }

//		EE.getEE().dinfo(log, TAG, "Returning successfully");
		return fundInfoRef;
	}

	/**
	 *
	 */
	public static FLA_FundInfo getFundInfoById(long id) {
		FLA_FundInfo fundInfo = ofy().load().type(FLA_FundInfo.class).id(id).now();
		return fundInfo;
	}

	/**
	 * 
	 */
	public static FLA_FundInfo getFundInfo(Key<FLA_FundInfo> fiKey) {
		FLA_FundInfo fundInfo = ofy().load().type(FLA_FundInfo.class).id(fiKey.getId()).now();
		return fundInfo;
	}
	
	/**
	 * 
	 */
	public static void deleteAllOfDatastore(IndentWriter iwError, IndentWriter iwInfo) throws Exception {
		EE ee = EE.getEE();

		int count = 0;
		int totalCount = 0;
		long timeStop = 0;
		long timeDelta = 0;
		long timeStart = 0;
		
		count = 0;
		totalCount = 0;
		ee.dinfo(log, TAG, "Will now delete FLA_FundInfo");
		timeStart = System.currentTimeMillis();
		do {
			List<FLA_FundInfo> al = ofy().load().type(FLA_FundInfo.class).list();
			for (int i=0; i < al.size(); i++) {
				ofy().delete().key(Key.create(FLA_FundInfo.class,  al.get(i).mId)).now();
			}
			count = al.size();
			totalCount += count;
		} while(count > 0 && EE.timerContinue());
		timeStop = System.currentTimeMillis();		
		timeDelta = (timeStop - timeStart)/1000;
		ee.dinfo(log, TAG, "DONE. It took: " + timeDelta + ", to delete: " + totalCount + " entries");
		if (!EE.timerContinue()) {
			ee.dinfo(log, TAG, "We have no more time");
			return;
		}

		count = 0;
		totalCount = 0;
		ee.dinfo(log, TAG, "Will now delete FLA_Currency");
		timeStart = System.currentTimeMillis();
		do {
			List<FLA_Currency> al = ofy().load().type(FLA_Currency.class).limit(1000).list();
			for (int i=0; i < al.size(); i++) {
				ofy().delete().key(Key.create(FLA_Currency.class,  al.get(i).mKey_CurrencyName)).now();
			}
			count = al.size();
			totalCount += count;
		} while(count > 0 && EE.timerContinue());
		timeStop = System.currentTimeMillis();		
		timeDelta = (timeStop - timeStart)/1000;
		ee.dinfo(log, TAG, "DONE. It took: " + timeDelta + ", to delete: " + totalCount + " entries");
		if (!EE.timerContinue()) {
			ee.dinfo(log, TAG, "We have no more time");
			return;
		}
		
		count = 0;
		totalCount = 0;
		ee.dinfo(log, TAG, "Will now delete FLA_FundCategory");
		timeStart = System.currentTimeMillis();
		do {
			List<FLA_FundCategory> al = ofy().load().type(FLA_FundCategory.class).limit(1000).list();
			for (int i=0; i < al.size(); i++) {
				ofy().delete().key(Key.create(FLA_FundCategory.class,  al.get(i).mKey_CategoryName)).now();
			}
			count = al.size();
			totalCount += count;
		} while(count > 0 && EE.timerContinue());
		timeStop = System.currentTimeMillis();		
		timeDelta = (timeStop - timeStart)/1000;
		ee.dinfo(log, TAG, "DONE. It took: " + timeDelta + ", to delete: " + totalCount + " entries");
		if (!EE.timerContinue()) {
			ee.dinfo(log, TAG, "We have no more time");
			return;
		}
		
		count = 0;
		totalCount = 0;
		ee.dinfo(log, TAG, "Will now delete FLA_FundIndex");
		timeStart = System.currentTimeMillis();
		do {
			List<FLA_FundIndex> al = ofy().load().type(FLA_FundIndex.class).limit(1000).list();
			for (int i=0; i < al.size(); i++) {
				ofy().delete().key(Key.create(FLA_FundIndex.class,  al.get(i).mKey_IndexName)).now();
			}
			count = al.size();
			totalCount += count;
		} while(count > 0 && EE.timerContinue());
		timeStop = System.currentTimeMillis();		
		timeDelta = (timeStop - timeStart)/1000;
		ee.dinfo(log, TAG, "DONE. It took: " + timeDelta + ", to delete: " + totalCount + " entries");
		if (!EE.timerContinue()) {
			ee.dinfo(log, TAG, "We have no more time");
			return;
		}
		
		timeStop = System.currentTimeMillis();		
		timeDelta = (timeStop - timeStart)/1000;
		ee.dinfo(log, TAG, "DONE. It took: " + timeDelta + ", to delete: " + totalCount + " entries");
		if (!EE.timerContinue()) {
			ee.dinfo(log, TAG, "NO MORE TIME");
			return;
		}
	}

}

/*
import static com.googlecode.objectify.ObjectifyService.ofy;

Car porsche = new Car("2FAST", RED);
ofy().save().entity(porsche).now();    // async without the now()

assert porsche.id != null;    // id was autogenerated

// Get it back
Result<Car> result = ofy().load().key(Key.create(Car.class, porsche.id));  // Result is async
Car fetched1 = result.now();    // Materialize the async value

// More likely this is what you will type
Car fetched2 = ofy().load().type(Car.class).id(porsche.id).now();

// Or you can issue a query
Car fetched3 = ofy().load().type(Car.class).filter("license", "2FAST").first().now();

// Change some data and write it
porsche.color = BLUE;
ofy().save().entity(porsche).now();    // async without the now()

// Delete it
ofy().delete().entity(porsche).now();    // async without the now()*/

/* 
QUERY
@Entity
class Car {
    @Id String vin; // Can be Long, long, or String
    String color;
}	  
ofy().save().entity(new Car("123123", "red")).now();
Car c = ofy().load().type(Car.class).id("123123").now();
ofy().delete().entity(c);			

// Operators are >, >=, <, <=, in, !=, <>, =, ==
List<Car> cars = ofy().load().type(Car.class).filter("year >", 1999).list();
List<Car> cars = ofy().load().type(Car.class).filter("year >=", 1999).list();
List<Car> cars = ofy().load().type(Car.class).filter("year !=", 1999).list();
List<Car> cars = ofy().load().type(Car.class).filter("year in", yearList).list();

// No operator means ==
Car car = ofy().load().type(Car.class).filter("vin", "123456789").first().now();

// The Query itself is Iterable
Query<Car> q = ofy().load().type(Car.class).filter("vin >", "123456789");
for (Car car: q) {
    System.out.println(car.toString());
}

// Queries within transactions require ancestor()
List<Car> cars = ofy().load().type(Car.class).ancestor(parent).list();

// You can filter keys as well
List<Car> range = ofy().load().filterKey(">=", startKey).filterKey("<", endKey).list();

// You can query for just keys, which will return Key objects much more efficiently than fetching whole objects
Iterable<Key<Car>> allKeys = ofy().load().type(Car.class).keys();

// Useful for deleting items
ofy().delete().keys(allKeys);
Query objects are immutable. You can build them up by reassigning the variable:

Query<Car> q = ofy().load().type(Car.class);
q = q.filter("vin >", "123456789");
q = q.filter("color", RED);
Query result objects (Iterable<?>, List<?>, Ref<?>) are inherently asynchronous. The Query itself does not start execution:

// Query implements Iterable, but this does not start an actual query
Iterable<Car> query = ofy().load().type(Car.class).filter("vin >", "123456789");

// This starts executing an asynchronous query
Iterable<Car> cars = ofy().load().type(Car.class).filter("vin >", "123456789").iterable();		
*/
