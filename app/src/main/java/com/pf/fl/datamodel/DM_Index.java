package com.pf.fl.datamodel;

/**
 * Created by magnushyttsten on 10/7/16.
 */

public class DM_Index implements DM_NameId {
    private String name;
    public DM_Index(String s) { name = s; }
    public String getName() { return name; }
}
