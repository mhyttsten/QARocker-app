package com.pf.mr.be;

import java.io.IOException;
import java.util.logging.Logger;

import com.pf.shared.IndentWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Quizlet client Id: CEa4Mxvm4v
//    https://api.quizlet.com/2.0/sets/415?client_id=CEa4Mxvm4v&whitespace=1
//    https://quizlet.com/api/2.0/docs/making-api-calls#user-authenticated-calls

public class CronWorkServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(CronWorkServlet.class.getName());
    private static final String TAG = CronWorkServlet.class.getSimpleName();

    /**
     *
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            log.info("CronWorkServlet, we entered at: " + new java.util.Date());
            doGetImpl();
            log.info("...we are now finished at: " + new java.util.Date());
        } catch(Exception exc) {
            log.severe("CronWorkServlet, exception caught: " + exc.getMessage());
        }
    }

    /**
     *
     */
    public void doGetImpl() throws Exception {
        IndentWriter iw = null;

        String contentStr = null;

        log.info("*** Now starting another Cron Job");
        String[] authorizationStrings = Constants.getAuthorizationStrings();
        String[] urlQuizletSets = Constants.getQuizletSets();
        String[] urlQLSetIntoFirebase = Constants.getURLToPutQLSetsIntoFirebase();
        String[] quizletUser = Constants.getQLUserStrings();
        for (int i=0; i < authorizationStrings.length; i++) {
            execFlowFor(quizletUser[i], urlQuizletSets[i], authorizationStrings[i], urlQLSetIntoFirebase[i]);
            log.info(".................");
        }
    }

    public void execFlowFor(
            String quizletUser,
            String quizletSet,
            String authorizationString,
            String urlQLSetIntoFirebase) throws Exception {

        IndentWriter iw = new IndentWriter();
        String contentStr = null;

        log.info("Now retrieving Quizlet data and put into Firebase for user: " + quizletUser);

        {
            log.info("...Will now get from Quizlet");
            iw = new IndentWriter();
            byte[] r = Misc.getHttpContent(
                    quizletSet,
                    "GET",
                    null,
                    authorizationString,
                    null,
                    iw);
            if (r != null) {
                contentStr = new String(r);
                log.info("......Done, number of bytes retrieved: " + r.length);
                // log.info("......Content\n" + contentStr);
            } else {
                log.severe("......Received null from Quizlet:\n" + iw.getString());
            }
        }

//        log.info("...Converting JSON to object model");
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            List<QLSet> qlSets = mapper.readValue(contentStr,
//                    new TypeReference<List<QLSet>>() {
//                    }); // QLSet[].class also possible
//            log.info("......Number of sets: " + qlSets.size());
//        } catch(Exception exc) {
//            log.severe("*** Exception caught:\n" + exc.toString() + "\n" + exc.getMessage()
//                    + "\n" + Misc.getStackTraceString(exc));
//        }

        {
            log.info("...Will now clear Firebase sets path");
            iw = new IndentWriter();
            byte[] r = Misc.getHttpContent(
                    urlQLSetIntoFirebase,
                    "PUT",
                    "application/x-www-form-urlencoded",
                    null,
                    "{ }",
                    iw);
            if (r != null && !iw.getString().contains("responseCode: 200")) {
                String rstr = new String(r);
                log.severe("......Received non-null when clearing Firebase path rstr:\n" + rstr);
                log.severe("......Received non-null when clearing Firebase path iw:\n" + iw.getString());
            }
            log.info("......Done");
        }

        {
            log.info("...Will now insert Quizlet sets into Firebase");
            iw = new IndentWriter();
            // contentStr = "{\"alanisawesome\":{\"birthday\":\"June 23, 1912\",\"name\":\"Alan Turing\"}}";
            byte[] r = Misc.getHttpContent(
                    urlQLSetIntoFirebase,
                    "PUT",
                    "application/x-www-form-urlencoded",
                    null,
                    contentStr,
                    iw);
            if (r != null) {
                String rstr = new String(r);
                log.info("......Done, number of bytes retrieved: " + r.length);
            } else {
                log.severe("......Received null from Firebase: " + iw.getString());
            }
        }
    }
}
