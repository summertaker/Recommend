package com.summertaker.recommend.common;

import java.text.DecimalFormat;

import okhttp3.MediaType;

public class Config {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final static String PACKAGE_NAME = "com.summertaker.stock";
    public final static String PREFERENCE_KEY = PACKAGE_NAME;
    public final static String PREFERENCE_SETTINGS = "settings";
    public final static String PREFERENCE_TAGS = "tags";
    public final static String PREFERENCE_TAG_MODE = "tag_mode";
    public final static String PREFERENCE_TAG_MODE_ON = "tag_mode_on";
    public final static String PREFERENCE_TAG_MODE_OFF = "tag_mode_off";
    public final static String PREFERENCE_PORTFOLIOS = "portfolios";

    //public static String USER_AGENT_DESKTOP = "Mozilla/5.0 (Macintosh; U; Mac OS X 10_6_1; en-US) AppleWebKit/530.5 (KHTML, like Gecko) Chrome/ Safari/530.5";
    //public static String USER_AGENT_MOBILE = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16";

    public static String KEY_ACTIVITY_TITLE = "activity_title";

    public static String KEY_NEWS_BREAKING = "news_breaking";
    public static String KEY_NEWS_FEATURE = "news_feature";
    public static String KEY_NEWS_MAIN = "news_main";
    public static String KEY_NEWS_RANKING = "news_ranking";

    public static String KEY_WORD_CATEGORY = "word_category";
    public static String KEY_WORD_CATEGORY_BREAKING_INCLUDE = "breaking_include";
    public static String KEY_WORD_CATEGORY_BREAKING_EXCLUDE = "breaking_exclude";
    public static String KEY_WORD_CATEGORY_NAVER_INCLUDE = "naver_include";
    public static String KEY_WORD_CATEGORY_NAVER_EXCLUDE = "naver_exclude";
    public static String KEY_WORD_CATEGORY_NAVER_AD = "naver_ad";
    public static String KEY_WORD_CATEGORY_DAUM_INCLUDE = "daum_include";
    public static String KEY_WORD_CATEGORY_DAUM_EXCLUDE = "daum_exclude";
    public static String KEY_WORD_CATEGORY_DAUM_AD = "daum_ad";

    public static String KEY_FAVORITES = "favorites";
    public static String KEY_BASE_ITEM = "base_item";
    public static String KEY_ITEM_PRICE = "item_price";
    //public static String KEY_ITEM_SEARCH = "item_search";

    public final static String PREFERENCE_FAVORITE_MODE = "favorite_mode";
    public final static String PREFERENCE_FAVORITE_MODE_ON = "favorite_mode_on";
    public final static String PREFERENCE_FAVORITE_MODE_OFF = "favorite_mode_off";

    public static String KEY_FLUCTUATION_RISE = "fluc_rise";
    public static String KEY_FLUCTUATION_JUMP = "fluc_jump";
    //public static String KEY_FLUC_CEILING = "fluc_ceiling";
    public static String KEY_FLUCTUATION_FALL = "fluc_fall";
    public static String KEY_FLUCTUATION_CRASH = "fluc_crash";
    //public static String KEY_FLUC_FLOOR = "fluc_floor";

    public static String KEY_BASE_TRADE = "base_trade";
    public static String KEY_TRADE_BUY = "trade_buy";
    public static String KEY_TRADE_SELL = "trade_sell";
    public static String KEY_TRADE_FOREIGNER = "trade_foreigner";
    public static String KEY_ACC_TRADE_FOREIGNER = "acc_trade_foreigner";
    public static String KEY_TRADE_INSTITUTION = "trade_institution";
    public static String KEY_ACC_TRADE_INSTITUTION = "acc_trade_institution";
    //public static String KEY_ACCUTRADE_FOREIGNER = "accutrade_foreigner";
    //public static String KEY_ACCUTRADE_INSTITUTION = "accutrade_institution";
    public static String KEY_TRADE_OVERSEAS = "trade_overseas";
    public static String KEY_TRADE_DOMESTIC = "trade_domestic";
    public static String KEY_TRADER_BUY = "trader_buy";
    public static String KEY_TRADER_SELL = "trader_sell";

    public static String KEY_WEEK_RISE = "week_rise";
    public static String KEY_WEEK_FB = "week_fb";
    public static String KEY_WEEK_NB = "week_nb";

    public static String KEY_RECOMMEND = "recommend";

