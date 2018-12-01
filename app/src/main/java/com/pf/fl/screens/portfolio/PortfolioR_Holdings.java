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
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.pf.fl.screens.main.FLSingleton;
import com.pf.fl.screens.main.MainActivity_RV_Adapter;
import com.pf.mr.R;

public class PortfolioR_Holdings extends Fragment {
    private Activity _parentActivity;

    public static PortfolioR_Holdings newInstance(Activity parentActivity) {
        PortfolioR_Holdings fragment = new PortfolioR_Holdings();
        fragment._parentActivity = parentActivity;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.portfolio_r_holdings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView(view);
    }

// *************************************************************
    // RecyclerView setup

    private PortfolioR_Holdings_RV_Adapter _rvAdapter;

    private void setupRecyclerView(View v) {
        _rvAdapter = new PortfolioR_Holdings_RV_Adapter(_parentActivity);
        RecyclerView recyclerView = (RecyclerView)v.findViewById(R.id.recycler_view_fl);
        recyclerView.setLayoutManager(new LinearLayoutManager(_parentActivity, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(_rvAdapter);
    }

}
