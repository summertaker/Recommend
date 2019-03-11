package com.summertaker.recommend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.summertaker.recommend.common.BaseApplication;
import com.summertaker.recommend.common.BaseFragment;
import com.summertaker.recommend.common.Config;
import com.summertaker.recommend.common.DataManager;
import com.summertaker.recommend.data.Item;
import com.summertaker.recommend.data.Site;
//import com.summertaker.recommend.detail.DetailActivity;
import com.summertaker.recommend.util.RecyclerTouchListener;
import com.summertaker.recommend.util.Util;

import java.util.ArrayList;

public class RecommendFragment extends BaseFragment {

    private Callback mEventListener;

    private int mPosition = -1;

    private boolean mIsLoading = false;

    private Site mSite;

    private ArrayList<Item> mItems = new ArrayList<>();
    private RecommendAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DividerItemDecoration mDividerItemDecoration;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mLowestPrice; // 최저가
    private int mHighestPrice; // 최고가
    private float mRateOfFluctuation; // 등락률

    private boolean mListMode = false;

    // Container Activity must implement this interface
    public interface Callback {
        void onRecommendFragmentEvent(String event);

        void onFragmentItemSizeChange(int position, int itemSize);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            try {
                mEventListener = (Callback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement Listener.");
            }
        }
    }

    public RecommendFragment() {
    }

    public static RecommendFragment newInstance(int position) {
        RecommendFragment fragment = new RecommendFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recommend_fragment, container, false);

        mContext = getContext();
        initBaseFragment(mContext, rootView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = getArguments().getInt("position", 0);
            mSite = BaseApplication.getInstance().getRecommendPagerItems().get(mPosition);
        }

        mAdapter = new RecommendAdapter(mContext, mPosition, mSite.getId(), mItems);
        mAdapter.setHasStableIds(true);

        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Item item = mItems.get(position);
                //Toast.makeText(mContext, item.getName() + " " + item.getNor(), Toast.LENGTH_SHORT).show();
                BaseApplication.getInstance().setItem(item);

                //Intent intent = new Intent(mContext, DetailActivity.class);
                //intent.putExtra("code", item.getCode());
                //intent.putExtra("nor", String.valueOf(item.getNor()));
                //startActivityForResult(intent, Config.ACTIVITY_REQUEST_CODE);

