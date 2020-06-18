package com.example.riding;

import android.Manifest;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MoimInfoBasicActivity extends BaseActivity implements OnMapReadyCallback {

    private TextView Title;
    private TextView Date;
    private TextView Time;
    private TextView Location;
    private TextView Content;
    private TextView CurrentMember;
    private TextView Slash;
    private TextView Members;
    private TextView Myeong;
    private TextView Member;

    private ImageView GroupImage;
    private ImageView map_pin;
    private Button btnRegister;
    private Button btnRegisterCancel;
    private Button btnEdit;
    private Button btnDelete;

    private String postkey; // 대응하는 게시글을 가져오기 위한 키값
    private int position;
    private int cancel; // 소모임 참여취소 여부 결정하는 int값 (다이얼로그에서 받아옴)

    private String moimUID; // 모임글 작성자 UID: 알람보낼 때 사용하기 위해 변수로 선언

    private RecyclerView recyclerView; // 리사이클러뷰 변수 선언
    private ArrayList<Member> memberlist = new ArrayList<>();
    private ArrayList<String> uidLists = new ArrayList<>();

    public GoogleMap mGoogleMap = null; // 구글맵 참조변수
    private Marker currentMarker = null; // 현위치 마커 변수

    private String TAG = "This is MoimWriteActivity";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;

    public final int ThisMember_CANCELED = 0; // 스레드에서 메세지 보낼 때 메시지 코드

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됨
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    private int DEFAULT_ZOOM_LEVEL = 13;

    private double lat; // 받아온 위도값 (프로필에서 넘어온 주소에 마커 추가하기 위해)
    private double lon; // 경도값

    private View mLayout; // Snackbar 사용하기 위해서는 View가 필요합니다.  // (참고로 Toast에서는 Context가 필요했습니다.)

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    android.location.Location mCurrentLocatiion; // 현위치
    LatLng currentPosition; // 현위치 위도, 경도

    List<Marker> previous_marker = null; // 추가할 마커 리스트 정보

    Thread_Cancel thread; // 혹시나 참여취소 안하면 Destroy에서 멈춰줘야 되니까 변수로 선언
    Adapter_forMember adapter; // 참여인원 바뀔 때 바로 적용되게(?) 변수선언

    private FusedLocationProviderClient mFusedLocationClient; // 현재위치 받아올 FusedLocationProviderClient 객체
    private LocationRequest locationRequest; // 위치 업데이트에 대한 서비스 품질을 요청하는데 사용됨(?)
    private Location location;

    private final int GALLERY_CODE = 1112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moim_info_basic);
        Log.e("onCreate ", "ok");

        Title = (TextView) findViewById(R.id.Title);
        Date = (TextView) findViewById(R.id.Date);
        Time = (TextView) findViewById(R.id.Time);
        Location = (TextView) findViewById(R.id.Location);
        Content = (TextView) findViewById(R.id.Content);
        CurrentMember = (TextView) findViewById(R.id.CurrentMember);
        Slash = (TextView) findViewById(R.id.Slash);
        Members = (TextView) findViewById(R.id.Members);
        Myeong = (TextView) findViewById(R.id.Myeong);
        Member = (TextView) findViewById(R.id.Member);

        btnRegister = (Button) findViewById(R.id.btnRegister); // 소모임에 [참여하기] 버튼
        btnRegisterCancel = (Button) findViewById(R.id.btnRegisterCancel);
        btnEdit = (Button) findViewById(R.id.btnEdit); // 로그인한 UID가 작성자 UID랑 일치할 때만 버튼이 보이게 할 거임
        btnDelete = (Button) findViewById(R.id.btnDelete);

        GroupImage = (ImageView) findViewById(R.id.GroupImage);
        map_pin = (ImageView) findViewById(R.id.map_pin);

        UserAuthInfoSet();

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // fusedlocationproviderclient메소드 가져옴

        Intent intent = getIntent(); // (AllmoimFragment에서 넘어올 때) moimAdapter에서 아이템 onClick할 때 인텐트를 넘겼음
