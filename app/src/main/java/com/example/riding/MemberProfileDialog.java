package com.example.riding;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.constraint.Constraints.TAG;

public class MemberProfileDialog {

    private Context context;
    public FirebaseFirestore db; // FirebaseFirestore 변수선언 (멤버 프로필 Firestore에 저장하기 위해)
    public String phonenumber;

    public ExifInterface exif = null;
    public int alreadyfriendwithme; // 이미 친구이면 1, 친구가 아니면 2

    public MemberProfileDialog(Context context) {
        this.context = context;
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void callFunction(final String UID, final String myUID) {

        final Dialog dlg = new Dialog(context); // 커스텀 다이얼로그를 정의하기위해 Dialog 클래스를 생성함
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE); // 액티비티의 타이틀바를 숨김
        dlg.setContentView(R.layout.dialog_member_profile); // 커스텀 다이얼로그의 레이아웃을 설정함
        dlg.show(); // 커스텀 다이얼로그를 노출한다.

        db = FirebaseFirestore.getInstance(); // Firestore 객체 가져오기

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final CircleImageView profileImage = (CircleImageView) dlg.findViewById(R.id.profileImage);
        final TextView Nickname = (TextView) dlg.findViewById(R.id.Nickname);
        final TextView Email = (TextView) dlg.findViewById(R.id.Email);
        final TextView Message = (TextView) dlg.findViewById(R.id.Message);
        final TextView PhoneNumber = (TextView) dlg.findViewById(R.id.PhoneNumber);
        final TextView realNickname = (TextView) dlg.findViewById(R.id.realNickname);
        final TextView realEmail = (TextView) dlg.findViewById(R.id.realEmail);
        final TextView realMessage = (TextView) dlg.findViewById(R.id.realMessage);
        final TextView realPhoneNumber = (TextView) dlg.findViewById(R.id.realPhoneNumber);
        final Button btnAddFriend = (Button) dlg.findViewById(R.id.btnAddFriend);
        final Button btnCancelFriend = (Button) dlg.findViewById(R.id.btnCancelFriend);
        final Button btnCall = (Button) dlg.findViewById(R.id.btnCall);

        DocumentReference docRef = db.collection("Profile").document(UID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Member mem = documentSnapshot.toObject(Member.class);

                realNickname.setText(mem.Nickname);
                realEmail.setText(mem.Email);
                realMessage.setText(mem.Message);
                realPhoneNumber.setText(mem.PhoneNumber);

                phonenumber = mem.PhoneNumber; // 전화걸때 사용하기 위해 전화번호를 변수에 따로 저장

      //          setImage(mem.Imagepath, profileImage);

                // 이미지 세팅은 스토리지에서 꺼내와서 한다
                StorageReference sr = FirebaseStorage.getInstance().getReference().child("profile/"+ mem.UID);
                try {
                    final File file = File.createTempFile("profile", "jpg"); // 임시파일 생성
                    // createTempFile(접두사 문자열, 접미사 문자열(파일 확장자), 파일 만들 디렉토리(기본일 경우 null))
                    sr.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() { // FileDownloadTask.TaskSnapshot
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Success Case
                            Bitmap bitmapImage = BitmapFactory.decodeFile(file.getPath()); // 파일의 경로를 가져와서 비트맵으로 변환해줌
                            profileImage.setImageBitmap(bitmapImage);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // 나랑 친구인지 아닌지 확인해서 버튼 활성화 => int 변수값[alreadyfriendwithme]으로 받아와서 이미 친구이면 친구취소버튼 활성화, 친구가 아니면 친구추가버튼 활성화
        final DocumentReference doc = db.collection("Profile").document(myUID); // "Profile" 콜렉션에서 myUID에 해당하는 문서 가져오기
        doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Member mem = documentSnapshot.toObject(Member.class);

                if(mem.FriendUID == null){ // 친구목록이 없을 때
                    btnAddFriend.setVisibility(View.VISIBLE);
                    btnCancelFriend.setVisibility(View.GONE);
                    btnCall.setVisibility(View.VISIBLE);

                }else if(mem.FriendUID.contains(UID)){ // 내 친구목록에 이친구의 UID가 포함되어 있다면
                    Log.e("my friendUID ", String.valueOf(mem.FriendUID));
                    Log.e("friendUID ", UID);
                    alreadyfriendwithme = 1;
                    Log.e("contain alrefriwme ", String.valueOf(alreadyfriendwithme));
                    btnAddFriend.setVisibility(View.GONE);
                    btnCancelFriend.setVisibility(View.VISIBLE);
                    btnCall.setVisibility(View.VISIBLE);

                    // 버튼 세팅이 제대로 잘 안됐던 건 버튼 세팅조건이 여러군데에 있어서 그런 것. if, else if, 잘 묶어서 한번에 조건 줘야함
                }else if(UID.equals(myUID)){ // 로그인한 UID의 프로필이면 (내 프로필이면)
                    btnAddFriend.setVisibility(View.GONE);
                    btnCancelFriend.setVisibility(View.GONE);
                    btnCall.setVisibility(View.GONE);

                }else{
                    alreadyfriendwithme = 2;
                    Log.e("notcontain alrefriwme ", String.valueOf(alreadyfriendwithme));
                    btnAddFriend.setVisibility(View.VISIBLE);
                    btnCancelFriend.setVisibility(View.GONE);
                    btnCall.setVisibility(View.VISIBLE);
                }
            }
        });

/* 밖에다 두니까 동작을.. 안함 그냥 안에서 세팅해도 세팅이 됨
        if(alreadyfriendwithme == 1){ // 이미 친구임
            Log.e("2 contain alrefriwme ", String.valueOf(alreadyfriendwithme));
            btnAddFriend.setVisibility(View.GONE);
            btnCancelFriend.setVisibility(View.VISIBLE);
        }else if(alreadyfriendwithme == 2){ // 친구가 아님
            Log.e("2 ntcontain alrefriwme ", String.valueOf(alreadyfriendwithme));
            btnAddFriend.setVisibility(View.VISIBLE);
            btnCancelFriend.setVisibility(View.GONE);
         }   */

        btnAddFriend.setOnClickListener(new View.OnClickListener() { // 버튼을 누르면 이 멤버와 친구가 된다
            @Override
            public void onClick(View view) {

                final DocumentReference docRef = db.collection("Profile").document(myUID); // "Profile" 콜렉션에서 myUID에 해당하는 문서 가져오기
                docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Member mem = documentSnapshot.toObject(Member.class);

                        // 바로 추가하는 코드
                        if(mem.FriendUID!=null) { // 친구 UID 리스트가 있을 때
                            ArrayList<String> array = mem.FriendUID; // 멤버 객체에서 친구UID 어레이를 가져옴
                            array.add(UID); // 친구UID 어레이에 친구 UID를 추가함

                            docRef.update("FriendUID", array); // 친구 UID 필드에 어레이를 추가함

                        }else if(mem.FriendUID == null){ // 친구 UID 리스트가 없을 때
                            ArrayList array = new ArrayList(); // 새로 만듬
                            mem.FriendUID = array;
                            array.add(UID);

                            docRef.update("FriendUID", array); // 친구 UID 필드에 어레이를 추가함
                        }
                    }
                });

 /*               final DocumentReference docRef2 = db.collection("Profile").document(UID); // "Profile" 콜렉션에서 myUID에 해당하는 문서 가져오기
                docRef2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Member mem = documentSnapshot.toObject(Member.class);

                        // 바로 추가하는 코드
                        if(mem.FriendUID!=null) { // 친구 UID 리스트가 있을 때
                            ArrayList<String> array = mem.FriendUID; // 멤버 객체에서 친구UID 어레이를 가져옴
                            array.add(myUID); // 친구UID 어레이에 친구 UID를 추가함

                            docRef.update("FriendUID", array); // 친구 UID 필드에 어레이를 추가함

                        }else if(mem.FriendUID == null){ // 친구 UID 리스트가 없을 때
                            ArrayList array = new ArrayList(); // 새로 만듬
                            mem.FriendUID = array;
                            array.add(myUID);

                            docRef.update("FriendUID", array); // 친구 UID 필드에 어레이를 추가함
                        }
                    }
                });  */

/*                // 파베에서 바꾸는 코드. 트랜잭션 이용 X 안해야겠다
                final DocumentReference docRef = db.collection("Profile").document(myUID); // "Profile" 콜렉션에서 myUID에 해당하는 문서 가져오기
                Log.e("Transaction myUID ", myUID);

                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(docRef);

                        if(snapshot !=null) {
                            // 친구리스트에 멤버UID 추가하기 위해 FriendUID 어레이 가져옴
                            ArrayList newarray = (ArrayList) snapshot.get("FriendUID");

                            Log.e("Transaction UID ", UID);

                            // 어레이리스트에 내 UID 추가
                            newarray.add(UID);
                            transaction.update(docRef, "FriendUID", newarray);

                        } else if(snapshot == null) {
                        }
                        return null;
                    }
                }); */

/*                btnAddFriend.setVisibility(View.GONE);
                btnCancelFriend.setVisibility(View.VISIBLE);
                Toast.makeText(context, "친구가 추가되었습니다", Toast.LENGTH_SHORT); */
                dlg.dismiss();
            }
        });

        btnCancelFriend.setOnClickListener(new View.OnClickListener() { // 버튼을 누르면 이 멤버와 친구가 된다
            @Override
            public void onClick(View view) {

                // 파베에서 바꾸는 코드. 트랜잭션 이용
                final DocumentReference docRef = db.collection("Profile").document(myUID); // "Profile" 콜렉션에서 myUID에 해당하는 문서 가져오기
                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(docRef);

                        // 친구리스트에 멤버UID 추가하기 위해 FriendUID 어레이 가져옴
                        ArrayList newarray = (ArrayList) snapshot.get("FriendUID");

                        // 어레이리스트에 내 UID 제거
                        newarray.remove(UID);
                        transaction.update(docRef, "FriendUID", newarray);

                        return null;
                    }
                });
                dlg.dismiss();
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tel = "tel:" + phonenumber;
                view.getContext().startActivity(new Intent("android.intent.action.DIAL", Uri.parse(tel))); // 전화번호가 나와있는 다이얼화면 보이는 인텐트
            }
        });
    }

    public void setImage(String path, ImageView view){ // Picturepath 여러곳 받아와서 세팅하기 위함

        exif = null;  // 사진 회전시켜서 넣기 위함
        try {
            exif = new ExifInterface(path); // ExifInterface 객체에 이미지 경로(imagepath)를 넣음
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        view.setImageBitmap(rotate(bitmap, exifDegree)); // 이미지 뷰에 비트맵 넣기
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

}