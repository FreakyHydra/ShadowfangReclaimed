package com.shadowfang.core.infoboard;

import java.util.ArrayList;
import java.util.List;

public class CustomBoardProgram {
    private String name;
    private List<String> pages;

    public CustomBoardProgram() {}

    public CustomBoardProgram(String name, List<String> pages) {
        this.name = name;
        this.pages = pages;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getPages() { return pages != null ? pages : new ArrayList<>(); }
    public void setPages(List<String> pages) { this.pages = pages; }
}
