package com.summertaker.recommend.common;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.summertaker.recommend.R;
import com.summertaker.recommend.data.Item;
import com.summertaker.recommend.data.Portfolio;
import com.summertaker.recommend.data.Setting;
import com.summertaker.recommend.data.Site;
import com.summertaker.recommend.data.Tag;
import com.summertaker.recommend.data.Word;

import java.util.ArrayList;
import java.util.Calendar;

public class BaseApplication extends Application {

    private static BaseApplication mInstance;

    public static final String TAG = BaseApplication.class.getSimpleName();

    private LinearLayout.LayoutParams mParams;
    //private LinearLayout.LayoutParams mParamsNoMargin;

    public static Animation mRotateAnimation;

    public static int COLOR_INK;
    public static int COLOR_PRIMARY;
    public static int COLOR_DANGER;
    public static int COLOR_SUCCESS;
    public static int COLOR_INFO;
    public static int COLOR_WARNING;

    private RequestQueue mRequestQueue;

    public static String DATA_PATH;
    //public static boolean CACHE_MODE = true;

    private ArrayList<Setting> mSettings = new ArrayList<>(); // 설정 목록
    private ArrayList<Tag> mTags = new ArrayList<>(); // 태그 목록
    private ArrayList<Word> mWords = new ArrayList<>(); // 뉴스 포함/제외 단어 목록
    private ArrayList<Portfolio> mPortfolios = new ArrayList<>(); // 포트폴리오 태그 목록

    //private ArrayList<WordCategory> mWordCategories = new ArrayList<>(); // 단어 카테고리

    private Item mItem = new Item();
    private ArrayList<Item> mBaseItems = new ArrayList<>(); // 다음 전종목 시세 (for 기초 데이터)
    //private ArrayList<Item> mUniqueItems = new ArrayList<>(); // 다음 전종목 시세 (종목 이름 정리)
    private ArrayList<Item> mItemPrices = new ArrayList<>(); // 다음 전종목 시세 (for 실시간 가격)

    private ArrayList<Item> mWeekRiseItems = new ArrayList<>(); // 다음 금융 > 국내 > 주간 마켓 트렌드 > 상승률 상위
    private ArrayList<Item> mWeekTradeItems = new ArrayList<>(); // 다음 금융 > 국내 > 주간 마켓 트렌드 > 외국인 + 기관 순매도

    private ArrayList<Item> mBaseTradeItems = new ArrayList<>(); // 다음 외국인, 기관 매수 (for 기초 데이터)
    private ArrayList<Item> mTraderItems = new ArrayList<>(); // 거래원 종목 목록

    //private ArrayList<Item> mTradeItems = new ArrayList<>(); // 다음 외국인, 기관 매매 종목
    //private ArrayList<Item> mRecommendReturnItems = new ArrayList<>(); // 네이버 추천 종목별 수익률
    private ArrayList<Item> mRecommendTopItems = new ArrayList<>(); // 네이버 종목별 추천 건수 상위
    //private ArrayList<Item> mRecommendCurrentItems = new ArrayList<>(); // 네이버 현재 추천 종목

    private ArrayList<Site> mNewsPagerItems = new ArrayList<>();
    private ArrayList<Site> mTopPagerItems = new ArrayList<>();
    private ArrayList<Site> mWeekPagerItems = new ArrayList<>();
    private ArrayList<Site> mFluctuationPagerItems = new ArrayList<>();
    private ArrayList<Site> mTradePagerItems = new ArrayList<>();
    private ArrayList<Site> mTraderPagerItems = new ArrayList<>();
    private ArrayList<Site> mRecommendPagerItems = new ArrayList<>();
    private ArrayList<Site> mDetailPagerItems = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        int margin = (int) (5 * density);
        mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mParams.setMargins(0, 0, margin, 0);
        //mParamsNoMargin = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        COLOR_INK = ResourcesCompat.getColor(this.getResources(), R.color.ink, null);
        COLOR_PRIMARY = ResourcesCompat.getColor(this.getResources(), R.color.primary, null);
        COLOR_DANGER = ResourcesCompat.getColor(this.getResources(), R.color.danger, null);
        COLOR_SUCCESS = ResourcesCompat.getColor(this.getResources(), R.color.success, null);
        COLOR_INFO = ResourcesCompat.getColor(this.getResources(), R.color.info, null);
        COLOR_WARNING = ResourcesCompat.getColor(this.getResources(), R.color.warning, null);

        mRotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);

        DATA_PATH = Environment.getExternalStorageDirectory().toString();
        DATA_PATH += java.io.File.separator + "android";
        DATA_PATH += java.io.File.separator + "data";
        DATA_PATH += java.io.File.separator + getApplicationContext().getPackageName();

        //mWordCategories.add(new WordCategory(Config.KEY_WORD_CATEGORY_NAVER_INCLUDE, getString(R.string.word_category_naver_include)));
        //mWordCategories.add(new WordCategory(Config.KEY_WORD_CATEGORY_NAVER_EXCLUDE, getString(R.string.word_category_naver_exclude)));
        //mWordCategories.add(new WordCategory(Config.KEY_WORD_CATEGORY_DAUM_INCLUDE, getString(R.string.word_category_daum_include)));
        //mWordCategories.add(new WordCategory(Config.KEY_WORD_CATEGORY_DAUM_INCLUDE, getString(R.string.word_category_daum_exclude)));

        // 뉴스 페이저 아이템
        //mNewsPagerItems.add(new Site(Config.KEY_NEWS_BREAKING, getString(R.string.pager_item_news_breaking), Config.URL_BREAKING_LIST));
        mNewsPagerItems.add(new Site(Config.KEY_NEWS_FEATURE, getString(R.string.pager_item_news_feature), Config.URL_NAVER_NEWS_FEATURE));
        mNewsPagerItems.add(new Site(Config.KEY_NEWS_MAIN, getString(R.string.pager_item_news_main), Config.URL_NAVER_NEWS_MAIN));
        mNewsPagerItems.add(new Site(Config.KEY_NEWS_RANKING, getString(R.string.pager_item_news_ranking), Config.URL_NAVER_NEWS_RANKING));

        // TOP
        mTopPagerItems.add(new Site("", getString(R.string.pager_item_news_main), ""));
        mTopPagerItems.add(new Site("", getString(R.string.pager_item_news_main), ""));
        mTopPagerItems.add(new Site("", getString(R.string.pager_item_news_main), ""));

        // WEEK
        mWeekPagerItems.add(new Site(Config.KEY_WEEK_RISE, getString(R.string.pager_item_week_rise), ""));
        mWeekPagerItems.add(new Site(Config.KEY_WEEK_FB, getString(R.string.pager_item_week_fb), ""));
        mWeekPagerItems.add(new Site(Config.KEY_WEEK_NB, getString(R.string.pager_item_week_nb), ""));

        // 등락 페이저 아이템
        mFluctuationPagerItems.add(new Site(Config.KEY_FLUCTUATION_RISE, getString(R.string.pager_item_fluc_rise), ""));
        // 급등 - 파서 구현해야 함
        //mFlucPagerItems.add(new Site(Config.KEY_FLUCTUATION_JUMP, getString(R.string.pager_item_fluc_jump), ""));
        //mFlucPagerItems.add(new Site(Config.KEY_FLUC_CEILING, getString(R.string.pager_item_fluc_ceiling), ""));
        mFluctuationPagerItems.add(new Site(Config.KEY_FLUCTUATION_FALL, getString(R.string.pager_item_fluc_fall), ""));
        // 급락 - 파서 구현해야 함
        //mFlucPagerItems.add(new Site(Config.KEY_FLUCTUATION_CRASH, getString(R.string.pager_item_fluc_crash), ""));
        //mFlucPagerItems.add(new Site(Config.KEY_FLUC_FLOOR, getString(R.string.pager_item_fluc_floor), ""));

        // 매매 페이저 아이템
        mTradePagerItems.add(new Site(Config.KEY_TRADE_FOREIGNER, Config.KEY_TRADE_BUY, getString(R.string.pager_item_trade_foreign_buy), ""));
        mTradePagerItems.add(new Site(Config.KEY_ACC_TRADE_FOREIGNER, Config.KEY_TRADE_BUY, getString(R.string.pager_item_acc_trade_foreign_buy), ""));
        mTradePagerItems.add(new Site(Config.KEY_TRADE_INSTITUTION, Config.KEY_TRADE_BUY, getString(R.string.pager_item_trade_institution_buy), ""));
        mTradePagerItems.add(new Site(Config.KEY_ACC_TRADE_INSTITUTION, Config.KEY_TRADE_BUY, getString(R.string.pager_item_acc_trade_institution_buy), ""));
        //mTradePagerItems.add(new Site(Config.KEY_TRADE_FOREIGNER, Config.KEY_TRADE_SELL, getString(R.string.pager_item_trade_foreign_sell), ""));
        //mTradePagerItems.add(new Site(Config.KEY_TRADE_INSTITUTION, Config.KEY_TRADE_SELL, getString(R.string.pager_item_trade_institution_sell), ""));
        mTradePagerItems.add(new Site(Config.KEY_TRADE_OVERSEAS, Config.KEY_TRADE_BUY, getString(R.string.pager_item_trade_overseas_buy), ""));
        mTradePagerItems.add(new Site(Config.KEY_TRADE_DOMESTIC, Config.KEY_TRADE_BUY, getString(R.string.pager_item_trade_domestic_buy), ""));

        // 거래원
        mTraderPagerItems.add(new Site(Config.KEY_TRADER_BUY, null, getString(R.string.pager_item_trader_buy), ""));
        mTraderPagerItems.add(new Site(Config.KEY_TRADER_SELL, null, getString(R.string.pager_item_trader_sell), ""));

        // 추천 페이저 아이템
        mRecommendPagerItems.add(new Site(Config.KEY_RECOMMEND_CURRENT, getString(R.string.pager_item_recommend_current), Config.URL_NAVER_RECOMMEND_CURRENT_LIST));
        mRecommendPagerItems.add(new Site(Config.KEY_RECOMMEND_TOP, getString(R.string.pager_item_recommend_top), Config.URL_NAVER_RECOMMEND_TOP_LIST));
        mRecommendPagerItems.add(new Site(Config.KEY_RECOMMEND_RETURN, getString(R.string.pager_item_recommend_return), Config.URL_NAVER_RECOMMEND_RETURN_LIST));
        //mRecommendPagerItems.add(new Site(Config.KEY_RECOMMEND_WISE, getString(R.string.pager_item_recommend_wise), Config.URL_NAVER_RECOMMEND_WISE_LIST));

        // 종목 상세 페이저 아이템
        mDetailPagerItems.add(new Site(Config.KEY_DETAIL_INFO, getString(R.string.pager_item_detail_info), ""));
        mDetailPagerItems.add(new Site(Config.KEY_DETAIL_OVERVIEW, getString(R.string.pager_item_detail_overview), ""));
        mDetailPagerItems.add(new Site(Config.KEY_DETAIL_NEWS, getString(R.string.pager_item_detail_news), ""));
        //mDetailPagerItems.add(new Site(Config.KEY_DETAIL_REASON, getString(R.string.pager_item_detail_reason), ""));
    }

    public static synchronized BaseApplication getInstance() {
        return mInstance;
    }

    public static String getDataPath() {
        return DATA_PATH;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag_list if tag_list is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public boolean isMarketClosed() {
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        if (dayOfWeek == 7 || dayOfWeek == 1) {
            return true;
        } else if (hour < 9 || hour > 15) {
            return true;
        } else if (hour == 15 && minute > 30) {
            return true;
        }

        return false;
    }

    public ArrayList<Setting> getSettings() {
        return mSettings;
    }

    public void setSettings(ArrayList<Setting> settings) {
        this.mSettings = settings;
    }

    public ArrayList<Tag> getTags() {
        return mTags;
    }

    public ArrayList<Portfolio> getPortfolios() {
        return mPortfolios;
    }

    public void setPortfolios(ArrayList<Portfolio> portfolios) {
        this.mPortfolios = portfolios;
    }

    public ArrayList<Word> getWords() {
        return mWords;
    }

    public Item getItem() {
        return mItem;
    }

    public void setItem(Item item) {
        this.mItem = item;
    }

    public ArrayList<Item> getBaseItems() {
        return mBaseItems;
    }

    public ArrayList<Item> getItemPrices() {
        return mItemPrices;
    }

    public ArrayList<Item> getBaseTradeItems() {
        return mBaseTradeItems;
    }

    public ArrayList<Item> getWeekRiseItems() {
        return mWeekRiseItems;
    }

    public ArrayList<Item> getWeekTradeItems() {
        return mWeekTradeItems;
    }

    public ArrayList<Item> getTraderItems() {
        return mTraderItems;
    }

    public ArrayList<Site> getTopPagerItems() {
        return mTopPagerItems;
    }

    public ArrayList<Site> getWeekPagerItems() {
        return mWeekPagerItems;
    }

    public ArrayList<Item> getRecommendTopItems() {
        return mRecommendTopItems;
    }

    public ArrayList<Site> getNewsPagerItems() {
        return mNewsPagerItems;
    }

    public ArrayList<Site> getFluctuationPagerItems() {
        return mFluctuationPagerItems;
    }

    public ArrayList<Site> getTradePagerItems() {
        return mTradePagerItems;
    }

    public ArrayList<Site> getTraderPagerItems() {
        return mTraderPagerItems;
    }

    public ArrayList<Site> getRecommendPagerItems() {
        return mRecommendPagerItems;
    }

    public ArrayList<Site> getDetailPagerItems() {
        return mDetailPagerItems;
    }

    public String getStringSetting(String field) {
        String value = "";
        for (Setting setting : mSettings) {
            if (setting.getField().equals(field)) {
                value = setting.getValue().trim();
                break;
            }
        }

        return value;
    }

    public int getIntSetting(String field) {
        String value = getStringSetting(field).trim();
        return value.isEmpty() ? 0 : Integer.valueOf(value);
    }

    public float getFloatSetting(String field) {
        String value = getStringSetting(field).trim();
        return value.isEmpty() ? 0 : Float.valueOf(value);
    }

    public void setSetting(String field, String value) {
        for (int i = 0; i < mSettings.size(); i++) {
            if (mSettings.get(i).getField().equals(field)) {
                mSettings.remove(i);
            }
        }

        Setting setting = new Setting();
        setting.setField(field);
        setting.setValue(value);
        mSettings.add(setting);
    }

    public static String getChartUrl(String code) {
        long millis = System.currentTimeMillis();
        //return "https://ssl.pstatic.net/imgfinance/chart/mobile/candle/day/" + code + "_end.png?sidcode=" + millis; // 네이버 일봉
        //return "https://ssl.pstatic.net/imgfinance/chart/mobile/candle/week/" + code + "_end.png?sidcode=" + millis; // 네이버 주봉

        return "https://fn-chart.dunamu.com/images/kr/candle/d/A" + code + ".png?" + millis; // 다음 일봉
        //return "https://fn-chart.dunamu.com/images/kr/candle/w/A" + code + ".png?" + millis; // 다음 주봉
        //return "https://fn-chart.dunamu.com/images/kr/candle/m/A" + code + ".png?" + millis; // 다음 월봉
        //return "https://chart-finance.daumcdn.net/time3/year/" + code +"-290157.png?date=" + millis; // 다음 1년
    }

    public static String getWeekChartUrl(String code) {
        long millis = System.currentTimeMillis();
        return "https://ssl.pstatic.net/imgfinance/chart/item/area/week/" + code + ".png?sidcode=" + millis;
    }

    public static String getDayCandleChartUrl(String code) {
        long millis = System.currentTimeMillis();
        return "https://fn-chart.dunamu.com/images/kr/candle/d/A" + code + ".png?" + millis; // 다음 일봉
    }

    public static String getWeekCandleChartUrl(String code) {
        long millis = System.currentTimeMillis();
        return "https://fn-chart.dunamu.com/images/kr/candle/w/A" + code + ".png?" + millis; // 다음 주봉
    }

    public static String getMonthCandleChartUrl(String code) {
        long millis = System.currentTimeMillis();
        return "https://fn-chart.dunamu.com/images/kr/candle/m/A" + code + ".png?" + millis; // 다음 월봉
    }

    public static String getDayChartUrl(String code) {
        long millis = System.currentTimeMillis();
        //return "https://fn-chart.dunamu.com/images/kr/stock/d/A" + code + ".png?" + millis;
        return "https://ssl.pstatic.net/imgfinance/chart/mobile/mini/" + code + "_end_up_tablet.png?" + millis; // 오늘
        //return "https://ssl.pstatic.net/imgfinance/chart/item/area/day/" + code + ".png?sidcode=" + millis; // 어제 + 오늘
    }

    // 현재가
    public void renderPrice(Item item, TextView tvPrice, TextView tvPriceL) {
        if (item.getRof() > 0) {
            if (tvPrice != null) {
                tvPrice.setTextColor(COLOR_DANGER);
            }
            if (tvPriceL != null) {
                tvPriceL.setTextColor(COLOR_DANGER);
            }
        } else if (item.getRof() < 0) {
            if (tvPrice != null) {
                tvPrice.setTextColor(COLOR_PRIMARY);
            }
            if (tvPriceL != null) {
                tvPriceL.setTextColor(COLOR_PRIMARY);
            }
        } else {
            if (tvPrice != null) {
                tvPrice.setTextColor(COLOR_INK);
            }
            if (tvPriceL != null) {
                tvPriceL.setTextColor(COLOR_INK);
            }
        }
        String price = Config.NUMBER_FORMAT.format(item.getPrice());
        if (tvPrice != null) {
            tvPrice.setText(price);
        }
        if (tvPriceL != null) {
            tvPriceL.setText(price);
        }
    }

    // 전일비
    public void renderPof(Item item, TextView tvPof, String fmPof, TextView tvPofL, String fmPofL) {
        int pof = item.getPof();
        if (item.getRof() > 0) {
            tvPof.setTextColor(COLOR_DANGER);
            if (tvPofL != null) {
                tvPofL.setTextColor(COLOR_DANGER);
            }
        } else if (item.getRof() < 0) {
            pof = -pof;
            tvPof.setTextColor(COLOR_PRIMARY);
            if (tvPofL != null) {
                tvPofL.setTextColor(COLOR_PRIMARY);
            }
        } else {
            tvPof.setTextColor(COLOR_INK);
            if (tvPofL != null) {
                tvPofL.setTextColor(COLOR_INK);
            }
        }

        String pofText = Config.SIGNED_FORMAT.format(pof);
        String text = (fmPof == null || fmPof.isEmpty()) ? pofText : String.format(fmPof, pofText);
        tvPof.setText(text);
        if (tvPofL != null) {
            text = (fmPofL == null || fmPofL.isEmpty()) ? pofText : String.format(fmPofL, pofText);
            tvPofL.setText(text);
        }
    }

    // 등락률
    public void renderRof(Item item, TextView tvFlucIcon, TextView tvFlucIconL, TextView tvRof, TextView tvRofL) {
        String flucIcon = "";
        if (item.getRof() > 0) {
            flucIcon = "▲";
            if (tvFlucIcon != null) {
                tvFlucIcon.setTextColor(COLOR_DANGER);
            }
            if (tvFlucIconL != null) {
                tvFlucIconL.setTextColor(COLOR_DANGER);
            }
            if (tvRof != null) {
                tvRof.setTextColor(COLOR_DANGER);
            }
            if (tvRofL != null) {
                tvRofL.setTextColor(COLOR_DANGER);
            }
        } else if (item.getRof() < 0) {
            flucIcon = "▼";
            if (tvFlucIcon != null) {
                tvFlucIcon.setTextColor(COLOR_PRIMARY);
            }
            if (tvFlucIconL != null) {
                tvFlucIconL.setTextColor(COLOR_PRIMARY);
            }
            if (tvRof != null) {
                tvRof.setTextColor(COLOR_PRIMARY);
            }
            if (tvRofL != null) {
                tvRofL.setTextColor(COLOR_PRIMARY);
            }
        } else {
            if (tvFlucIcon != null) {
                tvFlucIcon.setTextColor(COLOR_INK);
            }
            if (tvFlucIconL != null) {
                tvFlucIconL.setTextColor(COLOR_INK);
            }
            if (tvRof != null) {
                tvRof.setTextColor(COLOR_INK);
            }
            if (tvRofL != null) {
                tvRofL.setTextColor(COLOR_INK);
            }
        }
        if (tvFlucIcon != null) {
            tvFlucIcon.setText(flucIcon);
        }
        if (tvFlucIconL != null) {
            tvFlucIconL.setText(flucIcon);
        }

        String text = Config.DECIMAL_FORMAT.format(item.getRof()) + "%";
        if (tvRof != null) {
            tvRof.setText(text);
        }
        if (tvRofL != null) {
            tvRofL.setText(text);
        }
    }

    public void renderTag(Context context, Item item, LinearLayout layout) {
        layout.removeAllViews();

        if (item == null || item.getTagIds() == null) {
            return;
        }

        /*
        // 추천수 태그
        if (item.getNor() > 0) {
            TextView tv = new TextView(context);
            tv.setLayoutParams(mParams);
            String text = "추+" + item.getNor();
            tv.setText(text);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.TAG_TEXT_SIZE_XS);
            tv.setBackground(context.getResources().getDrawable(R.drawable.tag_background_xs));
            tv.setTextColor(Color.parseColor(Config.COLOR_RECO_FGC));
            GradientDrawable drawable = (GradientDrawable) tv.getBackground();
            drawable.setColor(Color.parseColor(Config.COLOR_RECO_BGC));
            layout.addView(tv);
        }
        */

        // 태그
        for (Tag tag : BaseApplication.getInstance().getTags()) {
            if (!item.getTagIds().contains(String.valueOf(tag.getId()))) {
                continue;
            }

            TextView tv = new TextView(context);
            tv.setLayoutParams(mParams);
            tv.setText(tag.getName());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.TAG_TEXT_SIZE_XS);
            tv.setTextColor(Color.parseColor(Config.TAG_FGC_ON));
            //tv.setBackground(ContextCompat.getDrawable(this, R.drawable.tag_background_xs));

            GradientDrawable drawable = (GradientDrawable) tv.getBackground();
            drawable.setColor(Color.parseColor(Config.TAG_BGC_ON));

            /*
            // 글자색
            if (tag.getFgc() != null && tag.getFgc().length() == 7) {
                tv.setTextColor(Color.parseColor(tag.getFgc()));
            }

            // 배경색
            if (tag.getBgc() != null && tag.getBgc().length() == 7) {
                // https://stackoverflow.com/questions/18391830/how-to-programmatically-round-corners-and-set-random-background-colors
                GradientDrawable drawable = (GradientDrawable) tv.getBackground();
                drawable.setColor(Color.parseColor(tag.getBgc()));
            }
            */

            layout.addView(tv);
        }
    }
}
