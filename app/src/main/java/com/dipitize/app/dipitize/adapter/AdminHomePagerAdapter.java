package com.dipitize.app.dipitize.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.dipitize.app.dipitize.fragment.AdminHomeFragment;
import com.dipitize.app.dipitize.fragment.FundRequestsFragment;
import com.dipitize.app.dipitize.fragment.MatchPlayersFragment;
import com.dipitize.app.dipitize.fragment.WithdrawRequestsFragment;

public class AdminHomePagerAdapter extends FragmentPagerAdapter {

    public AdminHomePagerAdapter(FragmentManager fragmentManager) { super(fragmentManager); }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new AdminHomeFragment();
        } else if (position == 1) {
            return new MatchPlayersFragment();
        } else if (position == 2) {
            return new FundRequestsFragment();
        } else if (position == 3) {
            return new WithdrawRequestsFragment();
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Home";
        } else if (position == 1){
            return "Match Players";
        } else if (position == 2){
            return "Fund Requests";
        } else if (position == 3){
            return "Withdrawal Requests";
        } else {
            return super.getPageTitle(position);
        }
    }
}
