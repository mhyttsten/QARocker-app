package com.pf.mr.screens.five_vertical_bars;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.pf.mr.R;
import com.pf.mr.datamodel.StatTermForUser;
import com.pf.mr.execmodel.ECalculateStats;
import com.pf.mr.execmodel.ESet;
import com.pf.mr.screens.display_set_stats.RehearsalFinishedActivity;
import com.pf.mr.utils.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FiveVerticalBarsFragment extends Fragment {
    public static final String TAG = FiveVerticalBarsFragment.class.getSimpleName();

    public List<ESet> mESets = new ArrayList<>();

    private View mView;
    private boolean mHasData;
    private int mHeight = -1;
    private boolean mViewHeightUpdated;
    private RehearsalFinishedActivity mRFA;
    private ECalculateStats mECS;

    public FiveVerticalBarsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        mView = inflater.inflate(R.layout.fragment_five_vertical_bars, container, false);

        ViewTreeObserver vtObserver2 = mView.getViewTreeObserver();
        vtObserver2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.i(TAG, "***** onGlobalLayout.fragmentView, width: " + mView.getWidth() + ", height: " + mView.getHeight());
                mHeight = mView.getHeight();
                adjustBarHeights(mESets);
            }
        });

        final List<ESet> esets = new ArrayList<>();
        Misc.getESets(mRFA.mUserEmail, mRFA.mSetName, esets, new Runnable() {
            @Override
            public void run() {
                mESets = esets;
                Log.i(TAG, "Firebase data received");
                mHasData = true;
                adjustBarHeights(mESets);
            }
        });

        return mView;
    }

    public void adjustBarHeights(List<ESet> sets) {
        if (mHasData && mHeight > 0 && !mViewHeightUpdated) {
            mViewHeightUpdated = true;
            adjustBarHeightsImpl(sets);
            mRFA.updateUI(mECS);
        }
    }

    public void adjustBarHeightsImpl(List<ESet> sets) {
        Log.i(TAG, "setBarHeight");

        mECS = new ECalculateStats();
        for (ESet set: sets) {
            mECS.addAll(set.mStatsAll);
        }

        float totalF = mECS.mCTotal;
        float c1ratio = (float)mECS.mCL1 / totalF;
        float c2ratio = (float)mECS.mCL2 / totalF;
        float c3ratio = (float)mECS.mCL3 / totalF;
        float c4ratio = (float)mECS.mCL4 / totalF;
        float c5ratio = (float)mECS.mCL5 / totalF;

        int v1Height = (int)(mHeight * c1ratio);
        int v2Height = (int)(mHeight * c2ratio) ;
        int v3Height = (int)(mHeight * c3ratio);
        int v4Height = (int)(mHeight * c4ratio);
        int v5Height = (int)(mHeight * c5ratio);

        final TextView v1 = (TextView)mView.findViewById(R.id.fvb_col1);
        final TextView v2 = (TextView)mView.findViewById(R.id.fvb_col2);
        final TextView v3 = (TextView)mView.findViewById(R.id.fvb_col3);
        final TextView v4 = (TextView)mView.findViewById(R.id.fvb_col4);
        final TextView v5 = (TextView) mView.findViewById(R.id.fvb_col5);

        v1.setText(String.valueOf(mECS.mCL1));
        v2.setText(String.valueOf(mECS.mCL2));
        v3.setText(String.valueOf(mECS.mCL3));
        v4.setText(String.valueOf(mECS.mCL4));
        v5.setText(String.valueOf(mECS.mCL5));

        if (v1Height > 0) {
            v1.getLayoutParams().height = v1Height;
            v1.setHeight(v1Height);
        }
        if (v2Height > 0) {
            v2.getLayoutParams().height = v2Height;
            v2.setHeight(v2Height);
        }
        if (v3Height > 0) {
            v3.getLayoutParams().height = v3Height;
            v3.setHeight(v3Height);
        }
        if (v4Height > 0) {
            v4.getLayoutParams().height = v4Height;
            v4.setHeight(v4Height);
        }
        if (v5Height > 0) {
            v5.getLayoutParams().height = v5Height;
            v5.setHeight(v5Height);
        }
    }

    public void setParentActivity(RehearsalFinishedActivity rfa) {
        mRFA = rfa;
    }


}
