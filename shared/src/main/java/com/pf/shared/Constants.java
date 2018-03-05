package com.pf.shared;

/**
 * Created by magnushyttsten on 2/19/18.
 */

public class Constants {

    public static final long TIMEINS_BEFORE_DEADLINE = 8*60;

    public static final String TIMEZONE_STOCKHOLM = "Europe/Stockholm";
    public static final String TIMEZONE_NEW_YORK  = "America/New_York";

    public static final String ENCODING_HTTP_READ = "UTF-8";
    public static final String ENCODING_FILE_READ = "UTF-8";
    public static final String ENCODING_FILE_WRITE = "UTF-8";

    public static final String BUCKET_PF = "mh-pffundlifter";
    public static final String BUCKET_QL = "ql-magnushyttsten.appspot.com";
//    public static final String BUCKET = BUCKET_PF;
    public static final String BUCKET = BUCKET_QL;

    public static final String PORTFOLIOS  = "backend/portfolios.bin";

    public static final String PORTFOLIO_DB_MASTER_BIN  = "backend/fundportfolio-db-master.bin";
    public static final String FUNDINFO_DB_MASTER_BIN  = "backend/fundinfo-db-master.bin";
    public static final String FUNDINFO_DB_MASTER      = "backend/fundinfo-db-master";
    public static final String FUNDINFO_DB_TEST_JSP      = "backend/fundinfo-db-test-jsp";
    public static final String PREFIX_FUNDINFO_DB      = "backend/fundinfo-db-date-";
    public static final String PREFIX_FUNDINFO_LOGS_DEBUG   = "backend/fundinfo-logs-debug-";
    public static final String PREFIX_FUNDINFO_LOGS_EXTRACT = "backend/fundinfo-logs-extract-";
    public static final String FUNDINFO_LOGS_EXTRACT_MASTER_TXT = "backend/fundinfo-logs-extract-master.txt";
    public static final String PREFIX_FUNDINFO_DELETED = "backend/fundinfo-deleted-";
    public static final String EXT_BIN   = ".bin";
    public static final String EXT_TXT   = ".txt";
}