    public static String KEY_RECOMMEND_RETURN = "recommend_return";
    public static String KEY_RECOMMEND_TOP = "recommend_top";
    public static String KEY_RECOMMEND_CURRENT = "recommend_current";
    public static String KEY_RECOMMEND_WISE = "recommend_wise";
    public static String KEY_RECOMMEND_REASON = "recommend_reason";
    public static String KEY_DETAIL_INFO = "detail_info";
    public static String KEY_DETAIL_OVERVIEW = "detail_overview";
    public static String KEY_DETAIL_REASON = "detail_reason";
    public static String KEY_DETAIL_NEWS = "detail_news";

    //public static String KEY_INCLUDE = "include";
    //public static String KEY_EXCLUDE = "exclude";

    public static String KEY_PARAM = "param";
    public static String PARAM_GO_TO_THE_TOP = "go_to_the_top";
    public static String PARAM_DO_REFRESH = "do_refresh";
    public static String PARAM_TOGGLE_CHART = "toggle_chart";
    public static String PARAM_TOGGLE_LIST = "toggle_list";
    public static String PARAM_DATA_CHANGED = "data_changed";
    public static String PARAM_LOAD_STARTED = "refresh_started";
    public static String PARAM_LOAD_FINISHED = "refresh_finished";
    public static String PARAM_FINISH = "finish";
    public static String PARAM_REFRESH_ALL_FRAGMENT = "refresh_all_fragment";

    //public static String URL_DAUM_PRICE_KOSPI = "http://finance.daum.net/quote/all.daum?type=S&stype=P";
    //public static String URL_DAUM_PRICE_KOSDAQ = "http://finance.daum.net/quote/all.daum?type=S&stype=Q";
    public static String URL_DAUM_PRICE_KOSPI = "http://finance.daum.net/api/quotes/stocks?fieldName=&order=&perPage=&market=KOSPI&page=&changes=UPPER_LIMIT%2CRISE%2CEVEN%2CFALL%2CLOWER_LIMIT";
    public static String URL_DAUM_PRICE_KOSDAQ = "http://finance.daum.net/api/quotes/stocks?fieldName=&order=&perPage=&market=KOSDAQ&page=&changes=UPPER_LIMIT%2CRISE%2CEVEN%2CFALL%2CLOWER_LIMIT";
    public static String URL_DAUM_DETAIL = "http://finance.daum.net/item/main.daum?code=";
    public static String URL_DAUM_DOMESTIC = "http://finance.daum.net/quote/index.daum";
    //public static String URL_DAUM_RISE_LIST = "http://finance.daum.net/quote/rise.daum?stype=P&col=pchgrate&order=desc&page=";
    public static String URL_DAUM_TRADE_FOREIGNER_KOSPI_LIST = "http://finance.daum.net/quote/foreign.daum?stype=P";
    public static String URL_DAUM_TRADE_FOREIGNER_KOSDAQ_LIST = "http://finance.daum.net/quote/foreign.daum?stype=Q";
    public static String URL_DAUM_ACC_TRADE_FOREIGNER_KOSPI_LIST = "http://finance.daum.net/quote/foreign.daum?stype=P&before=5";
    public static String URL_DAUM_ACC_TRADE_FOREIGNER_KOSDAQ_LIST = "http://finance.daum.net/quote/foreign.daum?stype=Q&before=5";
    public static String URL_DAUM_TRADE_INSTITUTION_KOSPI_LIST = "http://finance.daum.net/quote/institution.daum?stype=P";
    public static String URL_DAUM_TRADE_INSTITUTION_KOSDAQ_LIST = "http://finance.daum.net/quote/institution.daum?stype=Q";
    public static String URL_DAUM_ACC_TRADE_INSTITUTION_KOSPI_LIST = "http://finance.daum.net/quote/institution.daum?stype=P&before=5";
    public static String URL_DAUM_ACC_TRADE_INSTITUTION_KOSDAQ_LIST = "http://finance.daum.net/quote/institution.daum?stype=Q&before=5";
    public static String URL_DAUM_TRADE_OVERSEAS_KOSPI_LIST = "http://finance.daum.net/quote/trader.daum?trcode=1&stype=P&type=P";
    public static String URL_DAUM_TRADE_OVERSEAS_KOSDAQ_LIST = "http://finance.daum.net/quote/trader.daum?trcode=1&stype=Q&type=P";
    public static String URL_DAUM_TRADE_DOMESTIC_KOSPI_LIST = "http://finance.daum.net/quote/trader.daum?trcode=0&stype=P&type=P";
    public static String URL_DAUM_TRADE_DOMESTIC_KOSDAQ_LIST = "http://finance.daum.net/quote/trader.daum?trcode=0&stype=Q&type=P";
    public static String URL_DAUM_TRADER_LIST = "http://finance.daum.net/quote/trader.daum";
    public static String URL_DAUM_ACCUTRADE_FOREIGN_KOSPI_LIST = "http://finance.daum.net/quote/signal_foreign.daum?col=accumul_volume&order=desc&stype=1&type=buy&gubun=F"; // 누적매매순
    public static String URL_DAUM_ACCUTRADE_FOREIGN_KODAQ_LIST = "http://finance.daum.net/quote/signal_foreign.daum?col=accumul_volume&order=desc&stype=2&type=buy&gubun=F"; // 누적매매순
    public static String URL_DAUM_ACCUTRADE_INSTITUTION_KOSPI_LIST = "http://finance.daum.net/quote/signal_foreign.daum?col=accumul_volume&order=desc&stype=1&type=buy&gubun=I"; // 누적매매순
    public static String URL_DAUM_ACCUTRADE_INSTITUTION_KODAQ_LIST = "http://finance.daum.net/quote/signal_foreign.daum?col=accumul_volume&order=desc&stype=2&type=buy&gubun=I"; // 누적매매순
    public static String URL_DAUM_NEWS_LIST = "http://finance.daum.net/item/news.daum?code=";
    public static String URL_DAUM_SEARCH = "https://m.search.daum.net/search?q=%s";
    public static String URL_DAUM_FINANCE = "http://m.finance.daum.net/m/item/main.daum?code=%s";

