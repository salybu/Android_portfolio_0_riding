package com.example.riding;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.content.Context.MODE_PRIVATE;

public class AllmoimFragment extends BaseFragment {

    private RecyclerView recyclerView; // 리사이클러뷰 변수 선언
    private ArrayList<moimPost_fs> moimlist_fs = new ArrayList<>(); // Firestore용 리스트 // 모임 아이템을 담을 Arraylist 객체 (Firestore DB용) 생성
// 없어도 됨    private ArrayList<Timestamp> uidLists = new ArrayList<>(); // UID List : 모임 키값은 스토리지 사진키값으로도 사용한 타임스탬프로..

    public static AllmoimFragment newInstance() { return new AllmoimFragment(); } // fragment안에 Fragment라서 새로 만들때 호출해줘야 됨

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_allmoim, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_moim);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("All moim Fragment", "onActivityCreated");

/*        database.getInstance().getReference().child("moimlist").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                moimlist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    moimPost moimitem = snapshot.getValue(moimPost.class);
                    String key = snapshot.getKey();
                    moimlist.add(moimitem);
                    uidLists.add(key);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });  */
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("All moim Fragment", "onResume");

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity())); // 리사이클러뷰에 레이아웃 매니저 연결 - 리니어레이아웃매니저 새로 생성(Context는 this)

        UserAuthInfoSet(); // FirebaseAuth 객체에서 UID, email값 가져오는 메소드
        FirestoreDbSet(); // FireStore DB 연결 메소드

        final SharedPreferences pref = this.getActivity().getSharedPreferences("Moims", MODE_PRIVATE); /// Moims 셰어드 불러옴

        final Adapter_forEvent adapter_fs = new Adapter_forEvent(moimlist_fs); // 어댑터 객체 생성 (Firestore DB용)
//        final Adapter_forEvent adapter_fs = new Adapter_forEvent(moimlist_fs, uidLists); // 어댑터 객체 생성 (Firestore DB용)
        recyclerView.setAdapter(adapter_fs); // 리사이클러뷰에 어댑터 연결

        moimlist_fs.clear(); // 모임 아이템 담은 어레이리스트
        adapter_fs.notifyDataSetChanged();

/*        // 셰어드에서 꺼내는 코드
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

                String title = object.getString("Title"); // JSON Object에 저장했던 값 꺼내오기 ("키값"에 해당하는 "밸류값")
                Log.e("All moim title ", title);

                String memo = object.getString("Memo");
                Log.e("All moim memo ", memo);

                String date = object.getString("Date");
                Log.e("All moim time ", date);

                String time = object.getString("Time");
                Log.e("All moim time ", time);

                int members = object.getInt("Members");
                Log.e("All moim time ", String.valueOf(members));

                int currentmember = object.getInt("CurrentMember");
                Log.e("All moim time ", String.valueOf(currentmember));

                String UID = object.getString("UID");
                Log.e("All moim frag ", UID);

                moimPost_fs item = new moimPost_fs(title, memo, date, time, members, UID, 0);

                moimlist_fs.add(item); // 본 액티비티(FeedActivity) Arraylist에 아이템 추가해줌
                Log.e("All moim Frag list ", moimlist_fs.get(0).Title);
                Log.e("All moim Frag list ", moimlist_fs.get(0).Date);

                adapter_fs.notifyDataSetChanged(); // Data값 바뀌었다고 어댑터에게 알려줌
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }   */

        // 파베 Firestore에서 가져오는 코드
        db.collection("Moim")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                moimPost_fs moim = document.toObject(moimPost_fs.class);

/*                                String key = document.getId(); // 모임 아이템 키값 담은 어레이리스트
                                moim.key = key;
                                Log.e("moim.key ", moim.key);   */

                                moimlist_fs.add(moim); // 이게 아래로 가는지 위로 가는지..
                                Comparator<moimPost_fs> noAsc = new Comparator<moimPost_fs>() {
                                    @Override
                                    public int compare(moimPost_fs item1, moimPost_fs item2) {
                                        int ret = 0;

                                        Log.e("compare ", "work ");

                                        if (item1.dateforalign.compareTo(item2.dateforalign) < 0){ // 아이템의 Date 변환값을 비교
                                            ret = -1;

                                        } else if(item1.dateforalign.compareTo(item2.dateforalign) == 0){
                                            ret = 0;

                                        } else if(item1.dateforalign.compareTo(item2.dateforalign) > 0){
                                            ret = 1;

                                        }

                                        return ret;
                                        // 위의 코드를 간단히 만드는 방법 // return (item1.getNo() - item2.getNo());
                                    }
                                };
                                Collections.sort(moimlist_fs, noAsc);   // -1 리턴의 경우 o1 < o2 (o1이 o2보다 작음) 먼저 옴

/*                                    for(int i = 0 ; i < moimlist_fs.size() ; i++){
                                        Timestamp t = moimlist_fs.get(i).timestamp;
                                        uidLists.add(t);
                                    } 헛짓 */

/*                                for(int i = 0; i < moimlist_fs.size() ; i++) {
                                    String keyy = moim.key;
                                    Log.e("string key ", keyy);
                                    uidLists.add(keyy);  }    */
