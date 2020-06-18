package com.example.riding;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class ParticipationCancelDialog {

    private Context context;
    public FirebaseFirestore db; // FirebaseFirestore 변수선언 (멤버 프로필 Firestore에 저장하기 위해)
    public int positiontrans; // 취소할지 말지 여부를 선택한 결과를 전달할 int값 (취소하기=1, 취소안하기=0)

    public ParticipationCancelDialog(Context context) {
        this.context = context;
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public int callFunction(final int position, final String email, final String postkey, final String UID, Adapter_forMember ad) {
        Log.e("callFunction postkey ", postkey);

        final Dialog dlg = new Dialog(context); // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE); // 액티비티의 타이틀바를 숨긴다.
        dlg.setContentView(R.layout.dialog_cancel); // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.show(); // 커스텀 다이얼로그를 노출한다.

        db = FirebaseFirestore.getInstance(); // Firestore 객체 가져오기

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final Button okButton = (Button) dlg.findViewById(R.id.okButton);
        final Button cancelButton = (Button) dlg.findViewById(R.id.cancelButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 파베에서 바꾸는 코드. 트랜잭션 이용
                final DocumentReference docRef = db.collection("Moim").document(postkey); // "Moim" 콜렉션에서 문서구분 키값에 해당하는 문서 가져오기
                Log.e("Transaction postkey ", postkey);
                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(docRef);

                        // 참여멤버 UID 삭제하기 위해 MemberUID 어레이 가져옴
                        ArrayList newarray = (ArrayList) snapshot.get("MemberUID");
                        newarray.remove(UID); // 어레이에서 내 UID값 삭제
                        transaction.update(docRef, "MemberUID", newarray); // 트랜잭션 이용해서 반영

                        // 현재 인원수 줄이기
                        int newCurrentMember = ((Long) snapshot.get("CurrentMember")).intValue() - 1;
                        transaction.update(docRef, "CurrentMember", newCurrentMember);

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
                });

                positiontrans = 1;
                ad.notifyDataSetChanged();

/*                Intent intent = new Intent(context, MoimInfoBasicActivity.class);
                String title = moimArraylist.get(position).Title;
                String date = moimArraylist.get(position).Date;
                String location = moimArraylist.get(position).Location;
                intent.putExtra("Title", title);
                intent.putExtra("Date", date);
                intent.putExtra("Location", location);
                context.startActivity(intent);
  */
                dlg.dismiss(); // 커스텀 다이얼로그를 종료한다.
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "소모임에 그대로 참여합니다.", Toast.LENGTH_SHORT).show();

                positiontrans = 0;
                dlg.dismiss(); // 커스텀 다이얼로그를 종료한다.
            }
        });

        return positiontrans; // 결과전달
    }

}