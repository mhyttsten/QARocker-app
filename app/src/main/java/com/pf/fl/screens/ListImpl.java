package com.pf.fl.screens;

import java.util.ArrayList;
import java.util.List;

public class ListImpl {

    public String mTitle;
    public Class mTargetClass;
    public List<HeaderAndBody> mHeaderAndBody = new ArrayList<>();

    public static class HeaderAndBody {
        public String mHeader;
        public String mBody;
        public HeaderAndBody(String header, String body) {
            mHeader = header;
            mBody = body;
        }
        public HeaderAndBody() { }
    };
}
