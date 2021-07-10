package com.iskcon.bvks.ui.filter;

public enum Filter {
    LANGUAGES("Languages"),
    COUNTRIES("Countries"),
    PLACE("Place"),
    YEARS("Years"),
    MONTH("Month"),
    CATEGORIES("Categories"),
    TRANSLATION("Translation");

    private String filter;

    Filter(String filter) {
        this.filter = filter;
    }

    public String getTitle() {
        return filter;
    }
}