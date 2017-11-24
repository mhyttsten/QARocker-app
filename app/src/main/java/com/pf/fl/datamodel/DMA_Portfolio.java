package com.pf.fl.datamodel;

import com.pf.shared.MM;

import java.util.ArrayList;
import java.util.List;

public class DMA_Portfolio {

    public void setName(String aname) {
        name = aname;
        id = name;
    }

    public String id;
    public String name;
    public String date_created = MM.getNowAs_YYMMDD_HHMMSS(null);
    public List<Long> fund_ids = new ArrayList<>();
}
