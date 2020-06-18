package com.example.riding;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class ChatFragment extends BaseFragment {

    private RecyclerView recyclerView; // 리사이클러뷰 변수 선언
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm");

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false); // 채팅 프래그먼트
        recyclerView = (RecyclerView) view.findViewById(R.id.chatfragment_recyclerview);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter()); // Chat-Adapter 연결
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext())); // 리사이클러뷰 레이아웃 매니저

        return view;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatmodel = new ArrayList<>(); // 채팅방 정보를 담을 어레이리스트
        private String uid;
        private ArrayList<String> destinationUsers = new ArrayList<>();

        public ChatRecyclerViewAdapter(){
            UserAuthInfoSet();
            uid = UID; // 로그인한 UID
            Log.e("Adapter UID ", UID);

            FirebaseDatabase.getInstance().getReference().child("Chatrooms").orderByChild("users/"+UID) // 로그인한 UID를 채팅멤버로 포함하는 채팅방 데이터를 가져옴
                    .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.e("Read ", "Success ");

                            chatmodel.clear(); // 채팅방 리스트 클리어
                            for(DataSnapshot item : dataSnapshot.getChildren()){
                                    // 채팅방 리스트에 ChatModel.class값 넣기
                                    chatmodel.add(item.getValue(ChatModel.class)); // item의 밸류값을 가져오기 -> ChatModel.class 형태로
                                    Log.e("TAG", String.valueOf(item));
                                    Log.e("chatmodel ", String.valueOf(chatmodel));
                            }
                            notifyDataSetChanged(); // 데이터가 바뀌었음을 어댑터에 알리기
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chatroom_item, viewGroup, false); // 뷰홀더에 채
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder viewHolder, final int position) {

            CustomViewHolder customViewHolder = (CustomViewHolder) viewHolder;
            String destinationUID = null;

            for(String user: chatmodel.get(position).users.keySet()){ // 일일이 챗방에 있는 유저를 체크
                 if(!user.equals(UID)) { // 내가 아닌 사람
                     destinationUID = user;
                     destinationUsers.add(destinationUID);
                 }
            }

            FirebaseFirestore.getInstance().collection("Profile").document(destinationUID)
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Member member = documentSnapshot.toObject(Member.class);

                    try {
                        ((CustomViewHolder) viewHolder).exif = new ExifInterface(member.Imagepath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int exifOrientation = ((CustomViewHolder) viewHolder).exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int exifDegree = exifOrientationToDegrees(exifOrientation);
                    Bitmap bitmap = BitmapFactory.decodeFile(member.Imagepath);
                    ((CustomViewHolder) viewHolder).imageView.setImageBitmap(rotate(bitmap, exifDegree)); // 이미지 뷰에 비트맵 넣기
                    ((CustomViewHolder) viewHolder).textView_title.setText(member.Nickname);
                }
            });

            // 메세지를 내림차순으로 정렬 후 마지막 메세지의 키값을 가져옴
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.<String>reverseOrder());
//            Map<String, ChatModel.Comment> commentMap = new TreeMap<>();

            commentMap.putAll(chatmodel.get(position).comments);
            Log.e("commentMap ", String.valueOf(chatmodel.get(position).comments));

/*            String lastMessageKey = (String) commentMap.keySet().toArray()[0];
            String lastMessageKey = (String) commentMap.keySet().toArray()[(commentMap.size()-1)];

            customViewHolder.textView_last_message.setText(chatmodel.get(position).comments.get(lastMessageKey).message);*/

            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), MessageActivity.class);
                    intent.putExtra("destinationUID", destinationUsers.get(position));
                    startActivity(intent);
                }
            });

            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
/*            long unixTime = (long) chatmodel.get(position).comments.get(lastMessageKey).timestamp;
            Date date = new Date(unixTime);
            customViewHolder.textView_timestamp.setText(simpleDateFormat.format(date));*/
        }

        @Override
        public int getItemCount() {
            return chatmodel.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textView_title;
            public TextView textView_last_message;
            protected ExifInterface exif; // 사진 회전시키는 메소드 쓰기 위해 변수선언
            public TextView textView_timestamp;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);

                imageView = (ImageView) itemView.findViewById(R.id.chatitem_imageView);
                textView_title = (TextView) itemView.findViewById(R.id.chatitem_textView_title);
                textView_last_message = (TextView) itemView.findViewById(R.id.chatitem_textView_lastMessage);
                textView_timestamp = (TextView) itemView.findViewById(R.id.chatitem_textView_timestamp);
                this.exif = null; // 꼬이지 않게 매번 초기화(null) 해줘야됨
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
            // Matrix 객체 생성
            Matrix matrix = new Matrix();
            // 회전 각도 셋팅
            matrix.postRotate(degree);
            // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
            return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                    src.getHeight(), matrix, true);
        }
    }
}