                Util.startKakaoStockDeepLink(mContext, item.getCode());
            }

            @Override
            public void onLongClick(View view, int position) {
                //mEventListener.onRecommendFragmentEvent(Config.PARAM_FINISH);

                //Util.startKakaoStockDeepLink(mContext, mItems.get(position).getCode());
                //mDataManager.updateMyItem(mItems.get(position).getCode(), Config.KEY_FAVORITES);
                //mEventListener.onFragmentEvent(Config.PARAM_DATA_CHANGED);
            }
        }));

        mDividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);

        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mLowestPrice = BaseApplication.getInstance().getIntSetting(Config.SETTING_RECOMMEND_LOWEST_PRICE); // 최저가
        mHighestPrice = BaseApplication.getInstance().getIntSetting(Config.SETTING_RECOMMEND_HIGHEST_PRICE); // 최고가
        mRateOfFluctuation = BaseApplication.getInstance().getFloatSetting(Config.SETTING_RECOMMEND_RATE_OF_FLUCTUATION); // 등락률
        //Log.e(TAG, mLowestPrice + " ~ " + mHighestPrice + ", " + mRateOfFluctuation);

        showBaseProgress();

        //final DataManager dm = new DataManager(mContext);

        //Log.e(TAG, "mSite.getId(): " + mSite.getId());
        if (mSite.getId().equals(Config.KEY_RECOMMEND_CURRENT)) {
            //-------------------
            // 현재 추천 종목
            //-------------------
            mDataManager.setOnRecommendCurrentItemLoaded(new DataManager.RecommendCurrentItemCallback() {
                @Override
                public void onLoad(ArrayList<Item> items) {
                    parseData(items);
                }
            });
            mDataManager.loadRecommendCurrentItem((Activity) mContext);
        } else if (mSite.getId().equals(Config.KEY_RECOMMEND_TOP)) {
            //-------------------
            // 추천수 상위
            //-------------------
            parseData(BaseApplication.getInstance().getRecommendTopItems());
            /*
            mDataManager.setOnRecommendTopItemLoaded(new DataManager.RecommendTopItemCallback() {
                @Override
                public void onLoad() {
                    parseData(BaseApplication.getInstance().getRecommendTopItems());
                }
            });
            mDataManager.loadRecommendTopItem((Activity) mContext);
            */
        } else if (mSite.getId().equals(Config.KEY_RECOMMEND_RETURN)) {
            //-------------------
            // 추천 종목 수익률
            //-------------------
            mDataManager.setOnRecommendReturnItemLoaded(new DataManager.RecommendReturnItemCallback() {
                @Override
                public void onLoad(ArrayList<Item> items) {
                    parseData(items);
                }
            });
            mDataManager.loadRecommendReturnItem((Activity) mContext);
        }

        return rootView;
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        //Log.e(">>", menuItem.getGroupId() + ", " + menuItem.getItemId());

        int position = menuItem.getOrder();
        if (position == mPosition) {
            int itemId = menuItem.getItemId();
            Item item = mItems.get(itemId);

            String tagId = String.valueOf(menuItem.getGroupId());
            mDataManager.setItemTagIds(item, tagId);

            //if (item.getTagIds().isEmpty()) {
            //    mItems.remove(itemId);
            //    mAdapter.notifyItemRemoved(itemId);
            //} else {
                mAdapter.notifyItemChanged(itemId);
            //}

            mSwipeRefreshLayout.setRefreshing(true);
            mDataManager.setOnItemTagSaved(new DataManager.ItemTagCallback() {
                @Override
                public void onItemTagSaved() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
            mDataManager.saveItemTag(item.getCode(), item.getTagIds());
        }

        return super.onContextItemSelected(menuItem);
    }

    /*
    private void parseRecommendTop() {
        for (Item bi : BaseApplication.getInstance().getItemPrices()) {
            if (bi.isRecommendTopItem() && isValidItem(bi)) {
                mItems.add(bi);
            }
        }

        // 추천수로 정렬
        Collections.sort(mItems, new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                if (a.getNor() < b.getNor()) {
                    return 1;
                } else if (a.getNor() > b.getNor()) {
                    return -1;
                }
                return 0;
            }
        });

        long id = 1;
        for (Item item : mItems) {
            item.setId(id);
            id++;
        }

        renderData();
    }
    */

    private void parseData(ArrayList<Item> items) {
        mItems.clear();

        long id = 1;
        for (Item item : items) {
            for (Item bi : BaseApplication.getInstance().getItemPrices()) {
                if (bi.getCode().equals(item.getCode())) {
                    //Log.e(TAG, bi.getName() + ": " + bi.getNor());
                    if (isValidItem(bi)) {
                        item.setId(id);
                        item.setPrice(bi.getPrice());   // 현재가
                        item.setPof(bi.getPof());       // 전일비
                        item.setRof(bi.getRof());       // 등락률
                        item.setTagIds(bi.getTagIds()); // 태그
                        item.setNor(bi.getNor());       // 추천수
                        mItems.add(item);
                        id++;

                        //if (mSite.getId().equals(Config.KEY_RECOMMEND_CURRENT)) {
                        //    Log.e(TAG, item.getName() + " " + item.getNor());
                        //}
                    }
                }
            }
        }

        /*
        long id = 1;
        for (Item item : items) {
            item.setId(id);
            mItems.add(item);
            id++;

            //if (mSite.getId().equals(Config.KEY_RECOMMEND_CURRENT)) {
            //    Log.e(TAG, item.getName() + " " + item.getNor());
            //}
        }
        */

        /*
        if (mSite.getId().equals(Config.KEY_RECOMMEND_TOP)) {
            // 추천수 정렬
            Collections.sort(mItems, new Comparator<Item>() {
                @Override
                public int compare(Item a, Item b) {
                    if (a.getNor() < b.getNor()) {
                        return 1;
                    } else if (a.getNor() > b.getNor()) {
                        return -1;
                    }
                    return 0;
                }
            });

            //long id = 1;
            //for (Item item : mItems) {
            //    item.setId(id);
            //    id++;
            //}
        }
        */

        /*
        for (Item item : mItems) {
            for (Portfolio portfolio : BaseApplication.getInstance().getPortfolios()) {
                if (portfolio.getCode().equals(item.getCode())) {
                    item.setTagIds(portfolio.getTagIds());
                }
            }
        }
        */

        renderData();
    }

    private void renderData() {
        for (Item item : mItems) {
            item.setListMode(mListMode);
        }

        mRecyclerView.removeItemDecoration(mDividerItemDecoration);
        if (mListMode) {
            mRecyclerView.addItemDecoration(mDividerItemDecoration); // Divider
        }

        hideBaseProgress();

        mAdapter.notifyDataSetChanged();
        mEventListener.onFragmentItemSizeChange(mPosition, mItems.size());
        //mEventListener.onFragmentEvent(Config.PARAM_LOAD_FINISHED);
        mIsLoading = false;
    }

    private boolean isValidItem(Item item) {
        if (mLowestPrice > 0 && item.getPrice() < mLowestPrice) { // 최저가
            return false;
        }

        if (mHighestPrice > 0 && item.getPrice() > mHighestPrice) { // 최고가
            return false;
        }

        if (mRateOfFluctuation > 0 && item.getPrice() < mRateOfFluctuation) { // 등락률
            return false;
        }

        return true;
    }

    public void goToTheTop() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (manager != null) {
            manager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void refreshFragment(boolean isActionRefresh) {
        if (!mIsLoading) {
            mEventListener.onRecommendFragmentEvent(Config.PARAM_LOAD_STARTED);
            //loadData();
        }
    }

    public void updateFragmentItem(Item newItem) {
        for (Item item : mItems) {
            if (item.getCode().equals(newItem.getCode())) {
                item.setTagIds(newItem.getTagIds());
                //break; // 중복 추천 종목이 있기에 주석 처리
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    public void toggleList() {
        mListMode = !mListMode;
        renderData();
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public int getItemSize() {
        return mItems.size();
    }

    public boolean getListMode() {
        return mListMode;
    }
}
