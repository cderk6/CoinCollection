package eaton.connor.coincollection;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {

    private Coin coin;
    private String coin_id;

    TableLayout tableLayout;

    StorageReference mStorage;
    FirebaseFirestore db;

    private DrawerLayout mDrawer;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawer.addDrawerListener(drawerToggle);

        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        // Init click listeners for nav
        nvDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.nav_home:
                                // switch to home activity
                                startActivity(new Intent(StatsActivity.this, HomeActivity.class));
                                finish();
                                break;
                            case R.id.nav_stats:
                                // switch to stats activity/fragment
                                startActivity(new Intent(StatsActivity.this, StatsActivity.class));
                                finish();
                                break;
                            case R.id.nav_signout:
                                // sign out and return to LoginActivity
                                signOut();
                                break;
                            default:
                                // home
                        }
                        return true;
                    }
                }
        );

        mStorage = FirebaseStorage.getInstance().getReference();

        tableLayout = (TableLayout) findViewById(R.id.stats_table);

        buildTable();
    }

    private void buildTable() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference ref = db.collection("users").document(uid).collection("coins");

        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    double count = 0;
                    double total_price= 0;
                    for (DocumentSnapshot document : task.getResult()) {
                        count++;
                        String price = document.get("price") == null ? "" : document.get("price").toString();
                        try {
                            total_price += Double.valueOf(price.replaceAll("[$]", ""));
                        } catch (NumberFormatException e) {
                            //price is blank. add nothing
                        }
                    }
                    TableRow row1 = new TableRow(StatsActivity.this);
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                    row1.setLayoutParams(lp);
                    TextView r1c1 = new TextView(StatsActivity.this);
                    TextView r1c2 = new TextView(StatsActivity.this);
                    r1c1.setText("Number of coins: ");
                    r1c2.setText(String.valueOf(count));
                    row1.addView(r1c1);
                    row1.addView(r1c2);

                    TableRow row2 = new TableRow(StatsActivity.this);
                    row2.setLayoutParams(lp);
                    TextView r2c1 = new TextView(StatsActivity.this);
                    TextView r2c2 = new TextView(StatsActivity.this);
                    r2c1.setText("Total worth: ");
                    r2c2.setText(String.valueOf(total_price));
                    row2.addView(r2c1);
                    row2.addView(r2c2);

                    TableRow row3 = new TableRow(StatsActivity.this);
                    row3.setLayoutParams(lp);
                    TextView r3c1 = new TextView(StatsActivity.this);
                    TextView r3c2 = new TextView(StatsActivity.this);
                    r3c1.setText("Average price: ");
                    r3c2.setText(String.valueOf(total_price/count));
                    row3.addView(r3c1);
                    row3.addView(r3c2);

                    tableLayout.addView(row1);
                    tableLayout.addView(row2);
                    tableLayout.addView(row3);

                }
            }
        });
    }

    private void signOut() {
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // signed out. Switch to LoginActivity
                startActivity(new Intent(StatsActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
