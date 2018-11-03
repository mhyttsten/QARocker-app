package com.pf.fl.screens.recyclerview;

import android.widget.TextView;

public class MarketReturnEntity {
    public String _name;
    public String _nationality;
    public String _club;

    public Integer _rating;
    public Integer _age;

    public MarketReturnEntity(String name, String nationality, String club, Integer rating, Integer age) {
        _name = name;
        _nationality = nationality;
        _club = club;
        _rating = rating;
        _age = age;
    }

}