//        postkey = intent.getStringExtra("key"); // keyArraylist의 key값을 받아옴 (파이어베이스에서 받아올 때)
        final String title = intent.getStringExtra("Title");
        final String date = intent.getStringExtra("Date");
        final String location = intent.getStringExtra("Location");
//        position = intent.getIntExtra("Position", 0);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_member);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false));  // 리사이클러뷰에 레이아웃 매니저 연결 - 리니어레이아웃매니저 새로 생성(Context는 this)

        memberlist.clear(); // 멤버 프로필 아이템 담은 어레이리스트 매번 클리어

        adapter = new Adapter_forMember(memberlist, uidLists); // 액티비티 내에서 위의 ArrayList 담을 어댑터 객체 생성
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결

        thread = new Thread_Cancel();
        thread.start();

/*        database.getInstance().getReference().child("moimlist").child(postkey).addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    moimPost moimitem = dataSnapshot.getValue(moimPost.class);

                    String allmembers = Integer.toString(moimitem.Members); // 전체 멤버수

                    Title.setText(moimitem.Title);
                    Date.setText(moimitem.Date);
                    Time.setText(moimitem.Time);
                    Content.setText(moimitem.Memo);
                    setImage(moimitem.Imagepath, GroupImage);
                    Members.setText(allmembers);
                    // 이미지 세팅..
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });   Realtime DB 에서 꺼내올 때 쓰는 코드 */

        FirestoreDbSet(); // FireStore 객체 가져오는 메소드

