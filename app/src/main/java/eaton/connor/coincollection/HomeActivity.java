package eaton.connor.coincollection;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

public class HomeActivity extends AppCompatActivity {
    private static final String EXTRA_IDP_RESPONSE = "extra_idp_response";
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private FloatingActionMenu fam;
    private FloatingActionButton fab_scan, fab_manual;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
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


        fam = (FloatingActionMenu) findViewById(R.id.floating_action_menu);
        fab_scan = (FloatingActionButton) findViewById(R.id.floating_scan);
        fab_manual = (FloatingActionButton) findViewById(R.id.floating_manual);

/*        fab_scan.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // Open scan activity
            }
        });
        */
        fab_manual.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // Open AddCoinActivity.class
                startActivity(new Intent(HomeActivity.this, AddCoinActivity.class));
            }
        });


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
    public void signOut(){
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // signed out. Switch to LoginActivity
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            }
        });
    }
}
