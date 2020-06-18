package com.example.riding;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class Menu3Fragment extends BaseFragment implements View.OnClickListener {

    private TextView Nickname;
    private TextView Message;
    private TextView Alert;
    private Button logout;
    private ConstraintLayout profilelayout;
    private CircleImageView profileImage;

    private RecyclerView recyclerView_alarm;
    private ArrayList<selectAlarm> alarmlist = new ArrayList<>(); // Firestore용 리스트 (수락/거절 선택요청 알림)
    private ArrayList<String> keylist = new ArrayList<>(); // 알람 삭제하기 위해 key값 담을 리스트

    private RecyclerView recyclerView_alarm2;
    private ArrayList<selectAlarm> alarmacdlist = new ArrayList<>(); // Firestore용 리스트 (수락/거절 알림)
    private ArrayList<String> keyacdlist = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu3, container, false);

        Nickname = (TextView) view.findViewById(R.id.Nickname);
        Message = (TextView) view.findViewById(R.id.Message);
        Alert = (TextView) view.findViewById(R.id.Alert);
        logout = (Button) view.findViewById(R.id.btnLogout);

        profilelayout = (ConstraintLayout) view.findViewById(R.id.myprofilelayout);
        profileImage = (CircleImageView) view.findViewById(R.id.profileImage);

        recyclerView_alarm = (RecyclerView) view.findViewById(R.id.recyclerview_alarm);
        recyclerView_alarm2 = (RecyclerView) view.findViewById(R.id.recyclerview_alarm2);
        profilelayout.setOnClickListener(this);

        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                auth.signOut();

                SharedPreferences auto = getActivity().getSharedPreferences("Autologin", MODE_PRIVATE); /// 자동로그인 여부 체크하는 셰어드파일 불러옴
                SharedPreferences.Editor editorauto = auto.edit();
                editorauto.putString("ID", null); /// 자동로그인 셰어드 파일 ID값을 초기화함
                editorauto.putBoolean("Autologin", false); /// 키값 "Autologin"에 Value값 false로 초기화함 : 자동로그인 안함
                editorauto.commit(); // 수정완료

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();  // Fragment 관리를 위해 FragmentManager를 사용함. 할 수 있는 일 1findFragmentById 등으로 Fragment 가져오기
                // 2popBackStack()메소드로 Fragment를 Backstack에서 꺼내기 3BeginTransaction()으로 FragmentTransaction 가져오기 (add, remove, replace)
                fragmentManager.beginTransaction().remove(Menu3Fragment.this).commit(); // FragmentTransAction API를 사용하면 add, remove, replace 등의 작업을 할 수 있음
                // Transaction 작업 후 마지막에 반드시 commit()을 호출해야 함

                Intent intent2 = new Intent(getActivity(), LoginActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.myprofilelayout:
                Intent intent = new Intent(getActivity(), RealProfileActivity.class); /// 메인액티비티 띄우는 인텐트 생성
                startActivity(intent); //// 인텐트 담아서 액티비티 시작
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

//        MainActivity m = new MainActivity();
//        m.refresh(); // 받아온 정보 반영해서 새로고침하기 위한 메소드

        // 값 가져와서 세팅하는 건 셰어드에 저장된 것만 읽어올 거. 저장하는 건 셰어드 + 파베 모두에 할 거
        final SharedPreferences pref = this.getActivity().getSharedPreferences("Member Info", MODE_PRIVATE); // 회원정보가 있는 셰어드 "Member Info" 불러오기
        final SharedPreferences.Editor editor = pref.edit();  // Key값 "입력한 이메일" 셰어드에서 Value값 JSONObject 불러오기

        UserAuthInfoSet(); // FirebaseAuth 객체에서 UID, email값 가져오는 메소드
        FirestoreDbSet(); // FireStore DB 연결 메소드

        String thisObject = pref.getString(email,"");

        JSONObject thisinfo = null; // JSONObject를 받기위한 변수 선언
        try {
            thisinfo = new JSONObject(thisObject); // JSONObject 파싱

            String nicknamefromshared = thisinfo.getString("Nickname"); // 셰어드에서 닉네임 가져옴
            if(!nicknamefromshared.equals("")) { // 기본으로 세팅해준 값 ""이면 읽어오지 않기 (설정해 둔 hint값이 보일 수 있게)
                Nickname.setText(nicknamefromshared); // 셰어드에서 가져온 닉네임 setText 해줌
            }

            String message = thisinfo.getString("Message"); // 셰어드에서 상태메세지 가져옴
            if(!message.equals("")) {
                Message.setText(message); // 셰어드에서 가져온 메세지 setText 해줌
            }

            String imagepath = thisinfo.getString("Imagepath");
            if(!imagepath.equals("")){
                BaseActivity b = new BaseActivity(); // setImage 메소드 쓰려고 BasActivity 객체 생성
                b.setImage(imagepath, profileImage); // 상속받은 BaseActivity의 이미지 세팅 메소드 사용함
            }
        } catch (JSONException e) { //// JSONObject 파싱할 때 try, catch문
            e.printStackTrace();
        }

        recyclerView_alarm.setLayoutManager(new LinearLayoutManager(getActivity())); // 리사이클러뷰에 레이아웃 매니저 연결 - 리니어레이아웃매니저 새로 생성(Context는 this)
        recyclerView_alarm2.setLayoutManager(new LinearLayoutManager(getActivity()));

        final Adapter_forAlarm adapter_al = new Adapter_forAlarm(alarmlist, keylist); // 어댑터 객체 생성 (Firestore DB용)
        recyclerView_alarm.setAdapter(adapter_al); // 리사이클러뷰에 어댑터 연결

        final Adapter_forAlarmAccept adapter_ac = new Adapter_forAlarmAccept(alarmacdlist, keyacdlist); // 어댑터 객체 생성 (Firestore DB용)
        recyclerView_alarm2.setAdapter(adapter_ac); // 리사이클러뷰에 어댑터 연결

        alarmlist.clear(); // 모임 아이템 담은 어레이리스트
        alarmacdlist.clear();

        db.collection("Alarm") // 알람 컬렉션에서
                .whereEqualTo("receivedUID", UID).get() // 받는사람 UID가 로그인한 내 UID와 일치하는 알림정보 다 가져옴
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                selectAlarm alam = document.toObject(selectAlarm.class);

                                if(alam.Alarmtype == 1) { // 수락/거절 선택알림
                                    alarmlist.add(alam);

                                    String key = document.getId();
                                    keylist.add(key);

                                    adapter_al.notifyDataSetChanged(); // 어댑터에 바뀌었다고 알려줌

                                }else { // 수락알림 혹은 거절알림 (alam.Alarmtype == 2 or 3)
                                    alarmacdlist.add(alam);

                                    String key = document.getId();
                                    keyacdlist.add(key);

                                    adapter_ac.notifyDataSetChanged();
                                }
                            }
                        } else {
                        }
                    }
                });
