package eaton.connor.coincollection;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private static final String EXTRA_IDP_RESPONSE = "extra_idp_response";
    private static final String TAG = "HomeActivity";
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private FloatingActionMenu fam;
    private FloatingActionButton fab_scan, fab_sn, fab_manual;

    FirebaseFirestore db;
    FirebaseAuth auth;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    HashMap<String, List<String>> mapCoinId;
    HashMap<String, Coin> coins;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
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
                                // switch to home activity
                                startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                                finish();
                                break;
                            case R.id.nav_stats:
                                // switch to stats activity/fragment
                                startActivity(new Intent(HomeActivity.this, StatsActivity.class));
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


        fam = (FloatingActionMenu) findViewById(R.id.floating_action_menu);
        fab_scan = (FloatingActionButton) findViewById(R.id.floating_scan);
        fab_sn = (FloatingActionButton) findViewById(R.id.floating_sn);
        fab_manual = (FloatingActionButton) findViewById(R.id.floating_manual);

        fab_scan.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // Open scan activity
                startActivity(new Intent(HomeActivity.this, ScanActivity.class));
            }
        });

        fab_sn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // Open AlertDialog
                DialogFragment sn_frag = new SerialNumberDialogFragment();
                sn_frag.show(getSupportFragmentManager(), "sn");
            }
        });

        fab_manual.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // Open AddCoinActivity.class
                startActivity(new Intent(HomeActivity.this, AddCoinActivity.class));
            }
        });

        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        expListView.setAdapter(listAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                String coin_id = mapCoinId.get(listDataHeader.get(groupPosition)).get(childPosition);
                Toast.makeText(
                        getApplicationContext(),
                        listDataHeader.get(groupPosition)
                                + " : "
                                + coin_id, Toast.LENGTH_SHORT)
                        .show();

                Intent intent = new Intent(HomeActivity.this, CoinActivity.class);
                intent.putExtra(CoinActivity.CoinId, coin_id);
                intent.putExtra(CoinActivity.ACoin, coins.get(coin_id));

                startActivity(intent);
                return false;
            }
        });

        //coins = getCoins();
    }

    public static Intent createIntent(
            Context context,
            IdpResponse idpResponse) {

        Intent startIntent = new Intent();
        if (idpResponse != null) {
            startIntent.putExtra(EXTRA_IDP_RESPONSE, idpResponse);
        }

        return startIntent.setClass(context, HomeActivity.class);
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
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }
    private void signOut(){
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // signed out. Switch to LoginActivity
                startActivity(new Intent(HomeActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });
    }
/*
    private ArrayList<Coin> getCoins() {
        final ArrayList<Coin> coins = new ArrayList<>();
        String uid = auth.getCurrentUser().getUid();
        CollectionReference ref = db.collection("users").document(uid).collection("coins");

        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                coins.add(new Coin(document.getData()));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
        return coins;
    }
*/
    private void prepareListData() {
        String uid = auth.getCurrentUser().getUid();
        CollectionReference ref = db.collection("users").document(uid).collection("coins");

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        mapCoinId = new HashMap<String, List<String>>();
        coins = new HashMap<String, Coin>();

        ref.orderBy("denomination").orderBy("year").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, document.getId() + " => " + data);
                        String id = document.getId();
                        String key = data.get("denomination") == null ? "" : data.get("denomination").toString();
                        String year = data.get("year") == null ? "" : data.get("year").toString();
                        String mint = data.get("mint") == null ? "" : data.get("mint").toString();
                        String series = data.get("series") == null ? "" : data.get("series").toString();
                        String grade = data.get("grade") == null ? "" : data.get("grade").toString();
                        String info = year + "-" + mint + " " + series + " " + grade;
                        if(listDataHeader.contains(key))
                        {
                            listDataChild.get(key).add(info);
                            mapCoinId.get(key).add(document.getId());
                        } else {
                            listDataHeader.add(key);
                            List<String> val_list = new ArrayList<>();
                            val_list.add(info);
                            listDataChild.put(key, val_list);

                            List<String> id_list = new ArrayList<>();
                            id_list.add(id);
                            mapCoinId.put(key, id_list);
                        }
                        coins.put(id, new Coin(data));

                    }
                    listAdapter = new ExpandableListAdapter(HomeActivity.this, listDataHeader, listDataChild);

                    expListView.setAdapter(listAdapter);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }

            }
        });



    }
}
