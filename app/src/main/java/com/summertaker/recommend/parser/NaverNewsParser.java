package com.summertaker.recommend.parser;

import android.util.Log;

import com.summertaker.recommend.common.BaseApplication;
import com.summertaker.recommend.common.BaseParser;
import com.summertaker.recommend.common.Config;
import com.summertaker.recommend.data.News;
import com.summertaker.recommend.data.Word;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NaverNewsParser extends BaseParser {

    /**
     * 특징주 목록 파싱하기
     */
    public void parseFeatureList(String response, ArrayList<News> list) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);

        Element table = null;
        Elements divs = doc.select(".boardList2");
        if (divs != null) {
            Elements tables = divs.first().select("table");
            if (tables != null) {
                table = tables.first();
            }
        }

        if (table == null) {
            return;
        }

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        Date curDate = cal.getTime();
        int year = cal.get(Calendar.YEAR);
        String century = (year + "").substring(0, 2);

        long id = 1;
        for (Element tr : table.select("tr")) {
            Elements tds = tr.select("td");
            if (tds.size() != 3) {
                continue;
            }

            String title;
            String url;
            Date published = null;
            String publishedText;
            int elapsed = 0;

            Element el;

            el = tds.get(0).select("a").first();
            title = el.text();

            if (isInExcludeList(Config.KEY_WORD_CATEGORY_NAVER_EXCLUDE, title)) { // 제외 단어
                continue;
            }

            title = highlightText(Config.KEY_WORD_CATEGORY_NAVER_INCLUDE, title); // 단어 강조

            url = el.attr("href");
            url = "https://finance.naver.com" + url;

            publishedText = tds.get(1).text().replace(".", "-");
            publishedText = century + publishedText + ":00";

            try {
                published = sdf.parse(publishedText);
                long diff = curDate.getTime() - published.getTime();
                elapsed = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                //Log.e(TAG, "elapsed: " + days);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Log.e(TAG, title + " " + published + " " + elapsed);

            News news = new News();
            news.setId(id);
            news.setTitle(title);
            news.setUrl(url);
            news.setPublished(published);
            news.setPublishedText(publishedText);
            news.setElapsed(elapsed);

            list.add(news);

            id++;
        }
    }

    /**
     * 주요 뉴스 목록 파싱하기
     */
    public void parseMainList(String response, ArrayList<News> list) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        Date curDate = cal.getTime();
        //int year = cal.get(Calendar.YEAR);

        Element ul = doc.select(".newsList").first();

        long id = 1;
        for (Element li : ul.select("li")) {
            String title;
            String url;
            Date published = null;
            String publishedText;
            int elapsed = 0;

            Element dd = li.select(".articleSubject").first();
            Element a = dd.select("a").first();
            title = a.text();

            if (isInExcludeList(Config.KEY_WORD_CATEGORY_NAVER_EXCLUDE, title)) { // 제외 단어
                continue;
            }

            title = highlightText(Config.KEY_WORD_CATEGORY_NAVER_INCLUDE, title); // 단어 강조

            url = a.attr("href");
            url = "https://finance.naver.com" + url;

            publishedText = li.select(".wdate").first().text();

            try {
                published = sdf.parse(publishedText);
                long diff = curDate.getTime() - published.getTime();
                elapsed = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                //Log.e(TAG, "elapsed: " + days);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Log.e(TAG, title + " " + published + " " + elapsed);

            News news = new News();
            news.setId(id);
            news.setTitle(title);
            news.setUrl(url);
            news.setPublished(published);
            news.setPublishedText(publishedText);
            news.setElapsed(elapsed);

            list.add(news);

            id++;
        }
    }

    /**
     * 많이 본 뉴스 목록 파싱하기
     */
    public void parseRankingList(String response, ArrayList<News> list) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        Date curDate = cal.getTime();
        //int year = cal.get(Calendar.YEAR);

        long id = 1;
        Elements uls = doc.select(".simpleNewsList");
        for (Element ul : uls) {
            for (Element li : ul.select("li")) {
                String title;
                String url;
                Date published = null;
                String publishedText;
                int elapsed = 0;

                Element a = li.select("a").first();
                title = a.text();

                if (isInExcludeList(Config.KEY_WORD_CATEGORY_NAVER_EXCLUDE, title)) { // 제외 단어
                    continue;
                }

                title = highlightText(Config.KEY_WORD_CATEGORY_NAVER_INCLUDE, title); // 단어 강조

                url = a.attr("href");
                url = "https://finance.naver.com" + url;

                publishedText = li.select(".wdate").first().text();

                try {
                    published = sdf.parse(publishedText);
                    long diff = curDate.getTime() - published.getTime();
                    elapsed = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                    //Log.e(TAG, "elapsed: " + days);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //Log.e(TAG, title + " " + published + " " + elapsed);

                News news = new News();
                news.setId(id);
                news.setTitle(title);
                news.setUrl(url);
                news.setPublished(published);
                news.setPublishedText(publishedText);
                news.setElapsed(elapsed);

                list.add(news);

                id++;
            }
        }
    }

    /**
     * 속보 목록 파싱하기
     */
    public void parseBreakingList(String response, ArrayList<News> list) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        Date curDate = cal.getTime();
        //int year = cal.get(Calendar.YEAR);

        Element ul = doc.select(".realtimeNewsList").first();

        long id = 1;
        for (Element li : ul.select("li")) {
            for (Element el : li.select(".articleSubject")) {
                String title;
                String url;

                title = el.text();

                url = el.attr("href");
                url = "https://finance.naver.com" + url;

                //Log.e(TAG, id + ". " + title); // + " " + url);

                News news = new News();
                news.setId(id);
                news.setTitle(title);
                news.setUrl(url);
                list.add(news);

                id++;
            }
        }

        int count = 0;
        for (Element li : ul.select("li")) {
            for (Element el : li.select(".wdate")) {
                Date published = null;
                String publishedText;
                int elapsed = 0;

                publishedText = el.text();

                try {
                    published = sdf.parse(publishedText);
                    long diff = curDate.getTime() - published.getTime();
                    elapsed = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                    //Log.e(TAG, "elapsed: " + days);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //Log.e(TAG, i + ". " + published + " " + elapsed);

                list.get(count).setPublished(published);
                list.get(count).setPublishedText(publishedText);
                list.get(count).setElapsed(elapsed);

                count++;
            }
        }

        for (int i = 0; i < list.size(); i++) {
            String title = list.get(i).getTitle();
            if (isInExcludeList(Config.KEY_WORD_CATEGORY_NAVER_EXCLUDE, title)) { // 제외 단어
                list.remove(i);
            }
        }

        for (int i = 0; i < list.size(); i++) {
            String title = list.get(i).getTitle();
            title = highlightText(Config.KEY_WORD_CATEGORY_NAVER_INCLUDE, title); // 단어 강조
            list.get(i).setTitle(title);
        }
    }

    /**
     * 뉴스 상세 페이지 내용 파싱하기
     */
    public News parseDetail(String response) {
        //Log.e(TAG, "> response\n" + response);

        News news = new News();
        news.setContent("");

        if (response == null || response.isEmpty()) {
            Log.e(TAG, "> response is null.");
            return news;
        }

        Document doc = Jsoup.parse(response);

        // 내용
        Elements els = doc.select("#content");
        if (els.size() == 0) {
            Log.e(TAG, "> #content is null.");
            return news;
        }

        Element el = els.first();

        // 불필요한 태그 제거
        el.select("table").remove();
        el.select("div").remove();
        el.select("a").remove();
        el.select("img").remove();

        String html = el.html();

        // 광고 단어 이후 내용 제거
        for (Word word : BaseApplication.getInstance().getWords()) {
            if (word.getCategory().equals(Config.KEY_WORD_CATEGORY_NAVER_AD)) {
                String value = word.getValue();
                value = value.replaceAll("\\(", "\\\\(");
                value = value.replaceAll("\\)", "\\\\)");
                html = html.split(value)[0];
            }
        }

        // 불필요한 단어 제거
        html = html.replaceAll("\\[.*\\]", "");
        html = html.replaceAll("＜.*＞", "");

        // 줄 단위 정리
        String[] rows = html.split("<br>");
        ArrayList<String> list = new ArrayList<>();
        for (String row : rows) {
            String text = row.replaceAll("<[^>]*>", "").trim();
            text = text.replaceAll("&gt;", "");
            text = text.replaceAll("&lt;", "");

            if (text.isEmpty()) continue;
            //Log.e(TAG, text);

            list.add(text);
        }

        StringBuilder sb = new StringBuilder();
        int count = 1;
        for (String text : list) {
            if (count < list.size()) {
                text = "<p>" + text + "</p>";
            }
            //Log.e(TAG, text);

            sb.append(text);
            count++;
        }
        String content = sb.toString();

        /*
        html = html.replaceAll("\\(\\d+\\)", "");
        html = html.replaceAll("^[<br>\\n*\\s*]+", "");
        html = html.replaceAll("<br>\\n*<br>\\n*[<br>\\n*]+", "<br>");
        html = html.replaceAll("[<br>\\n*\\s*]+$", "");
        html = html.replaceAll("!--.+-->", "");
        html = html.trim();
        //s = s.replaceAll("[\r\n]+", "\n");
        */

        // 단어 강조
        content = highlightText(Config.KEY_WORD_CATEGORY_NAVER_INCLUDE, content);

        // 종목 이름 강조
        content = highlightItemName(content);

        news.setContent(content);

        return news;
    }
}
