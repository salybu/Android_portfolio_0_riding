package com.example.riding;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends BaseActivity {

    private Button button; // 채팅 넘기는 버튼
    private EditText editText; // 메세지 작성 EditText 창

    private String UIDforChat; // 상대 UID
    private String uid;
    private String ChatRoomUID;

    private RecyclerView recyclerView;
    private Member destinationUsermodel;

    private SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        UserAuthInfoSet();
        FirestoreDbSet();

        uid = UID; // 채팅을 요구하는 UID. 단말기에 로그인된 UID
        UIDforChat = getIntent().getStringExtra("UIDforChat"); // 친구목록에서 받아온 친구 UID값을 채팅을 요구하는 UID에 대입함

        button = findViewById(R.id.messageActivity_button);
        editText = findViewById(R.id.messageActivity_editText);

        recyclerView = (RecyclerView) findViewById(R.id.messageActivity_recyclerView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel chatModel = new ChatModel(); // 채팅방 정보를 담은 클래스(ChatModel) 새로 생성

/*                chatModel.uid = UID; // ChatModel의 변수 uid에 현재 내 UID를 대입함
                chatModel.UIDforChat = UIDforChatt; // ChatModel의 변수 상대 uid에 인텐트로 받아온 채팅상대 UID를 대입함   */
                chatModel.users.put(uid, true); // 채팅방 클래스(chatModel)의 변수 users 맵에 내 uid,와 상대 UID를 대입함
                chatModel.users.put(UIDforChat, true); // 채팅을 요구하는 UID를 대입함

                if(ChatRoomUID == null) { // 채팅방 UID가 없으면
                    button.setEnabled(false); // 서버에 확실히 연결될 때까지 button이 작동하지 않도록 막음. 아래에서 다시 풀어줌
                    FirebaseDatabase.getInstance().getReference().child("Chatrooms").push()
                            .setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) { // 데이터 입력에 성공했을 때
                            checkChatRoom(); // 새로운 방 ID를 만들지 않도록 체크하는 메소드
                        }
                    }); // push()로 채팅방마다 고유키값을 부여함
                } else {
                    ChatModel.Comment comment = new ChatModel.Comment(); // 채팅방.Comment변수(해시맵)에 넣을 새로운 객체 생성
                    comment.uid = uid; // 코멘트의 UID는 내 로그인한 UID
                    comment.message = editText.getText().toString(); // 코멘트에 대입할 메세지는 EditText에서 가져온 String값
                    comment.timestamp = ServerValue.TIMESTAMP; // 서버밸류를 이용해서 타임스탬프 찍기

                    FirebaseDatabase.getInstance().getReference().child("Chatrooms").child(ChatRoomUID)
                            .child("Comments").push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
//                            sendGCM();
                            editText.setText("");
                        }
                    }); // 작성한 코멘트 객체를 넣음
                }
            }
        });
        // 버튼 누르기 전에 채팅방이 잇는지 체크함
        checkChatRoom();
    }

    void sendGCM(){
/*
        Gson gson = new Gson();

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = destinationUsermodel.Token;
        notificationModel.notification.title = "보낸이 아이디";
        notificationModel.notification.text = editText.getText().toString();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));
        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("Authorization", "key=AIzaSyCat_wlsxVSAPya3toKysFjtQWX0jVxSKw") // 서버키
                .url("https://fcm.googleapis.com/fcm/send") // fcm할 때
// gcm할 때                .url("https://gcm-http.googleapis.com/gcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okhttpclient = new OkHttpClient();

        OkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(Response response) throws IOException {
            }
        });  */
    }

    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("Chatrooms") // "Chatrooms" 아래 데이터들 중
                .orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() { // 내 UID를 채팅방 유저 UID로 가지고 있는 채팅방의 경우
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    ChatModel chatModel = item.getValue(ChatModel.class); // 모든 데이터들을 ChatModel형으로 담음
                        if(chatModel.users.containsKey(UIDforChat)){ // 또한 채팅방 유저 UID에 상대 UID도 가지고 있으면
                            ChatRoomUID = item.getKey(); // 그 때 그 채팅방의 고유키값을 ChatRoomUID 변수에 대입함
                            button.setEnabled(true); // Chatroom방 고유키를 받아왔을 때 버튼 Enable시키기
                            recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                            recyclerView.setAdapter(new RecyclerViewAdapter());
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        // 리사이클러뷰에 보여줄 정보
        List<ChatModel.Comment> comments; // 코멘트 리스트
        Member member; // 멤버객체 정보

        public RecyclerViewAdapter(){
            comments = new ArrayList<>();

            // 파이어스토어에서 유저 정보 가져옴
            db.collection("Profile").document(UIDforChat) // 상대방 UID에 해당하는 문서 가져옴
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    member = documentSnapshot.toObject(Member.class); // 멤버 형태로 클래스에 담음
                    getMessageList(); // 채팅 대화목록을 전부 보여줌
                }
            });
        }

        public void getMessageList(){
            FirebaseDatabase.getInstance().getReference().child("Chatrooms") // 채팅방 아래
                    .child(ChatRoomUID).child("Comments").addValueEventListener(new ValueEventListener() { // 채팅방 UID 아래 코멘트 있는 걸 전부 가져오기
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear(); // 데이터 추가될 때마다 채팅방에 있는 모든 코멘트를 다 보내주기 때문에 데이터가 쌓임. 쌓인 데이터 clear 계속 해줌

                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        comments.add(item.getValue(ChatModel.Comment.class)); // 코멘트(어레이)에 담을 아이템을 ChatModel.Comment클래스로 변환함
                    }
                    notifyDataSetChanged(); // 메세지 갱신. 어댑터에 알림
                    recyclerView.scrollToPosition(comments.size() -1); // 리사이클러뷰를 (코멘트 크기 -1)까지 내림
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item, viewGroup, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            final MessageViewHolder messageViewHolder = ((MessageViewHolder) viewHolder);

            if(comments.get(position).uid.equals(UID)) { // 내가 보낸 메세지. 코멘트리스트 아이템의 uid값이 로그인한 UID와 같다면
                messageViewHolder.textView_mymessage.setVisibility(View.VISIBLE);
                messageViewHolder.textView_mymessage.setText(comments.get(position).message); // 코멘트에 저장된 message값을 textView에 보임
           //     messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbubb); // 내 말풍선 BackgroundResource drawable의 Right버블 말풍선 너무 큼
            //    messageViewHolder.textView_message.inflate; // 내 말풍선 BackgroundResource drawable의 Right버블 말풍선
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE); // 내 말풍선이니까 유저 정보는 안보여야 됨
                messageViewHolder.textView_theirmessage.setVisibility(View.GONE); // 내 말풍선이니까 저쪽 메세지창은 안보여야 됨
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT); // 전체 레이아웃

            }else{ // 상대방이 보낸 메세지

/*                try { // 프로필 이미지 세팅
                    messageViewHolder.exif = new ExifInterface(member.Imagepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int exifOrientation = messageViewHolder.exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int exifDegree = exifOrientationToDegrees(exifOrientation);
                Bitmap bitmap = BitmapFactory.decodeFile(member.Imagepath);
                messageViewHolder.imageView_profile.setImageBitmap(rotate(bitmap, exifDegree)); // 이미지 뷰에 비트맵 넣기     */

                // 프로필 이미지 세팅을 위해 FirebaseStorage 객체아래 "멤버UID"로 저장돼 있는 프로필 사진을 불러옴
                StorageReference sr = FirebaseStorage.getInstance().getReference().child("profile/"+ member.UID);
                try {
                    final File file = File.createTempFile("profile", "jpg"); // 임시파일 생성
                    // createTempFile(접두사 문자열, 접미사 문자열(파일 확장자), 파일 만들 디렉토리(기본일 경우 null))
                    sr.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() { // FileDownloadTask.TaskSnapshot
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Success Case
                            Bitmap bitmapImage = BitmapFactory.decodeFile(file.getPath()); // 파일의 경로를 가져와서 비트맵으로 변환해줌
                            messageViewHolder.imageView_profile.setImageBitmap(bitmapImage);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                messageViewHolder.textView_theirmessage.setVisibility(View.VISIBLE);
                messageViewHolder.textView_name.setText(member.Nickname); // 멤버의 닉네임을 TextView.이름에 세팅함
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE); // Linearlayout (프로필 이미지+이름) 보이게 함
 //               messageViewHolder.textView_theirmessage.setBackgroundResource(R.drawable.leftbubb); // 상대 말풍선이니까 drawable의 Left버블 말풍선 세팅
                messageViewHolder.textView_theirmessage.setText(comments.get(position).message); // 코멘트의 position별 message를 tV_message에 세팅
                messageViewHolder.textView_mymessage.setVisibility(View.INVISIBLE); // 상대 말풍선이니까 내 말풍선은 안보이게 함
       //         messageViewHolder.textView_theirmessage.setTextSize(25); // 글자크기는 25 !
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
            }

            long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            messageViewHolder.textView_timestamp.setText(time);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_mymessage;
            public TextView textView_theirmessage;
            public TextView textView_name;
            public CircleImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            protected ExifInterface exif; // 사진 회전시키는 메소드 쓰기 위해 변수선언
            public TextView textView_timestamp;

            public MessageViewHolder(View view) {
                super(view);
                textView_mymessage = (TextView) view.findViewById(R.id.messageItem_textView_mymessage);
                textView_theirmessage = (TextView) view.findViewById(R.id.messageItem_textView_theirmessage);
                textView_name = (TextView) view.findViewById(R.id.messageItem_textview_name);
                imageView_profile = (CircleImageView) view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_main);
                this.exif = null; // 꼬이지 않게 매번 초기화(null) 해줘야됨
                textView_timestamp = (TextView) view.findViewById(R.id.messageItem_textView_timestamp);
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

            return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true); // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        }
    }
}