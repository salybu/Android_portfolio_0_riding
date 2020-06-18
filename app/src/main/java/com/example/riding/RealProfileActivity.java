package com.example.riding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RealProfileActivity extends BaseActivity {

    private CircleImageView profileImage;
    private TextView Nickname;
    private TextView Email;
    private TextView realEmail;
    private TextView Message;
    private TextView PhoneNumber;
    private EditText etNickname;
    private EditText etMessage;
    private EditText etPhoneNumber;
    private TextView btnDone;

    private String token;
    private int reqWidth;

    private final int GALLERY_CODE = 5000;

    private String Imagepath; // 이미지 경로 저장하기 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_profile);

        final FirebaseDatabase database = FirebaseDatabase.getInstance(); // (RealTime DB) 파이어베이스 데이터베이스 객체를 가져옴
        final DatabaseReference myRef = database.getReference(); // 데이터베이스에서 레퍼런스를 얻어옴

        UserAuthInfoSet();
        FirestoreDbSet();
        StorageSet();

        profileImage = (CircleImageView) findViewById(R.id.profileImage);
        Nickname = (TextView) findViewById(R.id.Nickname);
        Message = (TextView) findViewById(R.id.Message);
        Email = (TextView) findViewById(R.id.Email);

        realEmail = (TextView) findViewById(R.id.realEmail);
        realEmail.setText(email); // 사용자 이메일값 가져와서 Email Textview에 세팅함

        realEmail = (TextView) findViewById(R.id.realEmail);
        PhoneNumber = (TextView) findViewById(R.id.PhoneNumber);
        btnDone = (TextView) findViewById(R.id.btnEdit);
        etNickname = (EditText) findViewById(R.id.etNickname);
        etMessage = (EditText) findViewById(R.id.etMessage);
        etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);

        // 값 가져와서 세팅하는 건 셰어드에 저장된 것만 읽어올 거. 저장하는 건 셰어드 + 파베 모두에 할 거
        final SharedPreferences pref = getSharedPreferences("Member Info", MODE_PRIVATE); // 회원정보가 있는 셰어드 "Member Info" 불러오기
        final SharedPreferences.Editor editor = pref.edit();  // Key값 "입력한 이메일" 셰어드에서 Value값 JSONObject 불러오기
        String thisObject = pref.getString(email,"");

        JSONObject thisinfo = null; // JSONObject를 받기위한 변수 선언
        try {
            thisinfo = new JSONObject(thisObject); // JSONObject 파싱

            String nicknamefromshared = thisinfo.getString("Nickname"); // 셰어드에서 닉네임 가져옴
            if(!nicknamefromshared.equals("")) { // 기본으로 세팅해준 값 ""이면 읽어오지 않기 (설정해 둔 hint값이 보일 수 있게)
                etNickname.setText(nicknamefromshared); // 셰어드에서 가져온 닉네임 setText 해줌
            }

            String message = thisinfo.getString("Message"); // 셰어드에서 상태메세지 가져옴
            if(!message.equals("")) {
                etMessage.setText(message); // 셰어드에서 가져온 메세지 setText 해줌
            }

            String phonenumber = thisinfo.getString("PhoneNumber");
            if(!phonenumber.equals("")) {
                etPhoneNumber.setText(phonenumber);
            }

            String imagepath = thisinfo.getString("Imagepath");
            if(!imagepath.equals("")){
                setImage(imagepath, profileImage); // 상속받은 BaseActivity의 이미지 세팅 메소드 사용함
                Imagepath = imagepath;
            }
        } catch (JSONException e) { //// JSONObject 파싱할 때 try, catch문
            e.printStackTrace();
        }

        profileImage.setOnClickListener(new View.OnClickListener() { // 동그란 이미지뷰 클릭하면 사진선택할 수 있도록
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nickname = etNickname.getText().toString();
                final String message = etMessage.getText().toString();
                final String phonenumber = etPhoneNumber.getText().toString();

/*                Member mem = new Member(nickname, message, phonenumber, imagepath);
                myRef.child("memberlist/").push().setValue(mem);     RealTime DB에 저장할 때 코드 */

/*                // 파베 Firestore에 저장
                token = FirebaseInstanceId.getInstance().getToken();
                Log.e("This is Realpro Token", token);  */

                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w("Real Profile Activity", "getInstanceId failed", task.getException());
                                    return; }

                                // Get new Instance ID token
                                token = task.getResult().getToken();
                                Log.e("Token", token);

                                Member mem = new Member(nickname, message, phonenumber, Imagepath, email, UID, token);
                                db.collection("Profile").document(UID).set(mem);
                                // "Profile" 컬렉션 아래, "로그인한 UID값" 문서 아래, "멤버 클래스(프로필 정보)"값을 firestore에 저장
                            }
                        });
                Log.e("Profile Activity TAG", UID);

                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                Log.e(" TAG ", refreshedToken);

