package com.pf.shared;

import java.util.ArrayList;
import java.util.List;

public class ExtractData {

    public String date;
    public int extractTotal;
    public int extractUpdated;
    public int countInvalid;
    public int countNotUpdated;

    public List<Pair<String, String>> urlsInvalid = new ArrayList<>();
    public List<Pair<String, String>> urlsNotUpdated = new ArrayList<>();
}
