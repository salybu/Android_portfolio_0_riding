package com.example.riding;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
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

public class Menu2Fragment extends Fragment {

    // 2개의 메뉴에 들어갈 Fragment들
    private AllmoimFragment allmoimFragment = new AllmoimFragment();
    public static Menu2Fragment newInstance() {
        return new Menu2Fragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fv = inflater.inflate(R.layout.fragment_menu2, container, false);
        TabLayout tabLayout = (TabLayout) fv.findViewById(R.id.tab_layout);
        changeView(0);

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
                fg = FriendFragment.newInstance();
                setChildFragment(fg);
                break ;

            case 1 :
                fg = ChatFragment.newInstance();
                setChildFragment(fg);
                /*
                <android.support.design.widget.TabItem
                android:id="@+id/tabItem2"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="채팅" />  xml에 tabitem2로 넣는 거 */

                break ;
        }
    }

    private void setChildFragment(Fragment child) {
        FragmentTransaction childFt = getChildFragmentManager().beginTransaction();

        if (!child.isAdded()) {
            childFt.replace(R.id.frame_layout_friend, child);
            childFt.addToBackStack(null);
            childFt.commit();
        }
    }
}