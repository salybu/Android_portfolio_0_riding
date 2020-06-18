package com.example.riding;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    // FrameLayout에 각 메뉴의 Fragment를 바꿔 줄 FragmentManager
    private FragmentManager fragmentManager = getSupportFragmentManager();

    // 3개의 메뉴에 들어갈 Fragment들
    private Menu1Fragment menu1Fragment = new Menu1Fragment();
    private Menu2Fragment menu2Fragment = new Menu2Fragment();
    private Menu3Fragment menu3Fragment = new Menu3Fragment();
    private FloatingActionButton fab;

    // RealTime Database 에서 가져오기
    final FirebaseDatabase database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 객체를 가져옴
    final DatabaseReference myRef = database.getReference(); // 데이터베이스에서 레퍼런스를 얻어옴

    private String TAG = "This is Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        FragmentTransaction transaction = fragmentManager.beginTransaction(); // 첫 화면 지정
        transaction.replace(R.id.frame_layout, menu1Fragment).commitAllowingStateLoss();

        fab = (FloatingActionButton) findViewById(R.id.fab); // 플로팅 버튼 위젯 연결
        fab.setOnClickListener(this);

        UserAuthInfoSet();

        // bottomNavigationView의 아이템이 선택될 때 호출될 리스너 등록
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (item.getItemId()) {
                    case R.id.navigation_menu1: {
                        transaction.replace(R.id.frame_layout, menu1Fragment).commitAllowingStateLoss();
                        break;
                    }
                    case R.id.navigation_menu2: {
                        transaction.replace(R.id.frame_layout, menu2Fragment).commitAllowingStateLoss();
                        break;
                    }
                    case R.id.navigation_menu3: {
                        transaction.replace(R.id.frame_layout, menu3Fragment).commitAllowingStateLoss();
                        break;
                    }
                }
                return true;
            }
        });
        passPushTokenToServer();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                Intent intent = new Intent(getApplicationContext(), MoimWriteActivity.class); /// 메인액티비티 띄우는 인텐트 생성
                startActivity(intent); //// 인텐트 담아서 액티비티 시작
                break;
        }
    }

    public void refresh(){
        FragmentTransaction transaction = fragmentManager.beginTransaction(); // 첫 화면 지정
        //   transaction.detach(this).attach(this).commit();
        transaction.replace(R.id.frame_layout, menu3Fragment).commitAllowingStateLoss();
    }

    public void passPushTokenToServer(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String, Object> map = new HashMap<>();
        map.put("pushToken ", token);

        FirebaseFirestore.getInstance().collection("Profile").document(uid)
                .update("Token", token);
    }

    class ContentDownload extends AsyncTask<String, Void, String> {

        Context context;
        ProgressDialog asyncDialog = new ProgressDialog(context);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("콘텐츠 확인 중 입니다...");
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            for (int i = 0; i < 5; i++) {
                asyncDialog.setProgress(i * 30);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String abc = "Parsing & Download OK!!!";
            return abc;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            asyncDialog.dismiss();
        }
    }

}

class ContentDownloads extends AsyncTask<String, Void, String> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        String abc = "Parsing & Download OK!!!";
        return abc;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}