package eaton.connor.coincollection;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ParsingActivity extends AppCompatActivity {
    private ProgressBar loading;

    public static final String SerialNumber = "SerialNumber";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parsing);

        loading = (ProgressBar) findViewById(R.id.progressBar3);

        String serial_num = getIntent().getStringExtra(SerialNumber);

        if (serial_num != null){
            parseCoinInfo(serial_num);
        }
    }
    private void parseCoinInfo(final String serial_num) {
        final StringBuffer year = new StringBuffer("");
        final StringBuffer mint = new StringBuffer("");
        final StringBuffer denom = new StringBuffer("");
        final StringBuffer grade = new StringBuffer("");
        final StringBuffer price = new StringBuffer("");
        final StringBuffer series = new StringBuffer("");



        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();
                builder.append(serial_num);
                try {

                    String URL = "https://www.pcgs.com/cert/" + serial_num;

                    Document doc = Jsoup.connect(URL).get();
                    Element table = doc.select("table").get(0);
                    Elements rows = table.select("tr");

                    for (int i = 0; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cols = row.select("td");

                        builder.append("," + cols.get(1).text());
                        if(cols.get(0).text().equals("Date, mintmark")){
                            String[] parts = cols.get(1).text().split("-");
                            year.append(parts[0]);
                            if(parts.length > 1)
                            {
                                mint.append(parts[1]);
                            }
                        }
                        else if(cols.get(0).text().startsWith("De")){
                            denom.append(cols.get(1).text());
                        }
                        else if(cols.get(0).text().startsWith("Gr")){
                            grade.append(cols.get(1).text());
                        }
                        else if(cols.get(0).text().startsWith("PCGS P")){
                            price.append(cols.get(1).text());
                        }
                        else if(cols.get(0).text().startsWith("PCGS #")){
                            try {
                                String n = cols.get(1).text();
                                String URL_series = "http://www.pcgscoinfacts.com/Coin/Detail/" + n;
                                Document doc_series = Jsoup.connect(URL_series).get();
                                Element table_series = doc_series.select("table#tblSeriesAndLevel").get(0);
                                Element row_series = table_series.select("tr").get(0);
                                Element col_series = row_series.select("td").get(1);
                                series.append(col_series.text());
                            } catch (IOException e) {
                                runOnUiThread(new Runnable(){
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "The coin series could not be determined at this time. Try again later.", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }
                } catch (IOException e) {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "The server could not be reached. Try again later.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IndexOutOfBoundsException e) {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Coin info could not be found for this serial number.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                Intent intent = new Intent(ParsingActivity.this, AddCoinActivity.class);
                intent.putExtra(AddCoinActivity.SerialNumber, serial_num);
                intent.putExtra(AddCoinActivity.Year, year.toString());
                intent.putExtra(AddCoinActivity.Mint, mint.toString());
                intent.putExtra(AddCoinActivity.Denom, denom.toString());
                intent.putExtra(AddCoinActivity.Grade, grade.toString());
                intent.putExtra(AddCoinActivity.Price, price.toString());
                intent.putExtra(AddCoinActivity.Series, series.toString());
                startActivity(intent);
                finish();
            }
        }).start();
    }
}
