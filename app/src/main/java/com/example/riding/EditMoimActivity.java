package com.example.riding;

import android.Manifest;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class EditMoimActivity extends BaseActivity implements OnMapReadyCallback {

    private EditText Title;
    private EditText Memo;
    private EditText realLocation;

    private TextView Date;
    private TextView Time;
    private TextView realTime;
    private TextView Location;
    private TextView Pictures;
    private TextView addPic;

    private int Members;
    private int currentmem;
    int reqWidth;

    boolean needRequest = false; // 사용자가 GPS 활성시켰는지 검사
    private double lat; // 받아온 위도값 (프로필에서 넘어온 주소에 마커 추가하기 위해)
    private double lon; // 경도값

    private Spinner spnrMembers;

    private ImageView Imageview;
    private ImageView icnSearch;
    private Button btnEdit;

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소
    private View mLayout; // Snackbar 사용하기 위해서는 View가 필요합니다.  // (참고로 Toast에서는 Context가 필요했습니다.)

    Location mCurrentLocatiion; // 현위치
    LatLng currentPosition; // 현위치 위도, 경도

    private final int GALLERY_CODE = 1112;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private String TAG = "This is EditMoimActivity";

    private int members; // 전체 인원수
    private String imagepath_s; // 이미지 수정할 때 전달을 위한 변수
    private String postkey; // 대응하는 게시글을 가져오기 위한 키값

    public GoogleMap mGoogleMap = null; // 구글맵 참조변수
    private Marker currentMarker = null; // 현위치 마커 변수

    private FusedLocationProviderClient mFusedLocationClient; // 현재위치 받아올 FusedLocationProviderClient 객체
    private LocationRequest locationRequest; // 위치 업데이트에 대한 서비스 품질을 요청하는데 사용됨(?)
    private Location location;

    Adapter_forMember adapter; // 참여인원 바뀔 때 바로 적용되게(?) 변수선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_moim);

        Title = (EditText) findViewById(R.id.Title);
        Date = (TextView) findViewById(R.id.Day);
        Time = (TextView) findViewById(R.id.Time);
        realTime = (TextView) findViewById(R.id.realTime);
        Pictures = (TextView) findViewById(R.id.Pictures);
        Location = (TextView) findViewById(R.id.Location);
        addPic = (TextView) findViewById(R.id.addPic);
        Memo = (EditText) findViewById(R.id.Memo);
        realLocation = (EditText) findViewById(R.id.realLocation);
//        spnrMembers = (Spinner) findViewById(R.id.spnrMembers);

        btnEdit = (Button) findViewById(R.id.btnEdit); // 수정완료 버튼
        Imageview = (ImageView) findViewById(R.id.imageView2);
        icnSearch = (ImageView) findViewById(R.id.icnSearch);

        Intent intent = getIntent(); // 모임정보에서 수정 액티비티로 넘어올 때 인텐트를 넘겼음
        postkey = intent.getStringExtra("key"); // keyArraylist의 key값을 받아옴

        Calendar cal = new GregorianCalendar(); // 세팅된 날짜와 시간을 가져오기 위한 Calendar 인스턴스 선언

        UserAuthInfoSet();
        FirestoreDbSet();

        FragmentManager fragmentManager = getFragmentManager(); // 맵 세팅
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // fusedlocationproviderclient메소드 가져옴

        DisplayMetrics metrics = this.getResources().getDisplayMetrics(); // 기기화면의 가로, 세로길이를 구해 담음
        reqWidth = metrics.widthPixels;

        Imageview.setOnClickListener(new View.OnClickListener() {  // 사진추가 클릭리스너 (버전에 따라 여러개/ 1개 선택할 수 있음)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        FirestoreDbSet(); // FireStore 객체 가져오는 메소드
        DocumentReference docRef = db.collection("Moim").document(postkey); // "Moim" 콜렉션에서 문서구분 키값에 해당하는 문서 가져오기
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                moimPost_fs moimitem = documentSnapshot.toObject(moimPost_fs.class);

                Year = moimitem.year;
                Month = moimitem.month;
                Day = moimitem.day;
                Hour = moimitem.hour;
                Minute = moimitem.minute;

                Title.setText(moimitem.Title);
                Date.setText(moimitem.Date);
                realTime.setText(moimitem.Time);
                Memo.setText(moimitem.Memo);
                realLocation.setText(moimitem.Location);

                imagepath_s = moimitem.Imagepath;
                members = moimitem.Members; // 전체 인원수 세팅

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
                            Imageview.setVisibility(View.VISIBLE);
                            Imageview.setImageBitmap(bitmapImage);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                lat = moimitem.lat;
                lon = moimitem.lon;

                setMoimAddress(realLocation.getText().toString(), lat, lon, mGoogleMap); // 입력한 값을 가지고 지도에 세팅함

                currentmem = moimitem.CurrentMember; // 현재 참여멤버수 값을 저장함 (스피너에 최소값으로 반영하도록)
            }
        });

        TimeSet(realTime, EditMoimActivity.this); // 날짜, 시간피커 다이얼로그 세팅
        DateSet(Date, EditMoimActivity.this);
