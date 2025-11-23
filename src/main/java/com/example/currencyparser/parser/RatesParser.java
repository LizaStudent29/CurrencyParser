package com.example.currencyparser.parser;

import com.example.currencyparser.entity.CurrencyRate;

import java.util.List;

public interface RatesParser {
    List<CurrencyRate> parseRates();
    String getSourceName();
}
