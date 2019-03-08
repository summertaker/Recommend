package com.summertaker.recommend.data;

public class Trade {
    private String trader; // "foreigner" ,"institution"
    private String buySell;  // "buy", "sell"
    private int Pot;       // Price of Trade
    private int Vot;       // Volume of Trade;

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

    public int getPot() {
        return Pot;
    }

    public void setPot(int pot) {
        Pot = pot;
    }

    public int getVot() {
        return Vot;
    }

    public void setVot(int vot) {
        Vot = vot;
    }
}
