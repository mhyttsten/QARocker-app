package com.pf.fl.datamodel;

import com.pf.shared.utils.MM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DMA_Portfolio {
    private static final String TAG = DMA_Portfolio.class.getSimpleName();

    public void setName(String aname) {
        name = aname;
        id = name;
    }

    public String id;
    public String name;
    public String date_created = MM.getNowAs_YYMMDD_HHMMSS(null);
    public List<Long> fund_ids = new ArrayList<>();

    private Map<String, Void> hm;
    public boolean existFundInPortfolio(String fundName) {
        if (hm == null) {
            hm = new HashMap<>();
            for (Long l: fund_ids) {
                hm.put(DM_Transform.fundsByIdHM.get(l).name, null);
            }
        }

        return hm.containsKey(fundName);
    }
}
