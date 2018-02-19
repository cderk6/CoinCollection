package eaton.connor.coincollection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AddCoinActivity extends AppCompatActivity {

    private Map<String, Object> user= new HashMap<>();
    private Map<String, Object> coin = new HashMap<>();
    private String denom, type, year, mint, grade = "";
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_coin);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // Populate these from db later
        String[] array_denom = new String[] {"$1"};
        String[] array_type = new String[] {"Morgan Dollar"};
        String[] array_year = new String[] {"1879"};
        String[] array_mint = new String[] {"O"};
        String[] array_grade = new String[] {"MS63"};

        final Spinner spinner_denom = (Spinner) findViewById(R.id.spinner_denomination);
        ArrayAdapter<String> adapter_denom = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, array_denom);
        adapter_denom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_denom.setAdapter(adapter_denom);
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
                android.R.layout.simple_spinner_item, array_type);
        adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_type.setAdapter(adapter_type);
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
                android.R.layout.simple_spinner_item, array_year);
        adapter_year.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_year.setAdapter(adapter_year);
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
                android.R.layout.simple_spinner_item, array_mint);
        adapter_mint.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mint.setAdapter(adapter_mint);
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
                android.R.layout.simple_spinner_item, array_grade);
        adapter_grade.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_grade.setAdapter(adapter_grade);
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
        switch (item.getItemId()){
            case R.id.action_add_coin:
                // Submit coin data to db
                addCoin();
                Toast.makeText(getApplicationContext(), "Coin added!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddCoinActivity.this, HomeActivity.class));
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
        coin.put("denomination", denom);
        coin.put("type", type);
        coin.put("year", year);
        coin.put("mint", mint);
        coin.put("grade", grade);

        String uid = addUser();
        CollectionReference ref = db.collection("users").document(uid).collection("coins");
        ref.add(coin);
    }

    private String addUser(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        user.put("user_id",  uid);

        db.collection("users").document(uid)
                .set(user, SetOptions.merge());
        return uid;
    }
}