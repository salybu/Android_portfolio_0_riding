package com.example.riding;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import static android.support.constraint.Constraints.TAG;

public class RegisterActivity extends AppCompatActivity {

    // 정규식
    public static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$"); // 비밀번호 정규식
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE); // 이메일 정규식

    private FirebaseAuth firebaseAuth; // 파이어베이스 인증 객체 생성

    // 이메일과 비밀번호
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPasswordConfirm;

    private TextView EmailCheck;
    private TextView PasswordCheck;
    private TextView PasswordConfirmCheck;

    private int TERMS_AGREE_1 = 0;
    private CheckBox checkBox3;

    private Button btnDone;

    private String email;
    private String password;

    private FirebaseFirestore db;

    Thread thread;
    public final int EMAIL_CONFIRMED = 0; // 스레드에서 메세지 보낼 때 메시지 코드
    public final int EMAIL_NOTCONFIRMED = 5;
    public final int PASSWORD_CONFIRMED = 1;
    public final int PASSWORD_NOTCONFIRMED = 4;
    public final int PASSWORDCONFIRM_CONFIRMED = 2;
    public final int PASSWORDCONFIRM_NOTCONFIRMED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance(); // 파이어베이스 인증 객체 선언
        db = FirebaseFirestore.getInstance(); // Cloud Firestore 객체 가져오기

        editTextEmail = findViewById(R.id.etEmail);
        editTextPassword = findViewById(R.id.etPassword);
        editTextPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        checkBox3 = (CheckBox) findViewById(R.id.checkBox3);
        btnDone = findViewById(R.id.btnRegister);

        EmailCheck = findViewById(R.id.EmailCheck);
        PasswordCheck = findViewById(R.id.PasswordCheck);
        PasswordConfirmCheck = findViewById(R.id.PasswordConfirmCheck);

        checkBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    TERMS_AGREE_1 = 1;
                }else{
                    TERMS_AGREE_1 = 0;
                }
            }
        });

        email = editTextEmail.getText().toString(); // EditText에 작성한 이메일 String값을 email 변수에 담음
        password = editTextPassword.getText().toString(); // EditText로 작성한 비밀번호 String값을 password 변수에 담음

        final Thread thread1 = new Thread_Email();
        final Thread thread2 = new Thread_PASSWORD();
        final Thread thread3 = new Thread_PASSWORDCONFIRM();
        thread1.start();
        thread2.start();
        thread3.start();

        btnDone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
/*                // 이메일 입력 확인
                if( editTextEmail.getText().toString().length() == 0 ) {
                    Toast.makeText(RegisterActivity.this, "Email을 입력하세요!", Toast.LENGTH_SHORT).show();
                    editTextEmail.requestFocus();
                    return;
                }

                // 비밀번호 입력 확인
                if( editTextPassword.getText().toString().length() == 0 ) {
                    Toast.makeText(RegisterActivity.this, "비밀번호를 입력하세요!", Toast.LENGTH_SHORT).show();
                    editTextPassword.requestFocus();
                    return;
                }  */

               if(!isValidEmail()){ return; } // 이메일 입력 확인
               if(!isValidPasswd()){ return; }  // 비밀번호 입력 확인

                // 비밀번호 확인 입력 확인
                if( editTextPasswordConfirm.getText().toString().length() == 0 ) {
                    Toast.makeText(RegisterActivity.this, "비밀번호 확인을 입력하세요!", Toast.LENGTH_SHORT).show();
                    editTextPasswordConfirm.requestFocus();
                    return;
                }

                // 비밀번호 일치 확인
                if( !editTextPassword.getText().toString().equals(editTextPasswordConfirm.getText().toString()) ) {
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show();
                    editTextPassword.setText("");
                    editTextPasswordConfirm.setText("");
                    editTextPassword.requestFocus();
                    return;
                }

/*                if(TERMS_AGREE_1 !=0){
                    Toast.makeText(RegisterActivity.this, "약관에 동의해주세요!", Toast.LENGTH_SHORT).show();
                    checkBox3.requestFocus();
                    return;
                }   */

                signup(); // FirebaseAuth 객체 생성

//                myRef.child("moimlist/").push().setValue(moo);
//                postRef.setValueAsync(moim);

