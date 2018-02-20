package com.pf.mr.be;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pf.shared.utils.IndentWriter;

// Quizlet client Id: CEa4Mxvm4v
//    https://api.quizlet.com/2.0/sets/415?client_id=CEa4Mxvm4v&whitespace=1
//    https://quizlet.com/api/2.0/docs/making-api-calls#user-authenticated-calls

public class GetQuizletAccessToken extends HttpServlet {
    private static final Logger log = Logger.getLogger(GetQuizletAccessToken.class.getName());
    private static final String TAG = GetQuizletAccessToken.class.getSimpleName();

    // Response to access code: {
    //    "access_token":"z6b5VZcr5cTzF838Ud9qvA9tfkx3YA9cN3rjvaxJ",
    //    "expires_in":315360000,
    //    "token_type":"bearer",
    //    "scope":"read",
    //    "user_id":"magnushyttsten"}

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {

            /* https://quizlet.com/api-dashboard
            1. In Quizlet dashboard, make sure Redirect URI:
               https://srrocker-1262.appspot.com/getQuizletAccessToken

            1.5. Make sure to update the basic authorization string below, the code after "Basic "
               is available at: shttp://quizlet.com/api/2.0/docs/authorization-code-flow

            2. Using browser, go to:
               gutesting
               https://quizlet.com/authorize?response_type=code&client_id=3YUNUnEmme&scope=read&state=RANDOM_STRING

               magnus (Google Signin)
               https://quizlet.com/authorize?response_type=code&client_id=CEa4Mxvm4v&scope=read&state=RANDOM_STRING
               Authorize to send a code to the redirect URI )here)
               This will call this servlet with state=RANDOM_STRING and code=...

            3. Assign your basic authorization variable below, found at
               https://quizlet.com/api/2.0/docs/authorization-code-flow

            4. Use the code to send an HTTP request to Quizlet, and get a access_token
            */

            String basicAuthorizationCode = "M1lVTlVuRW1tZTpUZEdDV0dyVllKUUtHemVwY1pDazVn";

            log.info("GetQuizletAccessToken, we entered at: " + new java.util.Date());
            Enumeration<String> e = req.getParameterNames();
            while (e.hasMoreElements()) {
                String pname = e.nextElement();
                log.info(pname + " = [" + req.getParameter(pname) + "]");
            }

            String state = req.getParameter("state");
            String code = req.getParameter("code");

            String url = "https://api.quizlet.com/oauth/token"
                    + "?grant_type=authorization_code"
                    + "&code=" + code
                    + "&redirect_uri=https://srrocker-1262.appspot.com/getQuizletAccessToken";
            String basicAuthorization = "Basic " + basicAuthorizationCode;

            // Get the access token using the code we got
            log.info("About to send request to get access token");
            IndentWriter iw = new IndentWriter();
            byte[] result = Misc.getHttpContent(
                    url,
                    "POST",
                    null,
                    basicAuthorization,
                    null,
                    iw);
            log.info("Sent request, debug iw was");
            log.info(iw.getString());
            String resultStr = new String(result);
            log.info("Result: " + result);
            log.info("Now writing result");
            resp.getOutputStream().write(("Response to access code: " + resultStr
                    + "<br>Debug: " + iw.getString()).getBytes());
            log.info("Done, exiting");

        } catch(Exception exc) {
            log.severe("CronWorkServlet, exception caught: " + exc);
            log.severe("Stacktrace: " + Misc.getStackTraceString(exc));

        }
    }
}
