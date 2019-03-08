package com.summertaker.recommend.parser;

import android.util.Log;

import com.summertaker.recommend.common.BaseParser;
import com.summertaker.recommend.data.Item;
import com.summertaker.recommend.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.ArrayList;

public class DaumParser extends BaseParser {

    /**
     * [다음 금융 > 국내] 페이지 파싱하기
     *
     * @param response
     * @param weekRiseItems
     * @param weekTradeItems
     */
    public void parseDomestic(String response, ArrayList<Item> weekRiseItems, ArrayList<Item> weekTradeItems) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);

        Element table;

        table = doc.select("#trendBody1").first(); // 주간 마켓 트렌드 > 상승률 상위
        parseDomesticWeekRiseList(weekRiseItems, table);

        table = doc.select("#trendBody3").first(); // 주간 마켓 트렌드 > 외국인 순매수
        parseDomesticWeekTradeList(weekTradeItems, true, false, table);

        table = doc.select("#trendBody4").first(); // 주간 마켓 트렌드 > 기관 순매수
        parseDomesticWeekTradeList(weekTradeItems, false, true, table);
    }

    private void parseDomesticWeekRiseList(ArrayList<Item> items, Element table) {
        Item item;
        for (Element tr : table.select("tr")) {
            Elements tds = tr.select("td");
            if (tds.size() != 10) {
                continue;
            }

            item = parseDomesticWeekRiseRow(tds.get(0), tds.get(1), tds.get(2), tds.get(3), tds.get(4));
            items.add(item);

            item = parseDomesticWeekRiseRow(tds.get(5), tds.get(6), tds.get(7), tds.get(8), tds.get(9));
            items.add(item);
        }
    }

    private Item parseDomesticWeekRiseRow(Element td1, Element td2, Element td3, Element td4, Element td5) {
        /*
        <tr class="" onmouseout="highlight(this,false)" onmouseover="highlight(this,true)">
        <td class="txt2"><a href="/item/main.daum?code=003720&nil_profile=stockprice&nil_menu=sisetrendupp1">삼영화학</a></td>
        <td class="num">1,130</td>
        <td class="num"><span class="stFt"><em>-</em>0</span></td>
        <td class="num"><span class="cFt">0.00%</span></td>
        <td class="num2 dtRbdGray"><span class="cUp">+58.71%</span></td>
        <td class="txt2"><a href="/item/main.daum?code=023440&&nil_profile=stockprice&nil_menu=sisetrendupq1">제일제강</a></td>
        <td class="num">3,960</td>
        <td class="num"><span class="stUp"><em>▲</em>165</span></td>
        <td class="num"><span class="cUp">+4.35%</span></td>
        <td class="num2"><span class="cUp">+62.96%</span></td>
        </tr>
        */

        String name;
        String code;
        int price;
        int pof;
        Float rof;
        Float rrw;

        Element el = td1.getElementsByTag("a").get(0);
        String href = el.attr("href");
        code = getCodeFromUrl(href);

        name = el.text();

        String temp;

        temp = td2.text();
        temp = temp.replace(",", "");
        price = Integer.parseInt(temp.trim()); // 현재가

        temp = td3.text();
        temp = temp.replace(",", "");
        temp = temp.replace("▲", "");
        temp = temp.replace("▼", "");
        temp = temp.replaceAll("\\s", "").trim();
        pof = Integer.parseInt(temp); // 전일비

        temp = td4.text();
        temp = temp.replace(",", "");
        temp = temp.replace("%", "");
        temp = temp.replaceAll("\\s", "").trim();
        rof = Float.valueOf(temp); // 등락률

        temp = td5.text();
        temp = temp.replace(",", "");
        temp = temp.replace("%", "");
        temp = temp.replaceAll("\\s", "").trim();
        rrw = Float.valueOf(temp); // 주간 상승률

        Item item = new Item();
        item.setCode(code);
        item.setName(name);
        item.setPrice(price);
        item.setPof(pof);
        item.setRof(rof);
        item.setRrw(rrw);

        //Log.e(TAG, name + " " + price + " / " + pof + " / " + rof + " / " + rrw);

        return item;
    }

    private void parseDomesticWeekTradeList(ArrayList<Item> items, boolean foreigner, boolean institution, Element table) {
        Item item;
        for (Element tr : table.select("tr")) {
            Elements tds = tr.select("td");
            if (tds.size() != 8) {
                continue;
            }

            item = parseDomesticWeekTradeRow(tds.get(0), tds.get(1), tds.get(2), tds.get(3));
            item.setForeigner(foreigner);
            item.setInstitution(institution);
            items.add(item);

            item = parseDomesticWeekTradeRow(tds.get(4), tds.get(5), tds.get(6), tds.get(7));
            item.setForeigner(foreigner);
            item.setInstitution(institution);
            items.add(item);
        }
    }

    private Item parseDomesticWeekTradeRow(Element td1, Element td2, Element td3, Element td4) {
        /*
        <tr class="" onmouseout="highlight(this,false)" onmouseover="highlight(this,true)">
        <td class="txt2"><a href="/item/main.daum?code=000660&nil_profile=stockprice&nil_menu=sisetrendforebuyp1">SK하이닉스</a></td>
        <td class="num">120,918</td>
        <td class="num">1,156</td>
        <td class="num2 dtRbdGray"><span class="cUp">+3.74%</span></td>
        <td class="txt2"><a href="/item/main.daum?code=263750&nil_profile=stockprice&nil_menu=sisetrendforebuyq1">펄어비스</a></td>
        <td class="num">19,613</td>
        <td class="num">77</td>
        <td class="num2"><span class="cUp">+0.30%</span></td>
        </tr>
        */

        String name;
        String code;
        int pot;
        int vot;
        Float rof;

        Element el = td1.getElementsByTag("a").get(0);
        String href = el.attr("href");
        code = getCodeFromUrl(href);

        name = el.text();

        String temp;

        temp = td2.text();
        temp = temp.replace(",", "");
        pot = Integer.parseInt(temp); // 거래 금액 (Price of Trade

        temp = td3.text();
        temp = temp.replace(",", "");
        vot = Integer.parseInt(temp); // 거래량 (Volume of Trade)
        vot = vot * 1000;

        temp = td4.text();
        temp = temp.replace(",", "");
        temp = temp.replace("%", "");
        rof = Float.valueOf(temp); // 등락률 (Rate of Fluctuation)

        Item item = new Item();
        item.setCode(code);
        item.setName(name);
        item.setPot(pot);
        item.setVot(vot);
        item.setRof(rof);

        //Log.e(TAG, name + " / " + pot + " / " + vot + " / " + rof);

        return item;
    }

    public void parsePriceListJson(String response, ArrayList<Item> items) {
        if (response == null || response.isEmpty()) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String code = Util.getString(obj, "symbolCode");
                code = code.substring(1, code.length());

                Item item = new Item();
                item.setId(i+1);
                item.setName(Util.getString(obj, "name"));
                item.setCode(code);
                item.setPrice(Util.getInt(obj, "tradePrice"));
                item.setPof(Util.getInt(obj, "changePrice"));
                item.setRof(BigDecimal.valueOf(Util.getDouble(obj, "changeRate")).floatValue()); // 등락률

                Log.e(TAG, item.getCode() + " " + item.getName() + " " + item.getPrice() + " / " + item.getRof());

                items.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void parsePriceList(String response, ArrayList<Item> items) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);

        Elements tables = doc.getElementsByTag("table");
        for (Element table : tables) {

            String className = table.attr("class");
            if (!"gTable clr".equals(className)) {
                continue;
            }

            Elements trs = table.getElementsByTag("tr");
            for (Element tr : trs) {

                Elements tds = tr.getElementsByTag("td");
                if (tds.size() != 6) {
                    continue;
                }

                parsePriceRow(items, tds.get(0), tds.get(1), tds.get(2));
                parsePriceRow(items, tds.get(3), tds.get(4), tds.get(5));
            }
        }
    }

    private void parsePriceRow(ArrayList<Item> items, Element td1, Element td2, Element td3) {
        Item item = new Item();

        Element el = td1.getElementsByTag("a").get(0);
        String href = el.attr("href");
        String code = getCodeFromUrl(href);
        item.setCode(code);

        String name = el.text();
        item.setName(name);

        String temp;

        temp = td2.text();
        temp = temp.replace(",", "");
        int price = Integer.parseInt(temp);
        item.setPrice(price);

        temp = td3.text();
        temp = temp.replace(",", "");
        temp = temp.replace("%", "");
        float rof = Float.valueOf(temp);
        item.setRof(rof);

        Log.e(TAG, name + " " + price + " / " + rof);

        items.add(item);
    }

    public void parseTradeList(String url, String response, ArrayList<Item> items) {
        if (response == null || response.isEmpty()) {
            return;
        }

        boolean foreigner = url.contains("foreign.daum");
        boolean institution = url.contains("institution.daum");
        boolean overseas = url.contains("trcode=1");
        boolean domestic = url.contains("trcode=0");
        boolean trader = url.contains("trader.daum");

        Document doc = Jsoup.parse(response);

        int count = 1;
        Elements tables = doc.getElementsByTag("table");
        for (Element table : tables) {

            String className = table.attr("class");
            if (!"dTable clr".equals(className)) {
                continue;
            }

            boolean buy = (count == 1);
            boolean sell = (count > 1);

            Elements trs = table.getElementsByTag("tr");
            for (Element tr : trs) {

                Elements tds = tr.getElementsByTag("td");
                if (tds.size() != 4 && tds.size() != 5) {
                    continue;
                }

                Element td4 = trader ? tds.get(4) : tds.get(3);

                Item item = parseTradeRow(tds.get(0), tds.get(1), tds.get(2), td4);
                item.setForeigner(foreigner);
                item.setInstitution(institution);
                item.setOverseas(overseas);
                item.setDomestic(domestic);
                item.setBuy(buy);
                item.setSell(sell);

                if (foreigner || institution) {
                    // 외국인, 기관
                    item.setVot(item.getVot() * 1000);
                } else {
                    // 거래원별 > 증권사
                    item.setVot(item.getPot());
                    item.setPot(0);
                }

                items.add(item);
            }

            count++;

            break; // 왼쪽(매수) 테이블만 파싱
        }
    }

    private Item parseTradeRow(Element td1, Element td2, Element td3, Element td4) {
        Item item = new Item();

        Element el = td1.getElementsByTag("a").get(0);
        String href = el.attr("href");
        String code = getCodeFromUrl(href);
        item.setCode(code);

        String name = el.text();
        item.setName(name);
        //Log.e(TAG, "name: " + name);

        String temp;

        temp = td2.text();
        temp = temp.replace(",", "");
        int pot = Integer.parseInt(temp); // Price of Trade
        item.setPot(pot);

        temp = td3.text();
        temp = temp.replace(",", "");
        int vot = Integer.parseInt(temp); // Volume of Trade
        item.setVot(vot);

        temp = td4.text();
        temp = temp.replace(",", "");
        temp = temp.replace("%", "");
        float rof = Float.valueOf(temp);
        item.setRof(rof);

        return item;
    }

    public void parseAccuTradeList(String url, String response, ArrayList<Item> items) {
        if (response == null || response.isEmpty()) {
            return;
        }

        boolean foreigner = url.contains("&gubun=F");
        boolean institution = url.contains("&gubun=I");

        Document doc = Jsoup.parse(response);

        /*
        <tr class=''>
        <td class="subject"><a href="/item/main.daum?code=A000270" title="기아차">기아차</a></td>
        <td class="num"><span class="cUp">31,450</span></td>
        <td class="num"><span class="stUp"><em>▲</em>100</span></td>
        <td class="num"><span class="cUp">+0.32%</span></td>
        <td class="num"><span>597,169</span></td>
        <td class="num"><span class="cUp">1,859,066</span></td>
        <td class="num"><span>+0.46%</span></td>
        <td class="num"><span>11일</span></td>
        </tr>
         */
        Elements tables = doc.getElementsByTag("table");
        for (Element table : tables) {

            String className = table.attr("class");
            if (!"gTable clr subPrice".equals(className)) {
                continue;
            }

            Elements trs = table.getElementsByTag("tr");
            for (Element tr : trs) {
                Elements tds = tr.getElementsByTag("td");
                if (tds.size() != 8) {
                    continue;
                }

                String name;
                String code;
                int price;
                int pof;
                Float rof;
                int vot;
                int pot;

                Element el = tds.get(0).getElementsByTag("a").get(0);
                String href = el.attr("href");
                code = getCodeFromUrl(href);
                name = el.text();
                //Log.e(TAG, "name: " + name);

                String temp;

                temp = tds.get(1).text();
                temp = temp.replace(",", "");
                price = Integer.parseInt(temp); // 현재가

                temp = tds.get(2).text();
                temp = temp.replace(",", "");
                temp = temp.replace("▲", "");
                temp = temp.replace("▼", "");
                pof = Integer.parseInt(temp); // 전일비 (Price of Fluctuation)

                temp = tds.get(3).text();
                temp = temp.replace(",", "");
                temp = temp.replace("%", "");
                rof = Float.valueOf(temp); // 등락률 (Rate of Fluctuation)

                temp = tds.get(4).text();
                temp = temp.replace(",", "");
                vot = Integer.parseInt(temp); // 거래량 (Volume of Trade)

                temp = tds.get(5).text();
                temp = temp.replace(",", "");
                pot = Integer.parseInt(temp); // 거래 금액 (Price of Trade

                Item item = new Item();
                item.setCode(code);
                item.setName(name);
                item.setPrice(price);
                item.setPof(pof);
                item.setRof(rof);
                item.setVot(vot);
                item.setPot(pot);
                item.setForeigner(foreigner);
                item.setInstitution(institution);

                items.add(item);
            }
        }
    }

    public void parseTraderList(String response, ArrayList<String> urls) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);

        Element select = doc.getElementById("trcode");
        Elements options = select.getElementsByTag("option");

        String url;
        for (Element option : options) {
            String name = option.text();
            name = name.replace("=", "");
            String code = option.attr("value");

            //Log.e(TAG, code + " " + name);
            if (code.equals("0") || code.equals("1")) continue;

            url = "http://finance.daum.net/quote/trader.daum?trcode=" + code + "&stype=P&type=P"; // 코스피
            urls.add(url);

            url = "http://finance.daum.net/quote/trader.daum?trcode=" + code + "&stype=Q&type=P"; // 코스닥
            urls.add(url);
        }
    }

    public void parseTraderItemList(String response, ArrayList<Item> items) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);

        int column = 1;
        Elements tables = doc.getElementsByTag("table");
        for (Element table : tables) {
            String attr = table.attr("class");
            if (!attr.equals("dTable clr")) continue;

            boolean buy = (column == 1);
            boolean sell = (column == 2);

            Elements trs = table.getElementsByTag("tr");
            for (Element tr : trs) {
                Elements tds = tr.getElementsByTag("td");
                if (tds.size() != 5) continue;

                Element a = tds.get(0).getElementsByTag("a").get(0);
                String href = a.attr("href");
                String[] array = href.split("=");
                String code = array[1];
                String name = a.text();

                String temp = tds.get(4).text();
                temp = temp.replace(",", "");
                temp = temp.replace("%", "");
                temp = temp.replace("％", "");
                float rof = Float.valueOf(temp);

                //Log.e(TAG, name + " (" + buy + " / " + sell + ")");

                boolean found = false;
                for (Item it : items) {
                    if (it.getCode().equals(code)) {
                        if (buy) {
                            it.setBuyCount(it.getBuyCount() + 1);
                        } else if (sell) {
                            it.setSellCount(it.getSellCount() + 1);
                        }
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Item item = new Item();
                    item.setCode(code);
                    item.setName(name);
                    item.setRof(rof);
                    item.setBuy(buy);
                    item.setSell(sell);
                    item.setBuyCount(1);
                    item.setSellCount(1);
                    items.add(item);
                }
            }

            column++;
        }
    }

    public Item parseItemDetail(String response) {
        Item item = new Item();

        if (response == null || response.isEmpty()) {
            return item;
        }

        Document doc = Jsoup.parse(response);

        // 제목
        Elements h2s = doc.getElementsByTag("h2");
        for (Element h2 : h2s) {
            String attr = h2.attr("onclick");
            //Log.e(TAG, "attr: " + attr);

            if (attr.contains("GoPage(")) {
                String name = h2.text();
                item.setName(name);
                break;
            }
        }

        // 개요
        Elements overviews = doc.getElementsByClass("tooltip_overview hide");
        if (overviews != null && overviews.size() > 0) {
            Element overview = overviews.first();
            item.setOverview(overview.html());
        }

        // 상세
        Elements uls = doc.getElementsByTag("ul");
        for (Element ul : uls) {
            String attr = ul.attr("class");
            //Log.e(TAG, "attr: " + attr);

            if ("list_stockrate".equals(attr)) {

                int price;
                int pof;
                float rof;
                String temp;

                Elements lis = ul.getElementsByTag("li");

                temp = lis.get(0).text();
                temp = temp.replace(",", "");
                price = Integer.valueOf(temp);

                temp = lis.get(1).text();
                temp = temp.replace(",", "");
                pof = Integer.valueOf(temp);

                temp = lis.get(2).text();
                temp = temp.replace(",", "");
                temp = temp.replace("%", "");
                temp = temp.replace("％", "");
                rof = Float.valueOf(temp);

                //Log.e(TAG, price + " " + pof + " " + rof);

                item.setPrice(price);
                item.setPof(pof);
                item.setRof(rof);
            }
        }

        return item;
    }
}
