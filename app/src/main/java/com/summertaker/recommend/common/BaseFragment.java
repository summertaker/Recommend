package com.summertaker.recommend.common;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.summertaker.recommend.R;
import com.summertaker.recommend.data.Item;

public class BaseFragment extends Fragment {

    protected String TAG;

    protected Context mContext;
    protected DataManager mDataManager;

    protected View mRootView;

    protected LinearLayout mLoProgress;
    protected ImageView mIvProgress;
    protected LinearLayout mLoContent;

    protected void initBaseFragment(Context context, View rootView) {
        TAG =  getClass().getSimpleName();

        mContext = context;
        mDataManager = new DataManager(context);

        mRootView = rootView;
    }

    protected void showBaseProgress() {
        if (mLoProgress == null) {
            mLoProgress = mRootView.findViewById(R.id.loProgress);
            mIvProgress = mRootView.findViewById(R.id.ivProgress);
            mLoContent = mRootView.findViewById(R.id.loContent);

            //mLoProgress = findViewById(R.id.loProgress);
            //mPbProgress = findViewById(R.id.pbProgress);
        }

        mLoProgress.setVisibility(View.VISIBLE);
        mIvProgress.startAnimation(BaseApplication.mRotateAnimation);
        mLoContent.setVisibility(View.GONE);
        //if (mPbProgress != null) {
        //    mPbProgress.setMax(mBaseDataTotal);
        //}
    }

    protected void hideBaseProgress() {
        mLoProgress.setVisibility(View.GONE);
        mIvProgress.clearAnimation();
        mLoContent.setVisibility(View.VISIBLE);
    }

    public void goToTheTop() {

    }

    public void notifyDataSetChanged() {

    }

    public void updateFragmentItem(Item item) {

    }

    public void refreshFragment() {

    }

    public void toggleChart() {

    }

    public int getItemSize() {
        return 0;
    }
}
