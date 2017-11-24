package com.pf.mr.be;

/**
 * Created by magnushyttsten on 3/26/16.
 */
public class TestGetQuizletAndPutIntoFirebase {
/*
    public static void main(String[] args) {
        try {
            mainImpl(args);
        } catch(Exception exc) {
            System.out.println("Exception caught: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    public static void mainImpl(String[] args) throws Exception {
        IndentWriter iw = null;

        String contentStr = null;

        {
            System.out.println("*** Will now get from Quizlet");
            iw = new IndentWriter();
            byte[] r = Misc.getHttpContent(
                    Constants.URL_GET_QUIZLET_SETS(),
                    "GET",
                    null,
                    Constants.AUTHORIZATION_STRING(),
                    null,
                    iw);
            if (r != null) {
                contentStr = new String(r);
            }
        }

        {
            System.out.println("\n\n");
            System.out.println("*** Will now insert into Firebase");
            iw = new IndentWriter();
            // contentStr = "{\"alanisawesome\":{\"birthday\":\"June 23, 1912\",\"name\":\"Alan Turing\"}}";
            // contentStr = "{}";
            byte[] r = Misc.getHttpContent(
                    Constants.URL_PUT_SETS_INTO_FIREBASE(),
                    "PUT",
                    "application/x-www-form-urlencoded",
                    null,
                    contentStr,
                    iw);
            if (r != null) {
                String rstr = new String(r);
                System.out.println(rstr);
            }
            System.out.println("Result from HTTP:\n" + iw.getString());
        }
    }
    */
}
