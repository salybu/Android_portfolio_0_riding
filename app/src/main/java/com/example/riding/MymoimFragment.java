package com.example.riding;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class MymoimFragment extends BaseFragment {

    private TextView MyMade;
    private TextView MyParticipation;
    private TextView MyFavorite;
    private TextView MyPast;

    private RecyclerView recyclerView_mymade;
    private RecyclerView recyclerView_my; // 리사이클러뷰 변수 선언
    private RecyclerView recyclerView_favorite;
    private RecyclerView recyclerView_past;

    private ArrayList<moimPost_fs> mymadelist = new ArrayList<>(); // 내가 만든 모임. 초기 객체까지 모두 생성해줌
    private ArrayList<moimPost_fs> myfuturelist = new ArrayList<>(); // 내가 참여할 모임
    private ArrayList<moimPost_fs> myfavoritelist = new ArrayList<>(); // 내가 찜한 모임
    private ArrayList<moimPost_fs> mypastlist = new ArrayList<>(); // 내가 참여했던 모임

    private ArrayList<String> uidLists_made = new ArrayList<>();
    private ArrayList<String> uidLists_future = new ArrayList<>();
    private ArrayList<String> uidLists_favorite = new ArrayList<>();
    private ArrayList<String> uidLists_past = new ArrayList<>();

    private SwipetoDeleteCallback swipetoDeleteCallback; // 스와이프해서 삭제할 수 있게 SwipetoDeleteCallback 클래스 넣었음

    public static MymoimFragment newInstance() {
        return new MymoimFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mymoim, container, false);
        MyMade = (TextView) view.findViewById(R.id.mymade); // 내가 만든 모임 텍스트뷰 위젯연결
        MyParticipation = (TextView) view.findViewById(R.id.myparticipation); // 내가 참여할 모임 텍스트뷰 위젯연결
        MyFavorite = (TextView) view.findViewById(R.id.myfavorite); // 내가 찜한 모임
        MyPast = (TextView) view.findViewById(R.id.mypast); // 내가 참여했던 모임

        recyclerView_mymade = (RecyclerView) view.findViewById(R.id.recyclerView_mymade);
        recyclerView_my = (RecyclerView) view.findViewById(R.id.recyclerView_my);
        recyclerView_favorite = (RecyclerView) view.findViewById(R.id.recyclerView_favorite);
        recyclerView_past = (RecyclerView) view.findViewById(R.id.recyclerView_past);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        UserAuthInfoSet(); // 로그인한 사용자 정보 가져와 세팅하는 메소드
        FirestoreDbSet(); // Firestore 데이터베이스 연결하는 메소드

        recyclerView_mymade.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_my.setLayoutManager(new LinearLayoutManager(getActivity())); // 리사이클러뷰에 레이아웃 매니저 연결 - 리니어레이아웃매니저 새로 생성(Context는 this)
        recyclerView_favorite.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView_past.setLayoutManager(new LinearLayoutManager(getActivity()));

/*        final Adapter_forEvent adapter_made = new Adapter_forEvent(mymadelist, uidLists_made,0);
        final Adapter_forEvent adapter_future = new Adapter_forEvent(myfuturelist, uidLists_future,0); // 액티비티 내에서 위의 ArrayList 담을 어댑터 객체 생성
        final Adapter_forEvent adapter_favorite = new Adapter_forEvent(myfavoritelist, uidLists_favorite,0);
        final Adapter_forEvent adapter_past = new Adapter_forEvent(mypastlist, uidLists_past,0);   */

        final Adapter_forEvent adapter_made = new Adapter_forEvent(mymadelist);
        final Adapter_forEvent adapter_future = new Adapter_forEvent(myfuturelist); // 액티비티 내에서 위의 ArrayList 담을 어댑터 객체 생성
        final Adapter_forEvent adapter_favorite = new Adapter_forEvent(myfavoritelist);
        final Adapter_forEvent adapter_past = new Adapter_forEvent(mypastlist);

        recyclerView_mymade.setAdapter(adapter_made); // 리사이클러뷰에 어댑터 연결
        recyclerView_my.setAdapter(adapter_future); // 리사이클러뷰에 어댑터 연결
        recyclerView_favorite.setAdapter(adapter_favorite);
        recyclerView_past.setAdapter(adapter_past);

        adapter_favorite.notifyDataSetChanged();
        adapter_future.notifyDataSetChanged();
        adapter_made.notifyDataSetChanged();
        adapter_past.notifyDataSetChanged();

        mymadelist.clear(); // 내가 만든 소모임 담은 어레이리스트
        myfuturelist.clear();
        myfavoritelist.clear();
        mypastlist.clear();

        final SharedPreferences pref = this.getActivity().getSharedPreferences("Moims", MODE_PRIVATE); /// Moims 셰어드 불러옴
/*
                myfuturelist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    moimPost moimitem = snapshot.getValue(moimPost.class);
                    String key = snapshot.getKey();
                    myfuturelist.add(moimitem);
                    uidLists.add(key);
                }
                adapter_future.notifyDataSetChanged(); */

/*                myfavoritelist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    moimPost moimitem = snapshot.getValue(moimPost.class);
                    String key = snapshot.getKey();
                    myfavoritelist.add(moimitem);
                    uidLists.add(key);
                }
                adapter_favorite.notifyDataSetChanged();

                mypastlist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    moimPost moimitem = snapshot.getValue(moimPost.class);
                    String key = snapshot.getKey();
                    mypastlist.add(moimitem);
                    uidLists.add(key);
                }
                adapter_past.notifyDataSetChanged();  */

        Calendar cal = new GregorianCalendar(); // 현재 날짜와 시간을 가져오기 위한 Calendar 인스턴스 선언
        Log.e("Mymoim ", String.valueOf(cal));

        db.collection("Moim") // "Moim" 콜렉션에서
                .whereEqualTo("UID", UID) // UID 필드(작성자)가 로그인한 UID값과 같은 문서
                .get() // 가져옴 => 내가 작성한 모임리스트에 넣어줌
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                moimPost_fs moim = document.toObject(moimPost_fs.class);
                                mymadelist.add(moim);

                                String key = document.getId(); // 모임 아이템 키값 담은 어레이리스트
                                uidLists_made.add(key);

                                swipetoDeleteCallback = new SwipetoDeleteCallback(adapter_made, getActivity(),uidLists_made);
                                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipetoDeleteCallback);
                                itemTouchHelper.attachToRecyclerView(recyclerView_mymade);

                                adapter_made.notifyDataSetChanged(); // 어댑터에 바뀌었다고 알려줌
                            }

                        } else {
                            // 셰어드에서 꺼내는 코드
                            String eventsArray = pref.getString("Entire",""); // Moims 셰어드에서 Key값 "Entire"로(전체 모임아이템) Value값 JSONArray(String형) 불러옴

                            JSONArray eventsJSONArray = null; // value값 JSONArray를 받아 저장하기 위해 빈 JSONArray 변수[*] 선언
                            try {
                                eventsJSONArray = new JSONArray(eventsArray); // String형 JSONArray[eventsArray]를 JSONArray 객체로 형변환해 앞서 선언한 변수[*]에 저장
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            assert eventsJSONArray != null; // JSONArray가 null 값 아닐 때 (조건)
                            for(int i=0 ; i <= (eventsJSONArray.length()-1) ; i++) { // JSONArray에 담긴 모든 값을 꺼내기 위한 반복문. i <= JSONArray 마지막 인덱스까지 반복하도록 함
                                try {
                                    JSONObject object = eventsJSONArray.getJSONObject(i); // 앞서 형변환한 JSONArray에서 JSONObject를 i개 꺼냄

                                    String UIDd = object.getString("UID");
                                    Log.e("All moim frag ", UIDd);

                                    if(UIDd.equals(UID)){
                                        String title = object.getString("Title"); // JSON Object에 저장했던 값 꺼내오기 ("키값"에 해당하는 "밸류값")
                                        String memo = object.getString("Memo");
                                        String date = object.getString("Date");
                                        String time = object.getString("Time");
                                        int members = object.getInt("Members");
                                        int currentmember = object.getInt("CurrentMember");
                                        String imagepath = object.getString("Imagepath");

                                        moimPost_fs item = new moimPost_fs(title, memo, date, time, members, UID, 0);

                                        mymadelist.add(item); // 본 액티비티(FeedActivity) Arraylist에 아이템 추가해줌
                                        adapter_made.notifyDataSetChanged(); // Data값 바뀌었다고 어댑터에게 알려줌
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });

        db.collection("Moim") // "Moim" 콜렉션에서
                .whereEqualTo("Bookmark."+ UID, true) // "북마크" 필드에 Hashmap<String, Boolean>에서 로그인한 UID값이 true인 모임글
                .get() // 가져옴
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                moimPost_fs moim = document.toObject(moimPost_fs.class);
                                myfavoritelist.add(moim);

                                String key = document.getId(); // 모임 아이템 키값 담은 어레이리스트
                                uidLists_favorite.add(key);

                                Log.e("Moim Fragment", key);

                                swipetoDeleteCallback = new SwipetoDeleteCallback(adapter_favorite, getActivity(), uidLists_favorite);
                                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipetoDeleteCallback);
                                itemTouchHelper.attachToRecyclerView(recyclerView_favorite);

                                adapter_favorite.notifyDataSetChanged(); // 어댑터에 바뀌었다고 알려줌
                            }
                        } else {
                        }
                    }
                });

        db.collection("Moim") // "Moim" 콜렉션에서
                .whereArrayContains("MemberUID", UID) // "멤버 UID" 필드(참여하는 멤버)에 로그인한 UID값이 포함된 모임글
                .get() // 가져옴 => 내가 참여할 모임 && 내가 참여했던 모임으로 나눠 담아야 함
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                moimPost_fs moim = document.toObject(moimPost_fs.class);

                                Calendar cal = new GregorianCalendar(); // 현재 날짜와 시간을 가져오기 위한 Calendar 인스턴스 선언

                                // 내가 만든 소모임은 위에 목록이 있으니까 빼고 필터링하겠음
                                if(!moim.UID.equals(UID)) { // moim 아이템의 작성자 UID와 로그인한 내 UID가 일치하지 않는 경우만 꺼내옴

                                    Date todaydate = new Date();

                                    if(moim.dateforalign.compareTo(todaydate) > 0){
                                        myfuturelist.add(moim); // 참여할 모임리스트에 추가

                                        String key = document.getId(); // 모임 아이템 키값 담은 어레이리스트
                                        uidLists_future.add(key); // 모임 고유키값 리스트에 키값 추가

                                        adapter_future.notifyDataSetChanged(); // 어댑터에 바뀌었다고 알려줌
                                    }else if(moim.dateforalign.compareTo(todaydate) <= 0){
                                        mypastlist.add(moim); // 참여했던 모임리스트에 추가

                                        String key = document.getId(); // 모임 아이템 키값 담은 어레이리스트
                                        uidLists_past.add(key); // 모임 고유키값 리스트에 키값 추가

                                        adapter_past.notifyDataSetChanged(); // 어댑터에 바뀌었다고 알려줌
                                    }

                                   /*

                                    // 연,월,일,시 중 1개라도 작은 게 있으면 과거모임 리스트로 넣어야 됨 (1차 필터링)
                                    if (moim.year < cal.get(Calendar.YEAR) || moim.month < cal.get(Calendar.MONTH) || moim.day < cal.get(Calendar.DAY_OF_MONTH)
                                            || moim.hour < cal.get(Calendar.HOUR_OF_DAY)) { // 오늘날짜보다 과거 일자인 경우
                                   moim.year > cal.get(Calendar.YEAR) || // 연도가 빠른 경우
                                   moim.year > cal.get(Calendar.YEAR) && moim.month > cal.get(Calendar.MONTH) || // 연도와 월이 빠른 경우
                                   moim.year > cal.get(Calendar.YEAR) && moim.month > cal.get(Calendar.MONTH) && moim.day > cal.get(Calendar.DAY_OF_MONTH) || // 연,월,일이 빠른 경우
                                   moim.year > cal.get(Calendar.YEAR) && moim.month > cal.get(Calendar.MONTH) && moim.day > cal.get(Calendar.DAY_OF_MONTH) && moim.hour > cal.get(Calendar.HOUR_OF_DAY) || // 연,월,일,시가 빠른 경우
                                   moim.year > cal.get(Calendar.YEAR) && moim.month > cal.get(Calendar.MONTH) && moim.day > cal.get(Calendar.DAY_OF_MONTH)  // 연,월,일,시,분이 빠른 경우
                                           && moim.hour > cal.get(Calendar.HOUR_OF_DAY) && moim.hour > cal.get(Calendar.HOUR_OF_DAY)){ // 현재 시간보다 미래의 날짜인 경우  */


                                        // 오늘 날짜보다 빠른 날은 1차 필터링 한 거에서 조건을 더 줌(2차 필터링)
                                     /*   if (moim.year > cal.get(Calendar.YEAR) || // 연도가 크거나
                                                moim.year == cal.get(Calendar.YEAR) && moim.month > cal.get(Calendar.MONTH) ||    // 연도가 같고 월이 크거나
                                                moim.year == cal.get(Calendar.YEAR) && moim.month == cal.get(Calendar.MONTH) && moim.day > cal.get(Calendar.DAY_OF_MONTH) ||    // 연,월 같고 일이 크거
                                                moim.year == cal.get(Calendar.YEAR) && moim.month == cal.get(Calendar.MONTH) && moim.day == cal.get(Calendar.DAY_OF_MONTH)
                                                        && moim.hour > cal.get(Calendar.HOUR_OF_DAY) ||  // 연,월,일 같고 시가 크거나
                                                moim.year == cal.get(Calendar.YEAR) && moim.month == cal.get(Calendar.MONTH) && moim.day == cal.get(Calendar.DAY_OF_MONTH)
                                                        && moim.hour > cal.get(Calendar.HOUR_OF_DAY) && moim.minute > cal.get(Calendar.MINUTE)) {  // 연,월,일,시 같고 분이 크거나

                                            myfuturelist.add(moim); // 참여할 모임리스트에 추가

                                            String key = document.getId(); // 모임 아이템 키값 담은 어레이리스트
                                            uidLists_future.add(key); // 모임 고유키값 리스트에 키값 추가

                                            adapter_future.notifyDataSetChanged(); // 어댑터에 바뀌었다고 알려줌

                                        } else { // 2차 필터링에 해당되지 않고 1차에서 걸러지는 것들은 모두 과거 리스트
                                            mypastlist.add(moim); // 참여했던 모임리스트에 추가

                                            String key = document.getId(); // 모임 아이템 키값 담은 어레이리스트
                                            uidLists_past.add(key); // 모임 고유키값 리스트에 키값 추가

                                            adapter_past.notifyDataSetChanged(); // 어댑터에 바뀌었다고 알려줌
                                        }



                                    }*/


                                }


                            }
                        } else {
                        }
                    }
                });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}

class SwipetoDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private ArrayList<String> uidList;
    private Adapter_forEvent Adapter; // 리사이클러뷰에 사용하는 어댑터
    private ColorDrawable swipeBackground = new ColorDrawable(Color.parseColor("#FF0000")); // 뒷 배경에 쓰이는 빨강색
    private Drawable deleteIcon; // 뒷 배경에 깔릴 아이콘

/*    private MyAdapter mAdapter;
    public SwipeToDeleteCallback(MyAdapter adapter) {
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mAdapter = adapter; } */

    public SwipetoDeleteCallback(Adapter_forEvent adapter, Context context, ArrayList<String> list) {
        super(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        Adapter = adapter;
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        uidList = list;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        int pos = viewHolder.getAdapterPosition();
        String key = uidList.get(pos);
//        Adapter.removeItem(viewHolder, key);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;
        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;

        if(dX > 0){
            swipeBackground.setBounds(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());
            deleteIcon.setBounds(itemView.getLeft() + iconMargin, itemView.getTop() + iconMargin,
                    itemView.getLeft() + iconMargin + deleteIcon.getIntrinsicWidth(), itemView.getBottom() - iconMargin);
        } else {
            swipeBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            deleteIcon.setBounds(itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth(), itemView.getTop() + iconMargin,
                    itemView.getRight() - iconMargin + deleteIcon.getIntrinsicWidth(), itemView.getBottom() - iconMargin);
        }
        swipeBackground.draw(c);
        c.save();

        if(dX > 0){
            c.clipRect(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());
        } else {
            c.clipRect(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        }
        deleteIcon.draw(c);
        c.restore();

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Set movement flags based on the layout manager
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            final int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        } else {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
    }
}