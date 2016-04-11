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

/**
 * A simple {@link Fragment} subclass.
 */
public class FiveVerticalBarsFragment extends Fragment {
    public static final String TAG = FiveVerticalBarsFragment.class.getSimpleName();

    private View mView;

    public FiveVerticalBarsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        mView = inflater.inflate(R.layout.fragment_five_vertical_bars, container, false);
        return mView;
    }

    public void adjustBarHeights(View fragmentView) {
        Log.i(TAG, "setBarHeight");
        final int pixelHeight = fragmentView.getHeight();
        final TextView v1 = (TextView)fragmentView.findViewById(R.id.fvb_col1);
        v1.getLayoutParams().height = pixelHeight / 2;
    }

}
