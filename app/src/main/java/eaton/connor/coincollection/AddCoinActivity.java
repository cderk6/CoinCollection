package eaton.connor.coincollection;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class AddCoinActivity extends AppCompatActivity {

    public static final String SerialNumber = "SerialNumber";
    public static final String Year = "Year";
    public static final String Mint = "Mint";
    public static final String Denom = "Denom";
    public static final String Grade = "Grade";
    public static final String Price = "Price";
    public static final String Series = "Series";


    private Map<String, Object> user = new HashMap<>();
    private String denom, type, year, mint, grade, barcode, price = "";
    FirebaseFirestore db;

    TextInputEditText SN_input;
    EditText price_input;
    RelativeLayout btn_obverse;
    RelativeLayout btn_reverse;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_coin);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        SN_input = (TextInputEditText) findViewById(R.id.input_serial_num);
        price_input = (EditText) findViewById(R.id.spinner_price);
        btn_obverse = (RelativeLayout) findViewById(R.id.relLayoutObv);
        btn_reverse = (RelativeLayout) findViewById(R.id.relLayoutRev);
        btn_obverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Click test obverse", Toast.LENGTH_SHORT).show();

            }
        });

        btn_reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Click test reverse", Toast.LENGTH_SHORT).show();

            }
        });

        String serial_num = getIntent().getStringExtra(SerialNumber);
        String s_denom = getIntent().getStringExtra(Denom);
        String s_price = getIntent().getStringExtra(Price);
        String s_year = getIntent().getStringExtra(Year);
        String s_mint = getIntent().getStringExtra(Mint);
        String s_grade = getIntent().getStringExtra(Grade);
        String s_series = getIntent().getStringExtra(Series);


        SN_input.setText(serial_num);

        handler = new Handler();
        /*
        if (serial_num != null){
            parseCoinInfo(serial_num);
        }
        */


        // Populate these from db later
        HashSet<String> array_denom = new HashSet<String>();
        array_denom.add("$1");
        HashSet<String> array_type = new HashSet<String>();
        array_type.add("Morgan Dollar");
        HashSet<String> array_year = new HashSet<String>();
        array_year.add("1879");
        HashSet<String> array_mint = new HashSet<String>();
        array_mint.add("O");
        HashSet<String> array_grade = new HashSet<String>();
        array_grade.add("MS63");


        if (s_denom != null && !s_denom.equals("")) array_denom.add(s_denom);
        if (s_year != null && !s_year.equals("")) array_year.add(s_year);
        if (s_mint != null) array_mint.add(s_mint);
        if (s_grade != null && !s_grade.equals("")) array_grade.add(s_grade);
        if (s_price != null && !s_price.equals("")) price_input.setText(s_price);
        if (s_series != null && !s_series.equals("")) array_type.add(s_series);

        final Spinner spinner_denom = (Spinner) findViewById(R.id.spinner_denomination);
        ArrayAdapter<String> adapter_denom = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>(array_denom));
        adapter_denom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_denom.setAdapter(adapter_denom);
        if(s_denom != null && !s_denom.equals("")) spinner_denom.setSelection(adapter_denom.getPosition(s_denom));
        spinner_denom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                denom = spinner_denom.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Spinner spinner_type = (Spinner) findViewById(R.id.spinner_type);
        ArrayAdapter<String> adapter_type = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>(array_type));
        adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_type.setAdapter(adapter_type);
        if(s_series != null && !s_series.equals("")) spinner_type.setSelection(adapter_type.getPosition(s_series));
        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                type = spinner_type.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Spinner spinner_year = (Spinner) findViewById(R.id.spinner_year);
        ArrayAdapter<String> adapter_year = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>(array_year));
        adapter_year.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_year.setAdapter(adapter_year);
        if(s_year != null && !s_year.equals("")) spinner_year.setSelection(adapter_year.getPosition(s_year));
        spinner_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                year = spinner_year.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Spinner spinner_mint = (Spinner) findViewById(R.id.spinner_mint);
        ArrayAdapter<String> adapter_mint = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>(array_mint));
        adapter_mint.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mint.setAdapter(adapter_mint);
        if(s_mint != null) spinner_mint.setSelection(adapter_mint.getPosition(s_mint));
        spinner_mint.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mint = spinner_mint.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Spinner spinner_grade = (Spinner) findViewById(R.id.spinner_grade);
        ArrayAdapter<String> adapter_grade = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new ArrayList<String>(array_grade));
        adapter_grade.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_grade.setAdapter(adapter_grade);
        if(s_grade != null && !s_grade.equals("")) spinner_grade.setSelection(adapter_grade.getPosition(s_grade));
        spinner_grade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                grade = spinner_grade.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        db = FirebaseFirestore.getInstance();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_coin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_coin:
                // Submit coin data to db
                addCoin();
                Toast.makeText(getApplicationContext(), "Coin added!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddCoinActivity.this, HomeActivity.class));
                finish();
                return true;
            case R.id.action_cancel:
                // Discard data. Switch to HomeActivity
                startActivity(new Intent(AddCoinActivity.this, HomeActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addCoin() {
        String barcode = SN_input.getText().toString();
        String price = price_input.getText().toString();

        Coin new_coin = new Coin(barcode, denom, type, year, mint, grade, price);

        String uid = addUser();
        CollectionReference ref = db.collection("users").document(uid).collection("coins");
        ref.add(new_coin.getMap());
    }

    private String addUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        user.put("user_id", uid);

        db.collection("users").document(uid)
                .set(user, SetOptions.merge());
        return uid;
    }

}
