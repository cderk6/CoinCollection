package eaton.connor.coincollection;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Connor on 3/9/2018.
 */

public class Coin {
    private String barcode;
    private String denom;
    private String series;
    private String year;
    private String mint;
    private String grade;
    private String price;


    public Coin(String barcode, String denom, String series, String year, String mint, String grade, String price)
    {
        this.barcode=barcode;
        this.denom=denom;
        this.series=series;
        this.year=year;
        this.mint=mint;
        this.grade=grade;
        this.price=price;

    }

    public Map getMap() {
        Map<String, Object> coin_map = new HashMap<>();

        coin_map.put("barcode", barcode);
        coin_map.put("denomination", denom);
        coin_map.put("series", series);
        coin_map.put("year", year);
        coin_map.put("mint", mint);
        coin_map.put("grade", grade);
        coin_map.put("price", price);

        return coin_map;
    }
}
