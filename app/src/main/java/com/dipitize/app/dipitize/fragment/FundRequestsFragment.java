package com.dipitize.app.dipitize.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dipitize.app.dipitize.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FundRequestsFragment extends Fragment {


    public FundRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fund_requests, container, false);
    }

}