/*
        final ArrayList<String> list = new ArrayList<>(); // input Array data
        // 현재 참여멤버수 이상으로 참여정원을 선택할 수 있도록 어레이 세팅함. 참여멤버수가 많은 순으로 거꾸로 조건줘야 됨
        if(currentmem >= 8) {
            list.add("8");
        }else if(currentmem >= 7){
            list.add("7");
        }else if(currentmem >= 6){
            list.add("6");
        }else if(currentmem >= 5){
            list.add("5");
        }else if(currentmem >= 4){
            list.add("4");
        }else if(currentmem >= 3){
            list.add("3");
        }else if(currentmem >= 2) {
            list.add("2");
        }

        ArrayAdapter spnrLabelAdapter; /// using ArrayAdapter
        spnrLabelAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list);
        spnrMembers.setAdapter(spnrLabelAdapter);
        spnrMembers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 노가다로 세팅하겠음 => 마지막에

                if(position == 0){
                    Members = 2;
                }else if(position == 1){
                    Members = 3;
                }else if(position == 2){
                    Members = 4;
                }else if(position == 3){
                    Members = 5;
                }else if(position == 4){
                    Members = 6;
                }else if(position == 5){
                    Members = 7;
                }else if(position == 6){
                    Members = 8;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if(Members == 2){ // 멤버설정 초기값 스피너에 세팅
            spnrMembers.setSelection(0);
        }else if(Members == 3){
            spnrMembers.setSelection(1);
        }else if(Members == 4){
            spnrMembers.setSelection(2);
        }else if(Members == 5){
            spnrMembers.setSelection(3);
        }else if(Members == 6){
            spnrMembers.setSelection(4);
        }else if(Members == 7){
            spnrMembers.setSelection(5);
        }else if(Members == 8){
            spnrMembers.setSelection(6);
        }
*/
        TimeSet(realTime, EditMoimActivity.this); // 날짜, 시간피커 다이얼로그 세팅
        DateSet(Date, EditMoimActivity.this);


        icnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = realLocation.getText().toString();
                LatLng latlng = setMoimAddress(location, lat, lon, mGoogleMap); // realLocation에 적은 문자에 해당하는 위치에 맞는 지도를 세팅
                lat = latlng.latitude;
                lon = latlng.longitude;
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = Title.getText().toString();
                String memo = Memo.getText().toString();
                String date = Date.getText().toString();
                String time = realTime.getText().toString();

                if (Title.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "제목을 입력하세요!", Toast.LENGTH_SHORT).show();
                    Title.requestFocus();
                    return;
                }

                // [moimPost_fs클래스] 필요한 정보를 다 넣은 생성자를 만들어서 moimPost 객체에 넣음
                moimPost_fs moo = new moimPost_fs(title, memo, date, time, members, UID, imagepath_s, realLocation.getText().toString(),
                        lat, lon, Year, Month, Day, Hour, Minute, 0);
                db.collection("Moim").document().set(moo) // firestore에 저장
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");

                                db.collection("Moim").document(postkey).delete(); // 원래 글 삭제
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });
                finish();
            }
        });
    }

    public void onMapReady(GoogleMap map) { // 처음에 지도를 준비하는 코드

        mGoogleMap = map;

        Log.e("OnMapReady ", "when ");

        // 런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기 전에 // 지도의 초기위치를 서울로 이동
//        setDefaultLocation();

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
            startLocationUpdates(); // 3. 위치 업데이트 시작

        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 3-3. 사용자에게 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( EditMoimActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
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
                Log.d(TAG, "onMapClick :");
            }
        });

        // 그냥 BaseActivity에 있는 setMoimAddress 메소드에 맞게 리턴값으로 위경도를 받음
