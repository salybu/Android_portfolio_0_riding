package com.example.riding;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;

import static android.support.constraint.Constraints.TAG;

public class Adapter_forEvent extends RecyclerView.Adapter<Adapter_forEvent.cViewHolder> {

    protected ArrayList<moimPost_fs> moimArraylist; /// 모임 아이템 어레이리스트 (Firestore DB용)
    protected ArrayList<Timestamp> KeyArraylist; /// 모임 아이템 어레이리스트 (타임스탬프로 하겠음)
    protected ArrayList<String> KeysArraylist; /// 모임 아이템 어레이리스트 (문서키값으로 한거)

    private int removedPosition = 0;
    private moimPost_fs removedItem;

    public FirebaseFirestore db; // FirebaseFirestore 변수선언 (멤버 프로필 Firestore에 저장하기 위해)

    private Context thisContext;
    public String Tag = "This is Adapter class";

    public class cViewHolder extends RecyclerView.ViewHolder { // 1. 리스너 추가

        protected TextView Title; // 뷰홀더 내부 뷰요소 변수로 선언
        protected TextView Date;
        protected TextView Time;
        protected TextView Location;
        protected TextView CurrentMember;
        protected TextView Members;

        protected ImageView FavoriteIcon;
        protected ImageView FavoriteIcon_click;

        protected String UID;
        protected FirebaseFirestore db; // 찜하기 클릭했을 때 처리하려면

        public final View itemView; // 아이템 클릭 이벤트 처리하기 위해 뷰(아이템뷰) 선언

        public cViewHolder(View view) { //// 뷰홀더 생성자
            super(view);
            Title = (TextView) view.findViewById(R.id.Title); /// 뷰홀더 변수연결
            Date = (TextView) view.findViewById(R.id.Date);
            Time = (TextView) view.findViewById(R.id.Time);
            Location = (TextView) view.findViewById(R.id.Location);
            CurrentMember = (TextView) view.findViewById(R.id.CurrentMember);
            Members = (TextView) view.findViewById(R.id.Members);

            FavoriteIcon = (ImageView) view.findViewById(R.id.FavoriteIcon);
            FavoriteIcon_click = (ImageView) view.findViewById(R.id.FavoriteIcon_click);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // FirebaseAuth 객체에서 최근 접속한 FirebaseUser 객체 가져옴 (로그인한 User 정보 가져오기 위해)
            UID = user.getUid();
            db = FirebaseFirestore.getInstance();

            itemView = view; // 레이아웃 객체화(?) 아이템 클릭이벤트 처리하기 위해 선언
        }
    }

    public Adapter_forEvent(ArrayList<moimPost_fs> list, ArrayList<Timestamp> llist) { // 어댑터 생성자, 만든 액티비티의 Context값을 넣고, arraylist 값을 넣음
        moimArraylist = list;
        KeyArraylist = llist;
    }

    public Adapter_forEvent(ArrayList<moimPost_fs> list, ArrayList<String> llist, int i) { // 어댑터 생성자, 만든 액티비티의 Context값을 넣고, arraylist 값을 넣음
        moimArraylist = list;
        KeysArraylist = llist;
    }

    public Adapter_forEvent(ArrayList<moimPost_fs> list) { // 어댑터 생성자, 만든 액티비티의 Context값을 넣고, arraylist 값을 넣음
        moimArraylist = list;
    }

