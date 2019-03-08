package com.summertaker.recommend.parser;

import com.summertaker.recommend.common.BaseApplication;
import com.summertaker.recommend.common.BaseParser;
import com.summertaker.recommend.common.Config;
import com.summertaker.recommend.data.Item;
import com.summertaker.recommend.data.News;
import com.summertaker.recommend.data.Word;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DaumNewsParser extends BaseParser {

    public void parseList(Item item, String response, ArrayList<News> dataList) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);

        Element root = doc.getElementById("itemNewsList");
        if (root == null) {
            return;
        }

        Element ul = root.getElementsByTag("ul").first();
        if (ul == null) {
            return;
        }

        DateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        Date curDate = cal.getTime();

        int year = cal.get(Calendar.YEAR);
        String century = (year + "").substring(0, 2);

        long id = 1;

        for (Element li : ul.select("li")) {
            String title;
            String url;
            String summary;
            Date published = null;
            String publishedText;
            int elapsed = 0;

            Element el;

            el = li.select("strong > a").first();
            title = cleanText(el.text());

            //if (title.contains("[fnRASSI]")) continue;
            //if (title.contains("코스피")) continue;
            //if (title.contains("코스닥")) continue;
            //if (title.contains("상담")) continue;

            if (isInExcludeList(Config.KEY_WORD_CATEGORY_DAUM_EXCLUDE, title)) { // 제외 단어
                continue;
            }

            title = highlightText(Config.KEY_WORD_CATEGORY_DAUM_INCLUDE, title); // 단어 강조

            url = el.attr("href");
            url = "http://m.finance.daum.net/" + url;

            el = li.select("span.datetime").first();
            publishedText = el.text();
            publishedText = century + publishedText; //.replace(".", "-");

            try {
                published = sdf.parse(publishedText);
                long diff = curDate.getTime() - published.getTime();
                elapsed = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                //Log.e(TAG, "elapsed: " + days);

            } catch (ParseException e) {
                e.printStackTrace();
            }
            //published = published + ":00";

            el = li.select("p > a").first();
            summary = cleanText(el.text());

            // 제목과 내용에 종목이름이 없으면 제외
            if (item.getName() != null && !item.getName().isEmpty()) {
                if (!title.contains(item.getName()) && !summary.contains(item.getName())) {
                    continue;
                }
            }

            //Log.e(TAG, title + " " + published + " " + url);

            News news = new News();
            news.setId(id);
            news.setTitle(title);
            news.setUrl(url);
            news.setSummary(summary);
            news.setPublished(published);
            news.setPublishedText(publishedText);
            news.setElapsed(elapsed);

            dataList.add(news);

            id++;
        }
    }

    public void parseDetail(String response, News news) {
        if (response == null || response.isEmpty()) {
            return;
        }

        Document doc = Jsoup.parse(response);

        // 내용
        Element con = doc.getElementById("dmcfContents");
        if (con == null) return;

        Element sec = con.getElementsByTag("section").get(0);
        if (sec == null) return;

        // 불필요한 태그 제거
        sec.select("figure").remove();
        sec.select("img").remove();

        // 불필요한 단어 제거
        String html = sec.html();
        html = html.replaceAll("【.*】", "");
        html = html.replaceAll("\\[.*\\]", "");
        html = html.replaceAll("^.+\">", "");

        // 광고 단어 이후 내용 제거
        for (Word word : BaseApplication.getInstance().getWords()) {
            if (word.getCategory().equals(Config.KEY_WORD_CATEGORY_DAUM_AD)) {
                String value = word.getValue();
                value = value.replaceAll("\\(", "\\\\(");
                value = value.replaceAll("\\)", "\\\\)");
                html = html.split(value)[0];
            }
        }

        // 줄 단위 정리
        String[] rows = html.split("</p>");
        ArrayList<String> list = new ArrayList<>();
        for (String row : rows) {
            String text = row.replaceAll("<[^>]*>", "").trim();

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
            //content += text;
            sb.append(text);
            count++;
        }

        String content = sb.toString();
        content = cleanText(content);

        // 단어 강조
        content = highlightText(Config.KEY_WORD_CATEGORY_DAUM_INCLUDE, content);

        // 종목 이름 강조
        content = highlightItemName(content);

        news.setContent(content);
    }

    private String cleanText(String text) {
        String s = text;
        s = s.replaceAll("\\(\\d+\\)", "");

        return s;
    }
}

