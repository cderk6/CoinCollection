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
    public static final String Grade = "Grade";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parsing);

        loading = (ProgressBar) findViewById(R.id.progressBar3);
        action = (TextView) findViewById(R.id.textView4);

        String serial_num = getIntent().getStringExtra(SerialNumber);
        String grade = getIntent().getStringExtra(Grade);


        if (serial_num != null && !serial_num.contains("-")) {
            if(serial_num.contains("/")) {
                parseCoinInfo(serial_num.substring(serial_num.indexOf("/") + 1));
            } else{
                parseCoinInfo(serial_num);
            }
        }
        else{
            parseNGC(serial_num, grade);
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
                String pcgs_id = serial_num.length() > 7 ? serial_num.substring(serial_num.length() - 8) : serial_num;

                String URL = "https://www.pcgs.com/cert/" + pcgs_id;
                Document doc = null;
                if (serial_num.length() != 18) {
                    for (int tries = 1; tries < 4; tries++) {
                        try {
                            doc = Jsoup.connect(URL).get();
                            break;
                        } catch (IOException e) {
                            Log.w("ParsingActivity", "PCGS timeout count: " + tries);
                        }
                    }
                }
                if (doc == null) {
                    //Failed to connect to PCGS after 3 attempts
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "PCGS servers could not be reached. Make sure you have internet access.", Toast.LENGTH_LONG).show();
                        }
                    });
                    if (serial_num.length() > 7) {
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
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                action.setText("Checking ICG");
                            }
                        });
                        parseICG(serial_num);
                    }

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
                                    String search_results = doc_series.outerHtml();
                                    int beg = search_results.indexOf("Series: </b>");
                                    int end = search_results.indexOf("</p>", beg);
                                    String col_series = search_results.substring(beg + 12, end);
                                    Log.w("ParsingActivity", col_series);
                                    series.append(col_series);
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
                        if (serial_num.length() > 7) {
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
                        } else {
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
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);

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

            if (result != null) {
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
                } catch (JSONException e) {
                    Log.w("ParsingActivity", "JSON Exception");
                    e.printStackTrace();
                }
            } else {
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
        final StringBuffer year = new StringBuffer("");
        final StringBuffer mint = new StringBuffer("");
        final StringBuffer denom = new StringBuffer("");
        final StringBuffer grade = new StringBuffer("");
        final StringBuffer price = new StringBuffer("");
        final StringBuffer series = new StringBuffer("");
        final StringBuffer details = new StringBuffer("");
        final StringBuffer variety = new StringBuffer("");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();
                builder.append(serial_num);
                try {
                    String URL;
                    if(serial_num.length() > 10) {
                        URL = "http://www.icgcoin.com/load_SNSearch.php?ctn=" + serial_num.substring(8, 18);
                    } else {
                        URL = "http://www.icgcoin.com/load_SNSearch.php?ctn=" + serial_num;
                    }
                    Document doc = null;
                    for (int tries = 1; tries < 4; tries++) {
                        try {
                            doc = Jsoup.connect(URL).get();
                            break;
                        } catch (IOException e) {
                            Log.w("ParsingActivity", "ICG timeout count: " + tries);
                        }
                    }
                    if (doc == null) {
                        //Failed to connect to ICG after 3 attempts
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "ICG servers could not be reached. Make sure you have internet access.", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {

                        Element table = doc.select("table").get(0);
                        Elements rows = table.select("tr");

                        for (int i = 0; i < rows.size(); i++) {
                            Element row = rows.get(i);
                            Elements cols = row.select("td");

                            builder.append("," + cols.get(1).text());
                            if (cols.get(0).text().startsWith("Coin D")) {
                                String[] parts = cols.get(1).text().trim().split("[-,]");
                                year.append(parts[0]);
                                if (parts.length > 1) {
                                    mint.append(parts[1]);
                                }
                            } else if (cols.get(0).text().startsWith("Denom")) {
                                denom.append(cols.get(1).text().trim());
                            } else if (cols.get(0).text().startsWith("Gra")) {
                                String temp = cols.get(1).text().replaceAll("[-,]", " ").trim();
                                grade.append(temp);
                            } else if (cols.get(0).text().startsWith("Var") || cols.get(0).text().startsWith("Det")) {
                                String temp = cols.get(1).text().replaceAll("[-,]", " ").trim();
                                details.append(temp);
                            }

                        }
                        String temp_denom = String.valueOf(denom);
                        if (denom.toString().equals("S$1")) temp_denom = "$1";
                        String search_results_URL = "https://www.collectorsuniverse.com/SpecSearch/Search/PCGS?callback=jQuery111309298615020573713_1520498037997&term=" +
                                year.toString() + "-" + mint.toString() + "+" + temp_denom + "+" + grade.toString().replace(" ", "+") + "+" + details.toString().replace(" ", "+") +
                                "&includeTypeCoins=true&includeworld=false&worldOnly=false&ancestorCategoryId=&pricedgrades=false&priceguideonly=true&popOnly=false&auctionpriceonly=false&_=1520498038011";
                        Document search_doc = null;
                        for (int tries = 1; tries < 4; tries++) {
                            try {
                                search_doc = Jsoup.connect(search_results_URL).ignoreContentType(true).get();
                                break;
                            } catch (IOException e) {
                                Log.w("ParsingActivity", "PCGS search results timeout count: " + tries);
                                e.printStackTrace();
                            }
                        }
                        if (search_doc == null) {
                            //Failed to connect to PCGS search after 3 attempts
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Coin price and series info could not be found at this time. Make sure you have internet access.", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            String search_results = search_doc.outerHtml();
                            int beg = search_results.indexOf("specno");
                            int end = search_results.indexOf("&quot;,&quot;description", beg);
                            String specno = search_results.substring(beg + 19, end);
                            Log.w("ParsingActivity", specno);

                            String PCGS_URL = "http://www.pcgscoinfacts.com/Coin/Detail/" + specno;

                            Document info_doc = null;
                            for (int tries = 1; tries < 4; tries++) {
                                try {
                                    info_doc = Jsoup.connect(PCGS_URL).ignoreContentType(true).get();
                                    break;
                                } catch (IOException e) {
                                    Log.w("ParsingActivity", "PCGS info timeout count: " + tries);
                                    e.printStackTrace();
                                }
                            }
                            if (info_doc == null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Coin price and series info could not be found at this time. Make sure you have internet access.", Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                try {

                                    Element info_table = info_doc.select("table:has(th.table-grade:contains(" + grade.toString().replaceAll("[^0-9]", "") + "))").get(0);
                                    //Element info_row = info_table.select("div#grade-" + grade.toString().replaceAll("[^0-9]", "")).get(0);
                                    //Element info_price = info_row.select("div.grade-price").get(0);
                                    Element info_row = info_table.select("tbody").get(0);
                                    Element info_price = info_row.select("td").get(1);
                                    price.append(info_price.text());
                                } catch (IndexOutOfBoundsException price_not_found) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Could not find a price for this coin.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                try {
                                    String info_results = info_doc.outerHtml();
                                    int info_beg = info_results.indexOf("Series: </b>");
                                    int info_end = info_results.indexOf("</p>", info_beg);
                                    String col_series = info_results.substring(info_beg + 12, info_end);
                                    Log.w("ParsingActivity", col_series);
                                    series.append(col_series);
                                    Element cn = info_doc.select("title").get(0);
                                    String coin_name = cn.text().substring(0, cn.text().indexOf("- PCGS"));
                                    variety.append(coin_name);
                                } catch (IndexOutOfBoundsException series_not_found) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Could not find a series for this coin.", Toast.LENGTH_LONG).show();
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
                        intent.putExtra(AddCoinActivity.Variety, variety.toString());
                        startActivity(intent);
                        finish();
                        return;

                    }

                } catch (IndexOutOfBoundsException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Only PCGS, NGC, and ICG are supported at this time. Try again if you think the serial number is valid.", Toast.LENGTH_LONG).show();
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
        });

        t.start();


    }

    private void parseNGC(final String serial_num, final String grade_input) {
        final StringBuffer year = new StringBuffer("");
        final StringBuffer mint = new StringBuffer("");
        final StringBuffer denom = new StringBuffer("");
        final StringBuffer grade = new StringBuffer("");
        final StringBuffer price = new StringBuffer("");
        final StringBuffer series = new StringBuffer("");

        action.setText("Checking NGC");


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();
                builder.append(serial_num);


                String URL = "https://www.ngccoin.com/certlookup/" + serial_num + "/" + grade_input;
                Document doc = null;
                for (int tries = 1; tries < 4; tries++) {
                    try {
                        doc = Jsoup.connect(URL).get();
                        break;
                    } catch (IOException e) {
                        Log.w("ParsingActivity", "NGC timeout count: " + tries);
                    }
                }
                if (doc == null) {
                    //Failed to connect to NGC after 3 attempts
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "NGC servers could not be reached. Make sure you have internet access.", Toast.LENGTH_LONG).show();
                        }
                    });

                    Intent intent = new Intent(ParsingActivity.this, AddCoinActivity.class);
                    intent.putExtra(AddCoinActivity.SerialNumber, serial_num);
                    intent.putExtra(AddCoinActivity.Year, year.toString());
                    intent.putExtra(AddCoinActivity.Mint, mint.toString());
                    intent.putExtra(AddCoinActivity.Denom, denom.toString());
                    intent.putExtra(AddCoinActivity.Grade, grade_input.toString());
                    intent.putExtra(AddCoinActivity.Price, price.toString());
                    intent.putExtra(AddCoinActivity.Series, series.toString());
                    startActivity(intent);
                    finish();
                    return;

                } else {
                    try {
                        Element link = doc.select("a.certlookup-stats-item").first();
                        String href = link.attr("href");
                        String ngc_id = "0" + href.substring(href.indexOf("=") + 1);

                        URL = "https://www.ngccoin.com/price-history/api/" + ngc_id + "/" + grade_input;

                        new JsonTask().execute(URL, serial_num);

                    } catch (IndexOutOfBoundsException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Invalid serial number", Toast.LENGTH_LONG).show();
                            }
                        });
                        Intent intent = new Intent(ParsingActivity.this, AddCoinActivity.class);
                        intent.putExtra(AddCoinActivity.SerialNumber, serial_num);
                        intent.putExtra(AddCoinActivity.Year, year.toString());
                        intent.putExtra(AddCoinActivity.Mint, mint.toString());
                        intent.putExtra(AddCoinActivity.Denom, denom.toString());
                        intent.putExtra(AddCoinActivity.Grade, grade_input.toString());
                        intent.putExtra(AddCoinActivity.Price, price.toString());
                        intent.putExtra(AddCoinActivity.Series, series.toString());
                        startActivity(intent);
                        finish();
                        return;
                    }

                }

            }
        });

        t.start();


    }

}

