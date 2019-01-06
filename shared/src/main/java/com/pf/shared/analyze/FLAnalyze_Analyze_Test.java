package com.pf.shared.analyze;

import com.pf.shared.datamodel.DB_FundInfo;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.D_Utils;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.Timer;

import java.util.ArrayList;
import java.util.List;

public class FLAnalyze_Analyze_Test {

    //------------------------------------------------------------------------
    public static void main(String[] args) {
        try {
            // test_analysis();
            IndentWriter iw = null;

            Timer t = new Timer();
            t.start();
            byte[] raw = MM.fileReadFrom("/Users/magnushyttsten/tmp/fundinfo-db-master.bin");
            t.mark("Reading file");
            DB_FundInfo.initialize(raw, true);

            int count = 0;
            int nofriday = 0;
            List<D_FundInfo> l = new ArrayList<>();
            String f = "181214";
            for (D_FundInfo fi: DB_FundInfo.getAllFundInfos()) {
                if (!fi._dpDays.get(0)._dateYYMMDD.equals(f)) {
                    System.out.println(fi);
                    nofriday++;
                }
                count += fi._dpDays.size();
            }
            System.out.println("No friday: " + nofriday);
            System.out.println("Average DPDs per fund: " + (count / DB_FundInfo.getAllFundInfos().size()));
            if (true)
                return;

            t.mark("Initialize DB");
            FLAnalyze_Analyze fla = new FLAnalyze_Analyze(DB_FundInfo.getAllFundInfos());
            t.mark("Instantiate FLAnalyze_Analyze");

            fla.setRange(4);
            t.mark("Called setRange1");
            iw = new IndentWriter();
            fla.analyze(iw);
            t.mark("Called analyze1");

            fla.setRange(8);
            t.mark("Called setRange2");
            iw = new IndentWriter();
            fla.analyze(iw);
            t.mark("Called analyze2");

            System.out.println(t);



        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }

    //------------------------------------------------------------------------
    public static void test_analysis() {
        List<D_FundInfo> fis = new ArrayList<>();
        fis.add(create_D_FundInfo(D_FundInfo.TYPE_PPM, "FL01", 8,
                new float[] { 0.40F, 0.40F, 0.40F, 0.40F, 0.40F, 0.40F, 0.40F, 0.40F}));
        fis.add(create_D_FundInfo(D_FundInfo.TYPE_PPM, "FL02", 8,
                new float[] { 0.90F, 0.50F, 0.50F, 0.50F, 0.50F, 0.50F, 0.10F, 0.50F}));
        fis.add(create_D_FundInfo(D_FundInfo.TYPE_PPM, "FL03", 8,
                new float[] { 0.10F, 0.10F, 0.10F, 0.10F, 0.10F, 0.10F, 0.10F, 0.10F}));
        fis.add(create_D_FundInfo(D_FundInfo.TYPE_PPM, "FL04", 8,
                new float[] { 0.80F, 0.80F, 0.80F, D_FundDPDay.FLOAT_NULL, 0.80F, 0.80F, 0.80F, D_FundDPDay.FLOAT_NULL}));

        FLAnalyze_Analyze fla = new FLAnalyze_Analyze(fis);
        fla.setRange(4);
        IndentWriter iw = new IndentWriter();
        fla.analyze(iw);

        fla.setRange(5);
        fla.analyze(iw);

        for (FLAnalyze_Analyze.FundRank fr: fla._frSummary) {
            System.out.println(fr._fi.getTypeAndName() + ", avg_rank: " + fr.getAverageRank_2F() + ", acc_r1w: " + fr._r1w);
        }
    }

    //------------------------------------------------------------------------
    private static D_FundInfo create_D_FundInfo(String type, String name, int weekCount, float[] r1ws) {
        D_FundInfo fi = new D_FundInfo();
        fi._type = type;
        fi._nameMS = name;
        fi._nameOrig = name;

        String[] fridays = D_Utils.getRecentDates(weekCount);
        for (int i=0; i < fridays.length; i++) {
            D_FundDPDay dpd = new D_FundDPDay();
            dpd._dateYYMMDD = fridays[i];
            dpd._r1w = r1ws[i];
            fi._dpDays.add(dpd);
        }
        return fi;
    }


}
