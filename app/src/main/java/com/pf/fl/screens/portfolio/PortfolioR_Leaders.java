/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.pf.fl.screens.portfolio;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.fl.screens.main.FLSingleton;
import com.pf.fl.screens.utils.MM_UIUtils;
import com.pf.mr.R;
import com.pf.shared.datamodel.D_FundInfo;

import java.util.List;

public class PortfolioR_Leaders extends Fragment {
    private PortfolioR_Activity _parentActivity;

    public static PortfolioR_Leaders newInstance(PortfolioR_Activity parentActivity) {
        PortfolioR_Leaders fragment = new PortfolioR_Leaders();
        fragment._parentActivity = parentActivity;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.portfolio_r_leaders, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView tv = (TextView)view.findViewById(R.id.tv01);
        int v = MM_UIUtils.getTextViewAsInt(tv);

        Button bl = (Button)view.findViewById(R.id.b01);
        bl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int oldV = MM_UIUtils.getTextViewAsInt(tv);
                if (oldV > 1) {
                    oldV--;
                    tv.setText(String.valueOf(oldV));
                    _rvAdapter.setRange(oldV);
                    _rvAdapter.notifyDataSetChanged();
                }
            }
        });
        Button br = (Button)view.findViewById(R.id.b02);
        br.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int oldV = MM_UIUtils.getTextViewAsInt(tv);
                oldV++;
                tv.setText(String.valueOf(oldV));
                _rvAdapter.setRange(oldV);
                _rvAdapter.notifyDataSetChanged();
            }
        });

        List<D_FundInfo> fis = DB_FundInfo_UI._fundsByType.get(FLSingleton._portfolioName);
        setupRecyclerView(view, v, fis);
    }

// *************************************************************
    // RecyclerView setup

    private PortfolioR_Leaders_RV_Adapter _rvAdapter;

    private void setupRecyclerView(View v, int weekCount, List<D_FundInfo> fis) {
        _rvAdapter = new PortfolioR_Leaders_RV_Adapter(_parentActivity, this, fis);
        _rvAdapter.setRange(weekCount);
        RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recycler_view_fl);
        recyclerView.setLayoutManager(new LinearLayoutManager(_parentActivity, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(_rvAdapter);
    }

}