/*                Member mem = new Member(nickname, message, phonenumber, Imagepath, email, UID, refreshedToken);
                db.collection("Profile").document(UID).set(mem);
                // "Profile" 컬렉션 아래, "로그인한 UID값" 문서 아래, "멤버 클래스(프로필 정보)"값을 firestore에 저장  */

                // 셰어드에도 저장
                String thisObject = pref.getString(email,"");

                JSONObject thisinfo = null; /// JSONObject를 받기 위한 변수 선언
                try {
                    thisinfo = new JSONObject(thisObject); //// JSONObject 파싱
                    thisinfo.put("Imagepath", Imagepath); // JSONObject에 각 변수들 수정한 값 넣음
                    thisinfo.put("Nickname", nickname);
                    thisinfo.put("Message", message);
                    thisinfo.put("PhoneNumber", phonenumber);
                } catch (JSONException e) { //// JSONObject 파싱할 때 try, catch문
                    e.printStackTrace();
                }
                editor.putString(email, thisinfo.toString()); /// JSONObject에 변경한 값 전부 넣음
                editor.commit();

                finish();
            }
        });
        DisplayMetrics metrics = this.getResources().getDisplayMetrics(); // 기기화면의 가로, 세로길이를 구해 담음
        reqWidth = metrics.widthPixels;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_CODE:
                    Uri uri = data.getData(); /// 사진 1장만 선택했을 때 data.getData()로 URI값 받아옴
                    sendPicture(uri, profileImage, reqWidth); // URI값 가져와서 메소드에 넣어줌 => 세팅됨
                    Imagepath = getRealPathFromURI(uri);
                    break;
                default:
                    break;
            }
        }
    }

}

    class Member{
        public String Nickname;
        public String Message;
        public String PhoneNumber;
        public String Imagepath;
        public String Token;
        public String Email;
        public String UID;
        public ArrayList<String> FriendUID;

        public Member(){ }

        public Member(String nickname, String message, String phonenumber, String imagepath, String email, String uid, String token) {
            this.Nickname = nickname;
            this.Message = message;
            this.Imagepath = imagepath;
            this.PhoneNumber = phonenumber;
            this.Email = email;
            this.UID = uid;
            this.Token = token;

            StorageReference stRef = FirebaseStorage.getInstance().getReference().child("profile/"+UID); // 파이어베이스 스토리지 프로필 아래 UID로 프로필 이미지 저장

            // 사진경로를 Filestream으로 변환해서 FirebaseStorage에 저장할 거임
            InputStream stream = null; // InputStream 새로운 객체를 만들고 null값 초기화
            try {
                stream = new FileInputStream(new File(imagepath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            UploadTask uploadTask = stRef.putStream(stream); // InputStream을 앞서 지정한 경로에 넣어줌
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            });
        }

        public Member(String nickname, String message, String phonenumber, String imagepath, String token, int i){
            this.Nickname = nickname;
            this.Message = message;
            this.PhoneNumber = phonenumber;
            this.Imagepath = imagepath;
            this.Token = token;
        }
    }