/*                                String key = document.getId(); // 모임 아이템 키값 담은 어레이리스트
                                uidLists.add(key);    */

                                adapter_fs.notifyDataSetChanged(); // 어댑터에 바뀌었다고 알려줌  // Log.d("TAG", document.getId() + " => " + document.getData());
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

                                    String title = object.getString("Title"); // JSON Object에 저장했던 값 꺼내오기 ("키값"에 해당하는 "밸류값")
                                    String memo = object.getString("Memo");
                                    String date = object.getString("Date");
                                    String time = object.getString("Time");
                                    int members = object.getInt("Members");
                                    int currentmember = object.getInt("CurrentMember");
                                    String UID = object.getString("UID");
                                    moimPost_fs item = new moimPost_fs(title, memo, date, time, members, UID, 0);

                                    moimlist_fs.add(item); // 본 액티비티(FeedActivity) Arraylist에 아이템 추가해줌
                                    Log.e("All moim Frag list ", moimlist_fs.get(0).Title);
                                    Log.e("All moim Frag list ", moimlist_fs.get(0).Date);

                                    adapter_fs.notifyDataSetChanged(); // Data값 바뀌었다고 어댑터에게 알려줌
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                });
        adapter_fs.notifyDataSetChanged();

/*        db = FirebaseFirestore.getInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity())); // 리사이클러뷰에 레이아웃 매니저 연결 - 리니어레이아웃매니저 새로 생성(Context는 this)

        UserAuthInfoSet(); // FirebaseAuth 객체에서 UID, email값 가져오는 메소드

        moimlist = new ArrayList<>(); // 모임 아이템을 담을 Arraylist 객체 (realTime DB용) 생성
        moimlist_fs = new ArrayList<>(); // 모임 아이템을 담을 Arraylist 객체 (Firestore DB용) 생성

        uidLists = new ArrayList<>(); // 모임 아이템 키 값을 가진 Arraylist 객체 생성

        final Adapter_forEvent adapter = new Adapter_forEvent(moimlist, uidLists); // 어댑터 객체 생성 (realTime DB용)
        final Adapter_forEvent adapter_fs = new Adapter_forEvent(moimlist_fs, uidLists, 0); // 어댑터 객체 생성 (Firestore DB용)
        recyclerView.setAdapter(adapter_fs); // 리사이클러뷰에 어댑터 연결

        swipetoDeleteCallback = new SwipetoDeleteCallback(adapter_fs, getActivity());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipetoDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        moimlist_fs.clear(); // 모임 아이템 담은 어레이리스트

        SharedPreferences pref = getActivity().getSharedPreferences("Moims", MODE_PRIVATE); /// Moims 셰어드 불러옴
        String eventsArray = pref.getString(email,""); // Events 셰어드에서 Key값 "(회원)id"로 Value값 JSONArray(String형) 불러옴

        JSONArray eventsJSONArray = null; // value값 JSONArray를 받아 저장하기 위해 빈 JSONArray 변수[*] 선언
        try {
            eventsJSONArray = new JSONArray(eventsArray); // String형 JSONArray[eventsArray]를 JSONArray 객체로 형변환해 앞서 선언한 변수[*]에 저장
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assert eventsJSONArray != null; //// JSONArray가 null 값 아닐 때 (조건)
        for(int i=0 ; i <= (eventsJSONArray.length()-1) ; i++) { /// JSONArray에 담긴 모든 값을 꺼내기 위한 반복문. i <= JSONArray 마지막 인덱스까지 반복하도록 함
            try {
                JSONObject object = eventsJSONArray.getJSONObject(i); /// 앞서 형변환한 JSONArray에서 JSONObject를 i개 꺼냄

                Log.e("Feed JSON get", object.toString());

                String title = object.getString("Title"); // JSONObject에서 Key값 "Title"로 Value값을 가져와서 (String) title 변수에 담음
                String memo = object.getString("Memo"); // JSONObject에서 Key값 "Memo"로 Value값을 가져와서 (String) memo 변수에 담음
                String date = object.getString("Date");
                String time = object.getString("Time");
                int members = object.getInt("Members"); // int 값으로 넣고, 꺼냈음
                String imagepath = object.getString("Imagepath");
                String currentmember = object.getString("CurrentMember");
                String location = object.getString("Location");
                String uid = object.getString("UID");

                moimPost_fs item = new moimPost_fs(title, memo, date, time, members, imagepath, UID);
                Log.e("Feed JSON get path", item.toString());

                moimlist_fs.add(item); // 본 액티비티(FeedActivity) Arraylist에 아이템 추가해줌
                adapter_fs.notifyDataSetChanged(); // Data값 바뀌었다고 어댑터에게 알려줌
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }   */
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e("All moim Fragment", "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("All moim Fragment", "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("All moim Fragment", "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("All moim Fragment", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("All moim Fragment", "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("All moim Fragment", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("All moim Fragment", "onDestroy");
    }
}

/* comparator 무식하게 썼다.. ^^
                                            if (item1.year < item2.year){
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = -10;
                                            } else if (item1.year == item2.year) {
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = -9;
                                            }else if (item1.month < item2.month){
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = -8;
                                            }else if (item1.month == item2.month){
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = -7;
                                            }else  if (item1.day < item2.day){
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = -1;

                                            }else if (item1.day == item2.day){
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = 0;
                                            }

                                               else if (item1.ampm.equals("오전") && item2.ampm.equals("오후")){
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = -4;
                                            }else if (item1.ampm.equals(item2.ampm)){
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = -3;
                                            }else if (item1.hour < item2.hour){
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = -2;
                                            }else if (item1.hour == item2.hour){
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = -1;
                                            }else if (item1.minute < item2.minute){
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = 0;
                                            }else if (item1.minute == item2.minute){
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = 1;
                                            }else{
                                                Log.e("compare item ", String.valueOf(item1.year));
                                                Log.e("compare item ", String.valueOf(item1.month));
                                                Log.e("compare item ", String.valueOf(item1.day));

                                                ret = 2;
                                            }  */