package com.pf.fl.screens.main;

import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.shared.analyze.FLAnalyze_Analyze;
import com.pf.shared.datamodel.D_Analyze_FundRank;
import com.pf.shared.datamodel.D_FundInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FLSingleton {
    public static String _portfolioName;
    public static Map<
            String,
            Map<String, D_Analyze_FundRank.D_Analyze_FundRankElement[]>> _type2Matrix
            = new HashMap<>();

    //------------------------------------------------------------------------
    public static void initialize() {
        // Initialize all the fund rank ranges for all fund types

        for (String type: D_FundInfo.TYPES) {

            List<D_FundInfo> fundsByType = DB_FundInfo_UI._fundsByType.get(type);

            Map<String, D_Analyze_FundRank.D_Analyze_FundRankElement[]> r = null;
            r = FLAnalyze_Analyze.setMaxRange(
                    type,
                    fundsByType,
                    16);

            _type2Matrix.put(type, r);
        }
    }
}