//        Log.e("TAG", String.valueOf(alarmlist.get(0)));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e("Menu3Fragment", "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Menu3Fragment", "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("Menu3Fragment", "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("Menu3Fragment", "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("Menu3Fragment", "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("Menu3Fragment", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Menu3Fragment", "onDestroy");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("Menu3Fragment", "onActivityCreated");
    }

}

class selectAlarm {

    public String sendUID;
    public String receivedUID;
    public String moimKey;
    public int Alarmtype; // 알림종류 1,2,3으로 구분 (1수락,거절 선택요청 알림  2수락 전달알림  3거절 전달알림 등등)

    selectAlarm(){}

    selectAlarm(String senduid, String receiveuid, String moimkey, int i){
        this.sendUID = senduid;
        this.receivedUID = receiveuid;
        this.moimKey = moimkey;
        this.Alarmtype = i;
    }
}

class Adapter_forAlarm extends RecyclerView.Adapter<Adapter_forAlarm.AlarmViewHolder>{

    protected ArrayList<selectAlarm> alarmArraylist; /// 모임 아이템 어레이리스트 (Firestore DB용)
    private ArrayList<String> keylist; // 알람 삭제하기 위해 key값 담을 리스트
    public FirebaseFirestore db = FirebaseFirestore.getInstance();; // FirebaseFirestore 변수선언 (멤버 프로필 Firestore에 저장하기 위해)

