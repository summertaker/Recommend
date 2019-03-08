package com.summertaker.recommend.common;

import com.summertaker.recommend.data.Item;
import com.summertaker.recommend.data.Word;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseParser {

    protected String TAG;

    public BaseParser() {
        TAG = this.getClass().getSimpleName();
    }

    protected String getCodeFromUrl(String url) {
        String code  ="";
        Pattern pattern = Pattern.compile("code=(\\d+)");
        Matcher matcher = pattern.matcher(url);
        while(matcher.find()) {
            code = matcher.group(1);
        }
        return code;
    }
    /**
     * 글자 제외
     * @param text
     * @return
     */
    protected boolean isInExcludeList(String category, String text) {
        boolean valid = false;

        String compare = text.replaceAll("\\s+", "").trim().toLowerCase();

        for (Word word : BaseApplication.getInstance().getWords()) {
            if (!word.getCategory().equals(category)) {
                continue;
            }

            String value = word.getValue().replaceAll("\\s+", "").trim().toLowerCase();
            if (compare.contains(value)) {
                valid = true;
                break;
            }
        }

        return valid;
    }

    /**
     * 글자 강조
     * @param category
     * @param text
     * @return
     */
    protected String highlightText(String category, String text) {
        String s = text;

        for (Word word : BaseApplication.getInstance().getWords()) {
            if (!word.getCategory().equals(category)) {
                continue;
            }

            s = s.replace(word.getValue(), String.format(Config.NEWS_TEXT_HIGHLIGHT_FORMAT, word.getValue()));
        }

        return s;
    }

    /**
     * 종목 이름 강조
     * @param content
     * @return
     */
    protected String highlightItemName(String content) {
        String text = content;

        ArrayList<String> foundList = new ArrayList<>();

        for (Item item : BaseApplication.getInstance().getItemPrices()) {
            String name = item.getName();
            if (text.contains(name)) {
                boolean valid = true;
                for (String found : foundList) {
                    if (found.contains(name)) {
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    foundList.add(name);
                    text = text.replace(name, String.format(Config.NEWS_ITEM_NAME_HYPERLINK_FORMAT, item.getCode(), name));
                }
            }
        }

        return text;
    }
}