//        DocumentReference docRef = db.collection("Moim").document(postkey);
        db.collection("Moim")
                .whereEqualTo("Title", title)
                .whereEqualTo("Date", date)
                .whereEqualTo("Location", location)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                moimPost_fs moimitem = document.toObject(moimPost_fs.class);

                                postkey = document.getId();
                                ArrayList<String> array = moimitem.MemberUID;

                                for(int i = 0; i < array.size(); i++) { // 3
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

                                if(moimitem.CurrentMember == moimitem.Members){ // (현재 참여한 멤버수 == 모임작성 시 제한을 둔 멤버수)이면
                                    btnRegister.setEnabled(false); // 버튼 막기
                                }

                                Title.setText(moimitem.Title);
                                Date.setText(moimitem.Date);
                                Time.setText(moimitem.Time);
                                Content.setText(moimitem.Memo);
                                Location.setText(moimitem.Location);

                                // 이미지 세팅은 스토리지에서 꺼내와서 한다
                                StorageReference sr = FirebaseStorage.getInstance().getReference().child("Moim/"+ moimitem.timestamp);
                                try {
                                    final File file = File.createTempFile("Moim", "jpg"); // 임시파일 생성
                                    // createTempFile(접두사 문자열, 접미사 문자열(파일 확장자), 파일 만들 디렉토리(기본일 경우 null))
                                    sr.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() { // FileDownloadTask.TaskSnapshot
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            // Success Case
                                            Bitmap bitmapImage = BitmapFactory.decodeFile(file.getPath()); // 파일의 경로를 가져와서 비트맵으로 변환해줌
                                            GroupImage.setImageBitmap(bitmapImage);
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

    //                            setImage(moimitem.Imagepath, GroupImage);  // 이미지 세팅, 이미지값은 무조건 기본값을 줬음

                                String allmembers = String.valueOf(moimitem.Members);
                                Members.setText(allmembers);
                                CurrentMember.setText(String.valueOf(moimitem.CurrentMember));

                                lat = moimitem.lat;
                                lon = moimitem.lon;

                                setMoimAddress(Location.getText().toString(), lat, lon, mGoogleMap); // 입력한 값을 가지고 지도에 세팅함

                                moimUID = moimitem.UID; // 모임글 작성자 UID(moimitem.UID)를 moimUID로 따로 변수에 저장함
                                if(moimUID.equals(UID)){ // 모임글 작성자 UID가 로그인한 UID와 같으면
                                    btnEdit.setVisibility(View.VISIBLE); // 수정, 삭제버튼 Visible로 세팅
                                    btnDelete.setVisibility(View.VISIBLE);
                                    btnRegister.setVisibility(View.GONE); // 참여하기 버튼은 GONE 보이지 않게 처리

                                } else if(moimitem.MemberUID.contains(UID)){ // 모임의 멤버 UID 중에 로그인한 UID가 포함되어 있으면
                                    btnRegister.setVisibility(View.INVISIBLE); // 참여하기 버튼은 안보이고
                                    btnRegisterCancel.setVisibility(View.VISIBLE); // 참여취소 버튼이 보이게 처리
                                }


                                // 참여요청 메세지 이미 보냈으면 참여하기 버튼 누를 수 없게 막기 (알람 보낸게 있으면 버튼 막기)
                                db.collection("Alarm")
                                        .whereEqualTo("sendUID", UID) // 보낸 UID는 지금 로그인한 내 UID
                                        .whereEqualTo("receivedUID", moimUID) // 받는 UID는 모임작성자 UID인 알람 문서가 있으면 버튼 막기
                                        .whereEqualTo("Alarmtype",1) // 참여요청 알람인 경우여야 함. 참여수락(Alarmtype 2)/참여거절(Alarmtype 3) 알림 내가 보낸거면 안됨
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        selectAlarm alam = document.toObject(selectAlarm.class);

                                                        if (alam !=null){ // sendUID, receivedUID가 일치하는 document, selectAlarm 클래스가 이미 있으면
                                                            btnRegister.setEnabled(false); // 버튼막기
                                                            Toast.makeText(MoimInfoBasicActivity.this, "모임 작성자에게 이미 참여요청 메세지를 보냈습니다.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }
                        } else {
                        }
                    }
                });

/*        DocumentReference docRef = db.collection("Moim").document(postkey);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                moimPost_fs moimitem = documentSnapshot.toObject(moimPost_fs.class);

                ArrayList<String> array = moimitem.MemberUID;

                for(int i = 0; i < array.size(); i++) { // 3
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

                Title.setText(moimitem.Title);
                Date.setText(moimitem.Date);
                Time.setText(moimitem.Time);
                Content.setText(moimitem.Memo);
                Location.setText(moimitem.Location);
                setImage(moimitem.Imagepath, GroupImage);  // 이미지 세팅, 이미지값은 무조건 기본값을 줬음

                String allmembers = String.valueOf(moimitem.Members);
                Members.setText(allmembers);
                CurrentMember.setText(String.valueOf(moimitem.CurrentMember));

                lat = moimitem.lat;
                lon = moimitem.lon;

                setMoimAddress(Location.getText().toString(), lat, lon, mGoogleMap); // 입력한 값을 가지고 지도에 세팅함

                String moimUID = moimitem.UID;
                if(moimUID.equals(UID)){
                    btnEdit.setVisibility(View.VISIBLE);
                    btnDelete.setVisibility(View.VISIBLE);
                    btnRegister.setVisibility(View.GONE);
                } else if(moimitem.MemberUID.contains(UID)){
                    btnRegister.setVisibility(View.INVISIBLE);
                    btnRegisterCancel.setVisibility(View.VISIBLE);
                }
            }
        });  */


/*        // 셰어드에서 꺼내는 코드 (모임에 참여한 멤버 리사이클러뷰)
        SharedPreferences pref = getSharedPreferences("Moims", MODE_PRIVATE); // Events 셰어드 불러옴
        SharedPreferences.Editor editor = pref.edit(); // 셰어드 파일을 수정할 에디터 불러옴
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

                if(i == position) { // 어댑터에서 받아온 position값에 일치하는 아이템 읽어오기
                    String memberUID = object.getString("MemberUID"); // 멤버 UID String 연속값 읽어오기
                    String[] memberUIDs = memberUID.split(","); // "," 기준으로 멤버 이메일 끊어읽기

                    for(int ii =0; ii < memberUIDs.length; ii++){
                        SharedPreferences pref2 = getSharedPreferences("Member Info", MODE_PRIVATE); // Member Info 셰어드 불러옴
                        SharedPreferences.Editor editor2 = pref2.edit(); // 셰어드 파일을 수정할 에디터 불러옴

                        String name = memberUIDs[ii];
                        String objec1 = pref2.getString(name, "");

                        JSONObject thisinfo = null;
                        thisinfo = new JSONObject(objec1);

                        String nname = thisinfo.getString("Nickname");
                        String mess = thisinfo.getString("Message");
                        String img = thisinfo.getString("Imagepath");

                        Member member = new Member(nname, mess, img);
                        memberlist.add(member);
                    }
/*                    eventsJSONArray.put(object);
                    editor.putString("Entire", eventsJSONArray.toString());
                    editor.commit();        */
/*                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 셰어드에서 꺼내는 코드 (모임 리사이클러뷰)
        SharedPreferences pref3 = getSharedPreferences("Moims", MODE_PRIVATE); /// Events 셰어드 불러옴
        String eventsArray3 = pref3.getString("Entire",""); // Moims 셰어드에서 Key값 "Entire"로(전체 모임아이템) Value값 JSONArray(String형) 불러옴

        JSONArray eventsJSONArray3 = null; // value값 JSONArray를 받아 저장하기 위해 빈 JSONArray 변수[*] 선언
        try {
            eventsJSONArray3 = new JSONArray(eventsArray3); // String형 JSONArray[eventsArray]를 JSONArray 객체로 형변환해 앞서 선언한 변수[*]에 저장
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assert eventsJSONArray3 != null; // JSONArray가 null 값 아닐 때 (조건)
        for(int i=0 ; i <= (eventsJSONArray3.length()-1) ; i++) { // JSONArray에 담긴 모든 값을 꺼내기 위한 반복문. i <= JSONArray 마지막 인덱스까지 반복하도록 함
            try {
                JSONObject object = eventsJSONArray3.getJSONObject(i); // 앞서 형변환한 JSONArray에서 JSONObject를 i개 꺼냄

                if(i == position) { // 어댑터에서 받아온 position값에 일치하는 아이템 읽어오기
                    String title = object.getString("Title"); // JSON Object에 저장했던 값 꺼내오기 ("키값"에 해당하는 "밸류값")
                    Title.setText(title);
                    Log.e("All moim title ", title);

                    String memo = object.getString("Memo");
                    Content.setText(memo);
                    Log.e("All moim memo ", memo);

                    String date = object.getString("Date");
                    Date.setText(date);
                    Log.e("All moim time ", date);

                    String time = object.getString("Time");
                    Time.setText(time);
                    Log.e("All moim time ", time);

                    int members = object.getInt("Members");
                    Members.setText(String.valueOf(members));
                    Log.e("All moim time ", String.valueOf(members));

                    int currentmember = object.getInt("CurrentMember");
                    Log.e("All moim time ", String.valueOf(currentmember));

                    String imagepath = object.getString("Imagepath");
                    setImage(imagepath, GroupImage);
                    Log.e("All moim time ", imagepath);

                    String UID = object.getString("UID");
                    Log.e("All moim frag ", UID);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }   */

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 인텐트를 보낸다
                Intent intent = new Intent(MoimInfoBasicActivity.this, EditMoimActivity.class);
                intent.putExtra("key", postkey); // 키 값을 담아 보냄
                startActivity(intent);
                finish();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                  db.collection("Moim").document(postkey)
/*                db.collection("Moim")
                        .whereEqualTo("Title", title)
                        .whereEqualTo("Date", date)
                        .whereEqualTo("Location", location) */
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Error deleting document", e);
                            }
                        });

                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

