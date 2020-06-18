package com.example.riding;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class ProgressExerciseActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnProgressDlg;
    private Button mBtnSpinner;
    private Button mBtnLarge;
    private Button mBtnMid;
    private Button mBtnSmall;
    private Button mBtnStick;

    private AsyncTask<Integer, String, Integer> mProgressDlg;
    private ProgressBar mProgressLarge;
    private ProgressBar mProgressMid;
    private ProgressBar mProgressSmall;
    private ProgressBar mProgressStick;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_exercise);

        mBtnProgressDlg = (Button) findViewById(R.id.btnProgressDialog);
        mBtnLarge = (Button)findViewById(R.id.btnProgressLarge);
        mBtnMid = (Button)findViewById(R.id.btnProgressMid);
        mBtnSmall = (Button)findViewById(R.id.btnProgressSmall);
        mBtnStick = (Button)findViewById(R.id.btnProgressStick);

        mProgressLarge = (ProgressBar) findViewById(R.id.progressBar1);
        mProgressMid = (ProgressBar) findViewById(R.id.progressBar2);
        mProgressSmall = (ProgressBar) findViewById(R.id.progressBar3);
        mProgressStick = (ProgressBar) findViewById(R.id.progressBar4);

        mBtnProgressDlg.setOnClickListener(this);
        mBtnLarge.setOnClickListener(this);
        mBtnMid.setOnClickListener(this);
        mBtnSmall.setOnClickListener(this);
        mBtnStick.setOnClickListener(this);

        mProgressLarge.setVisibility(ProgressBar.GONE);
        mProgressMid.setVisibility(ProgressBar.GONE);
        mProgressSmall.setVisibility(ProgressBar.GONE);
        mProgressStick.setVisibility(ProgressBar.GONE);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnProgressDialog:
                mProgressDlg = new ProgressDlg(ProgressExerciseActivity.this).execute(30);
                break;

            case R.id.btnProgressLarge:
                mProgressLarge.setVisibility(ProgressBar.VISIBLE);
                //게이지가 올라가거나 화살표가 돌아가는 것이 작업이 완료될 때 까지 멈추지 않게(boolean)
                mProgressLarge.setIndeterminate(true);
                //최대치 설정
                mProgressLarge.setMax(100);

                break;

            case R.id.btnProgressMid:
                mProgressMid.setVisibility(ProgressBar.VISIBLE);
                mProgressMid.setIndeterminate(true);
                mProgressMid.setMax(100);
                break;

            case R.id.btnProgressSmall:
                mProgressSmall.setVisibility(ProgressBar.VISIBLE);
                mProgressSmall.setIndeterminate(true);
                mProgressSmall.setMax(100);
                break;

            case R.id.btnProgressStick:
                mProgressStick.setVisibility(ProgressBar.VISIBLE);
                mProgressStick.setIndeterminate(true);
                mProgressStick.setMax(100);
                break;

            default:
                break;
        }
    }

}
