package com.pf.fl.datamodel;


public interface FL_DBCallback {
    void callback(boolean isError, String errorMessage, Object result);
}