    protected String memnickname; // 토스트 띄울 때 쓰려고 저장
    protected String addUID; // 모임에 추가할 참여요청한 사람의 UID 가져옴

    public class AlarmViewHolder extends RecyclerView.ViewHolder { // 1. 리스너 추가

        protected TextView nickname; // 뷰홀더 내부 뷰요소 변수로 선언
        protected TextView message;
        protected TextView moim;
        protected TextView message2;
        protected TextView message3;
        protected TextView message10;
        protected Button btnAccept;
        protected Button btnDeny;
        protected ImageView friendprofile;

        protected String UID;

        public final View itemView; // 아이템 클릭 이벤트 처리하기 위해 뷰(아이템뷰) 선언

        public AlarmViewHolder(View view) { //// 뷰홀더 생성자
            super(view);
            this.nickname = (TextView) view.findViewById(R.id.nickname); /// 뷰홀더 변수 연결
            this.message = (TextView) view.findViewById(R.id.message);
            this.moim = (TextView) view.findViewById(R.id.moim);
            this.message2 = (TextView) view.findViewById(R.id.message2);
            this.message3 = (TextView) view.findViewById(R.id.message3);
            this.message10 = (TextView) view.findViewById(R.id.message10);
            this.friendprofile = (ImageView) view.findViewById(R.id.friendprofile);

            this.btnAccept = (Button) view.findViewById(R.id.btnAccept);
            this.btnDeny = (Button) view.findViewById(R.id.btnDeny);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // FirebaseAuth 객체에서 최근 접속한 FirebaseUser 객체 가져옴 (로그인한 User 정보 가져오기 위해)
            UID = user.getUid();

            itemView = view; // 레이아웃 객체화(?) 아이템 클릭이벤트 처리하기 위해 선언
        }
    }

    public Adapter_forAlarm(ArrayList<selectAlarm> list, ArrayList<String> klist) { // 어댑터 생성자, 만든 액티비티의 Context값을 넣고, arraylist 값을 넣음
        alarmArraylist = list;
        keylist = klist;
    }