/*                // 정말 참여하시겠습니까? 네 누르면 참여하기에 리스트 올라감
                // 커스텀 다이얼로그를 생성. 사용자가 만든 클래스이다.
                ParticipationRequestDialog dialog = new ParticipationRequestDialog(MoimInfoBasicActivity.this);
                // 커스텀 다이얼로그를 호출한다.  ( // 커스텀 다이얼로그의 결과를 출력할 TextView를 매개변수로 같이 넘겨준다. dialog.callFunction(main_label); )
                dialog.callFunction(position, email, postkey, UID);    */

                // 수락/거절 알림 보내는 코드
                selectAlarm alarm = new selectAlarm(UID, moimUID, postkey, 1);
                db.collection("Alarm").add(alarm); // 알림 추가
                Toast.makeText(MoimInfoBasicActivity.this, "모임 작성자에게 참여요청 메세지를 보냈습니다.", Toast.LENGTH_SHORT).show();
                sendGson();
                btnRegister.setEnabled(false);
            }
        });

        btnRegisterCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

/*                // 커스텀 다이얼로그를 생성. 사용자가 만든 클래스이다.
                ParticipationCancelDialog dialog = new ParticipationCancelDialog(MoimInfoBasicActivity.this);
                // 커스텀 다이얼로그를 호출한다.  ( // 커스텀 다이얼로그의 결과를 출력할 TextView를 매개변수로 같이 넘겨준다. dialog.callFunction(main_label); )

                cancel = dialog.callFunction(position, email, postkey, UID, adapter); // 로그인한 사용자의 Email, 모임의 키값, 로그인한 사용자의 UID
                // 모임 멤버목록에서 참여인원 빠졌으니까 그걸 어댑터에 알려주기 위해 생성자에 넣어줌
                // 취소 여부를 받아옴 (참여취소 1, 참여취소 안함0)

                if(cancel ==1){ // 참여취소했으면
//                    adapter.notifyDataSetChanged(); // 모임 멤버목록에서 참여인원 빠졌으니까 그걸 어댑터에 알려줌
                    Toast.makeText(MoimInfoBasicActivity.this, "모임참여를 취소했습니다.", Toast.LENGTH_SHORT).show();
                    btnRegisterCancel.setEnabled(false);

                }else if(cancel==0){ // 참여취소 안했으면 아무일도 안일어남
                }
                cancel = 1; */

                // 파베에서 바꾸는 코드. 트랜잭션 이용
                final DocumentReference docRef = db.collection("Moim").document(postkey); // "Moim" 콜렉션에서 문서구분 키값에 해당하는 문서 가져오기
                Log.e("Transaction postkey ", postkey);
                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(docRef);

                        // 참여멤버 UID 삭제하기 위해 MemberUID 어레이 가져옴
                        ArrayList newarray = (ArrayList) snapshot.get("MemberUID");
                        newarray.remove(UID); // 어레이에서 내 UID값 삭제
                        transaction.update(docRef, "MemberUID", newarray); // 트랜잭션 이용해서 반영

                        // 현재 인원수 줄이기
                        int newCurrentMember = ((Long) snapshot.get("CurrentMember")).intValue() - 1;
                        transaction.update(docRef, "CurrentMember", newCurrentMember);

                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");

                        adapter.notifyDataSetChanged();
                        Intent intent = new Intent(MoimInfoBasicActivity.this, MoimInfoBasicActivity.class);
