package com.example.riding;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Adapter_forAlarmAccept extends RecyclerView.Adapter<Adapter_forAlarmAccept.AcceptViewHolder>{

    protected ArrayList<selectAlarm> alarmArraylist; /// 모임 아이템 어레이리스트 (Firestore DB용)
    private ArrayList<String> keylist; // 알람 삭제하기 위해 key값 담을 리스트
    public FirebaseFirestore db = FirebaseFirestore.getInstance();; // FirebaseFirestore 변수선언 (멤버 프로필 Firestore에 저장하기 위해)

    protected String memnickname; // 토스트 띄울 때 쓰려고 저장
    protected String addUID; // 모임에 추가할 참여요청한 사람의 UID 가져옴

    public class AcceptViewHolder extends RecyclerView.ViewHolder { // 1. 리스너 추가

        protected TextView message5; // 뷰홀더 내부 뷰요소 변수로 선언
        protected TextView message0;
        protected TextView moim;
        protected TextView message2;
        protected TextView message3;
        protected TextView acceptordeny;
        protected ImageView moimprofile;

        protected String UID;

        public final View itemView; // 아이템 클릭 이벤트 처리하기 위해 뷰(아이템뷰) 선언

        public AcceptViewHolder(View view) { //// 뷰홀더 생성자
            super(view);
            this.message5 = (TextView) view.findViewById(R.id.message5); /// 뷰홀더 변수 연결
            this.message0 = (TextView) view.findViewById(R.id.message0);
            this.moim = (TextView) view.findViewById(R.id.moim);
            this.message2 = (TextView) view.findViewById(R.id.message2);
            this.message3 = (TextView) view.findViewById(R.id.message3);
            this.acceptordeny = (TextView) view.findViewById(R.id.acceptordeny);
            this.moimprofile = (ImageView) view.findViewById(R.id.moimprofile);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // FirebaseAuth 객체에서 최근 접속한 FirebaseUser 객체 가져옴 (로그인한 User 정보 가져오기 위해)
            UID = user.getUid();

            itemView = view; // 레이아웃 객체화(?) 아이템 클릭이벤트 처리하기 위해 선언
        }
    }

    public Adapter_forAlarmAccept(ArrayList<selectAlarm> list, ArrayList<String> klist) { // 어댑터 생성자, 만든 액티비티의 Context값을 넣고, arraylist 값을 넣음
        alarmArraylist = list;
        keylist = klist;
    }

    @NonNull
    @Override
    public Adapter_forAlarmAccept.AcceptViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.accept_item, viewGroup, false);
        return new Adapter_forAlarmAccept.AcceptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final @NonNull Adapter_forAlarmAccept.AcceptViewHolder viewHolder, final int position) {

        // 알림정보를 꺼내서 뷰홀더에 세팅 - moimKey로 정보 꺼내기
        db.collection("Moim").document(alarmArraylist.get(position).moimKey) // 알람보낸 모임 키에 해당하는 모임문서를 가져옴
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                moimPost_fs mom = documentSnapshot.toObject(moimPost_fs.class); // 가져온 문서를 모임 클래스로 형변환해 꺼내옴
                viewHolder.moim.setText(mom.Title); // 가져온 모임이름을 뷰홀더 모임이름에 세팅함

                // 이미지 세팅하기 위해 Firebase Storage에 접근해야 함
                StorageReference sr = FirebaseStorage.getInstance().getReference().child("Moim/"+ mom.timestamp);
                try {
                    Log.e("moim timestamp ", String.valueOf(mom.timestamp));
                    final File filee = File.createTempFile("Moim", "jpg"); // 임시파일 생성

                    // createTempFile(접두사 문자열, 접미사 문자열(파일 확장자), 파일 만들 디렉토리(기본일 경우 null))
                    sr.getFile(filee).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() { // FileDownloadTask.TaskSnapshot
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Log.e("onSuccess ", "Ok ");
                            // Success Case
                            Bitmap bitmapImage = BitmapFactory.decodeFile(filee.getPath()); // 파일의 경로를 가져와서 비트맵으로 변환해줌
                            viewHolder.moimprofile.setImageBitmap(bitmapImage);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 알람 보낼 때 수락인지 거절인지 구분해 담아오니까 구분해서 세팅함
                if(alarmArraylist.get(position).Alarmtype == 2){ // 수락알림인 경우
                    viewHolder.acceptordeny.setText("수락");
                }else if(alarmArraylist.get(position).Alarmtype == 3){ // 거절알림인 경우
                    viewHolder.acceptordeny.setText("거절");
                }
            }
        });

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Alarm").document(keylist.get(position)).delete(); // 파이어베이스에서 삭제를 먼저하고 로컬리스트에서 삭제해야 함
                alarmArraylist.remove(position); // 알람리스트에서 삭제
                keylist.remove(position); // 키리스트에서 삭제
                notifyDataSetChanged(); // 어댑터에 알림
            }
        });

    }

    @Override
    public int getItemCount() {
        return alarmArraylist.size();
    }
}