/*               Member mem = new Member(nickname, message, phonenumber, imagepath, token);
                db.collection("Profile").document(editTextEmail.getText().toString()).set(mem)
                        // "Profile" 컬렉션 아래, "로그인한 UID값" 문서 아래, "멤버 클래스(프로필 정보)"값을 firestore에 저장
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });    */

                SharedPreferences pref = getSharedPreferences("Member Info", MODE_PRIVATE); // 회원정보를 담기 위한 셰어드 생성
                SharedPreferences.Editor editor = pref.edit(); // 정보 수정 및 편집을 위한 에디터 생성

                JSONObject userinfo = new JSONObject(); //// value값으로 넣을 JSONObject 생성
                try {
                    userinfo.put("Password", password);
                    userinfo.put("Imagepath", "");
                    userinfo.put("Message", "");
                    userinfo.put("Nickname", "");
                    userinfo.put("PhoneNumber", "");
                    userinfo.put("UID", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                editor.putString(email, userinfo.toString()); // 키값:id 밸류값:String값 userinfo(JSON 객체) 넣기
                editor.commit(); // editor 동작하게 코드 넣기

                /// 회원이 작성한 소모임 글목록을 담기 위한 셰어드 생성
                SharedPreferences prefe = getSharedPreferences("Moims", MODE_PRIVATE);
                SharedPreferences.Editor editor1 = prefe.edit(); // 정보 수정 및 편집을 위한 에디터 생성

                JSONArray eventinfo = new JSONArray(); // value값으로 넣을 JSONArray 생성
                editor1.putString(email, eventinfo.toString()); // 키값:id 밸류값:String값 eventinfo(JSON 객체) 넣기

/*                JSONArray event2info = new JSONArray(); // value값으로 넣을 JSONArray 생성
                editor1.putString("Entire", event2info.toString()); // 키값:id 밸류값:String값 eventinfo(JSON 객체) 넣기  */
                editor1.commit(); // editor 동작하게 코드 넣기

                if(thread1!= null || thread1.currentThread().isAlive() || !thread1.currentThread().isInterrupted()) {
                    thread1.interrupt();
                }
                if(thread2!= null || thread2.currentThread().isAlive() || !thread2.currentThread().isInterrupted()) {
                    thread2.interrupt();
                }
                if(thread3!= null || thread3.currentThread().isAlive() || !thread3.currentThread().isInterrupted()) {
                    thread3.interrupt();
                }
                finish();
            }
        });
    }

    public void signup() { // 회원 등록 메소드
        email = editTextEmail.getText().toString(); // EditText에 작성한 이메일 String값을 email 변수에 담음
        password = editTextPassword.getText().toString(); // EditText로 작성한 비밀번호 String값을 password 변수에 담음

        if(isValidEmail() && isValidPasswd()) { // 이메일, 비밀번호 유효성 검사 둘다 만족할 때
            createUser(email, password); // 회원등록 (이메일, 비밀번호)
        }
    }

    public void signin(View view) { // 로그인 메소드
        email = editTextEmail.getText().toString(); // EditText에 작성한 이메일 String값을 email 변수에 담음
        password = editTextPassword.getText().toString(); // EditText로 작성한 비밀번호 String값을 password 변수에 담음

        if(isValidEmail() && isValidPasswd()) { // 이메일, 비밀번호 유효성 검사 둘다 만족할 때
            loginUser(email, password); // 로그인 (이메일, 비밀번호)
        }
    }

    // 이메일 유효성 검사
    private boolean isValidEmail() {
        email = editTextEmail.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Email을 입력하세요!", Toast.LENGTH_SHORT).show(); // 이메일 공백
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(RegisterActivity.this, "Email을 형식에 맞게 입력하세요!", Toast.LENGTH_SHORT).show(); // 이메일 형식 불일치
            return false;
        } else {
            return true;
        }
    }

    // 비밀번호 유효성 검사
    private boolean isValidPasswd() {
        password = editTextPassword.getText().toString();

        if (password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "비밀번호를 입력하세요!", Toast.LENGTH_SHORT).show(); // 비밀번호 공백
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            Toast.makeText(RegisterActivity.this, "비밀번호를 형식에 맞게 입력하세요!", Toast.LENGTH_SHORT).show(); // 비밀번호 형식 불일치
            return false;
        } else {
            return true;
        }
    }

    // 회원가입
    private void createUser(String email, String password) {
        Log.e("create user", email);
        Log.e("create user", password);
        firebaseAuth.createUserWithEmailAndPassword(email, password) // 파이어베이스 인증객체(firebaseAuth)에 회원등록 메소드(createUserWithEmailAndPassword) 사용
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { // 회원등록 완료했을 시 완료리스너 추가함
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공
                            Toast.makeText(RegisterActivity.this, R.string.success_signup, Toast.LENGTH_SHORT).show();

/*                            final String UID = firebaseAuth.getCurrentUser().getUid(); // 만든 유저의 UID를 제대로 가져오려나 ..(?)
                            Log.e("createUser Success ", UID);

                            // 여기에 북마크 만들어야 되나
                            // 파베에서 바꾸는 코드. 트랜잭션 이용
                            final DocumentReference docRef = db.collection("Moim").document(); // "Moim" 콜렉션에서 모든(?) 문서 가져오기
                            db.runTransaction(new Transaction.Function<Void>() {
                                @Override
                                public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                    DocumentSnapshot snapshot = transaction.get(docRef);
                                    Log.e("transaction Apply? ","ok");
                                    // 소모임 글마다 북마크 표시하는 Hashmap<멤버 UID, Boolean> 가져옴
                                    HashMap<String, Boolean> hash = (HashMap<String, Boolean>) snapshot.get("Bookmark");

                                    // Hashmap에 새 Hashmap<새 멤버 UID, false> 값 대입
                                    hash.put(UID, false);
                                    transaction.update(docRef, "Bookmark", hash); // 트랜잭션 완료, 홋
                                    return null;
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Transaction success!");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Transaction failure.", e);
                                }
                            });  북마크 때문에 만들었으나 굳이 없어도 됨 */
                        } else {
                            // 회원가입 실패
                            Toast.makeText(RegisterActivity.this, R.string.failed_signup, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // 로그인
    private void loginUser(String email, String password)
    {
        firebaseAuth.signInWithEmailAndPassword(email, password) // 파이어베이스 인증객체에 회원로그인 메소드
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            Toast.makeText(RegisterActivity.this, R.string.success_login, Toast.LENGTH_SHORT).show();
                        } else {
                            // 로그인 실패
                            Toast.makeText(RegisterActivity.this, R.string.failed_login, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case EMAIL_CONFIRMED:
                    EmailCheck.setText("이메일 형식을 만족합니다.");
                    EmailCheck.setTextColor(Color.GREEN);
                    break;

                case EMAIL_NOTCONFIRMED:
                    EmailCheck.setText("이메일 형식에 맞게 작성해주세요");
                    EmailCheck.setTextColor(Color.RED);
                    break;

                case PASSWORD_CONFIRMED:
                    PasswordCheck.setText("비밀번호 형식을 만족합니다.");
                    PasswordCheck.setTextColor(Color.GREEN);
                    break;

                case PASSWORD_NOTCONFIRMED:
                    PasswordCheck.setText("(영어 대소문자, 특수문자, 숫자 포함 4~16자리)");
                    PasswordCheck.setTextColor(Color.RED);
                    break;

                case PASSWORDCONFIRM_CONFIRMED:
                    PasswordConfirmCheck.setText("비밀번호가 일치합니다.");
                    PasswordConfirmCheck.setTextColor(Color.GREEN);
                    break;

                case PASSWORDCONFIRM_NOTCONFIRMED:
                    PasswordConfirmCheck.setText("비밀번호가 일치하지 않습니다.");
                    PasswordConfirmCheck.setTextColor(Color.RED);
                    break;

                default:
                    break;
            }
        }
    };

    class Thread_Email extends Thread {
        int i = 0;

        @Override
        public void run() {
            super.run();
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    i++;

                    Message message = handler.obtainMessage(); // 메시지 얻어오기
                    Message message_not = handler.obtainMessage();
                    message.what = EMAIL_CONFIRMED; // 메시지 ID 설정
                    message_not.what = EMAIL_NOTCONFIRMED;

                    if(EMAIL_PATTERN.matcher(editTextEmail.getText().toString()).matches()) { // 이메일 형식을 만족할 때 메시지 전달
                  //  if(EMAIL_PATTERN.matcher(email).find()) {
                 //   if(email.length()!=0){
                        handler.sendMessage(message);
                    }else if(!EMAIL_PATTERN.matcher(editTextEmail.getText().toString()).matches()){
                        handler.sendMessage(message_not);
                    }

                    sleep(100); // 0.1초 씩 딜레이 부여
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class Thread_PASSWORD extends Thread {
        int i = 0;

        @Override
        public void run() {
            super.run();
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    i++;

                    Message message = handler.obtainMessage(); // 메시지 얻어오기
                    Message message_not = handler.obtainMessage();
                    message.what = PASSWORD_CONFIRMED; // 메시지 ID 설정
                    message_not.what = PASSWORD_NOTCONFIRMED;

                    if(PASSWORD_PATTERN.matcher(editTextPassword.getText().toString()).matches()) { // 비밀번호 형식을 만족할 때 메시지 전달
              //      if(PASSWORD_PATTERN.matcher(password).find()) {
                        handler.sendMessage(message);
                    }else if(!PASSWORD_PATTERN.matcher(editTextPassword.getText().toString()).matches()){
                        handler.sendMessage(message_not);
                    }

                    sleep(100); // 0.1초 씩 딜레이 부여
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class Thread_PASSWORDCONFIRM extends Thread {
        int i = 0;

        @Override
        public void run() {
            super.run();
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    i++;

                    Message message = handler.obtainMessage(); // 메시지 얻어오기
                    Message message_notmatch = handler.obtainMessage();
                    message.what = PASSWORDCONFIRM_CONFIRMED; // 메시지 ID 설정
                    message_notmatch.what = PASSWORDCONFIRM_NOTCONFIRMED;

                    if( editTextPassword.getText().toString().length() !=0 // 비밀번호 입력값이 0이 아니면서,
                            && editTextPassword.getText().toString().equals(editTextPasswordConfirm.getText().toString())) { // 비밀번호 입력값과 비밀번호 입력 확인값이 일치할 때 메시지 전달
                        handler.sendMessage(message);
                    }else if( !editTextPassword.getText().toString().equals(editTextPasswordConfirm.getText().toString())) { // 비밀번호 입력값과 비밀번호 입력 확인값이 일치하지 않을 때 메시지 전달
                        handler.sendMessage(message_notmatch);
                    }
                    sleep(100); // 0.1초 씩 딜레이 부여
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}