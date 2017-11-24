package com.pf.mr.be;

public class Constants {

    // Quizlet Access Tokens
    private static String QL_ACCESS_TOKEN_MagnusHyttsten_GoogleSignIn = "z6b5VZcr5cTzF838Ud9qvA9tfkx3YA9cN3rjvaxJ";
    public static String AUTHORIZATION_STRING_MagnusHyttsten_GoogleSignIn() {
        return "Bearer " + QL_ACCESS_TOKEN_MagnusHyttsten_GoogleSignIn;
    }
    private static String QL_USER_magnushyttsten = "magnushyttsten";

    private static String QL_ACCESS_TOKEN_gutesting = "z2v4aJappwAzXsvUsRVdKSuNsZbXtmc3BCXm4TE8";
    public static String AUTHORIZATION_STRING_gutesting() {
        return "Bearer " + QL_ACCESS_TOKEN_gutesting;
    }
    private static String QL_USER_gutesting = "gutesting";

    public static String[] getQLUserStrings() {
        return new String[]{
                QL_USER_magnushyttsten
//                ,
//                QL_USER_gutesting
        };
    }


    public static String[] getAuthorizationStrings() {
        return new String[]{
                AUTHORIZATION_STRING_MagnusHyttsten_GoogleSignIn()
//                ,
//                AUTHORIZATION_STRING_gutesting()
        };
    }

    public static String[] getQuizletSets() {
        String url = "https://api.quizlet.com/2.0/users/";
        return new String[] {
                url + QL_USER_magnushyttsten + "/sets"
//                ,
//                url + QL_USER_gutesting + "/sets"
        };
    }

    public static final String[] getURLToPutQLSetsIntoFirebase() {
        return new String[] {
                "https://ql-magnushyttsten.firebaseio.com/sets.json"
//                ,
//                "https://ql-gutester.firebaseio.com/sets.json"
        };
    }
}