    public static String URL_NAVER_NEWS_FEATURE = "https://finance.naver.com/news/market_special.nhn?&page="; // 뉴스 - 특징주
    public static String URL_NAVER_NEWS_MAIN = "https://finance.naver.com/news/mainnews.nhn?&page="; // 뉴스 - 주요 뉴스
    public static String URL_NAVER_NEWS_RANKING = "https://finance.naver.com/news/news_list.nhn?mode=RANK&page="; // 뉴스 - 많이 본 뉴스
    //public static String URL_NAVER_NEWS_BREAKING = "https://finance.naver.com/news/news_list.nhn?mode=LSS2D&section_id=101&section_id2=258&page="; // 뉴스 - 속보
    public static String URL_NAVER_FLUCTUATION_RISE_LIST_KOSPI = "https://finance.naver.com/sise/sise_rise.nhn?sosok=0"; // 상승(코스피)
    public static String URL_NAVER_FLUCTUATION_RISE_LIST_KOSDAQ = "https://finance.naver.com/sise/sise_rise.nhn?sosok=1"; // 상승(코스닥)
    public static String URL_NAVER_FLUCTUATION_JUMP_LIST_KOSPI = "https://finance.naver.com/sise/sise_low_up.nhn?sosok=0"; // 급등(코스피)
    public static String URL_NAVER_FLUCTUATION_JUMP_LIST_KOSDAQ = "https://finance.naver.com/sise/sise_low_up.nhn?sosok=1"; // 급등(코스닥)
    //public static String URL_NAVER_FLUCTUATION_CEILING_LIST = "https://finance.naver.com/sise/sise_upper.nhn"; // 상한가
    public static String URL_NAVER_FLUCTUATION_FALL_LIST_KOSPI = "https://finance.naver.com/sise/sise_fall.nhn?sosok=0"; // 하락(코스피)
    public static String URL_NAVER_FLUCTUATION_FALL_LIST_KOSDAQ = "https://finance.naver.com/sise/sise_fall.nhn?sosok=1"; // 하락(코스닥)
    public static String URL_NAVER_FLUCTUATION_CRASH_LIST_KOSPI = "https://finance.naver.com/sise/sise_high_down.nhn?sosok=0"; // 급락(코스피)
    public static String URL_NAVER_FLUCTUATION_CRASH_LIST_KOSDAQ = "https://finance.naver.com/sise/sise_high_down.nhn?sosok=1"; // 급락(코스닥)
    //public static String URL_NAVER_FLUCTUATION_FLOOR_LIST = "https://finance.naver.com/sise/sise_lower.nhn"; // 하한가
    public static String URL_NAVER_RECOMMEND_RETURN_LIST = "https://recommend.finance.naver.com/Home/GetYieldList"; // 추천 수익률
    public static String URL_NAVER_RECOMMEND_TOP_LIST = "https://recommend.finance.naver.com/Home/GetTopCompanyList"; // 추천수 상위
    public static String URL_NAVER_RECOMMEND_CURRENT_LIST = "https://recommend.finance.naver.com/Home/RecommendDetail"; // 현재 추천
    public static String URL_NAVER_RECOMMEND_REASON = "https://recommend.finance.naver.com/Home/RecommendDetail"; // 종목 추천 사유
    public static String URL_NAVER_RECOMMEND_WISE_LIST = "https://m.stock.naver.com/api/json/sise/recomItemListJson.nhn?pageSize=20&page="; // 와이즈 리포트 추천
    public static String URL_NAVER_SEARCH = "https://m.search.naver.com/search.naver?query=%s";
    public static String URL_NAVER_FINANCE = "https://m.stock.naver.com/item/main.nhn#/stocks/%s/total";

