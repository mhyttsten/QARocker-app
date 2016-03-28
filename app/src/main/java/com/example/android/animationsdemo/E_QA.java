package com.example.android.animationsdemo;

/**
 * Created by magnushyttsten on 3/25/16.
 */
public class E_QA {

    public String mKey;
    public String mQ;
    public String mA;

    public E_QA() { }
    public E_QA(String key, String q, String a)  { mKey = key; mQ = q; mA = a; }

    public void setKey(String key) { mKey = key; }
    public String getKey() { return mKey; }
    public void setQ(String q) { mQ = q; }
    public String getQ() { return mQ; }
    public void setA(String a) { mA = a; }
    public String getA() { return mA; }
}
