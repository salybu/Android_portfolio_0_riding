package com.example.riding;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Ex2Activity extends AppCompatActivity {

    private EditText mLongitude, mLatitude;
    private GoogleMap mGoogleMap;
    private CheckBox mSatellite;
    private Button mMove;

    private int DEFAULT_ZOOM_LEVEL = 13;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ex2);

        mLongitude = (EditText) findViewById(R.id.txtLongutude);
        mLatitude = (EditText) findViewById(R.id.txtLatitude);
        mSatellite = (CheckBox) findViewById(R.id.satellite);
        mMove = (Button) findViewById(R.id.btnMove);

        // 이벤트 등록
        mSatellite.setOnClickListener(satelliteOnClickListener);
        mMove.setOnClickListener(moveOnClickListener);

        // BitmapDescriptorFactory 생성하기 위한 소스
        MapsInitializer.initialize(getApplicationContext());

        GooglePlayServicesUtil.isGooglePlayServicesAvailable(Ex2Activity.this);
//        mGoogleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        // GPS 맵이동
        this.setGpsCurrent(mLongitude.getText().toString(), mLatitude.getText().toString());
    }

    /** 버튼 클릭시 이동 */
    private Button.OnClickListener moveOnClickListener =
            new Button.OnClickListener() {
                public void onClick(View v) {
                    setGpsCurrent(mLongitude.getText().toString(), mLatitude.getText().toString());
                }
            };

    private CheckBox.OnClickListener satelliteOnClickListener =
            new CheckBox.OnClickListener() {
                public void onClick(View v) {
                    setSatellite();
                }
            };

    private void setSatellite() {
        if (mSatellite.isChecked()) {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else {
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    };

    private void setGpsCurrent(String strLat, String strLng) {

        double latitude = 0;
        double longitude = 0;

/*
        GpsInfo gps = new GpsInfo(Ex2Activity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {

            if (strLat.equals("") || strLng.equals("")) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

            } else {
                latitude = Double.parseDouble(strLat);
                longitude = Double.parseDouble(strLng);
            }

            // Creating a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            // Showing the current location in Google Map
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Map 을 zoom 합니다.
            this.setZoomLevel(DEFAULT_ZOOM_LEVEL);

            // 마커 설정.
            MarkerOptions optFirst = new MarkerOptions();
            optFirst.position(latLng);// 위도 • 경도
            optFirst.title("Current Position");// 제목 미리보기
            optFirst.snippet("Snippet");
            optFirst.icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.ic_launcher));
            mGoogleMap.addMarker(optFirst).showInfoWindow();
        }  */

    }

    /**
     * 맵의 줌레벨을 조절합니다.
     * @param level
     */
    private void setZoomLevel(int level) {
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(level));
        Toast.makeText(this, "Zoom Level : " + String.valueOf(level),
                Toast.LENGTH_LONG).show();
    }

    /** Map 클릭시 터치 이벤트 */
    public void onMapClick(LatLng point) {

        // 현재 위도와 경도에서 화면 포인트를 알려준다
        Point screenPt = mGoogleMap.getProjection().toScreenLocation(point);

        // 현재 화면에 찍힌 포인트로 부터 위도와 경도를 알려준다.
        LatLng latLng = mGoogleMap.getProjection()
                .fromScreenLocation(screenPt);

        Log.d("맵좌표", "좌표: 위도(" + String.valueOf(point.latitude)
                + "), 경도(" + String.valueOf(point.longitude) + ")");
        Log.d("화면좌표", "화면좌표: X(" + String.valueOf(screenPt.x)
                + "), Y(" + String.valueOf(screenPt.y) + ")");
    }



}