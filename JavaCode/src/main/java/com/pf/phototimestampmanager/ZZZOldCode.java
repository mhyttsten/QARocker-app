package com.pf.phototimestampmanager;

/**
 * Created by magnushyttsten on 8/22/15.
 */
public class ZZZOldCode {
        /*
    public static OTuple3<Integer, String, String> execX(String command) throws Exception {
        // System.out.println("WILL EXEC: " + command);
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(command);
        InputStream istreamInput = p.getInputStream();
        InputStream istreamError = p.getErrorStream();
        int returnValue = p.waitFor();
        String stringOutput = new String(MM.readData(istreamInput));
        String stringError = new String(MM.readData(istreamError));

        OTuple3<Integer, String, String> ot = new OTuple3<>(new Integer(returnValue), stringOutput, stringError);
        if (returnValue != 0 || ot._o3.length() > 0) {
            IndentWriter iw = new IndentWriter();
            iw.println("*** Error for command: " + command);
            iw.println("    Exit code: " + ot._o1);
            iw.println("    Output:    " + ot._o2);
            iw.println("    Error:    " + ot._o3);
            throw new Exception(iw.getString());
        }

        //System.out.println("*** Output");
        //System.out.println(stringInput);
        //System.out.println("");
        //System.out.println("*** Error");
        //System.out.println(stringError);
        return ot;
    }
    */
}
