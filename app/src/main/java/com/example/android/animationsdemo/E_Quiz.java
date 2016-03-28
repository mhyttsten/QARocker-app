package com.example.android.animationsdemo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by magnushyttsten on 3/25/16.
 */
public class E_Quiz {
    public String mKey;
    public String mName;
    public List<E_QA> mQAs = new ArrayList<>();

    public E_Quiz() { }
    public E_Quiz(String name)  { mName = name; }

    public String toString() { return mKey + ": " + mName + ", qaSize: " + mQAs.size(); }

}
