package com.summertaker.recommend.parser;

import android.util.Log;

import com.summertaker.recommend.common.BaseApplication;
import com.summertaker.recommend.common.BaseParser;
import com.summertaker.recommend.common.Config;
import com.summertaker.recommend.data.Item;
import com.summertaker.recommend.data.Reason;
import com.summertaker.recommend.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NaverParser extends BaseParser {

    public void parseFluctuation(String response, ArrayList<Item> items) {
        if (response == null || response.isEmpty()) {
            return;
        }

        int lowestPrice = BaseApplication.getInstance().getIntSetting(Config.SETTING_LOWEST_PRICE); // 최저가
        int highestPrice = BaseApplication.getInstance().getIntSetting(Config.SETTING_HIGHEST_PRICE); // 최고가
        float rateOfFluctuation = BaseApplication.getInstance().getFloatSetting(Config.SETTING_LOWEST_ROF); // 등락률

        //Log.e(TAG, lowestPrice + " ~ " + highestPrice + ", " + rateOfFluctuation);

        Document doc = Jsoup.parse(response);

        Elements tables = doc.getElementsByTag("table");
        for (Element table : tables) {
            String className = table.attr("class");
            if (!"type_2".equals(className)) {
                continue;
            }

            long id = 1;

            Elements trs = table.getElementsByTag("tr");
            for (Element tr : trs) {
                Elements tds = tr.getElementsByTag("td");
                if (tds.size() != 12) {
                    continue;
                }

                String code;
                String name;
                int price;
                int pof;
                float rof;
                int vot;
                float per;
                float roe;
                String temp;

                Elements a = tds.get(1).getElementsByTag("a");
                String href = a.attr("href");
                code = getCodeFromUrl(href);
                name = a.text();

                temp = tds.get(2).text();
                temp = temp.replace(",", "");
                price = Integer.parseInt(temp);

                temp = tds.get(3).text();
                temp = temp.replace(",", "");
                pof = Integer.valueOf(temp);

                temp = tds.get(4).text();
                temp = temp.replace(",", "");
                temp = temp.replace("%", "");
                temp = temp.replace("％", "");
                rof = Float.valueOf(temp);

                if (lowestPrice > 0 && price < lowestPrice) {
                    continue;
                }

                if (highestPrice > 0 && price > highestPrice) {
                    continue;
                }

                //if (rateOfFluctuation > 0) {
                //    if (siteId.equals(Config.KEY_FLUCTUATION_RISE) && rof < rateOfFluctuation) {
                //            continue;
                //    }
                //    else if (siteId.equals(Config.KEY_FLUCTUATION_FALL) && rof > -rateOfFluctuation) {
                //        continue;
                //    }
                //}

                temp = tds.get(5).text();
                temp = temp.replace(",", "");
                vot = Integer.valueOf(temp);

                temp = tds.get(10).text();
                temp = temp.replace(",", "");
                temp = temp.replace("%", "");
                temp = temp.replace("％", "");
                temp = temp.replace("N/A", "0");
                per = Float.valueOf(temp);

                temp = tds.get(11).text();
                temp = temp.replace(",", "");
                temp = temp.replace("%", "");
                temp = temp.replace("％", "");
                temp = temp.replace("N/A", "0");
                roe = Float.valueOf(temp);

                //title = title.replaceAll("\\[.*\\]", "");

                //Log.e(TAG, "itemCd: " + code + ", " + name + ", " + price + ", " + adp + ", " + adr + ", " + volume + ", " + per + ", " + roe);

                Item item = new Item();
                item.setId(id);
                item.setCode(code);
                item.setName(name);
                item.setPrice(price);
                item.setPof(pof);
                item.setRof(rof);
                item.setVot(vot);
                item.setPer(per);
                item.setRoe(roe);

                items.add(item);
                id++;
            }
        }
    }

    public void parseRecommend(String response, String siteId, ArrayList<Item> items, boolean applyFilter) {
        if (response == null || response.isEmpty()) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            //Log.e(TAG, "jsonArray.length() = " + jsonArray.length());

            /*
            // 네이버 > 추천종목 > 추천종목별 수익률
            NUM	1
            TOTROW	102
            CMP_NM_KOR	DB하이텍
            CMP_CD	000990
            IN_DT	2018/05/16
            CNT	35
            BRK_NM_KOR	유안타증권
            BRK_CD	12
            PF_NM_KOR	대형주
            PF_CD	111
            ACCU_RTN	26.88524590164
            W_RTN	2.9255
            MN_RTN	28.1456
            MN3_RTN	null
            JAN1_RTN	null
            REASON_IN	▶ 삼성전자 TV 신제품 효과로 제품 Mix 개선 본격화 ▶ 2분기 중후반부터 가파른 가동률/실적 상승 전망
            ANL_DT	2018/05/15
            IN_DIFF_REASON	장 종료후 추천으로 추천일자 순연

            // 네이버 > 추천종목 > 종목별추천건수 상위
            "NUM":1,
            "TOTROW":92,
            "RECOMAND_CNT":2,
            "CMP_NM_KOR":"녹십자랩셀",
            "CMP_CD":"144510",
            "FIRST_IN_DT":"2018/06/18",
            "LAST_IN_DT":"2018/06/18",
            "TO_ADJ_PRICE":47550,
            "AVG_DT":2,
            "W_RTN":-12.10730000000000,
            "MN_RTN":-10.28310000000000,
            "MN3_RTN":-23.18260000000000,
            "JAN1_RTN":-4.90000000000000

            // 네이버 > 추천종목 > 현재 추천종목
            NUM	1
            TOTROW	105
            CMP_NM_KOR	대웅제약
            CMP_CD	069620
            BRK_NM_KOR	유안타증권
            BRK_CD	12
            PF_NM_KOR	대형주
            PF_CD	111
            IN_DT	2018/06/21
            TERM_CNT	0
            IN_DT_PRICE	189000
            RECOMM_PRICE	null
            PRE_ADJ_CLOSE_PRC	189000
            REASON_IN	▶ 골관절염치료제(아셀렉스), 유방암치료제(샴페넷) 등 다양한 제품 라인업으로 사업 확장성 기대 ▶ 가장 우려되었던 부분인 나보타공장 cGMP 에 대한 이슈 해결 ▶ 올해 하반기 또는 내년 상반기 나보타의 유럽 EMA 및 미국 FDA 승인 기대
            ACCU_RTN	null
            PRE_DT	2018/06/20
            FILE_NM	01211120180621_069620.pdf
            ANL_DT	2018/06/21
            IN_DIFF_REASON	null
            */

            //int lowestPrice = BaseApplication.getInstance().getIntSetting(Config.SETTING_RECO_LOWEST_PRICE); // 최저가
            //int highestPrice = BaseApplication.getInstance().getIntSetting(Config.SETTING_RECO_HIGHEST_PRICE); // 최고가
            //float rateOfFluctuation = BaseApplication.getInstance().getFloatSetting(Config.SETTING_RECO_RATE_OF_FLUCTUATION); // 등락률
            //Log.e(TAG, lowestPrice + " ~ " + highestPrice + ", " + rateOfFluctuation);

            DateFormat dateInFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            DateFormat dateOutFormat = new SimpleDateFormat(Config.DATE_FORMAT, Locale.getDefault());

            Calendar cal = Calendar.getInstance();
            Date d = cal.getTime();
            long curTime = d.getTime();

            long id = 1;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                //Log.e(TAG, "object: " + object.toString());

                String code = Util.getString(object, "CMP_CD"); // 종목 코드
                boolean exist = false;
                for (Item item : items) {
                    if (item.getCode().equals(code)) {
                        exist = true;
                        break;
                    }
                }
                if (exist) {
                    continue;
                }

                //int num = Util.getInt(object, "NUM");
                String name = Util.getString(object, "CMP_NM_KOR");     // 종목 이름
                String broker = Util.getString(object, "BRK_NM_KOR");   // 종목 코드
                String portfolio = Util.getString(object, "PF_NM_KOR"); // 종목 코드
                String listed = Util.getString(object, "IN_DT");        // 예측일
                int elapsed = Util.getInt(object, "CNT");               // 경과일
                //float ror = BigDecimal.valueOf(Util.getDouble(object, "ACCU_RTN")).floatValue(); // 추천일 후 수익률
                float ror = BigDecimal.valueOf(Util.getDouble(object, "MN_RTN")).floatValue(); // 1개월 수익률
                //float ror = BigDecimal.valueOf(Util.getDouble(object, "W_RTN")).floatValue(); // 1주일 수익률
                //Log.e(TAG, name + " " + ror);

                String reason = Util.getString(object, "REASON_IN");    // 추천 사유
                reason = reason.replace("▶ ", "- ");
                reason = reason.replaceAll("\n+$", "");
                //Log.e(TAG, reason);

                int nor = 1;
                int tpr = 0;
                if (siteId.equals(Config.KEY_RECOMMEND_TOP)) {
                    nor = Util.getInt(object, "RECOMAND_CNT");     // 추천수
                    listed = Util.getString(object, "LAST_IN_DT"); // 최근 추천일
                    tpr = Util.getInt(object, "TO_ADJ_PRICE");     // 목표가

                    try {
                        Date date = dateInFormat.parse(listed);
                        listed = dateOutFormat.format(date);
                        long oldTime = date.getTime();
                        long diffTime = curTime - oldTime;
                        elapsed = (int) ((((diffTime / 1000) / 60) / 60) / 24); // 경과일
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (siteId.equals(Config.KEY_RECOMMEND_CURRENT)) {
                    listed = Util.getString(object, "IN_DT");       // 추천일
                    tpr = Util.getInt(object, "PRE_ADJ_CLOSE_PRC"); // 목표가
                    elapsed = Util.getInt(object, "TERM_CNT");      // 경과일
                }

                //Log.e(TAG, name + " " + nor);

                try {
                    Date date = dateInFormat.parse(listed);
                    listed = dateOutFormat.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //int price = 0; // 현재가
                //float rof = 0; // 등락률
                //String tagIds = ""; // 태그
                //for (Item bi : BaseApplication.getInstance().getmBaseItems()) {
                //    if (code.equals(bi.getCode())) {
                //        price = bi.getPrice();
                //        rof = bi.getRof();
                //        tagIds = bi.getTagIds();
                //    }
                //}

                //if (siteId.equals(Config.KEY_RECO_RETURN)) {
                //    Log.e(TAG, name);
                //}

                /*
                if (applyFilter) {
                    if (lowestPrice > 0 && price < lowestPrice) { // 최저가
                        //Log.e(TAG, "price: " + price + ", lowestPrice: " + lowestPrice);
                        continue;
                    }

                    if (highestPrice > 0 && price > highestPrice) { // 최고가
                        //Log.e(TAG, "price: " + price + ", highestPrice: " + highestPrice);
                        continue;
                    }

                    if (rateOfFluctuation > 0 && rof < rateOfFluctuation) { // 등락률
                        //Log.e(TAG, "rof: " + rof + ", rateOfFluctuation: " + rateOfFluctuation);
                        continue;
                    }
                }
                */

                Item item = new Item();
                item.setId(id);
                item.setCode(code);
                item.setName(name);
                item.setBroker(broker);
                item.setPortfolio(portfolio);
                //item.setPrice(price);
                //item.setRof(rof);
                item.setListed(listed);
                item.setElapsed(elapsed);
                item.setNor(nor);
                item.setTpr(tpr);
                item.setRor(ror);
                item.setReason(reason);
                //item.setTagIds(tagIds);

                items.add(item);
                id++;
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void parseRecommendReasonList(String response, ArrayList<Reason> reasons) {
        if (response == null || response.isEmpty()) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            //Log.e(TAG, "jsonArray.length() = " + jsonArray.length());

            /*
            NUM	1
            TOTROW	6
            CMP_NM_KOR	삼성전기
            CMP_CD	009150
            BRK_NM_KOR	KB증권
            BRK_CD	13
            PF_NM_KOR	절대수익형 추천종목
            PF_CD	126
            IN_DT	2018/06/26
            TERM_CNT	9
            IN_DT_PRICE	148000
            RECOMM_PRICE	null
            PRE_ADJ_CLOSE_PRC	142500
            REASON_IN	▶ 글로벌 MLCC 공급부족은 2020년까지 불가피할 전망 ▶ 전장용 MLCC 시장 확대로 향후 구조적 실적개선 추세 기대
            ACCU_RTN	null
            PRE_DT	2018/07/04
            FILE_NM	01312620180626_009150.pdf
            ANL_DT	2018/06/26
            IN_DIFF_REASON	null
            */

            long id = 1;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                //Log.e(TAG, "object: " + object.toString());

                String broker = Util.getString(object, "BRK_NM_KOR");
                String published = Util.getString(object, "IN_DT");
                String portfolio = Util.getString(object, "PF_NM_KOR");
                String content = Util.getString(object, "REASON_IN");

                published = published.replace("/", "-");

                content = content.replace("▶ ", "- ");
                content = content.replaceAll("\n+$", "");

                //Log.e(TAG, "broker: " + broker);

                Reason reason = new Reason();
                reason.setId(id);
                reason.setBroker(broker);
                reason.setPortfolio(portfolio);
                reason.setPublished(published);
                reason.setContent(content);

                reasons.add(reason);
                id++;
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 네이버 와이즈 리포트 추천 목록 파싱하기
     *
     * @param response
     * @param items
     */
    public void parseWise(String response, ArrayList<Item> items) {
        //Log.e(TAG, "> response:\n" + response);

        try {
            JSONObject json = new JSONObject(response);
            JSONObject result = json.getJSONObject("result");
            JSONArray jsonArray = result.getJSONArray("itemList");
            /*
            {
                "cd":"009470",
                "nm":"삼화전기",
                "nv":45300,
                "cv":800,
                "cr":1.8,
                "rf":"2",
                "1wer":15.89,
                "accer":21.92,
                "dt":"20180702",
                "seq":1
            }
            */
            long id = 1;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String code = Util.getString(obj, "cd");
                String name = Util.getString(obj, "nm");
                int price = Util.getInt(obj, "nv");
                int pof = Util.getInt(obj, "cv");
                float rof = BigDecimal.valueOf(Util.getDouble(obj, "cr")).floatValue();

                //Log.e(TAG, name);

                Item item = new Item();
                item.setId(id);
                item.setCode(code);
                item.setName(name);
                item.setPrice(price);
                item.setPof(pof);
                item.setRof(rof);

                items.add(item);
                id++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "> ERROR: parseWise()\n" + e.getMessage());
        }
    }
}