    @NonNull @Override
    public Adapter_forAlarm.AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.moimparticipation_item, viewGroup, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull Adapter_forAlarm.AlarmViewHolder viewHolder, final int position) {

        // 알림정보를 꺼내서 뷰홀더에 세팅 - 보낸멤버 UID로 정보 꺼내기
        db.collection("Profile").document(alarmArraylist.get(position).sendUID) // 알람보낸 멤버 UID에 해당하는 프로필 문서를 가져옴
            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Member mem = documentSnapshot.toObject(Member.class); // 가져온 문서를 멤버 클래스로 형변환해 꺼내옴
                viewHolder.nickname.setText(mem.Nickname); // 가져온 멤버 닉네임을 뷰홀더 닉네임에 세팅함

                memnickname = mem.Nickname; // 멤버 닉네임 변수에 저장 (토스트 띄울 때 쓰려고 저장해둠)
                addUID = mem.UID; // 멤버 UID 변수에 저장

                // 이미지 세팅하기 위해 Firebase Storage에 접근해야 함
                StorageReference sr = FirebaseStorage.getInstance().getReference().child("profile/"+ mem.UID);
                try {
                    final File file = File.createTempFile("profile", "jpg"); // 임시파일 생성
                    // createTempFile(접두사 문자열, 접미사 문자열(파일 확장자), 파일 만들 디렉토리(기본일 경우 null))
                    sr.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() { // FileDownloadTask.TaskSnapshot
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Success Case
                            Bitmap bitmapImage = BitmapFactory.decodeFile(file.getPath()); // 파일의 경로를 가져와서 비트맵으로 변환해줌
                            viewHolder.friendprofile.setImageBitmap(bitmapImage);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        // 알림정보를 꺼내서 뷰홀더에 세팅 - moimKey로 정보 꺼내기
        db.collection("Moim").document(alarmArraylist.get(position).moimKey) // 알람보낸 모임 키에 해당하는 모임문서를 가져옴
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                moimPost_fs mom = documentSnapshot.toObject(moimPost_fs.class); // 가져온 문서를 모임 클래스로 형변환해 꺼내옴
                viewHolder.moim.setText(mom.Title); // 가져온 모임이름을 뷰홀더 모임이름에 세팅함
            }
        });

        // 수락했을 때
        viewHolder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Toast.makeText(context, memnickname + "님의 참여를 수락했습니다.", Toast.LENGTH_SHORT).show();

                // 파베에서 바꾸는 코드. 트랜잭션 이용
                final DocumentReference docRef = db.collection("Moim").document(alarmArraylist.get(position).moimKey); // "Moim" 콜렉션에서 문서구분 키값에 해당하는 문서 가져오기
                Log.e("Transaction postkey ", alarmArraylist.get(position).moimKey);
                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(docRef);

                        // 참여멤버 UID 추가하기 위해 MemberUID 어레이 가져옴
                        ArrayList newarray = (ArrayList) snapshot.get("MemberUID");

                            // 어레이리스트에 멤버 UID 추가
                            newarray.add(addUID);
                            transaction.update(docRef, "MemberUID", newarray);

                            // 현재 인원수 추가
                            int newCurrentMember = ((Long) snapshot.get("CurrentMember")).intValue() + 1;
                            transaction.update(docRef, "CurrentMember", newCurrentMember);

                        return null;
                    }
                });

                // 참여수락했다고 알림 보내야됨 // 보내는 사람, 받는 사람이 바뀌니까 바꿔 넣어줌 // 수락알림이니까 마지막 생성자값 2
                selectAlarm alarm = new selectAlarm(alarmArraylist.get(position).receivedUID, alarmArraylist.get(position).sendUID, alarmArraylist.get(position).moimKey,2);
                db.collection("Alarm").add(alarm); // 알림 추가

                // 알림목록에서 삭제 (본 수락/거절요청 알림) - 모든 코드 다 수행한 후에 마지막으로 삭제해야 됨!
                db.collection("Alarm").document(keylist.get(position)).delete(); // 파이어베이스에서 삭제를 먼저하고 로컬리스트에서 삭제해야 함
                alarmArraylist.remove(position); // 알람리스트에서 삭제
                keylist.remove(position); // 키리스트에서 삭제
                notifyDataSetChanged(); // 어댑터에 알림
            }
        });

        viewHolder.btnDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 참여거절했다고 알림 보내야됨 // 보내는 사람, 받는 사람이 바뀌니까 바꿔 넣어줌 // 거절알림이니까 마지막 생성자값 3
                selectAlarm alarm = new selectAlarm(alarmArraylist.get(position).receivedUID, alarmArraylist.get(position).sendUID, alarmArraylist.get(position).moimKey,3);
                db.collection("Alarm").add(alarm); // 알림 추가

                // 알림목록에서 삭제 (본 수락/거절요청 알림) - 모든 코드 다 수행한 후에 마지막으로 삭제해야 됨!
                db.collection("Alarm").document(keylist.get(position)).delete(); // 파이어베이스에서 삭제를 먼저하고 로컬리스트에서 삭제해야 함
                alarmArraylist.remove(position); // 알람리스트에서 삭제
                keylist.remove(position); // 키리스트에서 삭제
                notifyDataSetChanged(); // 어댑터에 알림
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context con = v.getContext();
                // 커스텀 다이얼로그를 생성. 사용자가 만든 클래스이다.
                MemberProfileDialog dialog = new MemberProfileDialog(con); // 친구 프로필 정보 볼 수 있는 다이얼로그 띄움
                dialog.callFunction(alarmArraylist.get(position).sendUID, alarmArraylist.get(position).receivedUID); // 친구 UID, 내 UID 넣는 거
            }
        });
    }

    @Override
    public int getItemCount() {
        return alarmArraylist.size();
    }
}