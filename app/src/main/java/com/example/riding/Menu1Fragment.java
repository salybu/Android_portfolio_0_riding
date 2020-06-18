package com.example.riding;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Menu1Fragment extends Fragment {

    // 2개의 메뉴에 들어갈 Fragment들
    private AllmoimFragment allmoimFragment = new AllmoimFragment();

    // 로딩 프로그레스바
    private AsyncTask<Integer, String, Integer> mProgressDlg;

//    private FragmentManager fmg = getChildFragmentManager(); // 프래그먼트 매니저 선언    => childFragment 만들기 시작하면서 코드가 달라짐
//    private FragmentTransaction fragmentTransactiong = fmg.beginTransaction(); // 프래그먼트 transaction 시작

    public static Menu1Fragment newInstance() {
        return new Menu1Fragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fv = inflater.inflate(R.layout.fragment_menu1, container, false);
        TabLayout tabLayout = (TabLayout) fv.findViewById(R.id.tab_layout);
        mProgressDlg = new ProgressDlg(getActivity()).execute(30);
        changeView(0); // 한번에 뜨게 하려니까

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // TODO : process tab selection event.
                int pos = tab.getPosition();
                changeView(pos);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // do nothing
            }
        });

        return fv;
    }

    private void changeView(int index) {
        Fragment fg;

        switch (index) {
            case 0 :
                fg = AllmoimFragment.newInstance();
                setChildFragment(fg);
//                fragmentTransactiong.replace(R.id.frame_layout, allmoimFragment).commitAllowingStateLoss();
                break ;

            case 1 :
                fg = MymoimFragment.newInstance();
                setChildFragment(fg);
//                fragmentTransactiong.replace(R.id.frame_layout, moimBoardFragment).commitAllowingStateLoss();
                break ;
        }
    }

    private void setChildFragment(Fragment child) {
        FragmentTransaction childFt = getChildFragmentManager().beginTransaction();

        if (!child.isAdded()) {
            childFt.replace(R.id.frame_layout_moim, child);
            childFt.addToBackStack(null);
            childFt.commit();
        }
    }
}