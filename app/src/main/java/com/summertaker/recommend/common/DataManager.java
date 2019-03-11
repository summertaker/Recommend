package com.summertaker.recommend.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.summertaker.recommend.data.Cause;
import com.summertaker.recommend.data.Item;
import com.summertaker.recommend.data.News;
import com.summertaker.recommend.data.Portfolio;
import com.summertaker.recommend.data.Reason;
import com.summertaker.recommend.data.Setting;
import com.summertaker.recommend.data.Tag;
import com.summertaker.recommend.data.Word;
import com.summertaker.recommend.parser.DaumParser;
import com.summertaker.recommend.parser.NaverNewsParser;
import com.summertaker.recommend.parser.NaverParser;
import com.summertaker.recommend.util.OkHttpSingleton;
import com.summertaker.recommend.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.RequestBody;

public class DataManager {

    private String TAG;

    private Context mContext;
    private SharedPreferences mPreferences;
    private String mDateFormatString = "yyyy-MM-dd HH:mm:ss";
    private SimpleDateFormat mSimpleDateFormat;

    private String mSiteId;

    private boolean mIsDataOnLoading = false;
    private int mUrlLoadCount = 0;
    private ArrayList<String> mUrls = new ArrayList<>();
    private ArrayList<Item> mItems = new ArrayList<>();

    public DataManager(Context context) {
        TAG = getClass().getSimpleName();
        mContext = context;
        mPreferences = context.getSharedPreferences(Config.PREFERENCE_KEY, Context.MODE_PRIVATE);
        mSimpleDateFormat = new SimpleDateFormat(mDateFormatString, Locale.getDefault());
    }

    /**
     * 환경설정 로드하기
     */
    public interface SettingCallback {
        void onLoad();
    }

    private SettingCallback mSettingCallback;

    public void setOnSettingLoaded(SettingCallback callback) {
        mSettingCallback = callback;
    }

    public void loadSetting() {
        requestSetting();
    }

