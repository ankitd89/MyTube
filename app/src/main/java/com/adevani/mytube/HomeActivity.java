package com.adevani.mytube;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    // Declaring our tabs and the corresponding fragments.
    android.support.v7.app.ActionBar.Tab bmwTab, toyotaTab;
    Fragment bmwFragmentTab = new FavouriteFragment();
    Fragment toyotaFragmentTab = new SearchFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Asking for the default ActionBar element that our platform supports.
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        // Screen handling while hiding ActionBar icon.
        actionBar.setDisplayShowHomeEnabled(false);

        // Screen handling while hiding Actionbar title.
        actionBar.setDisplayShowTitleEnabled(false);

        // Creating ActionBar tabs.
        actionBar.setNavigationMode(android.support.v7.app.ActionBar.NAVIGATION_MODE_TABS);

        // Setting custom tab icons.
        toyotaTab = actionBar.newTab().setText("Search");
        bmwTab = actionBar.newTab().setText("Favourite");
        bmwTab.setTag("Favourite");


        // Setting tab listeners.
        toyotaTab.setTabListener(new TabListener(toyotaFragmentTab));
        bmwTab.setTabListener(new TabListener(bmwFragmentTab));


        // Adding tabs to the ActionBar.
        actionBar.addTab(toyotaTab);
        actionBar.addTab(bmwTab);


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
