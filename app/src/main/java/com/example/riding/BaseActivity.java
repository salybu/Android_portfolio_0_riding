package com.example.riding;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    public ExifInterface exif = null;
    public int Year, Month, Day, Hour, Minute; // 시작할 날짜, 시간을 담을 변수

    public String UID;
    public String email;

    public FirebaseAuth auth; // FirebaseAuth 변수선언 (로그인한 파베 아이디랑 연결하기 위해)
    public FirebaseFirestore db; // FirebaseFirestore 변수선언 (멤버 프로필 Firestore에 저장하기 위해)
    public FirebaseStorage st; // FirebaseStorage 변수선언 (사진파일을 저장하기 위해)
    public StorageReference strf; // FirebaseStorag 사용하기 위해 참조변수 선언

    public DatePickerDialog dp; // 최소날짜 세팅하기 위해 날짜 픽 다이얼로그를 변수로 선언해야 함

    public void todayset(){
        Calendar cal = new GregorianCalendar(); // 현재 날짜와 시간을 가져오기 위한 Calendar 인스턴스 선언
        Year = cal.get(Calendar.YEAR);
        Month = cal.get(Calendar.MONTH);
        Day = cal.get(Calendar.DAY_OF_MONTH);
        Hour = cal.get(Calendar.HOUR_OF_DAY);
        Minute = cal.get(Calendar.MINUTE);
    }

    public void UserAuthInfoSet(){
        auth = FirebaseAuth.getInstance(); // 파이어베이스 인증객체 선언
        FirebaseUser user = auth.getCurrentUser(); // FirebaseAuth 객체에서 최근 접속한 FirebaseUser 객체 가져옴 (로그인한 User 정보 가져오기 위해)
        UID = user.getUid(); // 사용자 UID
        email = user.getEmail(); // 사용자 이메일
    }

    public void StorageSet(){
        st = FirebaseStorage.getInstance();
        strf = st.getReference();
    }

    public void FirestoreDbSet(){
        db = FirebaseFirestore.getInstance(); // Firestore 객체 가져오기
    }

    public void TimeSet(final TextView realTime, final Activity activity) {

        realTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener mTimeSetListener =
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Hour = hourOfDay;  // TODO Auto-generated method stub // 사용자가 입력한 값을 가져온뒤
                                Minute = minute;
                                TimeUpdate(realTime); // 텍스트뷰의 값을 업데이트함
                            }
                        };
                new TimePickerDialog(activity, mTimeSetListener, Hour, Minute, false).show();
            }
        });
    }

    public void DateSet(final TextView Date, final Activity activity) {

        Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Year = year;  // TODO Auto-generated method stub // 사용자가 입력한 값을 가져온뒤
                        Month = monthOfYear;
                        Day = dayOfMonth;

                        int StartMonth_edit = monthOfYear +1; // 형식 바꿔주기 위해 (+1)한 거
                        String mindate = year + "-" + StartMonth_edit + "-" + dayOfMonth + " 00:00:00.0";
                        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

                        Date minDate = null;
                        try {
                            minDate = transFormat.parse(mindate); // 설정한 오늘 날짜를 Date 형으로 치환
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        long mindatefinal = minDate.getTime(); // 오늘날짜에서 시간을 가져옴
                        dp.getDatePicker().setMinDate(mindatefinal); // 날짜 다이얼로그 변수로 선언해둠. 그래서 최소날짜 세팅할 수 있게

                        DateUpdate(Date);
                    }
                };
                // new DatePickerDialog(activity, mDateSetListener, Year, Month, Day).show();
                dp = new DatePickerDialog(activity, mDateSetListener, Year, Month, Day);
                dp.show();
            }
        });
    }

    public void DateUpdate (TextView Date) {   //// 리스너에서 선택한 값들 날짜, 시간에 저장하도록
        Date.setText(String.format("%d / %d / %d", Year, Month + 1, Day));
    }

    public void TimeUpdate (TextView realTime) {   //// 리스너에서 선택한 값들 날짜, 시간에 저장하도록
        Time time = new Time(Hour, Minute, 0);
        Format formatter = new SimpleDateFormat("a hh : mm");
        realTime.setText(formatter.format(time));
    }

    public void setImage(String path, ImageView view){ // Picturepath 여러곳 받아와서 세팅하기 위함

        exif = null;  // 사진 회전시켜서 넣기 위함
        try {
            // Exifinterface는 이미지가 가진 정보의 집합 클래스. 이미지가 가진 상세정보를 추출할 때 필요함. 아주 다양한 정보가 저장되는데 이미지의 기본값, 크기, 화소 등등
            exif = new ExifInterface(path); // ExifInterface 객체에 이미지 경로(imagepath)를 넣음. 지정된 경로에서 Exif 태그를 읽음
        } catch (IOException e) {
            e.printStackTrace();
        }
        // getAttributeInt 지정된 태그의 정수값을 반환하는 메소드 (exifInterface객체의 방향 int값, )
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL); // 정해진 상수값을 전달해 회전값을 얻어낼 수 있음
        int exifDegree = exifOrientationToDegrees(exifOrientation); //
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        view.setImageBitmap(rotate(bitmap, exifDegree)); // 이미지 뷰에 비트맵 넣기
    }

    public void sendPicture(Uri imgUri, ImageView view, int reqWidth) {
        String imagePath = getRealPathFromURI(imgUri); // path 절대경로

        Log.e("uri aaaaaaaaa", "aaaaaaaaaaaaaaaaaaaa" + imgUri);
        Log.e("reqwidth aaaaaaaaa", "aaaaaaaaaaaaaaaaaaaa" + reqWidth);

        if (!imagePath.equals("")) { // 받아온 파일경로가 있으면
            File file = new File(imagePath); /// 받아온 파일경로로 파일 객체를 만듬
            BitmapFactory.Options options = new BitmapFactory.Options(); // 비트맵팩토리에 옵션값을 새로 생성함. 비트맵팩토리(비트맵파일 만들어줌)
            options.inJustDecodeBounds = true; // 그냥 파일을 읽었다가는 OutofmemoryException 발생하니까 있는 정보를 먼저 뽑아내서 Size를 판단해주려고 함
            try {
                InputStream in = new FileInputStream(imagePath); /// 파일경로를 넣어 InputStream 객체[in]를 만듬
                Log.e("InputStream in", String.valueOf(in));

                BitmapFactory.decodeStream(in, null, options);
                in.close();
                in = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            final int width = options.outWidth;
            int inSampleSize = 1;

            if(reqWidth!=1) { // (Adapter_forMember_Hor)에서(reqWidth 구할 수 없을 때) 그냥 1 넣어줌
                if (width > reqWidth) {
                    int widthRatio = Math.round((float) width / (float) reqWidth);
                    Log.e("int widthRatio", String.valueOf(widthRatio));
                    Log.e("float reqWidth", String.valueOf(reqWidth));

                    inSampleSize = widthRatio;
                }
            }

            BitmapFactory.Options imgOptions = new BitmapFactory.Options();
            imgOptions.inSampleSize = inSampleSize;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, imgOptions); // 경로를 통해 비트맵으로 전환 +옵션줘서 이미지 크기줄임
            Log.e("bitmap", String.valueOf(bitmap));
            //         imageView.setImageBitmap(bitmap);

            ExifInterface exif = null;

            Log.e("exif aaaaaaaaa", "aaaaaaaaaaaaaaaaaaaa" + exif);

            try {
                exif = new ExifInterface(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);
//                 Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            view.setImageBitmap(rotate(bitmap, exifDegree)); // 이미지 뷰에 비트맵 넣기
        }
    }

    public int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public Bitmap rotate(Bitmap src, float degree) {

        Matrix matrix = new Matrix(); // Matrix 객체 생성
        matrix.postRotate(degree); // 회전 각도 셋팅

        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public String getRealPathFromURI(Uri contentUri) {
        int column_index=0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }
        return cursor.getString(column_index);
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
}