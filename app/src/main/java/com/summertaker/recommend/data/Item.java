package com.summertaker.recommend.data;

public class Item {
    private long id;
    private String code;
    private String name;
    private String overview;
    private String broker;
    private String portfolio;
    private int price;
    private int pof;        // 전일비 Price of Fluctuation
    private float rof;      // 등락률 Rate of Fluctuation
    private float rrw;      // 주간 상승률 Rise Rate for Week
    private float per;      // Price Earning Ratio
    private float roe;      // Return of Equity
    private String listed;
    private int elapsed;
    private int tpr;        // Target Price
    private float ror;      // Rate of Return
    private int nor;        // Number of Recommendation
    private String reason;
    private int pot;        // Price of Trade
    private int vot;        // Volume of Trade
    private String tagIds;
    private int point;      // Grade Point
    private int buyCount = 0; // 거래원 매수 중복수
    private int sellCount = 0; // 거래원 매도 중복수
    private int charCount = 0;
    private int buyVolume;
    private boolean favorite;
    private String chartUrl;

    private boolean chartMode;
    private boolean listMode;
    private boolean rise;
    private boolean foreigner;
    private boolean institution;
    private boolean overseas;
    private boolean domestic;
    private boolean buy;
    private boolean sell;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(String portfolio) {
        this.portfolio = portfolio;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getPof() {
        return pof;
    }

    public void setPof(int pof) {
        this.pof = pof;
    }

    public float getRof() {
        return rof;
    }

    public void setRof(float rof) {
        this.rof = rof;
    }

    public float getRrw() {
        return rrw;
    }

    public void setRrw(float rrw) {
        this.rrw = rrw;
    }

    public float getPer() {
        return per;
    }

    public void setPer(float per) {
        this.per = per;
    }

    public float getRoe() {
        return roe;
    }

    public void setRoe(float roe) {
        this.roe = roe;
    }

    public String getListed() {
        return listed;
    }

    public void setListed(String listed) {
        this.listed = listed;
    }

    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }

    public int getTpr() {
        return tpr;
    }

    public void setTpr(int tpr) {
        this.tpr = tpr;
    }

    public float getRor() {
        return ror;
    }

    public void setRor(float ror) {
        this.ror = ror;
    }

    public int getNor() {
        return nor;
    }

    public void setNor(int nor) {
        this.nor = nor;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getPot() {
        return pot;
    }

    public void setPot(int pot) {
        this.pot = pot;
    }

    public int getVot() {
        return vot;
    }

    public void setVot(int vot) {
        this.vot = vot;
    }

    public String getTagIds() {
        return tagIds;
    }

    public void setTagIds(String tagIds) {
        this.tagIds = tagIds;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getCharCount() {
        return charCount;
    }

    public void setCharCount(int charCount) {
        this.charCount = charCount;
    }

    public int getBuyVolume() {
        return buyVolume;
    }

    public void setBuyVolume(int buyVolume) {
        this.buyVolume = buyVolume;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isChartMode() {
        return chartMode;
    }

    public void setChartMode(boolean chartMode) {
        this.chartMode = chartMode;
    }

    public boolean isRise() {
        return rise;
    }

    public void setRise(boolean rise) {
        this.rise = rise;
    }

    public boolean isForeigner() {
        return foreigner;
    }

    public void setForeigner(boolean foreigner) {
        this.foreigner = foreigner;
    }

    public boolean isInstitution() {
        return institution;
    }

    public void setInstitution(boolean institution) {
        this.institution = institution;
    }

    public boolean isOverseas() {
        return overseas;
    }

    public void setOverseas(boolean overseas) {
        this.overseas = overseas;
    }

    public boolean isDomestic() {
        return domestic;
    }

    public void setDomestic(boolean domestic) {
        this.domestic = domestic;
    }

    public boolean isBuy() {
        return buy;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }

    public boolean isSell() {
        return sell;
    }

    public void setSell(boolean sell) {
        this.sell = sell;
    }

    public String getChartUrl() {
        return chartUrl;
    }

    public void setChartUrl(String chartUrl) {
        this.chartUrl = chartUrl;
    }

    public boolean isListMode() {
        return listMode;
    }

    public void setListMode(boolean listMode) {
        this.listMode = listMode;
    }

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
    }

    public int getSellCount() {
        return sellCount;
    }

    public void setSellCount(int sellCount) {
        this.sellCount = sellCount;
    }
}