    @NonNull @Override
    public Adapter_forEvent.cViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.moim_item, viewGroup, false);
        return new cViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Adapter_forEvent.cViewHolder viewHolder, final int position) {
        Log.e("Bindholder work?", "yes");

        // realtime 쓸 때랑 firestore 쓸 때 Arraylist, moimArraylist 번갈아가며 바꿔줘야 됨
        String allmembers = Integer.toString(moimArraylist.get(position).Members); // 전체 멤버수

        viewHolder.Title.setText(moimArraylist.get(position).Title);
        viewHolder.Date.setText(moimArraylist.get(position).Date);
        viewHolder.Time.setText(moimArraylist.get(position).Time);
        viewHolder.Members.setText(allmembers);
        viewHolder.Location.setText(moimArraylist.get(position).Location);
        viewHolder.CurrentMember.setText(String.valueOf(moimArraylist.get(position).CurrentMember));
        viewHolder.Members.setText(String.valueOf(moimArraylist.get(position).Members));

        if(moimArraylist.get(position).Bookmark.get(viewHolder.UID) == null){
        }else if(moimArraylist.get(position).Bookmark.get(viewHolder.UID) == true){ // Bookmark에 저장된 UID의 밸류값이 true이면 북마크 되어있는 거
            viewHolder.FavoriteIcon.setVisibility(View.INVISIBLE); // 안 찜한 아이콘 안보이게
            viewHolder.FavoriteIcon_click.setVisibility(View.VISIBLE); // 찜한 아이콘 보이게
        } else if(moimArraylist.get(position).Bookmark.get(viewHolder.UID) == false){ // false이면 안돼있는 거
            viewHolder.FavoriteIcon.setVisibility(View.VISIBLE); // 안 찜한 아이콘 보이게
            viewHolder.FavoriteIcon_click.setVisibility(View.INVISIBLE); // 찜한 아이콘 안보이게
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

               // 파이어베이스 쓸 때 코드
                Context context = v.getContext();
                Intent intent = new Intent(context, MoimInfoBasicActivity.class);
/*                String value = KeyArraylist.get(position); // 키 값을 가져옴
                intent.putExtra("key", value);   */
                String title = moimArraylist.get(position).Title;
                String date = moimArraylist.get(position).Date;
                String location = moimArraylist.get(position).Location;
                intent.putExtra("Title", title);
                intent.putExtra("Date", date);
                intent.putExtra("Location", location);
                context.startActivity(intent);

/*                // 셰어드 쓸 때 코드
                Context context = v.getContext();
                Intent intent = new Intent(context, MoimInfoBasicActivity.class);
                intent.putExtra("Position", position);
                context.startActivity(intent);  */
            }
        });

        viewHolder.FavoriteIcon.setOnClickListener(new View.OnClickListener() { // 좋아요 눌렀을 때 클릭
            @Override
            public void onClick(View v) {
                viewHolder.FavoriteIcon.setVisibility(View.INVISIBLE); // 안 찜한 아이콘 안보이게
                viewHolder.FavoriteIcon_click.setVisibility(View.VISIBLE); // 찜한 아이콘 보이게

                db = FirebaseFirestore.getInstance();

              // 파베에서 바꾸는 코드. 트랜잭션 이용 // 키값을 타임스탬프로 바꿔서 문서를 가져온 다음에 트랜잭션 쓰기 // 키 어레이리스트 만들 필요없이 모임어레이리스트에서 타임스탬프값 가져온다
//                final DocumentReference docRef = db.collection("Moim").document(KeyArraylist.get(position)); // "Moim" 콜렉션에서 문서구분 키값에 해당하는 문서 가져오기
                db.collection("Moim")
        //                .whereEqualTo("timestamp", KeyArraylist.get(position))
                        .whereEqualTo("timestamp", moimArraylist.get(position).timestamp)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String keyb = document.getId(); // timestamp가 일치하는 문서를 찾고, 그 키값을 가져옴

                                        // 그 키값에 해당하는 문서의 북마크 리스트를 업데이트함 .. 해시맵 업데이트하는 방법 없으니까 그냥 트랜잭션 쓰겠음
                                //        db.collection("Moim").document(keyb).update("Bookmark", FieldValue.arrayUnion(viewHolder.UID, true)); 배열만 가능

                                        final DocumentReference docRef = db.collection("Moim").document(keyb); // "Moim" 콜렉션에서 문서구분 키값에 해당하는 문서 가져오기
                                        db.runTransaction(new Transaction.Function<Void>() {
                                            @Override
                                            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                                DocumentSnapshot snapshot = transaction.get(docRef);

                                                // 북마크에 UID True값을 추가함
                                                HashMap<String, Boolean> hash = (HashMap<String, Boolean>) snapshot.get("Bookmark");
                                                hash.put(viewHolder.UID, true);
                                                transaction.update(docRef, "Bookmark", hash);

                                                Log.e("apply ", "Success "); // Success
                                                return null;
                                            }
                                        });
                                    }
                                }
                            }
                        });
            }
        });

        viewHolder.FavoriteIcon_click.setOnClickListener(new View.OnClickListener() { // 좋아요 이미 누른 상태에서 취소 클릭
            @Override
            public void onClick(View v) {
                viewHolder.FavoriteIcon_click.setVisibility(View.INVISIBLE);
                viewHolder.FavoriteIcon.setVisibility(View.VISIBLE);

                db = FirebaseFirestore.getInstance();

                // 파베에서 바꾸는 코드. 트랜잭션 이용 // 키값을 타임스탬프로 바꿔서 문서를 가져온 다음에 트랜잭션 쓰기
                db.collection("Moim")
                        .whereEqualTo("timestamp", moimArraylist.get(position).timestamp)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String keyb = document.getId(); // timestamp가 일치하는 문서를 찾고, 그 키값을 가져옴

                                        // 그 키값에 해당하는 문서의 북마크 리스트를 업데이트함 .. 해시맵 업데이트하는 방법 없으니까 그냥 트랜잭션 쓰겠음
                                        final DocumentReference docRef = db.collection("Moim").document(keyb); // "Moim" 콜렉션에서 문서구분 키값에 해당하는 문서 가져오기
                                        db.runTransaction(new Transaction.Function<Void>() {
                                            @Override
                                            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                                DocumentSnapshot snapshot = transaction.get(docRef);

                                                // 북마크에 UID False값을 추가함
                                                HashMap<String, Boolean> hash = (HashMap<String, Boolean>) snapshot.get("Bookmark");
                                                hash.put(viewHolder.UID, false);
                                                transaction.update(docRef, "Bookmark", hash);

                                                Log.e("apply ", "Success "); // Success
                                                return null;
                                            }
                                        });
                                    }
                                }
                            }
                        });


