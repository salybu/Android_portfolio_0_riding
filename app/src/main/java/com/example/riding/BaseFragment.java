package com.example.riding;

import android.support.v4.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseFragment extends Fragment {

    public FirebaseAuth auth; // FirebaseAuth 변수선언 (로그인한 파베 아이디랑 연결하기 위해)
    public FirebaseFirestore db; // FirebaseFirestore 변수선언 (멤버 프로필 Firestore에 저장하기 위해)

    public String UID;
    public String email;

    public void UserAuthInfoSet(){
        auth = FirebaseAuth.getInstance(); // FirebaseAuth에 접속한 객체를 변수에 담음
        FirebaseUser user = auth.getCurrentUser(); // FirebaseAuth 객체에서 최근 접속한 FirebaseUser 객체 가져옴 (로그인한 User 정보 가져오기 위해)
        UID = user.getUid(); // 사용자 UID
        email = user.getEmail(); // 사용자 이메일
    }

    public void FirestoreDbSet(){
        db = FirebaseFirestore.getInstance(); // Firestore 객체 가져오기
    }

}