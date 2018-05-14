package eaton.connor.coincollection;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Connor on 3/9/2018.
 */

public class Coin implements Parcelable {
    private String barcode;
    private String denom;
    private String series;
    private String variety;
    private String year;
    private String mint;
    private String grade;
    private String price;


    public Coin(String barcode, String denom, String series, String year, String mint, String grade, String price, String variety) {
        this.barcode = barcode;
        this.denom = denom;
        this.series = series;
        this.variety = variety;
        this.year = year;
        this.mint = mint;
        this.grade = grade;
        this.price = price;

    }

    public Coin(Map<String, Object> coin_map) {
        this.barcode = coin_map.get("barcode") == null ? "" : coin_map.get("barcode").toString();
        this.denom = coin_map.get("denomination") == null ? "" : coin_map.get("denomination").toString();
        this.series = coin_map.get("series") == null ? "" : coin_map.get("series").toString();
        this.variety = coin_map.get("variety") == null ? "" : coin_map.get("variety").toString();
        this.year = coin_map.get("year") == null ? "" : coin_map.get("year").toString();
        this.mint = coin_map.get("mint") == null ? "" : coin_map.get("mint").toString();
        this.grade = coin_map.get("grade") == null ? "" : coin_map.get("grade").toString();
        this.price = coin_map.get("price") == null ? "" : coin_map.get("price").toString();
    }

    public Coin(Parcel in) {
        String[] data = new String[8];

        in.readStringArray(data);
        this.barcode = data[0];
        this.denom = data[1];
        this.series = data[2];
        this.variety = data[3];
        this.year = data[4];
        this.mint = data[5];
        this.grade = data[6];
        this.price = data[7];
    }

    public Map getMap() {
        Map<String, Object> coin_map = new HashMap<>();

        coin_map.put("barcode", barcode);
        coin_map.put("denomination", denom);
        coin_map.put("series", series);
        coin_map.put("variety", variety);
        coin_map.put("year", year);
        coin_map.put("mint", mint);
        coin_map.put("grade", grade);
        coin_map.put("price", price);

        return coin_map;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getDenom() {
        return denom;
    }

    public String getSeries() {
        return series;
    }

    public String getVariety() {
        return variety;
    }

    public String getYear() {
        return year;
    }

    public String getMint() {
        return mint;
    }

    public String getGrade() {
        return grade;
    }

    public String getPrice() {
        return price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{
                this.barcode,
                this.denom,
                this.series,
                this.variety,
                this.year,
                this.mint,
                this.grade,
                this.price
        });
    }

    public static final Parcelable.Creator<Coin> CREATOR = new Parcelable.Creator<Coin>() {

        @Override
        public Coin createFromParcel(Parcel source) {
// TODO Auto-generated method stub
            return new Coin(source);  //using parcelable constructor
        }

        @Override
        public Coin[] newArray(int size) {
// TODO Auto-generated method stub
            return new Coin[size];
        }
    };

}

