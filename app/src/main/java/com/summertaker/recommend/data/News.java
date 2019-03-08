package com.summertaker.recommend.data;

import android.support.annotation.NonNull;

import java.util.Comparator;
import java.util.Date;

public class News implements Comparable<News> {
    private long id;
    private String title;
    private String url;
    private String summary;
    private String content;
    private Date published;
    private String publishedText;
    private int elapsed;
    private String elapsedText;

    @Override
    public int compareTo(@NonNull News o) {
        return 0;
    }

    public static Comparator<News> compareToPublished = new Comparator<News>() {

        public int compare(News d1, News d2) {

            Date v1 = d1.getPublished();
            Date v2 = d2.getPublished();

            //return value1 - value2; //ascending order
            return (int) v2.getTime() - (int) v1.getTime(); //descending order
        }

    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    public String getPublishedText() {
        return publishedText;
    }

    public void setPublishedText(String publishedText) {
        this.publishedText = publishedText;
    }

    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }

    public String getElapsedText() {
        return elapsedText;
    }

    public void setElapsedText(String elapsedText) {
        this.elapsedText = elapsedText;
    }
}
