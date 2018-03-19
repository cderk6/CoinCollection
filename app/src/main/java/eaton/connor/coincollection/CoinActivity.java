package eaton.connor.coincollection;

import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class CoinActivity extends AppCompatActivity {

    public static final String CoinId = "CoinId";
    public static final String ACoin = "ACoin";

    private Coin coin;
    private String coin_id;

    TableLayout tableLayout;

    StorageReference mStorage;

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private ImageView img_big;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
        mDrawer.addDrawerListener(drawerToggle);

        nvDrawer =(NavigationView) findViewById(R.id.nvView);

        // Init click listeners for nav
        nvDrawer.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch(item.getItemId()){
                            case R.id.nav_home:
                                // switch to home activity/fragment
                                break;
                            case R.id.nav_profile:
                                // switch to profile activity/fragment
                                break;
                            case R.id.nav_stats:
                                // switch to stats activity/fragment
                                break;
                            case R.id.nav_about:
                                // switch to about activity/fragment
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
        coin = getIntent().getParcelableExtra(ACoin);
        coin_id = getIntent().getStringExtra(CoinId);

        tableLayout = (TableLayout) findViewById(R.id.tableLayout);

        img_big = (ImageView) findViewById(R.id.expanded_image);


        buildTable();
    }

    private void buildTable(){
        Map<String, String> coin_map = coin.getMap();
        String[] picOrder = {"Obverse", "Reverse"};
        String[] tableOrder = {"year", "mint", "series", "grade", "denomination", "price", "barcode"};
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        for (int i = 0; i < picOrder.length; i++){
            TextView c1 = new TextView(this);
            c1.setText(picOrder[i].toUpperCase());
            final StorageReference ref= mStorage.child("Users").child(uid).child(picOrder[i] + "s").child(coin_id);
            ImageView img = new ImageView(this);
            GlideApp.with(this).load(ref).into(img);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GlideApp.with(CoinActivity.this).load(ref).into(img_big);
                    img_big.setVisibility(View.VISIBLE);
                    img_big.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            img_big.setVisibility(View.GONE);
                        }
                    });
                }
            });
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            row.addView(c1);
            row.addView(img);
            tableLayout.addView(row);
        }


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
    private void signOut(){
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // signed out. Switch to LoginActivity
                startActivity(new Intent(CoinActivity.this, LoginActivity.class));
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
        }

        return super.onOptionsItemSelected(item);
    }
}
