package com.ts.music.entity;

public class RadioBean {
    private String num;
    private int icon;
    private String name;
    private String title;
    private String time;

    public RadioBean(String num, int icon, String name, String title, String time) {
        this.num = num;
        this.icon = icon;
        this.name = name;
        this.title = title;
        this.time = time;
    }

    public RadioBean() {
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