/*                String value = KeyArraylist.get(position); // 키 값을 가져옴
                intent.putExtra("key", value);   */
                        intent.putExtra("Title", title);
                        intent.putExtra("Date", date);
                        intent.putExtra("Location", location);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                    }
                });
                adapter.notifyDataSetChanged();
            }
        });
        adapter.notifyDataSetChanged();
    }

    private void sendGson() {
        FirebaseFirestore.getInstance().collection("Profile").document(moimUID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        com.example.riding.Member mem = documentSnapshot.toObject(com.example.riding.Member.class);

                        SendNotification.sendNotification(mem.Token, mem.Nickname, mem.Nickname+"님이 모임에 참여하고자 합니다. 참여요청 메세지가 전달되었습니다.");
        //                SendNotification.sendNotification(mPushToken, profile.getNickName(), mFcmMessage);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("onStart ", "ok");
    };

    @Override
    public void onResume() {
        super.onResume();
        Log.e("onResume ", "ok");
    };

    @Override
    public void onPause() {
        super.onPause();
        Log.e("onPause ", "ok");
    };

    @Override
    public void onStop() {
        super.onStop();
        Log.e("onPause ", "ok");
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        thread.interrupt();
        Log.e("onPause ", "ok");
    };

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case ThisMember_CANCELED:
                    adapter.notifyDataSetChanged();
                    break;

                default:
                    break;
            }
        }
    };

    class Thread_Cancel extends Thread {
        int i = 0;

        @Override
        public void run() {
            super.run();
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    i++;

                    Message message = handler.obtainMessage(); // 메시지 얻어오기
                    message.what = ThisMember_CANCELED; // 메시지 ID 설정

                    if(cancel==1) { // 로그인한 유저가 소모임에 참여를 취소했을 때 (다이얼로그에서 취소값(1) 받아올 때)
                        handler.sendMessage(message);
                    }
                    sleep(100); // 0.1초 씩 딜레이 부여
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("onRestart ", "ok");
/*
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_member);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false));  // 리사이클러뷰에 레이아웃 매니저 연결 - 리니어레이아웃매니저 새로 생성(Context는 this)

        memberlist = new ArrayList<>(); // 멤버 프로필 아이템을 담을 Arraylist 객체 생성
        memberlist.clear(); // 멤버 프로필 아이템 담은 어레이리스트 매번 클리어

        final Adapter_forMember adapter = new Adapter_forMember(memberlist); // 액티비티 내에서 위의 ArrayList 담을 어댑터 객체 생성
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결

/*        database.getInstance().getReference().child("moimlist").child(postkey).addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    moimPost moimitem = dataSnapshot.getValue(moimPost.class);

                    String allmembers = Integer.toString(moimitem.Members); // 전체 멤버수

                    Title.setText(moimitem.Title);
                    Date.setText(moimitem.Date);
                    Time.setText(moimitem.Time);
                    Content.setText(moimitem.Memo);
                    setImage(moimitem.Imagepath, GroupImage);
                    Members.setText(allmembers);
                    // 이미지 세팅..
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });   Realtime DB 에서 꺼내올 때 쓰는 코드 */
/*
        FirestoreDbSet(); // FireStore 객체 가져오는 메소드

        DocumentReference docRef = db.collection("Moim").document(postkey);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                moimPost_fs moimitem = documentSnapshot.toObject(moimPost_fs.class);

                ArrayList<String> array = moimitem.MemberUID;

                for(int i = 0; i < array.size(); i++) {
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

                Title.setText(moimitem.Title);
                Date.setText(moimitem.Date);
                Time.setText(moimitem.Time);
                Content.setText(moimitem.Memo);
                Location.setText(moimitem.Location);
                setImage(moimitem.Imagepath, GroupImage);  // 이미지 세팅, 이미지값은 무조건 기본값을 줬음

                // 모임아이템 타임스탬프로 이미지를 저장했으니까 그걸로 불러옴
 //               FirebaseStorage.getInstance().getReference().child("Moim/"+moimitem.timestamp)


                String allmembers = String.valueOf(moimitem.Members);
                Members.setText(allmembers);

                lat = moimitem.lat;
                lon = moimitem.lon;

                setMoimAddress(Location.getText().toString(), lat, lon, mGoogleMap); // 입력한 값을 가지고 지도에 세팅함

                String moimUID = moimitem.UID;

                if(moimUID.equals(UID)){
                        btnEdit.setVisibility(View.VISIBLE);
                        btnDelete.setVisibility(View.VISIBLE);
                        btnRegister.setVisibility(View.GONE);
                } else if(moimitem.MemberUID.contains(UID)){
                        btnRegister.setVisibility(View.INVISIBLE);
                        btnRegisterCancel.setVisibility(View.VISIBLE);
                }
            }
        });
   */
    }

    @Override
    public void onMapReady(GoogleMap map) { // 처음에 지도를 준비하는 코드
        mGoogleMap = map;

        // 런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기 전에 // 지도의 초기위치를 서울로 이동
//        setDefaultLocation();

        Log.e("onMapReady lat ", String.valueOf(lat));
        Log.e("onMapReady lon ", String.valueOf(lon));

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)

//            startLocationUpdates(); // 3. 위치 업데이트 시작

        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 3-3. 사용자에게 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( MoimInfoBasicActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                    }
                }).show();

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다. // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d( TAG, "onMapClick :");
            }
        });
    }


    public void setDefaultLocation() {

        // 디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    private void startLocationUpdates() { // 위치 업데이트

        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();

        }else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED ) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mGoogleMap.setMyLocationEnabled(true);
        }
    }

    // 여기부터는 런타임 퍼미션 처리를 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }
        return false;
    }

    // LocationCallback 클래스 => FusedLocationProvider 장치 위치가 변경되었거나 더 이상 바꿀 수 없을 때 불려오는 클래스. requestLocationUpdates() 메소드를

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) { // FusedLocationProvider를 통해 지리적 위치결과를 가져오는 데이터 클래스: LocationResult
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations(); //

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition = new LatLng(location.getLatitude(), location.getLongitude()); // 현위치는 location의 위도, 경도값을 넣은 새로운 위치값

/*                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());  원래 마커 클릭했을 때 타이틀:주소, 내용:위,경도 */

                String markerTitle = "현위치";
                String markerSnippet = getCurrentAddress(currentPosition);

                Log.d(TAG, "onLocationResult : " + markerSnippet);

                // 현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocatiion = location;

                mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        String title = marker.getTitle();
                        String address = marker.getSnippet();
                        return false;
                    }
                });
            }
        }
    };

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    public String getCurrentAddress(LatLng latlng) {

        // 지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }


    /* ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다. */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다. 2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                }else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() { // 위치서비스 설정을 했는지 여부를 표시하는 다이얼로그 만드는 메소드

        AlertDialog.Builder builder = new AlertDialog.Builder(MoimInfoBasicActivity.this); // 경고 다이얼로그 빌더를 만듬
        builder.setTitle("위치 서비스 비활성화"); // 빌더에 다이얼로그 제목을 띄움
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?"); // 빌더에 다이얼로그 메세지를 띄움
        builder.setCancelable(true); // 대화상자를 취소할 수 있는지 여부를 표시함
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() { // Yes 버튼을 누를 때 어떻게 할지 표시함
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS); // 위치 서비스 (설정페이지의 세부메뉴를 띄우기 위한 인텐트 코드목록)
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show(); // 빌더를 만들고 // 띄움
    }

    public boolean checkLocationServicesStatus() {
        // LocationManager는 앱이 안드로이드 위치 서비스에 액세스할 수 있는 기본 클래스임. 다른 시스템 서비스와 마찬가지로 getSystemService() 메소드 호출에서 참조를 얻을 수 있음
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); // 시스템 위치서비스에 대한 액세스 제공

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) // 지정된 프로바이더의 현재 활성화/비활성화 상태를 반환 (원하는 위치 제공자가 현재 사용가능한지 확인)
                // GPS와 같이 정확도가 높은 위치 제공자는 네트워크 기반 위치제공자 같이 정확도가 떨어지는 위치 제공자보다 수정시간이 더 오래 걸림
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GPS_ENABLE_REQUEST_CODE:

                    //사용자가 GPS 활성 시켰는지 검사
                    if (checkLocationServicesStatus()) {
                        if (checkLocationServicesStatus()) {

                            Log.d(TAG, "onActivityResult : GPS 활성화 되있음");

                            needRequest = true;
                            return;
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }

}
