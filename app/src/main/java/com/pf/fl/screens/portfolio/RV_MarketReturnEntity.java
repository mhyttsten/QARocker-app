package com.pf.fl.screens.portfolio;

public class RV_MarketReturnEntity {
    public String _name;
    public String _nationality;
    public String _club;

    public Integer _rating;
    public Integer _age;

    public RV_MarketReturnEntity(String name, String nationality, String club, Integer rating, Integer age) {
        _name = name;
        _nationality = nationality;
        _club = club;
        _rating = rating;
        _age = age;
    }

}
