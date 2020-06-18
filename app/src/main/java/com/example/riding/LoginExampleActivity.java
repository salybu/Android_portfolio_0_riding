package com.example.riding;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class LoginExampleActivity extends AppCompatActivity {

    // 비밀번호 정규식
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");

    // 파이어베이스 인증 객체 생성
    private FirebaseAuth firebaseAuth;

    // 이메일과 비밀번호
    private EditText editTextEmail;
    private EditText editTextPassword;

    private String email = ""; // 파이어베이스에 사용자 넘기기 위해 만든 변수
    private String password = ""; // 파베에 비밀번호 넘기기 위해 만든 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_example);

        // 파이어베이스 인증 객체 선언
        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.et_eamil); /// 이메일 입력 EditText
        editTextPassword = findViewById(R.id.et_password); /// 비밀번호 입력 EditText
    }

    public void signup(View view) { // 회원 등록 메소드
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
        if (email.isEmpty()) {
            // 이메일 공백
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // 이메일 형식 불일치
            return false;
        } else {
            return true;
        }
    }

    // 비밀번호 유효성 검사
    private boolean isValidPasswd() {
        if (password.isEmpty()) {
            // 비밀번호 공백
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            // 비밀번호 형식 불일치
            return false;
        } else {
            return true;
        }
    }

    // 회원가입
    private void createUser(final String email, final String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password) // 파이어베이스 인증객체에 회원등록 메소드
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { // 회원등록 완료했을 시 완료리스너 추가함
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공
                            Toast.makeText(LoginExampleActivity.this, R.string.success_signup, Toast.LENGTH_SHORT).show();
                        } else {
                            // 회원가입 실패
                            Log.e("회원가입 실패", email);
                            Log.e("회원가입 실패", password);
                            Toast.makeText(LoginExampleActivity.this, R.string.failed_signup, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(LoginExampleActivity.this, R.string.success_login, Toast.LENGTH_SHORT).show();
                        } else {
                            // 로그인 실패
                            Toast.makeText(LoginExampleActivity.this, R.string.failed_login, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}