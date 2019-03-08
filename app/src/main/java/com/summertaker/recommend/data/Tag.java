package com.summertaker.recommend.data;

public class Tag {
    private long id; // Drag & Drop 정렬에 사용
    private String name;
    private String bgc;
    private String fgc;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBgc() {
        return bgc;
    }

    public void setBgc(String bgc) {
        this.bgc = bgc;
    }

    public String getFgc() {
        return fgc;
    }

    public void setFgc(String fgc) {
        this.fgc = fgc;
    }
}
