package eaton.connor.coincollection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class CoinActivity extends AppCompatActivity {

    public static final String CoinId = "CoinId";
    public static final String ACoin = "ACoin";

    private Coin coin;

    TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        coin = getIntent().getParcelableExtra(ACoin);

        tableLayout = (TableLayout) findViewById(R.id.tableLayout);


        buildTable();
    }

    private void buildTable(){
        Map<String, String> coin_map = coin.getMap();
        String[] tableOrder = {"year", "mint", "series", "grade", "denomination", "price", "barcode"};

        for(int i = 0; i < tableOrder.length; i++) {
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            TextView c1 = new TextView(this);
            TextView c2 = new TextView(this);
            c1.setText((tableOrder[i] + ":   ").toUpperCase());
            c2.setText(coin_map.get(tableOrder[i]) == null ? "" : coin_map.get(tableOrder[i]).toString());
            row.addView(c1);
            row.addView(c2);
            tableLayout.addView(row);
        }

    }
}
