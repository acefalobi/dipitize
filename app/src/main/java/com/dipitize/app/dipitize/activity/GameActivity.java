package com.dipitize.app.dipitize.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.adapter.GamePagerAdapter;
import com.google.firebase.auth.FirebaseAuth;

public class GameActivity extends AppCompatActivity {

    GamePagerAdapter pagerAdapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pagerAdapter = new GamePagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.view_pager_game);
        viewPager.setAdapter(pagerAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals("JWsOb4iV7rYti9cSSqJgPjygxEK2")) {
                Intent home_intent = new Intent(this, AdminActivity.class);
                home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(home_intent);
                finish();
                return true;
            } else {
                Intent home_intent = new Intent(this, HomeActivity.class);
                home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(home_intent);
                finish();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
