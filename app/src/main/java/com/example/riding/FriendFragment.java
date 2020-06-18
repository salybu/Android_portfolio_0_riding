package com.example.riding;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FriendFragment extends BaseFragment {

    private RecyclerView recyclerView; // 리사이클러뷰 변수 선언
    private ArrayList<Member> memberlist = new ArrayList<>(); // 멤버 아이템 담을 어레이리스트
    private ArrayList<String> uidLists = new ArrayList<>(); // UID List 다시 생각해보기

    public static FriendFragment newInstance() {
        return new FriendFragment();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_friend);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("onActivityCreated", " work? yes");

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity())); // 리사이클러뷰에 레이아웃 매니저 연결 - 리니어레이아웃매니저 새로 생성(Context는 this)

        memberlist = new ArrayList<>(); /// 멤버 아이템을 담을 Arraylist 객체 생성
        memberlist.clear(); // 멤버 프로필 아이템 담은 어레이리스트 매번 클리어

        uidLists = new ArrayList<>(); /// 모임 아이템 키 값을 가진 Arraylist 객체 생성

        final Adapter_forMember_Hor adapter = new Adapter_forMember_Hor(memberlist); // 어댑터 객체 생성
//        final Adapter_forMember adapter = new Adapter_forMember(memberlist, uidLists); // 어댑터 객체 생성
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결

        UserAuthInfoSet(); // FirebaseAuth 객체에서 UID, email값 가져오는 메소드
        FirestoreDbSet(); // FireStore DB 연결 메소드

        DocumentReference docRef = db.collection("Profile").document(UID); // Firestore의 콜렉션("Profile") Reference를 가져오기
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Member mem = documentSnapshot.toObject(Member.class);

                ArrayList<String> array = mem.FriendUID;

                if(array!=null) {
                    for (int i = 0; i < array.size(); i++) {
                        final String UIDd = array.get(i); // 1멤버 UID값 전부 다 꺼낸다

                        DocumentReference doc = db.collection("Profile").document(UIDd); // 2프로필 파이어베이스에서 일치하는 것들 Member 클래스로 꺼내온다
                        doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Member mem = documentSnapshot.toObject(Member.class);
                                memberlist.add(mem); // 멤버 리스트에 모든 멤버 정보를 넣음
                                uidLists.add(UIDd);

                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });

    }

}