    public static String URL_SETTING = "http://summertaker.cafe24.com/stock/api/setting.php";
    public static String URL_ALERT = "http://summertaker.cafe24.com/stock/api/alert.php";
    public static String URL_GRADE = "http://summertaker.cafe24.com/stock/api/grade.php";
    public static String URL_CAUSE = "http://summertaker.cafe24.com/stock/api/cause.php";
    public static String URL_PORTFOLIO_UPDATE = "http://summertaker.cafe24.com/stock/api/portfolio_update.php";
    public static String URL_SETTING_SAVE = "http://summertaker.cafe24.com/stock/api/setting_save.php";
    //public static String URL_TAG_LIST = "http://summertaker.cafe24.com/stock/api/tag_list.php";
    public static String URL_TAG_SAVE = "http://summertaker.cafe24.com/stock/api/tag_save.php";
    public static String URL_TAG_SORT = "http://summertaker.cafe24.com/stock/api/tag_sort.php";
    public static String URL_TAG_DELETE = "http://summertaker.cafe24.com/stock/api/tag_delete.php";
    public static String URL_WORD_SAVE = "http://summertaker.cafe24.com/stock/api/word_save.php";
    public static String URL_WORD_DELETE = "http://summertaker.cafe24.com/stock/api/word_delete.php";
    public static String URL_BREAKING_LIST = "http://summertaker.cafe24.com/stock/api/breaking_list.php?page=";
    public static String URL_FEATURE_LIST = "http://summertaker.cafe24.com/stock/api/feature_list.php?page=";
    public static String URL_ITEM_HISTORY = "http://summertaker.cafe24.com/stock/api/item_history.php?date=";

    public static DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###,###");
    public static DecimalFormat SIGNED_FORMAT = new DecimalFormat("+#,###,###;-#");
    public static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("+#,##0.00;-#");

    public static int TAG_TEXT_SIZE_XS = 13;
    public static String TAG_FGC_OFF = "#555555";
    public static String TAG_BGC_OFF = "#e6e6e6";
    public static String TAG_FGC_ON = "#ffffff";
    public static String TAG_BGC_ON = "#777777";

    public static String NEWS_TEXT_HIGHLIGHT_FORMAT = "<font color='#D32F2F'>%s</font>"; // Red
    public static String NEWS_ITEM_NAME_HIGHLIGHT_FORMAT = "<font color='#1976D2'>%s</font>"; // Blue
    public static String NEWS_ITEM_NAME_HYPERLINK_FORMAT = "<a href='%s'>%s</a>"; // Blue

    public static String DATE_FORMAT = "yyyy.MM.dd";

    public static int ACTIVITY_REQUEST_CODE = 100;

    public static String SETTING_BUY_PRICE_PER_ITEM = "purchase_price_per_item";
    public static String SETTING_LOWEST_ROF = "lowest_rof";
    public static String SETTING_HIGHEST_ROF = "highest_rof";
    public static String SETTING_LOWEST_PRICE = "lowest_price";
    public static String SETTING_HIGHEST_PRICE = "highest_price";
    //public static String SETTING_PICKER_LOWEST_ROF = "picker_lowest_rof";
    //public static String SETTING_PICKER_HIGHEST_ROF = "picker_highest_rof";

    public static String SETTING_RECOMMEND_LOWEST_PRICE = "recommend_lowest_price";
    public static String SETTING_RECOMMEND_HIGHEST_PRICE = "recommend_highest_price";
    public static String SETTING_RECOMMEND_RATE_OF_FLUCTUATION = "recommend_rate_of_fluctuation";
}