//        LatLng latlng = setMoimAddress(realLocation.getText().toString(), lat, lon, mGoogleMap); // 입력한 값을 가지고 지도에 세팅함
    }

    public void setDefaultLocation() {

        // 디폴트 위치, 가져온 위/경도 설정값
        LatLng DEFAULT_LOCATION = new LatLng(lat, lon);
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

    public LatLng setMoimAddress(String location, double lat, double lon, GoogleMap map){
        final Geocoder geocoder = new Geocoder(getApplicationContext()); // 지오코더 객체 생성 -> Address 객체 생성 -> 필요한 정보 받아옴 getLatitude 등등
        List<Address> list11 = null;

        try {
            list11 = geocoder.getFromLocationName // Geocoder의 getFromLocationName 메소드는 주소를 List 변수에 저장
                    (location, // 지역 이름
                            10); // 읽을 개수
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test","입출력 오류 - 서버에서 주소변환시 에러발생");
        }

        if (list11 != null) {
            if (list11.size() == 0) {
                //                     tv.setText("해당되는 주소 정보는 없습니다");
            } else {
                // 해당되는 주소로 인텐트 날리기
                Address addr = list11.get(0); /// Address 형태로 List<Address>를 전달
                lat = addr.getLatitude();
                lon = addr.getLongitude();

                // Creating a LatLng object for the current location
                LatLng latLng = new LatLng(lat, lon);

                // Showing the current location in Google Map
                map.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                // Map을 zoom함. 아니면 너무 멀리보임
                setZoomLevel(16, map);

                // 마커 설정.
                MarkerOptions optFirst = new MarkerOptions();
                optFirst.position(latLng); // 위도 • 경도

                map.addMarker(optFirst).showInfoWindow();
            }
        }

        return new LatLng(lat, lon);
    }

    public void setZoomLevel(int level, GoogleMap map) {
        map.animateCamera(CameraUpdateFactory.zoomTo(level));
//        Toast.makeText(this, "Zoom Level : " + String.valueOf(level), Toast.LENGTH_LONG).show();
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

            List<android.location.Location> locationList = locationResult.getLocations(); //

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
            }
            else {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(EditMoimActivity.this); // 경고 다이얼로그 빌더를 만듬
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
        builder.create().show(); // 빌더를 만들고
        // 띄움
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
                case GALLERY_CODE:

                    Uri uri = data.getData(); // 사진 1장만 선택했을 때 data.getData()로 URI값 받아옴
                    ClipData clipdata = data.getClipData();

                    if(clipdata !=null){

                        for(int i =0; i <3; i++){
                            if(i<clipdata.getItemCount()){
                                Uri urione = clipdata.getItemAt(i).getUri();
                                switch (i){
                                    case 0:
                                        Imageview.setVisibility(View.VISIBLE);
                                        sendPicture(urione, Imageview, reqWidth);

                                        imagepath_s = getRealPathFromURI(urione);
          /*                              picturepath = getRealPathFromURI(urione); // 셰어드에 넣기 위해 파일경로 저장
                                        picturepath2 = ""; // 셰어드에 빈값 "" 넣기 위해 저장
                                        picturepath3 = "";
                                        Log.e("picturepath in case0", "  1 "+picturepath +"  2 "+ picturepath2 +"  3 "+picturepath3); */
          /*                            imageView2.setVisibility(View.GONE);
                                        imageView3.setVisibility(View.GONE);  */
                                        break;
                                }
                            }
                        }
                    }else if(uri != null){ // 사진 1장만 선택했을 때
                        Imageview.setVisibility(View.VISIBLE);

                        sendPicture(uri, Imageview, reqWidth); // URI값 가져와서 메소드에 넣어줌 => 세팅됨
                        imagepath_s = getRealPathFromURI(uri);
                    }
                    break;

                case GPS_ENABLE_REQUEST_CODE:

                    //사용자가 GPS 활성시켰는지 검사
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