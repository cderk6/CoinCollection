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

    public Coin(Map<String, Object> coin_map)
    {
        this.barcode=coin_map.get("barcode") == null ? "" : coin_map.get("barcode").toString();
        this.denom=coin_map.get("denomination") == null ? "" : coin_map.get("denomination").toString();
        this.series=coin_map.get("series") == null ? "" : coin_map.get("series").toString();
        this.year=coin_map.get("year") == null ? "" : coin_map.get("year").toString();
        this.mint=coin_map.get("mint") == null ? "" : coin_map.get("mint").toString();
        this.grade=coin_map.get("grade") == null ? "" : coin_map.get("grade").toString();
        this.price=coin_map.get("price") == null ? "" : coin_map.get("price").toString();
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
