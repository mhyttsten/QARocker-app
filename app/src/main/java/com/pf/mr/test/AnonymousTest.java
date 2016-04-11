package com.pf.mr.test;

import java.util.Date;
import java.util.List;

public class AnonymousTest {

    private String mMember;


    public static void main(String[] args) {
        new AnonymousTest().doTheJob(null, null, null);
    }

    public static void doTheJob(List<String> work, List<Date> result, Runnable r) {


        String w = work.remove(0);



    }


}
