package eaton.connor.coincollection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class ParsingActivity extends AppCompatActivity {
    private ProgressBar loading;
    private TextView action;

    public static final String SerialNumber = "SerialNumber";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parsing);

        loading = (ProgressBar) findViewById(R.id.progressBar3);
        action = (TextView) findViewById(R.id.textView4);

        String serial_num = getIntent().getStringExtra(SerialNumber);



        if (serial_num != null) {
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

        action.setText("Checking PCGS");


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();
                builder.append(serial_num);


                String URL = "https://www.pcgs.com/cert/" + serial_num;
                Document doc = null;
                for (int tries = 1; tries < 4; tries++) {
                    try {
                        doc = Jsoup.connect(URL).get();
                        break;
                    } catch (IOException e) {
                        Log.w("ParsingActivity", "PCGS timeout count: " + tries);
                    }
                }
                if (doc == null) {
                    //Failed to connect to PCGS after 3 attempts
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "PCGS servers could not be reached. Check your internet connection.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    try {
                        Element table = doc.select("table").get(0);
                        Elements rows = table.select("tr");

                        for (int i = 0; i < rows.size(); i++) {
                            Element row = rows.get(i);
                            Elements cols = row.select("td");

                            builder.append("," + cols.get(1).text());
                            if (cols.get(0).text().equals("Date, mintmark")) {
                                String[] parts = cols.get(1).text().split("-");
                                year.append(parts[0]);
                                if (parts.length > 1) {
                                    mint.append(parts[1]);
                                }
                            } else if (cols.get(0).text().startsWith("De")) {
                                denom.append(cols.get(1).text());
                            } else if (cols.get(0).text().startsWith("Gr")) {
                                grade.append(cols.get(1).text());
                            } else if (cols.get(0).text().startsWith("PCGS P")) {
                                price.append(cols.get(1).text());
                            } else if (cols.get(0).text().startsWith("PCGS #")) {
                                try {
                                    String n = cols.get(1).text();
                                    String URL_series = "http://www.pcgscoinfacts.com/Coin/Detail/" + n;
                                    Document doc_series = Jsoup.connect(URL_series).get();
                                    Element table_series = doc_series.select("table#tblSeriesAndLevel").get(0);
                                    Element row_series = table_series.select("tr").get(0);
                                    Element col_series = row_series.select("td").get(1);
                                    series.append(col_series.text());
                                } catch (IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "The coin series could not be determined at this time. Try again later.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
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
                        return;
                    } catch (IndexOutOfBoundsException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                action.setText("Checking NGC");
                            }
                        });
                        //Try NGC
                        String ngc_id = serial_num.substring(0, 6);
                        String ngc_grade = serial_num.substring(6, 8);
                        URL = "https://www.ngccoin.com/price-history/api/" + ngc_id + "/" + ngc_grade;

                        new JsonTask().execute(URL, serial_num);
                    }

                }

            }
        });

        t.start();





    }
    private class JsonTask extends AsyncTask<String, String, String> {
        String serial_num;

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                serial_num = params[1];
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result != null){
                try {
                    JSONObject json = new JSONObject(result);
                    JSONObject coin = json.getJSONObject("coin");
                    JSONArray prices = json.getJSONArray("prices");
                    String grade = json.getString("displayGrade") == null ? "" : json.getString("displayGrade");
                    String price = prices.getJSONArray(prices.length() - 1).getString(1) == null ? "" : prices.getJSONArray(prices.length() - 1).getString(1);
                    String mintmark = coin.getString("MintMark") == null ? "" : coin.getString("MintMark");
                    String year = coin.getString("CoinYear") == null ? "" : coin.getString("CoinYear");
                    String denom = coin.getString("Denomination") == null ? "" : coin.getString("Denomination");
                    String series = coin.getString("DesignType") == null ? "" : coin.getString("DesignType");

                    Intent intent = new Intent(ParsingActivity.this, AddCoinActivity.class);
                    intent.putExtra(AddCoinActivity.SerialNumber, serial_num);
                    intent.putExtra(AddCoinActivity.Year, year);
                    intent.putExtra(AddCoinActivity.Mint, mintmark);
                    intent.putExtra(AddCoinActivity.Denom, denom);
                    intent.putExtra(AddCoinActivity.Grade, grade);
                    intent.putExtra(AddCoinActivity.Price, price);
                    intent.putExtra(AddCoinActivity.Series, series);
                    startActivity(intent);
                    finish();
                }catch (JSONException e){
                    Log.w("ParsingActivity", "JSON Exception");
                    e.printStackTrace();
                }
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        action.setText("Checking ICG");
                    }
                });
                parseICG(serial_num);
            }
        }
    }
    private void parseICG(final String serial_num) {
        //try parsing


        //if unsuccessful
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Only PCGS, NGC, and ICG are supported.", Toast.LENGTH_LONG).show();
            }
        });
        Intent intent = new Intent(ParsingActivity.this, AddCoinActivity.class);
        intent.putExtra(AddCoinActivity.SerialNumber, serial_num);
        intent.putExtra(AddCoinActivity.Year, "");
        intent.putExtra(AddCoinActivity.Mint, "");
        intent.putExtra(AddCoinActivity.Denom, "");
        intent.putExtra(AddCoinActivity.Grade, "");
        intent.putExtra(AddCoinActivity.Price, "");
        intent.putExtra(AddCoinActivity.Series, "");
        startActivity(intent);
        finish();

    }
}

