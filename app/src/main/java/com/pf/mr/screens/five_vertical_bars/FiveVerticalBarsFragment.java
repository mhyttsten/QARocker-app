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
import com.pf.mr.utils.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FiveVerticalBarsFragment extends Fragment {
    public static final String TAG = FiveVerticalBarsFragment.class.getSimpleName();

    private String mEmail;
    private String mSetName;
    public ESet mESet;

    private View mView;
    private boolean mHasData;
    private int mHeight = -1;
    private boolean mViewHeightUpdated;

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
                adjustBarHeights();
            }
        });

        final List<ESet> esets = new ArrayList<>();
        Misc.getESets(mEmail, mSetName, esets, new Runnable() {
            @Override
            public void run() {
                if (esets.size() != 1) {
                    Log.e(TAG, "*** Error, expected 1 result from getting sets");
                    return;
                }
                mESet = esets.get(0);
                Log.i(TAG, "Firebase data received, set name: " + mESet.mSet.title);
                mHasData = true;
                adjustBarHeights(mESet.mStatsAll);
            }
        });

        return mView;
    }

    public void adjustBarHeights(List<StatTermForUser> l) {
        if (mHasData && mHeight > 0 && !mViewHeightUpdated) {
            mViewHeightUpdated = true;
            adjustBarHeightsImpl(l);
        }
    }

    public void adjustBarHeightsImpl(List<StatTermForUser> l) {
        Log.i(TAG, "setBarHeight");

        ECalculateStats ecs = new ECalculateStats();
        ecs.addAll(l);

        float totalF = ecs.mCTotal;
        float c1ratio = (float)ecs.mCL1 / totalF;
        float c2ratio = (float)ecs.mCL2 / totalF;
        float c3ratio = (float)ecs.mCL3 / totalF;
        float c4ratio = (float)ecs.mCL4 / totalF;
        float c5ratio = (float)ecs.mCL5 / totalF;

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

        v1.setText(String.valueOf(ecs.mCL1));
        v2.setText(String.valueOf(ecs.mCL2));
        v3.setText(String.valueOf(ecs.mCL3));
        v4.setText(String.valueOf(ecs.mCL4));
        v5.setText(String.valueOf(ecs.mCL5));

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

    public void setData(String email, String setName) {
        mEmail = email;
        mSetName = setName;
    }


}
