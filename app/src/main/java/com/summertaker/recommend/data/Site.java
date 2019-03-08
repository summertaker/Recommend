package com.summertaker.recommend.data;

public class Site {
    private String id;
    private String groupId;
    private String title;
    private String url;
    private String trader;
    private String buySell;

    public Site(String id, String title, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
    }

    public Site(String id, String groupId, String title, String url) {
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public String getTrader() {
        return trader;
    }

    public void setTrader(String trader) {
        this.trader = trader;
    }

    public String getBuySell() {
        return buySell;
    }

    public void setBuySell(String buySell) {
        this.buySell = buySell;
    }
}
