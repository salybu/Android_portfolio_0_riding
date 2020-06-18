package com.example.riding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class LoginActivity extends BaseActivity {

    // 이메일, 비밀번호 등등
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button btnLogin;
    private Button btnRegist;
    private CheckBox cxAutoLogin;

    private int AutoLogin = 5; // 자동로그인 체크 시 1, 체크안할 시 디폴트값 0
    public static SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = (EditText) findViewById(R.id.etEmail);
        editTextPassword = (EditText) findViewById(R.id.etPassword);
        btnRegist = (Button) findViewById(R.id.btnRegist);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        cxAutoLogin = (CheckBox) findViewById(R.id.cxAutoLogin);

/*        SharedPreferences gggg = getSharedPreferences("Moims", MODE_PRIVATE);
        SharedPreferences ggggg = getSharedPreferences("Member Info", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = gggg.edit();
        SharedPreferences.Editor editor2 = ggggg.edit();
        editor1.clear();
        editor2.clear();
        editor1.commit();
        editor2.commit();  */

        auth = FirebaseAuth.getInstance(); // 파이어베이스 인증객체 선언 (로그인할 때 비교할)

        SharedPreferences auto = getSharedPreferences("Autologin", MODE_PRIVATE); // 자동로그인 여부 체크할 셰어드파일 생성 및 가져옴
        String id = auto.getString("ID", null); // 자동로그인할 때 Auth 객체나 관련정보를 넘겨야 할 거 같음.. 그럴 필요없음 Auth객체에 저장돼있음ㅋ
        Boolean Autologin = auto.getBoolean("Autologin", false); /// 자동로그인 여부를 체크해둔 Boolean값 받아옴

        if(Autologin == true){ // 설정해둔 자동로그인 값이 True이면 바로 Main액티비티로 넘어감
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish(); // 로그아웃 했다가 나오려면 finish 시키면 안될 듯
        }

        btnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 회원가입 버튼
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // SINGLE_TOP : 이미 만들어진게 있으면 그걸 쓰고, 없으면 만들어서 써라
                startActivityForResult(intent, 1000);
            }
        });

        cxAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    AutoLogin = 1;
                }else{
                    AutoLogin = 0;
                }
            }
        });

        btnLogin.setOnClickListener(mClickListner);
    }

    View.OnClickListener mClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String emailinput = editTextEmail.getText().toString(); // 이메일과 비밀번호 입력값 가져옴
            String passwordinput = editTextPassword.getText().toString();

            loginUser(emailinput, passwordinput);
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1000 && resultCode == RESULT_OK) {
            Toast.makeText(LoginActivity.this, "회원가입을 완료했습니다!", Toast.LENGTH_SHORT).show();
            editTextEmail.setText(data.getStringExtra("email"));
        }
    }

    private void loginUser(final String email, String password) { // 로그인
        auth.signInWithEmailAndPassword(email, password) // 파이어베이스 인증객체에 회원로그인 메소드
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { // 로그인 성공

                            if(AutoLogin == 1){ // 자동로그인에 체크했다면,
                                SharedPreferences auto = getSharedPreferences("Autologin", MODE_PRIVATE); // 자동로그인 여부 체크하는 셰어드파일 불러옴
                                SharedPreferences.Editor editorauto = auto.edit(); // 정보 수정 및 편집을 위한 에디터 생성
                                editorauto.putString("ID", email); // 로그인하려는 ID값을 자동로그인 셰어드 파일에 저장함
                                editorauto.putBoolean("Autologin", true); /// 키값 "Autologin"에 Value값 true 대입함 : 이후에 자동로그인 하겠다는 의미
                                editorauto.commit(); // 수정완료
                            }

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);

                            Toast.makeText(LoginActivity.this, R.string.success_login, Toast.LENGTH_SHORT).show();
                            finish();
                        } else { // 로그인 실패
                            Toast.makeText(LoginActivity.this, R.string.failed_login, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}