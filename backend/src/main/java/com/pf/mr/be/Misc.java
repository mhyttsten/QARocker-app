package com.pf.mr.be;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.pf.shared.IndentWriter;

/**
 * Created by magnushyttsten on 3/26/16.
 */
public class Misc {

    public static String getStackTraceString(Throwable e) {
        StackTraceElement[] st = e.getStackTrace();
        String str = "";
        if (st == null)
            return "";
        for (int i = 0; i < st.length; i++)
            str = str + st[i].toString() + "\n";
        return str;
    }

    public static byte[] getHttpContent(String url,
                                        String method,
                                        String contentType,
                                        String authorization,
                                        String content,
                                        IndentWriter iw) throws Exception {
        if (iw == null) {
            iw = new IndentWriter();
        }

        iw.setIndentChar('.');
        iw.println("getHttpContent, entered");
        iw.push();
        URL object = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) object.openConnection();

        connection.setRequestMethod(method);
        connection.setReadTimeout(60 * 1000);
        connection.setConnectTimeout(60 * 1000);
        if (contentType != null) {
            connection.setRequestProperty("Content-Type", contentType);
        }
        if (authorization != null) {
            connection.setRequestProperty("Authorization", authorization);
        }
        if (content != null) {
            connection.setDoOutput(true);
            connection.getOutputStream().write(content.getBytes());
        }

        int responseCode = connection.getResponseCode();
        iw.println("responseCode: " + responseCode);
        String responseMsg = connection.getResponseMessage();
        iw.println("responseMessage: " + responseMsg);
        iw.pop();

        if (responseCode == 200) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] b = new byte[8 * 1024 * 1024];
            InputStream is = connection.getInputStream();
            int rlen = -1;
            while ((rlen = is.read(b)) > 0) {
                bout.write(b, 0, rlen);
            }
            return bout.toByteArray();

            //String encoding = connection.getContentEncoding() == null ? "UTF-8"
            //        : connection.getContentEncoding();
            // jsonResponse = IOUtils.toString(inputStr, encoding);

        }
        return null;
    }
}
