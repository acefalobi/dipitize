package com.dipitize.app.dipitize.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.dipitize.app.dipitize.fragment.Game1Fragment;
import com.dipitize.app.dipitize.fragment.Game2Fragment;

public class GamePagerAdapter extends FragmentPagerAdapter {

    public GamePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new Game1Fragment();
        } else if (position == 1) {
            return new Game2Fragment();
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
