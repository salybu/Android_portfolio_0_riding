package com.example.riding;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MoimWriteActivity extends BaseActivity implements OnMapReadyCallback {

    private EditText Title;
    private EditText Memo;

    private GoogleMap mGoogleMap = null; // 구글맵 참조변수
    private Marker currentMarker = null; // 현위치 마커 변수

    private String TAG = "This is MoimWriteActivity";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됨
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    private int DEFAULT_ZOOM_LEVEL = 13;

    private double lat; // 받아온 위도값 (프로필에서 넘어온 주소에 마커 추가하기 위해)
    private double lon; // 경도값
    private String address; // 주소값
    private String name; // 이름

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    Location mCurrentLocatiion; // 현위치
    LatLng currentPosition; // 현위치 위도, 경도

    List<Marker> previous_marker = null; // 추가할 마커 리스트 정보

    private FusedLocationProviderClient mFusedLocationClient; // 현재위치 받아올 FusedLocationProviderClient 객체
    private LocationRequest locationRequest; // 위치 업데이트에 대한 서비스 품질을 요청하는데 사용됨(?)
    private Location location;

    private final int GALLERY_CODE = 1112;

    private TextView Date;
    private TextView realTime;
    private int Members = 0;
    int reqWidth;

    private View mLayout; // Snackbar 사용하기 위해서는 View가 필요합니다.  // (참고로 Toast에서는 Context가 필요했습니다.)

    private EditText realLocation;
    private TextView PicturesPick;
    private ImageView imageView;
    private ImageView icnMap;
    private ImageView icnSearch;
    private String imagePath = "/storage/emulated/0/Download/12029_69730_2733.jpg";
    private Spinner spnrMembers;
    private Button btnDone;

/*    // RealTime Database 에서 가져오기
    final FirebaseDatabase database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 객체를 가져옴
    final DatabaseReference myRef = database.getReference(); // 데이터베이스에서 레퍼런스를 얻어옴 */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moim_write);

        // RealTime Database 에서 가져오기
        final FirebaseDatabase database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 객체를 가져옴
        final DatabaseReference myRef = database.getReference(); // 데이터베이스에서 레퍼런스를 얻어옴

        UserAuthInfoSet();
        FirestoreDbSet();

        Title = (EditText) findViewById(R.id.Title); // 위젯연결
        Memo = (EditText) findViewById(R.id.Memo);
        spnrMembers = (Spinner) findViewById(R.id.spnrMembers);
        realLocation = (EditText) findViewById(R.id.realLocation);
        Date = (TextView) findViewById(R.id.Day);
        realTime = (TextView) findViewById(R.id.realTime);
        PicturesPick = (TextView) findViewById(R.id.Pictures);
        imageView = (ImageView) findViewById(R.id.imageView);
        icnMap = (ImageView) findViewById(R.id.icnMap);
        icnSearch = (ImageView) findViewById(R.id.icnSearch);
        btnDone = (Button) findViewById(R.id.btnEdit);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        todayset(); // 현재 캘린더 인스턴스 가져와서 오늘 날짜로 세팅

        DateUpdate(Date); // 현재 날짜와 시간을 화면위젯과 연결해 세팅함
        TimeUpdate(realTime);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // fusedlocationproviderclient메소드 가져옴

        DisplayMetrics metrics = this.getResources().getDisplayMetrics(); // 기기화면의 가로, 세로길이를 구해 담음
        reqWidth = metrics.widthPixels;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){   //// 갤러리앱 접근 권한 동의받는 거
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        final ArrayList<String> list = new ArrayList<>(); // input Array data
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("7");
        list.add("8");

        ArrayAdapter spnrLabelAdapter; /// using ArrayAdapter
        spnrLabelAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list);
        spnrMembers.setAdapter(spnrLabelAdapter);
        spnrMembers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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

 /*       icnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoimWriteActivity.this, MapActivity.class);
                startActivityForResult(intent, 109);  // 구글맵 마커 찍은 거 가져올 때 쓰던 인텐트
            }
        });  */

        TimeSet(realTime, MoimWriteActivity.this); // 날짜, 시간피커 다이얼로그 세팅
        DateSet(Date, MoimWriteActivity.this);

        PicturesPick.setOnClickListener(new View.OnClickListener() {  // 사진추가 클릭리스너 (버전에 따라 여러개/ 1개 선택할 수 있음)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
//                startActivityForResult(Intent.createChooser(intent,"다중 선택(최대 3장)을 하려면 '포토'를 선택하세요."), GALLERY_CODE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {  // 사진추가 클릭리스너 (버전에 따라 여러개/ 1개 선택할 수 있음)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
//                startActivityForResult(Intent.createChooser(intent,"다중 선택(최대 3장)을 하려면 '포토'를 선택하세요."), GALLERY_CODE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        icnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = realLocation.getText().toString();
                LatLng latlng = setMoimAddress(location, lat, lon, mGoogleMap); // realLocation에 적은 문자에 해당하는 위치에 맞는 지도를 세팅
                lat = latlng.latitude;
                lon = latlng.longitude;
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
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
                    moimPost_fs moo = new moimPost_fs(title, memo, date, time, Members, UID, imagePath, realLocation.getText().toString(), lat, lon, Year, Month, Day, Hour, Minute, 0);
                    db.collection("Moim").document().set(moo) // firestore에 저장
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });

                // 셰어드에도 저장하는 코드
                SharedPreferences pref = getSharedPreferences("Moims", MODE_PRIVATE); // "Moims" 셰어드 파일을 꺼냄
                SharedPreferences.Editor editor = pref.edit(); // 셰어드 파일을 수정할 에디터 불러옴

                String eventsArray2 = pref.getString("Entire", ""); // 전체 목록에서 보여줘야 되니까 셰어드 따로 한번 더 꺼내옴
                // "키값:Entire"에 "밸류값:JSONArray"는 이미 하나 만들어둠

                JSONArray eventsJSONArray2 = null;
                try {
                    eventsJSONArray2 = new JSONArray(eventsArray2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject event2 = new JSONObject();
                try {
                    event2.put("Title", title);
                    event2.put("Memo", memo);
                    event2.put("Date", date);
                    event2.put("Time", time);
                    event2.put("Members", Members);
                    event2.put("Imagepath", imagePath);
                    event2.put("CurrentMember", 0);
                    event2.put("Location", realLocation.getText().toString()); // 지역 고르는거 다음에 넣기
                    event2.put("UID", UID);
                    event2.put("Lat", lat);
                    event2.put("Lon", lon);
                    event2.put("MemberEmail", email);
                    event2.put("Bookmark", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                eventsJSONArray2.put(event2);
                editor.putString("Entire", eventsJSONArray2.toString());
                editor.commit();
                finish();

/*                if(FirebaseNetworkException.)
                    try{
                    }catch(FirebaseNetworkException e){
                    }  */
/*                moimPost moo = new moimPost(title, memo, date, time, Members, imagePath);
                myRef.child("moimlist/").push().setValue(moo);   RealTime DB 예제 */
//                postRef.setValueAsync(moim);

/*                Map<String, Object> moim = new HashMap<>();
                moim.put("title", title);
                moim.put("memo", memo);
                moim.put("date", date);
                moim.put("time", time);
                moim.put("UID", UID);
                moim.put("Email", Email);

                db.collection("Moim")
                        .add(moim) // 문서 추가할 때 유의미한 ID를 만들지 않고 Firestore가 자동으로 ID를 생성하도록 하는 메소드 Add
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });    firestore에 해시맵 이용해서 넣기 */

/*                private void submitPost() { // document UserID에 클래스 넣어서 저장
                    final String userId = getUid();
                    DocumentReference docRef = db.collection("users").document(userId);
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            moimPost_fs moimpost_fs = documentSnapshot.toObject(moimPost_fs.class);
                            if (moimpost_fs == null) {
                            } else {
                                writeNewPost(userId, user.username, title, body);
                                Post post = new Post(userId, username, title, body);
                                db.collection("posts").add(post);
                            }
                        }
                    });
                }  */
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap map) { // 처음에 지도를 준비하는 코드

        mGoogleMap = map;

        // 런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기 전에 // 지도의 초기위치를 서울로 이동
        setDefaultLocation();

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
                        ActivityCompat.requestPermissions( MoimWriteActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(MoimWriteActivity.this); // 경고 다이얼로그 빌더를 만듬
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
                                        PicturesPick.setVisibility(View.GONE);
                                        imageView.setVisibility(View.VISIBLE);
                                        sendPicture(urione, imageView, reqWidth);
                                        imagePath = getRealPathFromURI(urione);
          /*                              picturepath = getRealPathFromURI(urione); // 셰어드에 넣기 위해 파일경로 저장
                                        picturepath2 = ""; // 셰어드에 빈값 "" 넣기 위해 저장
                                        picturepath3 = "";
                                        Log.e("picturepath in case0", "  1 "+picturepath +"  2 "+ picturepath2 +"  3 "+picturepath3); */
          /*                            imageView2.setVisibility(View.GONE);
                                        imageView3.setVisibility(View.GONE);  */
                                        break;
         /*                         case 1:
                                        imageView2.setVisibility(View.VISIBLE);
                                        sendPicture(urione, imageView2,1);
                                        imageView3.setVisibility(View.GONE);
                                        break;
                                    case 2:
                                        imageView3.setVisibility(View.VISIBLE);
                                        sendPicture(urione, imageView3,2);
                                        break;  */
                                }
                            }
                        }
                    }else if(uri != null){ // 사진 1장만 선택했을 때
                        PicturesPick.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        sendPicture(uri, imageView, reqWidth); // URI값 가져와서 메소드에 넣어줌 => 세팅됨
                        imagePath = getRealPathFromURI(uri);
                    }
                    break;

                case 109: // 위치 지정하기 위해 지도 액티비티 띄우고 주소 받아옴
                    realLocation.setText(data.getStringExtra("Address")); // 지도 액티비티에서 인텐트로 받아온 주소값을 세팅함
                    break;

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

        class moimPost_fs {

            public String Title;
            public String Memo;
            public String Date;
            public String Time;
            public String Location;
            public int Members;
            public int CurrentMember;
            public String Imagepath;
            public String UID;
            public String key;
            public double lat;
            public double lon;
            public ArrayList<String> MemberUID;
            public HashMap<String, Boolean> Bookmark;

            // 정렬하기 위해 Date, Time을 쪼개줌
            public int year;
            public int month;
            public int day;
            public int hour;
            public int minute;

            public Date dateforalign; // 모든 모임에서 정렬을 위해 날짜+시간을 Date로 바꿔줌
            public Timestamp timestamp; // 사진 저장하기 위한 고유키값으로 사용하려고 지정함

            public moimPost_fs(){
            }

            public moimPost_fs(String title, String memo, String date, String time, int members, String uid, int i){
                this.Title = title;
                this.Memo = memo;
                this.Date = date;
                this.Time = time;
                this.Members = members;
                this.UID = uid;
            }

            public moimPost_fs(String title, String memo, String date, String time, int members, String uid, String imagepath, String location,
                               double lat, double lon, int year, int month, int day, int hour, int min, int i) {
                this.Title = title;
                this.Memo = memo;
                this.Date = date;
                this.Time = time;
                this.Members = members;
                this.UID = uid;
                this.Imagepath = imagepath;
                this.Location = location;
                this.lat = lat;
                this.lon = lon;
                this.CurrentMember = 1;

                this.MemberUID = new ArrayList<>(); // 멤버 리스트 객체 생성
                this.MemberUID.add(uid);

                this.Bookmark = new HashMap<>();
                this.Bookmark.put(uid, false);

/*                String[] datesplit = this.Date.split(" "); // Date 값 쪼개서 연,월,일로 구분
                this.year = Integer.parseInt(datesplit[0]); // 쪼갠 String 연도값을 int로 파싱함
                this.month = Integer.parseInt(datesplit[2]);
                this.day = Integer.parseInt(datesplit[4]);

                String[] timesplit = this.Time.split(" "); // Time 값 쪼개서 오전/오후,시,분으로 구분
                this.ampm = timesplit[0]; // 쪼갠 String AM/PM 값 담음
                this.hour = Integer.parseInt(timesplit[1]); // 쪼갠 String 시간값을 int로 파싱해 변수에 넣음
                this.minute = Integer.parseInt(timesplit[3]); 굳이 쪼갤 필요가 없음 */

                this.year = year;
                this.month = month;
                this.day = day;
                this.hour = hour; // 24시간 기준으로 따로 저장해둔 거
                this.minute = min;

                this.timestamp = Timestamp.now(); // 현재시간에 맞는 타임스탬프 가져옴

                StorageReference stRef = FirebaseStorage.getInstance().getReference().child("Moim/"+timestamp); // 파이어베이스 스토리지 프로필 아래 timestamp로 모임이미지 저장
                // 사진경로를 Filestream으로 변환해서 FirebaseStorage에 저장할 거임
                InputStream stream = null; // InputStream 새로운 객체를 만들고 null값 초기화
                try {
                    stream = new FileInputStream(new File(imagepath)); // 이미지경로로 새 파일 만들고, 새 파일을 FileInputStream에 넣어서 stream 변수에 넣음
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // UploadTask
                UploadTask uploadTask = stRef.putStream(stream); // 앞서 생성한 InputStream 객체를 (stRef)지정한 경로에 넣어줌
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });


                String[] datesplit = this.Date.split(" "); // Date 값 쪼개서 연,월,일로 구분
                int iyear = Integer.parseInt(datesplit[0]); // 쪼갠 String 연도값을 int로 파싱함
                int imonth = Integer.parseInt(datesplit[2]) +1 ; // 쪼갠 "월"값에서 -1 해주기(원래 Date에 넣을 때 +1 해줘야함)
                int iday = Integer.parseInt(datesplit[4]);

                String stringDatem = iyear + " / " + imonth + " / " + iday; // 위에서 "월"값을 조정해준 걸 가지고 포맷에 맞게 다시 씀
                String stringdate = this.Date + "  " + this.Time;
                try {
                    dateforalign = new SimpleDateFormat("yyyy / MM / dd  a hh : mm").parse(stringdate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.e("Date for align ", String.valueOf(dateforalign));
                Log.e("Date for align month",String.valueOf(dateforalign.getMonth()));
            }

            public moimPost_fs(String title, String memo, String date, String time, int members, String imagepath, String uid, int i){
                this.Title = title;
                this.Memo = memo;
                this.Date = date;
                this.Time = time;
                this.Members = members;
                this.Imagepath = imagepath;
                this.UID = uid;
            }

            public moimPost_fs(String title, String memo, String date, String time, int members, String imagepath, String uid){
                this.Title = title;
                this.Memo = memo;
                this.Date = date;
                this.Time = time;
                this.Members = members;
                this.Imagepath = imagepath;
                this.UID = uid;

/*                this.MemberUID = new ArrayList<>(); // 멤버 리스트 객체 생성
                this.MemberUID.add(uid); // 멤버 리스트에 작성자 UID 삽입  */
            }

/*            public String getTitle() { return Title; }
            public String getMemo() { return Memo; }
            public String getDate() { return Date; }
            public String getTime() { return Time; }
            public int getMembers() { return Members; }
            public String getImagepath() { return Imagepath; } */
        }


    // 글쓰기 저장 누를 때 OnClickListner.. 인텐트 넘김
/*
    View.OnClickListener dOnClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String title = Title.getText().toString();
            String memo = Memo.getText().toString();

            if (Title.getText().toString().length() == 0) {
                Toast.makeText(getApplicationContext(), "제목을 입력하세요!", Toast.LENGTH_SHORT).show();
                Title.requestFocus();
                return;
            }

            SharedPreferences pref = getSharedPreferences("Events", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit(); }
            String eventsArray = pref.getString(id, "");

            JSONArray eventsJSONArray = null;
            try {
                eventsJSONArray = new JSONArray(eventsArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject event = new JSONObject();
            try {
                event.put("Title", "<< "+ title+ " >>");
                event.put("Memo", memo);

                if(!realLocation.getText().toString().equals("위치 추가를 원할 시 아이콘 클릭 →")){ // 위치를 추가했을 경우
                    event.put("Address", realLocation.getText().toString());
                }else{
                    event.put("Address", "");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            eventsJSONArray.put(event);
            editor.putString(id, eventsJSONArray.toString());
            editor.commit();

                PendingIntent pendingIntent = PendingIntent.getBroadcast(WriteActivity.this, 0, intentforreceiver, PendingIntent.FLAG_UPDATE_CURRENT);
                // 리시버를 동작하게 하기 위한 펜딩인텐트 인스턴스를 생성할 때 getBroadcast 메소드를 사용함

                if (Build.VERSION.SDK_INT >= 19) { // setExact메소드는 API 레벨 19 (킷캣) 이상에서 사용가능함. set메소드보다 더 정확하게 스케줄링됨.
                    alarmmanager.setExact(AlarmManager.RTC_WAKEUP, calendarforalarm.getTimeInMillis(), pendingIntent);
                } else {
                    alarmmanager.set(AlarmManager.RTC_WAKEUP, calendarforalarm.getTimeInMillis(), pendingIntent);
                }
            }

            /// 피드 띄우는 인텐트
            Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
    };

            @IgnoreExtraProperties
            class FirebasePost {
                public String id;
                public String name;
                public Long age;
                public String gender;

                public FirebasePost() {
                    // Default constructor required for calls to DataSnapshot.getValue(FirebasePost.class)
                }

                public FirebasePost(String id, String name, Long age, String gender) {
                    this.id = id;
                    this.name = name;
                    this.age = age;
                    this.gender = gender;
                }

                @Exclude
                public Map<String, Object> toMap() {
                    HashMap<String, Object> result = new HashMap<>(); // 해시맵<String, Object>에 결과를 넣음
                    result.put("id", id);
                    result.put("name", name);
                    result.put("age", age);
                    result.put("gender", gender);
                    return result;
                }
            }

/*    public void postFirebaseDatabase(boolean add){

        DatabaseReference mPostReference = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;
        if(add){
//            FirebasePost post = new FirebasePost(ID, name, age, gender);
//            postValues = post.toMap();
        }
//        childUpdates.put("/id_list/" + ID, postValues);
        mPostReference.updateChildren(childUpdates);
    }  */
/*
            class User {

                public String date_of_birth;
                public String full_name;
                public String nickname;

                public User(String dateOfBirth, String fullName) {
                    // ...
                }

                public User(String dateOfBirth, String fullName, String nickname) {
                    // ...
                }
            }
    DatabaseReference usersRef = ref.child("users");

    Map<String, User> users = new HashMap<>(); // 해시맵 users 사용하기
    users.put("alanisawesome", new User("June 23, 1912", "Alan Turing")); /// users에 넣기
    users.put("gracehop", new User("December 9, 1906", "Grace Hopper"));
    usersRef.setValueAsync(users);


    usersRef.child("alanisawesome").setValueAsync(new User("June 23, 1912", "Alan Turing"));
    usersRef.child("gracehop").setValueAsync(new User("December 9, 1906", "Grace Hopper"));


    DatabaseReference hopperRef = usersRef.child("gracehop");
    Map<String, Object> hopperUpdates = new HashMap<>();
    hopperUpdates.put("nickname", "Amazing Grace");

    hopperRef.updateChildrenAsync(hopperUpdates);
    */