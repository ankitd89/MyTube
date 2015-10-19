package com.adevani.mytube;

import android.support.v4.app.Fragment;

/**
 * Created by ankitdevani on 10/18/15.
 */
public class TabListener implements android.support.v7.app.ActionBar.TabListener {

    private Fragment fragment;

    // The contructor.
    public TabListener(Fragment fragment) {
        this.fragment = fragment;
    }


    @Override
    public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        ft.replace(R.id.activity_home, fragment);
    }

    @Override
    public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        ft.remove(fragment);
    }

    @Override
    public void onTabReselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }
}
