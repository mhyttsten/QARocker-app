package com.pf.fl.be.vanguard;

import com.pf.shared.utils.OTuple2G;
import com.pf.shared.utils.MM;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GetVanguardFunds {


    public static void main(String[] args) {
        try {
            mainImpl(args);
        } catch(Exception exc) {
            System.out.println(exc);
            exc.printStackTrace();
        }
    }

    public static void mainImpl(String[] args) throws Exception {
        FileInputStream fin = new FileInputStream(args[0]);
        int io = args[0].lastIndexOf("/");
        String directory = null;
        if (io != -1) {
            directory = args[0].substring(0, io);
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int dataInt = -1;
        do {
            dataInt = fin.read();
            if(dataInt != -1) {
                bout.write(dataInt & 0xFF);
            }
        }
        while(dataInt != -1);
        fin.close();
        byte[] data = bout.toByteArray();
        String html = new String(data, "UTF-8");

        processFunds_FundNameList(directory, html);
    }

    // Saves name of funds
    // From search result | Fees And Minimums view
    public static void processFunds_FundNameList(String directory, String html) throws Exception {
        int io = -1;

        OTuple2G<String, String> o = new OTuple2G<>();

        io = html.indexOf("Invest Now");
        if (io == -1) {
            throw new AssertionError("Cound not find \"Invest Now\"");
        }

        html = html.substring(io);
        o._o2 = html;

        List<String> fundNames = new ArrayList<>();
        while (true) {
            String tag_tr = MM.assignAndReturnNextTagValue(o, "<tr");
            if (tag_tr == null) {
                break;
            }
            String tag_a = MM.assignAndReturnNextTagValue(o, "<a");
            System.out.println("New fund: " + tag_a);

            fundNames.add(tag_a);

            io = o._o2.indexOf("<tr");
            int io2 = o._o2.indexOf("</tbody");
            if (io != -1 && io2 != -1 && io2 < io) {
                break;
            }
        }
        String path = directory;
        if (directory != null) {
            path += "/VG_RESULT.txt";
        } else {
            path = "VG_RESULT.txt";
        }

        FileOutputStream fout = new FileOutputStream(path);
        for (String s: fundNames) {
            fout.write((s+"\n").getBytes("UTF-8"));
        }
        fout.close();
    }

    public static void processFunds_FeesAndMinimums(String html) throws Exception {
        int io = -1;

        OTuple2G<String, String> o = new OTuple2G<>();
        o._o2 = html;

        io = html.indexOf("Fund Name (Ticker)");
        if (io == -1) {
            throw new AssertionError("Cound not find \"Fund Name (Ticker)\"");
        }

        System.out.println("index at: " + io);
        html = html.substring(io);
        io = html.indexOf("</table>");
        if (io == -1) {
            throw new AssertionError("Count not find end of table");
        }

        o._o2 = html.substring(0, io);

        while (true) {
            String tag_tr = MM.assignAndReturnNextTagValue(o, "<tr");
            if (tag_tr == null || tag_tr.trim().length() == 0) {
                break;
            }

            OTuple2G<String, String> o2 = new OTuple2G<>();
            o2._o2 = tag_tr;

            String fundName = MM.assignAndReturnNextTagValue(o, "<a");
            System.out.println("Fund name: " + fundName);

            String accountType = MM.assignAndReturnNextTagValue(o, "<td");
            System.out.println("...account type: " + accountType);

            String minimalInvestment = MM.assignAndReturnNextTagValue(o, "<td");
            System.out.println("...minimal investment: " + minimalInvestment);

            String transactionFee = MM.assignAndReturnNextTagValue(o, "<td");
            System.out.println("...transaction fee: " + transactionFee);

            String loadCommission = MM.assignAndReturnNextTagValue(o, "<td");
            System.out.println("...load commission: " + loadCommission);

            String purchaseFee = MM.assignAndReturnNextTagValue(o, "<td");
            System.out.println("...purchase fee: " + purchaseFee);

            String redemptionFee = MM.assignAndReturnNextTagValue(o, "<td");
            System.out.println("...redemption fee: " + redemptionFee);

            String m12b1Fee = MM.assignAndReturnNextTagValue(o, "<td");
            System.out.println("...12b-1 fee: " + m12b1Fee);
        }
    }

    public static void processSomething(String html, String fname) throws Exception {
        int io = -1;
        String s1 = "<span id=\"baseForm:fundFamilySelectOne_text\"";
        io = html.indexOf(s1);
        html = html.substring(io+s1.length());
        io = html.indexOf(">");
        html = html.substring(io+1);
        io = html.indexOf("<");
        String fundCompany = html.substring(0,io).trim();
        System.out.println("Company name: " + fundCompany);

        String s2 = "Select a fund to buy</td></tr>";
        io = html.indexOf(s2);
        html = html.substring(io+s2.length());

        String s3 = "<td ";
        String s4 = "</td></tr>";
        String s5 = "</table>";
        List<String> fundNames = new ArrayList<>();
        while (true) {
//            System.out.println(html.substring(0,100));
            io = html.indexOf(s3);
            int io2 = html.indexOf(s5);
            if (io == -1 || io2 < io) {
                System.out.println("Unexpected, could not find fund list boundary");
                break;
            }
            html = html.substring(io+1+s3.length());
            io = html.indexOf(">");
            html = html.substring(io+1);
            io = html.indexOf(s4);
            String fund = html.substring(0,io);
            fundNames.add(fund);
            System.out.println("Added fund: " + fund);
            html = html.substring(io+s4.length());
        }

        System.out.println("*** Result for fundCompany: [" + fundCompany + "]");

        String outfile = fname.substring(0, fname.indexOf(".")) + ".txt";
        FileOutputStream fout = new FileOutputStream(outfile);
        StringBuffer strb = new StringBuffer();
        for (String s: fundNames) {
            strb.append(fundCompany + "," + s + "\n");
        }
        byte[] ba = strb.toString().getBytes("UTF-8");
        fout.write(ba);
        fout.close();
        System.out.print(new String(ba, "UTF-8"));
    }

    public static void processFundCompanies_FromBuyDropdown(String s) throws Exception {
        int io = -1;
        String s1 = "Aberdeen";
        io = s.indexOf(s1);
        s = s.substring(io+s1.length());

        List<String> companies = new ArrayList<>();
        companies.add("Aberdeen");
        String s3 = "<tr><td ";
        String s4 = "</td>";
        while (true) {
            int len = s.length() < 1200 ? s.length() : 1200;
            System.out.println("Another round: " + s.substring(0, len));
            io = s.indexOf(s3);
            int io2 = s.indexOf("</tbody>");
            if (io2 == -1) throw new Exception("No </tbody>");
            if (io == -1 || io > io2) {
                break;
            }
            s = s.substring(io+s3.length());
            io = s.indexOf(">");
            s = s.substring(io+1);
            io = s.indexOf(s4);
            String company = s.substring(0, io).trim();
            companies.add(company);
            s = s.substring(io+s4.length());
        }
        FileOutputStream fout = new FileOutputStream("companies.txt");
        StringBuffer strb = new StringBuffer();
        for (String e: companies) {
            strb.append(e + "\n");
        }
        byte[] result = strb.toString().getBytes("UTF-8");
        fout.write(result);
        fout.close();
        System.out.print("Result\n" + new String(result, "UTF-8"));
    }
}
