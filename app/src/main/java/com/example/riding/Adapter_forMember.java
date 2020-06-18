package com.example.riding;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter_forMember extends RecyclerView.Adapter<Adapter_forMember.memberViewHolder> {

    protected ArrayList<Member> Arraylist; /// 모임 아이템 어레이리스트
    protected ArrayList<String> KeyArraylist; /// 모임 아이템 어레이리스트

    public ExifInterface exif = null;
    public String Tag = "This is Adapter class";

    public class memberViewHolder extends RecyclerView.ViewHolder { // 1. 리스너 추가

        protected TextView Nickname; // 뷰홀더 내부 뷰요소 변수로 선언
        protected TextView Message;
        protected CircleImageView profileImage;
        protected ExifInterface exif; // 사진 회전시키는 메소드 쓰기 위해 변수선언

        public final View itemView; // 아이템 클릭 이벤트 처리하기 위해 뷰(아이템뷰) 선언
        protected String UID;

        public memberViewHolder(View view) { //// 뷰홀더 생성자
            super(view);
            this.Nickname = (TextView) view.findViewById(R.id.Name); /// 뷰홀더 변수 연결
            this.Message = (TextView) view.findViewById(R.id.Message);
            this.profileImage = (CircleImageView) view.findViewById(R.id.profileImage);
            this.exif = null; // 꼬이지 않게 매번 초기화(null) 해줘야됨

            itemView = view; // 레이아웃 객체화 (?) 아이템 클릭이벤트 처리하기 위해 선언

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // FirebaseAuth 객체에서 최근 접속한 FirebaseUser 객체 가져옴 (로그인한 User 정보 가져오기 위해)
            UID = user.getUid();
        }
    }

    public class memberzzangViewHolder extends RecyclerView.ViewHolder { // 1. 리스너 추가

        protected TextView Nickname; // 뷰홀더 내부 뷰요소 변수로 선언
        protected TextView Message;
        protected CircleImageView profileImage;
        protected TextView Manager;

        public final View itemView; // 아이템 클릭 이벤트 처리하기 위해 뷰(아이템뷰) 선언

        public memberzzangViewHolder(View view) { //// 뷰홀더 생성자
            super(view);
            this.Manager = (TextView) view.findViewById(R.id.manager);
            this.Nickname = (TextView) view.findViewById(R.id.Name); /// 뷰홀더 변수 연결
            this.Message = (TextView) view.findViewById(R.id.Message);
            this.profileImage = (CircleImageView) view.findViewById(R.id.profileImage);

            itemView = view; // 레이아웃 객체화 (?) 아이템 클릭이벤트 처리하기 위해 선언
        }
    }

    public Adapter_forMember(ArrayList<Member> list) { // 어댑터 생성자, 만든 액티비티의 Context값을 넣고, arraylist 값을 넣음
        Arraylist = list;
    }

    public Adapter_forMember(ArrayList<Member> list, ArrayList<String> llist) { // 어댑터 생성자, 만든 액티비티의 Context값을 넣고, arraylist 값을 넣음
        Arraylist = list;
        KeyArraylist = llist;
    }

/*    @Override
    public int getItemViewType(int position) {
        if (Arraylist.get(position).getItemViewType() == 0) {
            return VIEW_TYPE_ZZANG;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }   */

    @NonNull @Override
    public Adapter_forMember.memberViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
/*       if (viewType == VIEW_TYPE_ZZANG) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member1_item, viewGroup, false);
            return new memberzzangViewHolder(v);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_item , viewGroup, false);
            return new memberViewHolder(v);
        }   */

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_item, viewGroup, false);
        return new memberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull Adapter_forMember.memberViewHolder viewHolder, final int position) {

        if(!Arraylist.get(position).Nickname.equals("")) {
            viewHolder.Nickname.setText(Arraylist.get(position).Nickname);
        }
        if(!Arraylist.get(position).Message.equals("")) {
            viewHolder.Message.setText(Arraylist.get(position).Message);
        }

/* 절대경로에서 꺼내던 거
        if(!Arraylist.get(position).Imagepath.equals("")){
            try {
                viewHolder.exif = new ExifInterface(Arraylist.get(position).Imagepath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int exifOrientation = viewHolder.exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);
            Bitmap bitmap = BitmapFactory.decodeFile(Arraylist.get(position).Imagepath);
            viewHolder.profileImage.setImageBitmap(rotate(bitmap, exifDegree)); // 이미지 뷰에 비트맵 넣기
        } */

        // 이미지 세팅은 스토리지에서 꺼내와서 한다
        StorageReference sr = FirebaseStorage.getInstance().getReference().child("profile/"+ Arraylist.get(position).UID);
        try {
            final File file = File.createTempFile("profile", "jpg"); // 임시파일 생성
            // createTempFile(접두사 문자열, 접미사 문자열(파일 확장자), 파일 만들 디렉토리(기본일 경우 null))
            sr.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() { // FileDownloadTask.TaskSnapshot
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Success Case
                    Bitmap bitmapImage = BitmapFactory.decodeFile(file.getPath()); // 파일의 경로를 가져와서 비트맵으로 변환해줌
                    viewHolder.profileImage.setImageBitmap(bitmapImage);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
/*                Context context = v.getContext();
                Intent intent = new Intent(context, FriendProfileActivity.class);
                String value = KeyArraylist.get(position); // 키 값을 가져옴
                intent.putExtra("key", value);
                context.startActivity(intent);*/

                Context context = v.getContext();

                // 커스텀 다이얼로그를 생성. 사용자가 만든 클래스이다.
                MemberProfileDialog dialog = new MemberProfileDialog(context);
                // 커스텀 다이얼로그를 호출한다.  ( // 커스텀 다이얼로그의 결과를 출력할 TextView를 매개변수로 같이 넘겨준다. dialog.callFunction(main_label); )

                dialog.callFunction(KeyArraylist.get(position), viewHolder.UID);
/*                if(!KeyArraylist.get(position).equals(viewHolder.UID)) { // 선택한 멤버 UID가 뷰홀더 UID(로그인한 UID)와 같지 않을 때 (로그인한 유저의 프로필이 아닐 때)
                    dialog.callFunction(KeyArraylist.get(position), viewHolder.UID);
                }else if(KeyArraylist.get(position).equals(viewHolder.UID)){ // 선택한 멤버 프로필이 로그인한 유저의 프로필일 때
                    Toast.makeText(context, "내 프로필입니다", Toast.LENGTH_SHORT);
                } */

            }
        });
    }

    @Override
    public int getItemCount() { return Arraylist.size(); }

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
        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }
}