package com.pf.shared;

import java.util.ArrayList;
import java.util.List;
import com.pf.shared.utils.OTuple2G;

public class ExtractData {

    public String date;
    public int extractTotal;
    public int extractUpdated;
    public int countInvalid;
    public int countNotUpdated;

    public List<OTuple2G<String, String>> urlsInvalid = new ArrayList<>();
    public List<OTuple2G<String, String>> urlsNotUpdated = new ArrayList<>();
}