/*                db = FirebaseFirestore.getInstance();
                // 파베에서 바꾸는 코드. 트랜잭션 이용
                final DocumentReference docRef = db.collection("Moim").document(KeyArraylist.get(position)); // "Moim" 콜렉션에서 문서구분 키값에 해당하는 문서 가져오기
                Log.e("Transaction postkey ", KeyArraylist.get(position));
                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(docRef);

                        // 북마크에 UID False값을 추가함
                        HashMap<String, Boolean> hash = (HashMap<String, Boolean>) snapshot.get("Bookmark");
                        hash.put(viewHolder.UID, false);
                        transaction.update(docRef, "Bookmark", hash);

                        Log.e("apply ", "Success "); // Success
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                    }
                });  */
            }
        });
    }

    public void removeItem(RecyclerView.ViewHolder viewholder, String key){
        removedPosition = viewholder.getAdapterPosition();
        removedItem = moimArraylist.get(viewholder.getAdapterPosition());

        // 실제로 파베에서 지우기 위한 코드 ^^ㅎ
        db.collection("Moim").document(key)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error deleting document", e);
                    }
                });

        moimArraylist.remove(viewholder.getAdapterPosition());
        notifyItemRemoved(viewholder.getAdapterPosition());

/*        Snackbar.make(viewholder.itemView, removedItem + " deleted!", Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                moimArraylist.add(removedPosition, removedItem);
                notifyItemInserted(removedPosition);
            }
        }).show();   */
    }

    @Override
    public int getItemCount() { return moimArraylist.size(); } // realtime 쓸 때랑 firestore 쓸 때 Arraylist, moimArraylist 번갈아가며 바꿔줘야 됨
}