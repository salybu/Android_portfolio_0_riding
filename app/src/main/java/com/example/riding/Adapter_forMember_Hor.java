package com.example.riding;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter_forMember_Hor extends RecyclerView.Adapter<Adapter_forMember_Hor.memberHorViewHolder> {

    protected ArrayList<Member> Arraylist; /// 멤버 아이템 어레이리스트
    public String Tag = "This is Adapter class";
    public Context context;

    final int CONN_TIME = 5000;

    public class memberHorViewHolder extends RecyclerView.ViewHolder {

        protected TextView Nickname; // 뷰홀더 내부 뷰요소 변수로 선언
        protected TextView Message;
        protected CircleImageView profileImage;
        protected ExifInterface exif; // 사진 회전시키는 메소드 쓰기 위해 변수선언

        public final View itemView; // 아이템 클릭 이벤트 처리하기 위해 뷰(아이템뷰) 선언

        public memberHorViewHolder(View view) { //// 뷰홀더 생성자
            super(view);
            this.Nickname = (TextView) view.findViewById(R.id.Name); /// 뷰홀더 변수 연결
            this.Message = (TextView) view.findViewById(R.id.Message);
            this.profileImage = (CircleImageView) view.findViewById(R.id.profileImage);
            this.exif = null; // 꼬이지 않게 매번 초기화(null) 해줘야됨

            itemView = view; // 레이아웃 객체화 (?) 아이템 클릭이벤트 처리하기 위해 선언
        }
    }

    public Adapter_forMember_Hor(ArrayList<Member> list) { // 어댑터 생성자, 만든 액티비티의 Context값을 넣고, arraylist 값을 넣음
        Arraylist = list;
    }

    @NonNull @Override
    public Adapter_forMember_Hor.memberHorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_horizon_item, viewGroup, false);
        return new Adapter_forMember_Hor.memberHorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Adapter_forMember_Hor.memberHorViewHolder viewHolder, final int position) {

        if(!Arraylist.get(position).Nickname.equals("")) {
            viewHolder.Nickname.setText(Arraylist.get(position).Nickname);
        }
        if(!Arraylist.get(position).Message.equals("")) {
            viewHolder.Message.setText(Arraylist.get(position).Message);
        }

        // Firebase Storage 객체 얻어오고, "profile/" 아래 멤버의 UID값으로 저장된 사진파일의 스토리지 레퍼런스를 선언함
//        StorageReference sr = FirebaseStorage.getInstance().getReference().child("profile/"+ Arraylist.get(position).UID+".jpeg");
/*        try {
            final File file = File.createTempFile("profile", "jpg"); // 임시파일 생성
            // createTempFile(접두사 문자열, 접미사 문자열(파일 확장자), 파일 만들 디렉토리(기본일 경우 null))
            sr.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() { // FileDownloadTask.TaskSnapshot
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Success Case
                    Bitmap bitmapImage = BitmapFactory.decodeFile(file.getPath());
                    viewHolder.profileImage.setImageBitmap(bitmapImage);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }   로컬에 받아와서 저장할 때 코드 */


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

/*       sr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
//                BaseActivity base = new BaseActivity();

                class UriAsyncTask extends AsyncTask<Uri, Void, Void> {

                    @Override
                    protected Void doInBackground(Uri... uri) {
                        try {
                            InputStream in = new URL("http://"+uri.toString()).openStream(); // uri값 받아와서 InputStream 객체로 만들고
                            Bitmap bmp = BitmapFactory.decodeStream(in); // 비트맵팩토리 사용해서 비트맵으로 변환함

/*                    Log.e("Storage ", String.valueOf(bmp));
                    ExifInterface exif = null;
                    try {
                        exif = new ExifInterface(imagePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int exifDegree = exifOrientationToDegrees(exifOrientation);  */
/*
                            viewHolder.profileImage.setImageBitmap(bmp); // 이미지 뷰에 비트맵 넣기
                        } catch(IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                }
                UriAsyncTask async = new UriAsyncTask();
                async.execute(uri);
            }
        });   */






 /*       if(!Arraylist.get(position).Imagepath.equals("")){
            try {
                viewHolder.exif = new ExifInterface(Arraylist.get(position).Imagepath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int exifOrientation = viewHolder.exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);
            Bitmap bitmap = BitmapFactory.decodeFile(Arraylist.get(position).Imagepath);
            viewHolder.profileImage.setImageBitmap(rotate(bitmap, exifDegree)); // 이미지 뷰에 비트맵 넣기
        }     */

        viewHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, MessageActivity.class); // 채팅방 내부 액티비티로 인텐트 넘김
                intent.putExtra("UIDforChat", Arraylist.get(position).UID); // 친구 UID(어레이에 담긴 멤버.포지션.의 UID)를 인텐트와 함께 넘김
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() { return Arraylist.size(); }

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