    private void requestSetting() {
        //Log.e(TAG, "url: " + Config.URL_SETTING);
        StringRequest strReq = new StringRequest(Request.Method.GET, Config.URL_SETTING, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "response:\n" + response);
                parseSetting(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseSetting("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }


    private void parseSetting(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject data = json.getJSONObject("data");

            parseSettings(data);   // 환경
            parseTags(data);       // 태그
            parsePortfolios(data); // 포트폴리오
            parseWords(data);      // 단어

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR: parseSetting()\n" + e.getMessage());
        }

        //readPortfolio(); // 포트폴리오 읽어오기

        mSettingCallback.onLoad();
    }

    /**
     * 알림 로드하기
     */
    public interface AlertCallback {
        void onLoad(int count);
    }

    private AlertCallback mAlertCallback;

    public void setOnAlertLoaded(AlertCallback callback) {
        mAlertCallback = callback;
    }

    public void loadAlert() {
        if (!mIsDataOnLoading) {
            mIsDataOnLoading = true;
            requestAlert();
        }
    }

    private void requestAlert() {
        StringRequest strReq = new StringRequest(Request.Method.GET, Config.URL_ALERT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseAlert(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseAlert("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseAlert(String response) {
        int count = 0;
        try {
            JSONObject json = new JSONObject(response);
            JSONObject data = json.getJSONObject("data");
            count = Util.getInt(data, "count");
            //Log.e(TAG, "count: " + count);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "> ERROR: parseAlert()\n" + e.getMessage());
        }

        mIsDataOnLoading = false;
        mAlertCallback.onLoad(count);
    }

    /**
     * 전종목 히스토리 로드하기
     */
    public interface ItemHistoryCallback {
        void onLoad(Date date, ArrayList<Item> items);
    }

    private ItemHistoryCallback mItemHistoryCallback;

    public void setOnItemHistoryLoaded(ItemHistoryCallback callback) {
        mItemHistoryCallback = callback;
    }

    public void loadItemHistory(String date) {
        requestItemHistory(date);
    }

    private void requestItemHistory(String date) {
        //Log.e(TAG, "url: " + Config.URL_ItemHistory);
        StringRequest strReq = new StringRequest(Request.Method.GET, Config.URL_ITEM_HISTORY + date, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "response:\n" + response);
                parseItemHistory(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseItemHistory("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseItemHistory(String response) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        ArrayList<Item> items = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(response);
            String dateString = Util.getString(json, "date");
            try {
                date = sdf.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            JSONArray jsonArray = json.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                Item item = new Item();
                item.setId(i + 1);
                item.setCode(Util.getString(obj, "code"));
                item.setName(Util.getString(obj, "name"));
                item.setPrice(Util.getInt(obj, "price"));
                item.setRof(BigDecimal.valueOf(Util.getDouble(obj, "rof")).floatValue());
                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "> ERROR: parseItemHistory()\n" + e.getMessage());
        }

        mItemHistoryCallback.onLoad(date, items);
    }

    /**
     * 등급 로드하기
     */
    public interface GradeCallback {
        void onLoad();
    }

    private GradeCallback mGradeCallback;

    public void setOnGradeLoaded(GradeCallback callback) {
        mGradeCallback = callback;
    }

    public void loadGrade() {
        if (BaseApplication.getInstance().isMarketClosed()) {
            //readCacheItems();
        } else {
            requestGrade();
        }
    }

    private void requestGrade() {
        //Log.e(TAG, "url: " + Config.URL_Grade);
        StringRequest strReq = new StringRequest(Request.Method.GET, Config.URL_GRADE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "response:\n" + response);
                parseGrade(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseGrade("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseGrade(String response) {
        BaseApplication.getInstance().getBaseItems().clear();
        try {
            JSONObject json = new JSONObject(response);
            JSONArray jsonArray = json.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                Item item = new Item();
                item.setId(i + 1);
                item.setCode(Util.getString(obj, "code"));
                item.setName(Util.getString(obj, "name"));
                item.setPrice(Util.getInt(obj, "price"));
                item.setPof(Util.getInt(obj, "pof"));
                item.setRof(BigDecimal.valueOf(Util.getDouble(obj, "rof")).floatValue());
                item.setNor(Util.getInt(obj, "nor"));
                item.setPoint(Util.getInt(obj, "point"));

                BaseApplication.getInstance().getBaseItems().add(item);
                //Log.e(TAG, item.getName());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "> ERROR: parseGrade()\n" + e.getMessage());
        }

        mGradeCallback.onLoad();
    }

    /**
     * [다음 금융 > 국내] 로드하기
     */
    public interface DaumDomesticCallback {
        void onLoad();
    }

    private DaumDomesticCallback mDaumDomesticCallback;

    public void setOnDaumDomesticLoaded(DaumDomesticCallback callback) {
        mDaumDomesticCallback = callback;
    }

    public void loadDaumDomestic() {
        mItems.clear();
        requestDaumDomestic();
    }

    private void requestDaumDomestic() {
        String url = Config.URL_DAUM_DOMESTIC;
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(mTag, "response:\n" + response);
                parseDaumDomestic(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                parseDaumDomestic("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseDaumDomestic(String response) {
        BaseApplication.getInstance().getWeekRiseItems().clear();
        BaseApplication.getInstance().getWeekTradeItems().clear();

        DaumParser daumParser = new DaumParser();
        daumParser.parseDomestic(response, BaseApplication.getInstance().getWeekRiseItems(), BaseApplication.getInstance().getWeekTradeItems());
        //Log.e(TAG, "WeekRiseItems().size = " + BaseApplication.getInstance().getWeekRiseItems().size());
        //Log.e(TAG, "WeekTradeItems().size = " + BaseApplication.getInstance().getWeekRiseItems().size());

        mDaumDomesticCallback.onLoad();
    }

    /**
     * [네이버 금융 > 국내 > 전종목 시세] 가져오기 (기초 정보)
     */
    public interface BaseItemsCallback {
        void onParse(int count);

        void onLoad();
    }

    private BaseItemsCallback mBaseItemsCallback;

    public void setOnBaseItemLoaded(BaseItemsCallback callback) {
        mBaseItemsCallback = callback;
    }

    public void loadBaseItems() {
        if (BaseApplication.getInstance().getBaseItems().size() == 0) {
            mUrls.clear();
            mUrls.add(Config.URL_DAUM_PRICE_KOSPI);
            mUrls.add(Config.URL_DAUM_PRICE_KOSDAQ);
            mUrlLoadCount = 0;

            mItems.clear();
            requestBaseItems();
        } else {
            //Toast.makeText(mContext, "BaseItems Memory Loaded!", Toast.LENGTH_SHORT).show();
            mBaseItemsCallback.onLoad();
        }
    }

    private void requestBaseItems() {
        StringRequest strReq = new StringRequest(Request.Method.GET, mUrls.get(mUrlLoadCount), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseBaseItems(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseBaseItems("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseBaseItems(String response) {
        DaumParser daumParser = new DaumParser();
        daumParser.parsePriceList(response, mItems);

        mUrlLoadCount++;
        if (mUrlLoadCount < mUrls.size()) {
            mBaseItemsCallback.onParse(mUrlLoadCount);
            requestBaseItems();
        } else {
            //Log.e(TAG, "BaseApplication.getInstance().getBaseItems().size(): " + BaseApplication.getInstance().getBaseItems().size());

            // 뉴스에서 종목 이름을 강조하기 위해 글자 길이로 정렬 (SK하이닉스 > SK)
            for (Item item : mItems) {
                item.setCharCount(item.getName().length());
            }
            Collections.sort(mItems, new Comparator<Item>() {
                @Override
                public int compare(Item a, Item b) {
                    if (a.getCharCount() < b.getCharCount()) {
                        return 1;
                    } else if (a.getCharCount() > b.getCharCount()) {
                        return -1;
                    }
                    return 0;
                }
            });

            //for (Item item : mItems) {
            //    Log.e(TAG, item.getCharCount() + " " + item.getName());
            //}

            /*
            // 이름 비교해서 중복 제거 (다음 금융 > 전종목 시세 > 업종순: 업종 중복 존재)
            ArrayList<Item> uniqueItems = new ArrayList<>();
            for (Item item : mItems) {
                boolean exist = false;
                for (Item unique : uniqueItems) {
                    if (unique.getName().contains(item.getName())) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    uniqueItems.add(item);
                }
            }
            */

            BaseApplication.getInstance().getBaseItems().addAll(mItems);
            //writeCacheItems(Config.KEY_BASE_ITEM, BaseApplication.getInstance().getBaseItems());
            mBaseItemsCallback.onLoad();
        }
    }

    /**
     * [네이버 금융 > 국내 > 전종목 시세] 가져오기 (실시간 가격)
     */
    public interface ItemPriceCallback {
        void onParse(int count);

        void onLoad();
    }

    private ItemPriceCallback mItemPriceCallback;

    public void setOnItemPriceLoaded(ItemPriceCallback callback) {
        mItemPriceCallback = callback;
    }

    public void loadItemPrice() {
        mUrls.clear();
        mUrls.add(Config.URL_DAUM_PRICE_KOSPI);
        mUrls.add(Config.URL_DAUM_PRICE_KOSDAQ);

        mUrlLoadCount = 0;
        mItems.clear();
        requestItemPrice();

        /*
        if (BaseApplication.getInstance().isMarketClosed()) {
            BaseApplication.getInstance().getItemPrices().clear();
            BaseApplication.getInstance().getItemPrices().addAll(readCacheItems(Config.KEY_ITEM_PRICE, 60 * 24));
        } else {
            BaseApplication.getInstance().getItemPrices().clear();
        }

        if (BaseApplication.getInstance().getItemPrices().size() == 0) {
            mUrls.clear();
            mUrls.add(Config.URL_DAUM_PRICE_KOSPI);
            mUrls.add(Config.URL_DAUM_PRICE_KOSDAQ);

            mUrlLoadCount = 0;
            requestItemPrice();
        } else {
            //Toast.makeText(mContext, "ItemPrice Cache Loaded!", Toast.LENGTH_SHORT).show();
            mItemPriceCallback.onLoad();
        }
        */
    }

    private void requestItemPrice() {
        StringRequest strReq = new StringRequest(Request.Method.GET, mUrls.get(mUrlLoadCount), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, response);
                parseItemPrice(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseItemPrice("");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fieldName", "");
                params.put("order", "");
                params.put("perPage", "");
                params.put("market", "KOSPI");
                params.put("page", "");
                params.put("changes", "UPPER_LIMIT,RISE,EVEN,FALL,LOWER_LIMIT");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Host", "finance.daum.net");
                params.put("Referer", "http://finance.daum.net/domestic/all_stocks");
                params.put("User-Age", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0");
                params.put("X-Requested-With", "XMLHttpRequest");
                return params;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
        //}
    }

    private void parseItemPrice(String response) {
        DaumParser daumParser = new DaumParser();
        daumParser.parsePriceListJson(response, mItems);

        mUrlLoadCount++;
        if (mUrlLoadCount < mUrls.size()) {
            mItemPriceCallback.onParse(mUrlLoadCount);
            requestItemPrice();
        } else {
            //Log.e(TAG, "ItemPrice: mItems.size(): " + mItems.size());
            //writeCacheItems(Config.KEY_ITEM_PRICE, BaseApplication.getInstance().getItemPrices());

            for (Item item : mItems) {
                item.setCharCount(item.getName().length()); // 종목 이름 글자수 설정

                // 추천수
                for (Item ti : BaseApplication.getInstance().getRecommendTopItems()) {
                    if (item.getCode().equals(ti.getCode())) {
                        item.setNor(ti.getNor());
                    }
                }
            }

            // 뉴스에서 종목 이름을 강조하기 위해 글자 길이로 정렬 ("SK하이닉스"를 "SK" 보다 먼저)
            Collections.sort(mItems, new Comparator<Item>() {
                @Override
                public int compare(Item a, Item b) {
                    if (a.getCharCount() < b.getCharCount()) {
                        return 1;
                    } else if (a.getCharCount() > b.getCharCount()) {
                        return -1;
                    }
                    return 0;
                }
            });

            BaseApplication.getInstance().getItemPrices().clear();
            BaseApplication.getInstance().getItemPrices().addAll(mItems);
            mItemPriceCallback.onLoad();
        }
    }

    /**
     * [다음 금융 > 종목 상세 정보] 로드하기
     */
    public interface ItemCallback {
        void onLoad(Item item);
    }

    private ItemCallback mItemCallback;

    public void setOnItemLoaded(ItemCallback callback) {
        mItemCallback = callback;
    }

    public void loadItem(String code) {
        requestItem(code);
    }

    private void requestItem(final String code) {
        String url = Config.URL_DAUM_DETAIL + code;
        //Log.e(mTag, "url: " + url);

        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(mTag, "response:\n" + response);
                parseItem(response, code);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                parseItem("", code);
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseItem(String response, String code) {
        DaumParser daumParser = new DaumParser();
        Item item = daumParser.parseItemDetail(response);
        item.setCode(code);
        mItemCallback.onLoad(item);
    }

    /**
     * [네이버 금융 > 국내 > 상승, 하락, 급등, 급락] 가져오기
     */
    public interface FluctuationCallback {
        void onParse(int count);

        void onLoad(ArrayList<Item> items);
    }

    private FluctuationCallback mFluctuationCallback;

    public void setOnFlucLoaded(FluctuationCallback callback) {
        mFluctuationCallback = callback;
    }

    public void loadFluctuation(String siteId) {
        mSiteId = siteId;

        mItems.clear();
        //if (BaseApplication.getInstance().isMarketClosed()) {
        //    mItems = readCacheItems(mSiteId, 60 * 24);
        //}

        //if (mItems.size() > 0) {
        //    //Toast.makeText(mContext, "등락 캐쉬 사용 (" + mItems.size() + ")", Toast.LENGTH_SHORT).show();
        //    mFlucCallback.onLoad(mItems);
        //} else {
        //Toast.makeText(mContext, "등락 데이터 로드...", Toast.LENGTH_SHORT).show();

        mUrls.clear();
        if (siteId.equals(Config.KEY_FLUCTUATION_RISE)) { // 상승
            mUrls.add(Config.URL_NAVER_FLUCTUATION_RISE_LIST_KOSPI);
            mUrls.add(Config.URL_NAVER_FLUCTUATION_RISE_LIST_KOSDAQ);
        } else if (siteId.equals(Config.KEY_FLUCTUATION_JUMP)) { // 급등
            mUrls.add(Config.URL_NAVER_FLUCTUATION_JUMP_LIST_KOSPI);
            mUrls.add(Config.URL_NAVER_FLUCTUATION_JUMP_LIST_KOSDAQ);
        } else if (siteId.equals(Config.KEY_FLUCTUATION_FALL)) { // 하락
            mUrls.add(Config.URL_NAVER_FLUCTUATION_FALL_LIST_KOSPI);
            mUrls.add(Config.URL_NAVER_FLUCTUATION_FALL_LIST_KOSDAQ);
        } else if (siteId.equals(Config.KEY_FLUCTUATION_CRASH)) { // 급락
            mUrls.add(Config.URL_NAVER_FLUCTUATION_CRASH_LIST_KOSPI);
            mUrls.add(Config.URL_NAVER_FLUCTUATION_CRASH_LIST_KOSDAQ);
        }

        if (mUrls.size() > 0) {
            mUrlLoadCount = 0;
            requestFluctuation();
        } else {
            Toast.makeText(mContext, "Invalid Site ID", Toast.LENGTH_SHORT).show();
        }
        //}
    }

    private void requestFluctuation() {
        String url = mUrls.get(mUrlLoadCount);
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseFluctuation(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseFluctuation("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseFluctuation(String response) {
        NaverParser naverParser = new NaverParser();
        naverParser.parseFluctuation(response, mItems);

        mUrlLoadCount++;
        if (mUrlLoadCount < mUrls.size()) {
            mFluctuationCallback.onParse(mUrlLoadCount);
            requestFluctuation();
        } else {
            //Log.e(TAG, "Fluc: mItems.size() = " + mItems.size());

            // 등락률 정렬
            if (mSiteId.equals(Config.KEY_FLUCTUATION_RISE) || mSiteId.equals(Config.KEY_FLUCTUATION_JUMP)) { // 상승, 급등
                Collections.sort(mItems, new Comparator<Item>() {
                    @Override
                    public int compare(Item a, Item b) {
                        if (a.getRof() < b.getRof()) {
                            return 1;
                        } else if (a.getRof() > b.getRof()) {
                            return -1;
                        }
                        return 0;
                    }
                });
            } else if (mSiteId.equals(Config.KEY_FLUCTUATION_FALL) || mSiteId.equals(Config.KEY_FLUCTUATION_CRASH)) { // 하락, 급락
                Collections.sort(mItems, new Comparator<Item>() {
                    @Override
                    public int compare(Item a, Item b) {
                        if (a.getRof() < b.getRof()) {
                            return -1;
                        } else if (a.getRof() > b.getRof()) {
                            return 1;
                        }
                        return 0;
                    }
                });
            }

            //writeCacheItems(mSiteId, mItems);
            mFluctuationCallback.onLoad(mItems);
        }
    }

    /**
     * [다음 금융 > 국내 > 외국인, 기관 매매] 가져오기 (for 기초 데이터)
     */
    public interface BaseTradeCallback {
        void onParse(int count);

        void onLoad();
    }

    private BaseTradeCallback mBaseTradeCallback;

    public void setOnBaseTradeLoaded(BaseTradeCallback callback) {
        mBaseTradeCallback = callback;
    }

    public void loadBaseTrade() {
        BaseApplication.getInstance().getBaseTradeItems().clear();

        mUrls.clear();
        mUrls.add(Config.URL_DAUM_TRADE_FOREIGNER_KOSPI_LIST);
        mUrls.add(Config.URL_DAUM_TRADE_FOREIGNER_KOSDAQ_LIST);
        mUrls.add(Config.URL_DAUM_TRADE_INSTITUTION_KOSPI_LIST);
        mUrls.add(Config.URL_DAUM_TRADE_INSTITUTION_KOSDAQ_LIST);

        mUrlLoadCount = 0;
        requestBaseTrade();
    }

    private void requestBaseTrade() {
        final String url = mUrls.get(mUrlLoadCount);
        //Log.e(TAG, url);
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseBaseTrade(url, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseBaseTrade(url, "");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseBaseTrade(String url, String response) {
        DaumParser daumParser = new DaumParser();
        daumParser.parseTradeList(url, response, BaseApplication.getInstance().getBaseTradeItems());

        mUrlLoadCount++;
        if (mUrlLoadCount < mUrls.size()) {
            mBaseTradeCallback.onParse(mUrlLoadCount);
            requestBaseTrade();
        } else {
            //Log.e(TAG, "mBaseTrades.size(): " + mBaseTrades.size());

            writeCacheItems(Config.KEY_BASE_TRADE, BaseApplication.getInstance().getBaseTradeItems());
            mBaseTradeCallback.onLoad();
        }
    }

    /**
     * [다음 금융 > 국내 > 외국인, 기관 매매] 가져오기
     */
    public interface TradeCallback {
        void onParse(int count);

        void onLoad(ArrayList<Item> items);
    }

    private TradeCallback mTradeCallback;

    public void setOnTradeLoaded(TradeCallback callback) {
        mTradeCallback = callback;
    }

    public void loadTrade(String sideId) {
        //mSiteId = sideId;

        mUrls.clear();
        if (sideId.equals(Config.KEY_TRADE_FOREIGNER)) {
            mUrls.add(Config.URL_DAUM_TRADE_FOREIGNER_KOSPI_LIST);
            mUrls.add(Config.URL_DAUM_TRADE_FOREIGNER_KOSDAQ_LIST);
        } else if (sideId.equals(Config.KEY_ACC_TRADE_FOREIGNER)) {
            mUrls.add(Config.URL_DAUM_ACC_TRADE_FOREIGNER_KOSPI_LIST);
            mUrls.add(Config.URL_DAUM_ACC_TRADE_FOREIGNER_KOSDAQ_LIST);
        } else if (sideId.equals(Config.KEY_TRADE_INSTITUTION)) {
            mUrls.add(Config.URL_DAUM_TRADE_INSTITUTION_KOSPI_LIST);
            mUrls.add(Config.URL_DAUM_TRADE_INSTITUTION_KOSDAQ_LIST);
        } else if (sideId.equals(Config.KEY_ACC_TRADE_INSTITUTION)) {
            mUrls.add(Config.URL_DAUM_ACC_TRADE_INSTITUTION_KOSPI_LIST);
            mUrls.add(Config.URL_DAUM_ACC_TRADE_INSTITUTION_KOSDAQ_LIST);
        } else if (sideId.equals(Config.KEY_TRADE_OVERSEAS)) {
            mUrls.add(Config.URL_DAUM_TRADE_OVERSEAS_KOSPI_LIST);
            mUrls.add(Config.URL_DAUM_TRADE_OVERSEAS_KOSDAQ_LIST);
        } else if (sideId.equals(Config.KEY_TRADE_DOMESTIC)) {
            mUrls.add(Config.URL_DAUM_TRADE_DOMESTIC_KOSPI_LIST);
            mUrls.add(Config.URL_DAUM_TRADE_DOMESTIC_KOSDAQ_LIST);
        }

        mUrlLoadCount = 0;
        mItems.clear();
        if (mUrls.size() > 0) {
            requestTrade();
        } else {
            Toast.makeText(mContext, "No URL", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestTrade() {
        final String url = mUrls.get(mUrlLoadCount);
        //Log.e(TAG, url);
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseTrade(url, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseTrade(url, "");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseTrade(String url, String response) {
        DaumParser daumParser = new DaumParser();
        daumParser.parseTradeList(url, response, mItems);

        mUrlLoadCount++;
        if (mUrlLoadCount < mUrls.size()) {
            mTradeCallback.onParse(mUrlLoadCount);
            requestTrade();
        } else {
            //Log.e(TAG, "mTrades.size(): " + mTrades.size());

            // 추천수
            for (Item item : mItems) {
                for (Item ti : BaseApplication.getInstance().getRecommendTopItems()) {
                    if (item.getCode().equals(ti.getCode())) {
                        item.setNor(ti.getNor());
                    }
                }
            }

            if (mItems.get(0).isForeigner() || mItems.get(0).isInstitution()) {
                // 거래금액으로 정렬
                Collections.sort(mItems, new Comparator<Item>() {
                    @Override
                    public int compare(Item a, Item b) {
                        if (a.getPot() < b.getPot()) {
                            return 1;
                        } else if (a.getPot() > b.getPot()) {
                            return -1;
                        }
                        return 0;
                    }
                });
            } else {
                // 거래량 정렬
                Collections.sort(mItems, new Comparator<Item>() {
                    @Override
                    public int compare(Item a, Item b) {
                        if (a.getVot() < b.getVot()) {
                            return 1;
                        } else if (a.getVot() > b.getVot()) {
                            return -1;
                        }
                        return 0;
                    }
                });
            }

            //writeCacheItems(mSiteId, mItems);
            mTradeCallback.onLoad(mItems);
        }
    }

    /**
     * [다음 금융 > 국내 > 외국인, 기관 누적 매매] 가져오기
     */
    public interface AccuTradeCallback {
        void onParse(int count);

        void onLoad(ArrayList<Item> items);
    }

    private AccuTradeCallback mAccuTradeCallback;

    public void setOnAccuTradeLoaded(AccuTradeCallback callback) {
        mAccuTradeCallback = callback;
    }

    public void loadAccuTrade() {
        mUrls.clear();
        mUrls.add(Config.URL_DAUM_ACCUTRADE_FOREIGN_KOSPI_LIST);
        mUrls.add(Config.URL_DAUM_ACCUTRADE_FOREIGN_KODAQ_LIST);
        mUrls.add(Config.URL_DAUM_ACCUTRADE_INSTITUTION_KOSPI_LIST);
        mUrls.add(Config.URL_DAUM_ACCUTRADE_INSTITUTION_KODAQ_LIST);

        mUrlLoadCount = 0;
        mItems.clear();
        requestAccuTrade();
    }

    private void requestAccuTrade() {
        final String url = mUrls.get(mUrlLoadCount);
        //Log.e(TAG, url);
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseAccuTrade(url, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseAccuTrade(url, "");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseAccuTrade(String url, String response) {
        DaumParser daumParser = new DaumParser();
        daumParser.parseAccuTradeList(url, response, mItems);

        mUrlLoadCount++;
        if (mUrlLoadCount < mUrls.size()) {
            mAccuTradeCallback.onParse(mUrlLoadCount);
            requestAccuTrade();
        } else {
            //Log.e(TAG, "AccuTrade: mItems.size(): " + mItems.size());

            // 거래량 정렬
            Collections.sort(mItems, new Comparator<Item>() {
                @Override
                public int compare(Item a, Item b) {
                    if (a.getVot() < b.getVot()) {
                        return 1;
                    } else if (a.getVot() > b.getVot()) {
                        return -1;
                    }
                    return 0;
                }
            });

            //for (Item item : mItems) {
            //    Log.e(TAG, item.getName() + " / " + item.isForeigner() + " / " + item.isInstitution());
            //}

            //writeCacheItems(mSiteId, mItems);
            mAccuTradeCallback.onLoad(mItems);
        }
    }

    /**
     * [다음 금융 > 국내 > 거래원별] 거래원 목록 가져오기
     */
    public interface TraderListCallback {
        void onLoad(ArrayList<String> urls);
    }

    private TraderListCallback mTraderListCallback;

    public void setOnTraderListLoaded(TraderListCallback callback) {
        mTraderListCallback = callback;
    }

    public void loadTraderList() {
        mItems.clear();
        requestTraderList();
    }

    private void requestTraderList() {
        String url = Config.URL_DAUM_TRADER_LIST;
        //Log.e(TAG, url);
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseTraderList(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseTraderList("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseTraderList(String response) {
        ArrayList<String> urls = new ArrayList<>();
        DaumParser daumParser = new DaumParser();
        daumParser.parseTraderList(response, urls);

        //Log.e(TAG, "urls.size(): " + urls.size());
        //writeCacheItems(mSiteId, mItems);
        mTraderListCallback.onLoad(urls);
    }

    /**
     * [다음 금융 > 국내 > 거래원별] 거래원별 종목 목록 가져오기
     */
    public interface TraderItemListCallback {
        void onParse(int count);

        void onLoad();
    }

    private TraderItemListCallback mTraderItemListCallback;

    public void setOnTraderItemListLoaded(TraderItemListCallback callback) {
        mTraderItemListCallback = callback;
    }

    public void loadTraderItemList(ArrayList<String> urls) {
        mUrls.clear();
        mUrls.addAll(urls);
        mUrlLoadCount = 0;

        mItems.clear();
        requestTraderItemList();
    }

    private void requestTraderItemList() {
        String url = mUrls.get(mUrlLoadCount);
        //Log.e(TAG, url);

        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseTraderItemList(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseTraderItemList("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseTraderItemList(String response) {
        DaumParser daumParser = new DaumParser();
        daumParser.parseTraderItemList(response, mItems);

        mUrlLoadCount++;
        if (mUrlLoadCount < mUrls.size()) {
            //if (mUrlLoadCount < 2) {
            mTraderItemListCallback.onParse(mUrlLoadCount);
            requestTraderItemList();
        } else {
            //Log.e(TAG, "mItems.size(): " + mItems.size());

            BaseApplication.getInstance().getTraderItems().clear();
            BaseApplication.getInstance().getTraderItems().addAll(mItems);
            mTraderItemListCallback.onLoad();
        }
    }

    /**
     * [네이버 금융 > 국내 > 추천 종목별 수익률] 가져오기
     */
    public interface RecommendReturnItemCallback {
        void onLoad(ArrayList<Item> items);
    }

    private RecommendReturnItemCallback mRecommendReturnItemCallback;

    public void setOnRecommendReturnItemLoaded(RecommendReturnItemCallback callback) {
        mRecommendReturnItemCallback = callback;
    }

    public void loadRecommendReturnItem(Activity activity) {
        mItems.clear();
        //mItems = readCacheItems(Config.KEY_RECOMMEND_RETURN, 60 * 24); // 1 day
        //if (mItems.size() > 0) {
        //    mRecommendReturnItemCallback.onLoad(mItems);
        //} else {
        requestRecommendReturnItem(activity);
        //}
    }

    private void requestRecommendReturnItem(final Activity activity) {
        String param = getRequestParameter(Config.KEY_RECOMMEND_RETURN, "");
        RequestBody requestBody = RequestBody.create(Config.JSON, param);
        okhttp3.Request request = new okhttp3.Request.Builder().url(Config.URL_NAVER_RECOMMEND_RETURN_LIST).post(requestBody).build();
        OkHttpSingleton.getInstance().getClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseString = response.body().string();
                //Log.e(">>>>>", "responseString\n" + responseString);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseRecommendReturnItem(responseString);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.e(TAG, "ERROR: " + e.getMessage());
                Log.e(TAG, "URL: " + Config.URL_NAVER_RECOMMEND_RETURN_LIST);
                parseRecommendReturnItem("");
            }
        });
    }

    private void parseRecommendReturnItem(String response) {
        NaverParser naverParser = new NaverParser();
        naverParser.parseRecommend(response, Config.KEY_RECOMMEND_RETURN, mItems, false);
        //Log.e(TAG, "RecommendReturn: Items.size(): " + mItems.size());

        //writeCacheItems(Config.KEY_RECOMMEND_RETURN, mItems);
        mRecommendReturnItemCallback.onLoad(mItems);
    }

    /**
     * [네이버 금융 > 국내 > 추천수 상위 종목] 가져오기
     */
    public interface RecommendTopItemCallback {
        void onLoad();
    }

    private RecommendTopItemCallback mRecommendTopItemCallback;

    public void setOnRecommendTopItemLoaded(RecommendTopItemCallback callback) {
        mRecommendTopItemCallback = callback;
    }

    public void loadRecommendTopItem(Activity activity) {
        //BaseApplication.getInstance().getRecommendTopItems().clear();
        //BaseApplication.getInstance().getRecommendTopItems().addAll(readCacheItems(Config.KEY_RECOMMEND_TOP, 60 * 24));

        if (BaseApplication.getInstance().getRecommendTopItems().size() == 0) {
            //Toast.makeText(mContext, "RecommendTop - Request!", Toast.LENGTH_SHORT).show();
            mItems.clear();
            requestRecommendTopItem(activity);
        } else {
            //Toast.makeText(mContext, "RecommendTop - Cache Loaded!", Toast.LENGTH_SHORT).show();
            mRecommendTopItemCallback.onLoad();
        }
    }

    private void requestRecommendTopItem(final Activity activity) {
        String param = getRequestParameter(Config.KEY_RECOMMEND_TOP, "");
        RequestBody requestBody = RequestBody.create(Config.JSON, param);
        okhttp3.Request request = new okhttp3.Request.Builder().url(Config.URL_NAVER_RECOMMEND_TOP_LIST).post(requestBody).build();
        OkHttpSingleton.getInstance().getClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseString = response.body().string();
                //Log.e(TAG, "RecommendTop: responseString\n" + responseString);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseRecommendTopItem(responseString);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.e(TAG, "ERROR: " + e.getMessage());
                Log.e(TAG, "URL: " + Config.URL_NAVER_RECOMMEND_TOP_LIST);
                parseRecommendTopItem("");
            }
        });
    }

    private void parseRecommendTopItem(String response) {
        NaverParser naverParser = new NaverParser();
        naverParser.parseRecommend(response, Config.KEY_RECOMMEND_TOP, mItems, false);
        //Log.e(TAG, "RecommendTop: " + mItems.size());
        //Log.e(TAG, "ItemPrices: " + BaseApplication.getInstance().getItemPrices().size());

        BaseApplication.getInstance().getRecommendTopItems().clear();
        BaseApplication.getInstance().getRecommendTopItems().addAll(mItems);
        //writeCacheItems(Config.KEY_RECOMMEND_TOP, BaseApplication.getInstance().getRecommendTopItems());

        for (Item bi : BaseApplication.getInstance().getItemPrices()) {
            for (Item item : mItems) {
                if (bi.getCode().equals(item.getCode())) {
                    //Log.e(TAG, item.getName() + ": " + item.getNor());
                    bi.setNor(item.getNor());
                }
            }
        }

        mRecommendTopItemCallback.onLoad();
    }

    /**
     * [네이버 금융 > 국내 > 현재 추천 종목] 가져오기
     */
    public interface RecommendCurrentItemCallback {
        void onLoad(ArrayList<Item> items);
    }

    private RecommendCurrentItemCallback mRecommendCurrentItemCallback;

    public void setOnRecommendCurrentItemLoaded(RecommendCurrentItemCallback callback) {
        mRecommendCurrentItemCallback = callback;
    }

    public void loadRecommendCurrentItem(Activity activity) {
        mItems.clear();
        //mItems = readCacheItems(Config.KEY_RECOMMEND_CURRENT, 60 * 24); // 1 day
        //if (mItems.size() > 0) {
        //    mRecommendCurrentItemCallback.onLoad(mItems);
        //} else {
        requestRecommendCurrentItem(activity);
        //}
    }

    private void requestRecommendCurrentItem(final Activity activity) {
        String param = getRequestParameter(Config.KEY_RECOMMEND_CURRENT, "");
        RequestBody requestBody = RequestBody.create(Config.JSON, param);
        okhttp3.Request request = new okhttp3.Request.Builder().url(Config.URL_NAVER_RECOMMEND_CURRENT_LIST).post(requestBody).build();
        OkHttpSingleton.getInstance().getClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseString = response.body().string();
                //Log.e(">>>>>", "responseString\n" + responseString);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseRecommendCurrentItem(responseString);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.e(TAG, "ERROR: " + e.getMessage());
                Log.e(TAG, "URL: " + Config.URL_NAVER_RECOMMEND_CURRENT_LIST);
                parseRecommendCurrentItem("");
            }
        });
    }

    private void parseRecommendCurrentItem(String response) {
        NaverParser naverParser = new NaverParser();
        naverParser.parseRecommend(response, Config.KEY_RECOMMEND_CURRENT, mItems, false);
        //Log.e(TAG, "RecommendCurrentItems.size(): " + mItems.size());

        //writeCacheItems(Config.KEY_RECOMMEND_CURRENT, mItems);
        mRecommendCurrentItemCallback.onLoad(mItems);
    }

    /**
     * [네이버 금융 > 국내 > 추천종목 > 현재 추천종목 > 추천 사유] 로드하기
     */

    public interface RecommendReasonCallback {
        void onLoad(ArrayList<Reason> reasons);
    }

    private RecommendReasonCallback mRecommendReasonCallback;

    public void setOnRecommendReasonLoaded(RecommendReasonCallback callback) {
        mRecommendReasonCallback = callback;
    }

    public void loadRecommendReason(Activity activity, String code) {
        requestRecommendReason(activity, code);
    }

    private void requestRecommendReason(final Activity activity, String code) {
        String param = getRequestParameter(Config.KEY_RECOMMEND_REASON, code);
        RequestBody requestBody = RequestBody.create(Config.JSON, param);
        okhttp3.Request request = new okhttp3.Request.Builder().url(Config.URL_NAVER_RECOMMEND_REASON).post(requestBody).build();
        OkHttpSingleton.getInstance().getClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                final String responseString = response.body().string();
                //Log.e(">>>>>", "responseString\n" + responseString);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseRecommendReason(responseString);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.e(TAG, "ERROR: " + e.getMessage());
                Log.e(TAG, "URL: " + Config.URL_NAVER_RECOMMEND_REASON);
                parseRecommendReason("");
            }
        });
    }

    private void parseRecommendReason(String response) {
        ArrayList<Reason> reasons = new ArrayList<>();

        NaverParser naverParser = new NaverParser();
        naverParser.parseRecommendReasonList(response, reasons);
        //Log.e(TAG, "reasons.size(): " + reasons.size());

        mRecommendReasonCallback.onLoad(reasons);
    }

    /*
     * [네이버 금융 > 국내 > 개별 종목 > 뉴스 목록] 가져오기
     */
    /*
    public interface ItemNewsCallback {
        void onLoad(ArrayList<News> list);
    }

    private ItemNewsCallback mItemNewsCallback;

    public void setOnItemNewsLoaded(ItemNewsCallback callback) {
        mItemNewsCallback = callback;
    }

    public void loadItemNews(Item item) {
        requestItemNews(item);
    }

    private void requestItemNews(final Item item) {
        String url = Config.URL_DAUM_NEWS_LIST + item.getCode();
        //Log.e(TAG, "url: " + url);

        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "response:\n" + response);
                parseItemNews(response, item);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                parseItemNews("", item);
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseItemNews(String response, Item item) {
        ArrayList<News> list = new ArrayList<>();

        DaumNewsParser daumNewsParser = new DaumNewsParser();
        daumNewsParser.parseList(item, response, list);

        mItemNewsCallback.onLoad(list);
    }
    */

    /**
     * 속보 로드하기
     */
    public interface BreakingCallback {
        void onLoad(ArrayList<News> list);
    }

    private BreakingCallback mBreakingCallback;

    public void setOnBreakingLoaded(BreakingCallback callback) {
        mBreakingCallback = callback;
    }

    public void loadBreaking(String url) {
        requestBreaking(url);
    }

    private void requestBreaking(String url) {
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseBreaking(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                parseBreaking("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseBreaking(String response) {
        ArrayList<News> list = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(response);
            JSONArray jsonArray = json.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                News news = new News();
                news.setId(i + 1);
                news.setTitle(Util.getString(obj, "title"));
                news.setPublishedText(Util.getString(obj, "published"));
                news.setElapsedText(Util.getString(obj, "elapsed"));
                list.add(news);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "> ERROR: parseBreaking()\n" + e.getMessage());
        }

        mBreakingCallback.onLoad(list);
    }

    /**
     * [상승 속보] 가져오기
     */
    public interface CauseCallback {
        void onLoad(ArrayList<Cause> list);
    }

    private CauseCallback mCauseCallback;

    public void setOnCauseLoaded(CauseCallback callback) {
        mCauseCallback = callback;
    }

    public void loadCause() {
        requestCause();
    }

    private void requestCause() {
        StringRequest strReq = new StringRequest(Request.Method.GET, Config.URL_CAUSE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "response:\n" + response);
                parseCause(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, "Volley Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                parseCause("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseCause(String response) {
        ArrayList<Cause> list = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Cause cause = new Cause();
                cause.setId(Util.getInt(obj, "id"));
                cause.setNo(Util.getInt(obj, "no"));
                cause.setType(Util.getString(obj, "type"));
                cause.setCode(Util.getString(obj, "code"));
                cause.setName(Util.getString(obj, "name"));
                cause.setPrice(Util.getInt(obj, "price"));
                cause.setRof(BigDecimal.valueOf(Util.getDouble(obj, "rof")).floatValue());
                cause.setPof(BigDecimal.valueOf(Util.getDouble(obj, "pof")).floatValue());
                cause.setTitle(Util.getString(obj, "title"));
                cause.setPublished(Util.getString(obj, "published"));
                cause.setElapsed(Util.getString(obj, "elapsed"));

                //Log.e(TAG, news.getTitle());

                list.add(cause);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mCauseCallback.onLoad(list);
    }

    /**
     * [네이버 금융 > 뉴스 목록] 가져오기
     */
    public interface NewsListCallback {
        void onLoad(ArrayList<News> list);
    }

    private NewsListCallback mNewsListCallback;

    public void setOnNewsListLoaded(NewsListCallback callback) {
        mNewsListCallback = callback;
    }

    public void loadNewsList(String url) {
        requestNewsList(url);
    }

    private void requestNewsList(final String url) {
        //Log.e(TAG, "url: " + url);
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "response:\n" + response);
                parseNewsList(url, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                parseNewsList(url, "");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseNewsList(String url, String response) {
        ArrayList<News> list = new ArrayList<>();

        NaverNewsParser naverNewsParser = new NaverNewsParser();
        if (url.contains("news_list.nhn?mode=RANK")) { // 많이 본 뉴스
            naverNewsParser.parseRankingList(response, list);
        } else if (url.contains("mainnews.nhn")) { // 주요 뉴스
            naverNewsParser.parseMainList(response, list);
        } else if (url.contains("market_special.nhn")) { // 특징주
            naverNewsParser.parseFeatureList(response, list);
        } else { // 속보, 특징주
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    News news = new News();
                    news.setId(Util.getInt(obj, "id"));
                    news.setTitle(Util.getString(obj, "title"));
                    news.setUrl(Util.getString(obj, "url"));
                    news.setElapsedText(Util.getString(obj, "elapsed"));
                    news.setPublishedText(Util.getString(obj, "published"));

                    //Log.e(TAG, news.getTitle());

                    list.add(news);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mNewsListCallback.onLoad(list);
    }

    /**
     * [네이버 금융 > 뉴스 상세] 가져오기
     */
    public interface NewsDetailCallback {
        void onLoad(News news);
    }

    private NewsDetailCallback mNewsDetailCallback;

    public void setOnNewsDetailLoaded(NewsDetailCallback callback) {
        mNewsDetailCallback = callback;
    }

    public void loadNewsDetail(String url) {
        requestNewsDetail(url);
    }

    private void requestNewsDetail(String url) {
        //Log.e(TAG, "url: " + url);
        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "response:\n" + response);
                parseNewsDetail(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                parseNewsDetail("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseNewsDetail(String response) {
        NaverNewsParser naverNewsParser = new NaverNewsParser();
        News news = naverNewsParser.parseDetail(response);

        mNewsDetailCallback.onLoad(news);
    }

    // 네이버 추천 종목 Ajax 파라미터
    private String getRequestParameter(String siteId, String code) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (siteId.equals(Config.KEY_RECOMMEND_RETURN)) {
                //-------------------
                // 추천 종목 수익률
                //-------------------
                jsonObject.put("brkcd", "0");
                jsonObject.put("cmpcd", "");
                jsonObject.put("curPage", "1");
                jsonObject.put("enddt", Util.getToday("yyyyMMdd"));
                jsonObject.put("orderCol", "8"); // 6 = 추천일 후 수익률, 7 = 1주일 수익률, 8 = 1개월 수익률
                jsonObject.put("orderType", "D");
                jsonObject.put("perPage", "50");
                jsonObject.put("pfcd", "0");
            } else if (siteId.equals(Config.KEY_RECOMMEND_TOP)) {
                //-------------------
                // 추천 건수 상위
                //-------------------
                jsonObject.put("brkcd", "0");
                jsonObject.put("cmpcd", "");
                jsonObject.put("curPage", "1");
                jsonObject.put("enddt", Util.getToday("yyyyMMdd"));
                jsonObject.put("orderCol", "1"); // 1 = 추천수, 7 = 1주일 주가 변화율
                jsonObject.put("orderType", "D");
                jsonObject.put("perPage", "50");
                jsonObject.put("pfcd", "0");
            } else if (siteId.equals(Config.KEY_RECOMMEND_CURRENT)) {
                //-------------------
                // 현재 추천 종목
                //-------------------
                jsonObject.put("brkCD", "0");
                jsonObject.put("cmpC", "");
                jsonObject.put("curPage", "1");
                jsonObject.put("orderCol", "4");
                jsonObject.put("orderType", "D");
                jsonObject.put("perPage", "120");
                jsonObject.put("pfCD", "0");
                jsonObject.put("stdDt", Util.getToday("yyyy-MM-dd"));
            } else if (siteId.equals(Config.KEY_RECOMMEND_REASON)) {
                //-------------------
                // 종목 추천 이유
                //-------------------
                jsonObject.put("brkCD", "0");
                jsonObject.put("cmpCD", code);
                jsonObject.put("curPage", "1");
                jsonObject.put("orderCol", "4");
                jsonObject.put("orderType", "D");
                jsonObject.put("perPage", "20");
                jsonObject.put("pfCD", "0");
                jsonObject.put("stdDt", Util.getToday("yyyy-MM-dd"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
        //Log.e(TAG, "postBody: " + postBody);
    }

    // 설정 파싱
    private void parseSettings(JSONObject data) {
        BaseApplication.getInstance().getSettings().clear();
        try {
            JSONArray settingArray = data.getJSONArray("settings");
            for (int i = 0; i < settingArray.length(); i++) {
                JSONObject obj = settingArray.getJSONObject(i);
                Setting setting = new Setting();
                setting.setField(Util.getString(obj, "field"));
                setting.setValue(Util.getString(obj, "value"));
                BaseApplication.getInstance().getSettings().add(setting);
                //Log.e(TAG, setting.getField());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "> ERROR: parseSettings()\n" + e.getMessage());
        }
    }

    // 단어 파싱
    public void parseWords(JSONObject data) {
        BaseApplication.getInstance().getWords().clear();
        try {
            JSONArray jsonArray = data.getJSONArray("words");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Word word = new Word();
                word.setId(Util.getInt(obj, "id"));
                word.setCategory(Util.getString(obj, "category"));
                word.setValue(Util.getString(obj, "value"));
                BaseApplication.getInstance().getWords().add(word);
                //Log.e(TAG, word.getValue());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "> ERROR: parseWords()\n" + e.getMessage());
        }
    }

    // 태그 파싱
    public void parseTags(JSONObject data) {
        BaseApplication.getInstance().getTags().clear();
        try {
            JSONArray jsonArray = data.getJSONArray("tags");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Tag tag = new Tag();
                tag.setId(Util.getInt(obj, "id"));
                tag.setName(Util.getString(obj, "name"));
                tag.setBgc(Util.getString(obj, "bgc"));
                tag.setFgc(Util.getString(obj, "fgc"));
                BaseApplication.getInstance().getTags().add(tag);
                //Log.e(TAG, tag.getName());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "> ERROR: parseTags()\n" + e.getMessage());
        }
    }

    // 포트폴리오 파싱
    public void parsePortfolios(JSONObject data) {
        BaseApplication.getInstance().getPortfolios().clear();
        try {
            JSONArray jsonArray = data.getJSONArray("portfolios");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Portfolio portfolio = new Portfolio();
                portfolio.setCode(Util.getString(obj, "code"));
                portfolio.setTagIds(Util.getString(obj, "tag_ids"));
                BaseApplication.getInstance().getPortfolios().add(portfolio);
                //Log.e(TAG, portfolio.getCode());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "> ERROR: parsePortfolios()\n" + e.getMessage());
        }
    }

    public String readPreferences(String key) {
        return mPreferences.getString(key, "");
    }

    public void writePreferences(String key, String value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 종목에 태그 설정하기
     *
     * @param item
     * @param selectedTagId
     */
    public void setItemTagIds(Item item, String selectedTagId) {
        String itemTagIds = item.getTagIds();
        String newTagIds;
        if (itemTagIds != null && !itemTagIds.isEmpty()) {
            String[] itemTagIdArray = itemTagIds.split(",");
            StringJoiner joiner = new StringJoiner(",");
            boolean found = false;
            for (String itemTagId : itemTagIdArray) {
                if (itemTagId.equals(selectedTagId)) {
                    found = true;
                } else {
                    joiner.add(itemTagId);
                }
            }
            if (!found) {
                joiner.add(selectedTagId);
            }

            itemTagIds = joiner.toString();
            itemTagIdArray = itemTagIds.split(",");
            for (String itemTagId : itemTagIdArray) {
                boolean valid = false;
                if (!itemTagId.isEmpty()) {
                    for (Tag tag : BaseApplication.getInstance().getTags()) {
                        if (tag.getId() == Long.valueOf(itemTagId)) {
                            valid = true;
                            break;
                        }
                    }
                }
                if (valid) {
                    joiner.add(itemTagId);
                }
            }
            newTagIds = joiner.toString();
        } else {
            newTagIds = selectedTagId;
        }

        item.setTagIds(newTagIds);

        boolean found = false;
        for (Portfolio portfolio : BaseApplication.getInstance().getPortfolios()) {
            if (portfolio.getCode().equals(item.getCode())) {
                portfolio.setTagIds(newTagIds);
                found = true;
            }
        }
        if (!found) {
            Portfolio portfolio = new Portfolio();
            portfolio.setCode(item.getCode());
            portfolio.setTagIds(newTagIds);
            BaseApplication.getInstance().getPortfolios().add(portfolio);
        }

        writePortfolios();
    }

    /**
     * 종목에 태그 설정하기
     *
     * @param item
     * @param selectedTagId
     */
    public void getItemTagIds(Item item, String selectedTagId) {
        String itemTagIds = item.getTagIds();
        String newTagIds;
        if (itemTagIds != null && !itemTagIds.isEmpty()) {
            String[] itemTagIdArray = itemTagIds.split(",");
            StringJoiner joiner = new StringJoiner(",");
            boolean found = false;
            for (String itemTagId : itemTagIdArray) {
                if (itemTagId.equals(selectedTagId)) {
                    found = true;
                } else {
                    joiner.add(itemTagId);
                }
            }
            if (!found) {
                joiner.add(selectedTagId);
            }

            itemTagIds = joiner.toString();
            itemTagIdArray = itemTagIds.split(",");
            for (String itemTagId : itemTagIdArray) {
                boolean valid = false;
                if (!itemTagId.isEmpty()) {
                    for (Tag tag : BaseApplication.getInstance().getTags()) {
                        if (tag.getId() == Long.valueOf(itemTagId)) {
                            valid = true;
                            break;
                        }
                    }
                }
                if (valid) {
                    joiner.add(itemTagId);
                }
            }
            newTagIds = joiner.toString();
        } else {
            newTagIds = selectedTagId;
        }

        item.setTagIds(newTagIds);

        boolean found = false;
        for (Portfolio portfolio : BaseApplication.getInstance().getPortfolios()) {
            if (portfolio.getCode().equals(item.getCode())) {
                portfolio.setTagIds(newTagIds);
                found = true;
            }
        }
        if (!found) {
            Portfolio portfolio = new Portfolio();
            portfolio.setCode(item.getCode());
            portfolio.setTagIds(newTagIds);
            BaseApplication.getInstance().getPortfolios().add(portfolio);
        }

        //writePortfolio();
    }

    /**
     * 종목 태그 저장하기
     */
    public interface ItemTagCallback {
        void onItemTagSaved();
    }

    private ItemTagCallback mItemTagCallback;

    public void setOnItemTagSaved(ItemTagCallback callback) {
        mItemTagCallback = callback;
    }

    public void saveItemTag(final String code, final String tagIds) {
        StringRequest strReq = new StringRequest(Request.Method.POST, Config.URL_PORTFOLIO_UPDATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(mContext, "Saved!", Toast.LENGTH_SHORT).show();
                for (Portfolio portfolio : BaseApplication.getInstance().getPortfolios()) {
                    if (portfolio.getCode().equals(code)) {
                        portfolio.setTagIds(tagIds);
                        break;
                    }
                }
                mItemTagCallback.onItemTagSaved();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "ERROR: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag_id", tagIds);
                params.put("code", code);
                return params;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    public void readSettings() {
        BaseApplication.getInstance().getSettings().clear();
        String cacheData = readPreferences(Config.PREFERENCE_SETTINGS);
        if (!cacheData.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(cacheData);
                JSONArray jsonArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    Setting setting = new Setting();
                    setting.setField(Util.getString(obj, "field"));
                    setting.setValue(Util.getString(obj, "value"));

                    BaseApplication.getInstance().getSettings().add(setting);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeSettings() {
        try {
            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();

            for (Setting setting : BaseApplication.getInstance().getSettings()) {
                JSONObject obj = new JSONObject();
                obj.put("field", setting.getField());
                obj.put("value", setting.getValue());
                array.put(obj);
            }
            json.put("data", array);
            writePreferences(Config.PREFERENCE_SETTINGS, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void readTags() {
        BaseApplication.getInstance().getTags().clear();
        //String cacheData = Util.readFile(Config.PREFERENCE_TAGS);
        String cacheData = readPreferences(Config.PREFERENCE_TAGS);
        if (!cacheData.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(cacheData);
                //String cacheDate = jsonObject.getString("date");
                //Log.e(TAG, cacheDate);
                //Log.e(TAG, cacheData);

                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    Tag tag = new Tag();
                    tag.setId(Util.getInt(obj, "id"));
                    tag.setName(Util.getString(obj, "name"));
                    //Log.e(TAG, tag.getId() + " / " + tag.getName());

                    BaseApplication.getInstance().getTags().add(tag);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeTags() {
        try {
            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();

            for (Tag tag : BaseApplication.getInstance().getTags()) {
                JSONObject obj = new JSONObject();
                obj.put("id", tag.getId());
                obj.put("name", tag.getName());

                //Log.e(TAG, portfolio.getCode() + " / " + portfolio.getTagIds());
                array.put(obj);
            }
            //String date = Util.getToday(mDateFormatString);
            //json.put("date", date);
            json.put("data", array);
            //Log.e(TAG, date);
            //Log.e(TAG, json.toString());

            //Util.writeFile(mContext, Config.PREFERENCE_TAGS, json.toString());
            writePreferences(Config.PREFERENCE_TAGS, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void readPortfolios() {
        BaseApplication.getInstance().getPortfolios().clear();

        //String cacheData = Util.readFile(Config.PREFERENCE_PORTFOLIOS);
        String cacheData = readPreferences(Config.PREFERENCE_PORTFOLIOS);
        if (!cacheData.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(cacheData);
                //String cacheDate = jsonObject.getString("date");
                //Log.e(TAG, cacheDate);
                //Log.e(TAG, cacheData);

                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    Portfolio portfolio = new Portfolio();
                    portfolio.setCode(Util.getString(obj, "code"));
                    portfolio.setTagIds(Util.getString(obj, "tagIds"));
                    //Log.e(TAG, portfolio.getCode() + " / " + portfolio.getTagIds());

                    BaseApplication.getInstance().getPortfolios().add(portfolio);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void writePortfolios() {
        try {
            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();

            for (Portfolio portfolio : BaseApplication.getInstance().getPortfolios()) {
                if (portfolio.getTagIds() == null || portfolio.getTagIds().isEmpty()) {
                    continue;
                }
                JSONObject obj = new JSONObject();
                obj.put("code", portfolio.getCode());
                obj.put("tagIds", portfolio.getTagIds());

                //Log.e(TAG, portfolio.getCode() + " / " + portfolio.getTagIds());

                array.put(obj);
            }
            //String date = Util.getToday(mDateFormatString);
            //json.put("date", date);
            json.put("data", array);
            //Log.e(TAG, date);
            //Log.e(TAG, json.toString());

            //Util.writeFile(mContext, Config.PREFERENCE_PORTFOLIOS, json.toString());
            writePreferences(Config.PREFERENCE_PORTFOLIOS, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Item> readCacheItems(String fileName, int expireMinutes) {
        ArrayList<Item> items = new ArrayList<>();
        //if (!BaseApplication.CACHE_MODE) {
        //    return items;
        //}

        String cacheData = Util.readFile(fileName);
        if (!cacheData.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(cacheData);
                String cacheDate = jsonObject.getString("date");
                //Log.e(TAG, cacheDate);
                //Log.e(TAG, cacheData);

                if (isValidCache(cacheDate, expireMinutes)) {
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        Item item = new Item();
                        item.setId(Util.getInt(obj, "id"));
                        item.setCode(Util.getString(obj, "code")); // 종목 코드
                        item.setName(Util.getString(obj, "name")); // 종목 이름
                        item.setPrice(Util.getInt(obj, "price"));  // 현재가
                        item.setPof(Util.getInt(obj, "pof"));    // 전일비
                        item.setRof(BigDecimal.valueOf(Util.getDouble(obj, "rof")).floatValue()); // 등락률

                        item.setListed(Util.getString(obj, "listed")); // 추천일
                        item.setElapsed(Util.getInt(obj, "elapse"));   // 추천 경과일
                        item.setRor(BigDecimal.valueOf(Util.getDouble(obj, "ror")).floatValue()); // 수익률
                        item.setNor(Util.getInt(obj, "nor"));          // 추천수
                        item.setReason(Util.getString(obj, "reason")); // 추천 사유

                        item.setForeigner(Util.getBoolean(obj, "foreigner"));     // 외국인
                        item.setInstitution(Util.getBoolean(obj, "institution")); // 기관
                        item.setOverseas(Util.getBoolean(obj, "overseas"));       // 해외 증권사
                        item.setDomestic(Util.getBoolean(obj, "domestic"));       // 국내 증권사
                        item.setBuy(Util.getBoolean(obj, "buy"));   // 매수
                        item.setSell(Util.getBoolean(obj, "sell")); // 매도
                        item.setPot(Util.getInt(obj, "pot"));       // 거래금액
                        item.setVot(Util.getInt(obj, "vot"));       // 거래량

                        //Log.e(TAG, "readCacheItems(): " + item.getName() + " " + item.getPrice() + " / " + item.getRof());

                        items.add(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return items;
    }

    public void writeCacheItems(String fileName, ArrayList<Item> items) {
        //if (!BaseApplication.CACHE_MODE) {
        //    return;
        //}
        try {
            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();
            for (int i = 0; i < items.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("id", items.get(i).getId());
                obj.put("code", items.get(i).getCode());   // 종목 코드
                obj.put("name", items.get(i).getName());   // 종목 이름
                obj.put("price", items.get(i).getPrice()); // 현재가
                obj.put("pof", items.get(i).getPof());     // 전일비
                obj.put("rof", items.get(i).getRof());     // 등락률

                obj.put("listed", items.get(i).getListed());   // 추천일
                obj.put("elapsed", items.get(i).getElapsed()); // 추천 경과일
                obj.put("ror", items.get(i).getRor());         // 수익률
                obj.put("nor", items.get(i).getNor());         // 추천수
                obj.put("reason", items.get(i).getReason());   // 추천 사유

                obj.put("foreigner", items.get(i).isForeigner());     // 외국인
                obj.put("institution", items.get(i).isInstitution()); // 기관
                obj.put("overseas", items.get(i).isOverseas());       // 해외 증권사
                obj.put("domestic", items.get(i).isDomestic());       // 국내 증권사
                obj.put("buy", items.get(i).isBuy());   // 매수
                obj.put("sell", items.get(i).isSell()); // 매도
                obj.put("pot", items.get(i).getPot());  // 거래금액
                obj.put("vot", items.get(i).getVot());  // 거래량

                //Log.e(TAG, "writeCacheItems(): " + items.get(i).getName() + " " + items.get(i).getPrice() + " / " + items.get(i).getRof());

                array.put(obj);
            }
            String date = Util.getToday(mDateFormatString);
            json.put("date", date);
            json.put("data", array);
            //Log.e(TAG, date);
            //Log.e(TAG, json.toString());

            Util.writeFile(mContext, fileName, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidCache(String cacheDate, int expireMinutes) {
        if (cacheDate == null || cacheDate.isEmpty()) {
            return false;
        } else {
            if (expireMinutes == 0) {
                return true;
            } else {
                try {
                    Calendar cal = Calendar.getInstance();
                    Date today = cal.getTime();
                    Date cache = mSimpleDateFormat.parse(cacheDate);

                    //https://stackoverflow.com/questions/5351483/calculate-date-time-difference-in-java
                    long diff = today.getTime() - cache.getTime();
                    //long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                    //Toast.makeText(mContext, minutes + "분 전 캐쉬", Toast.LENGTH_SHORT).show();

                    return (minutes < expireMinutes);
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }

                return false;
            }
        }
    }
}
