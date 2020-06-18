package com.example.riding;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class ParticipationRequestDialog {

    private Context context;
    public FirebaseFirestore db; // FirebaseFirestore 변수선언 (멤버 프로필 Firestore에 저장하기 위해)

    public ParticipationRequestDialog(Context context) {
        this.context = context;
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void callFunction(final int position, final String email, final String postkey, final String UID) {
        Log.e("callFunction postkey ", postkey);

        final Dialog dlg = new Dialog(context); // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE); // 액티비티의 타이틀바를 숨긴다.
        dlg.setContentView(R.layout.dialog_request); // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.show(); // 커스텀 다이얼로그를 노출한다.

        db = FirebaseFirestore.getInstance(); // Firestore 객체 가져오기

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
//        final EditText message = (EditText) dlg.findViewById(R.id.message);
        final Button okButton = (Button) dlg.findViewById(R.id.okButton);
        final Button cancelButton = (Button) dlg.findViewById(R.id.cancelButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*                 // '확인' 버튼 클릭시 메인 액티비티에서 설정한 main_label에
                // 커스텀 다이얼로그에서 입력한 메시지를 대입한다. main_label.setText(message.getText().toString());
                Toast.makeText(context, "\"" +  message.getText().toString() + "\" 을 입력하였습니다.", Toast.LENGTH_SHORT).show();   */

/*                // 셰어드에서 꺼내는 코드
                SharedPreferences pref = context.getSharedPreferences("Moims", Context.MODE_PRIVATE); // Events 셰어드 불러옴
                SharedPreferences.Editor editor = pref.edit(); // 셰어드 파일을 수정할 에디터 불러옴
                String eventsArray = pref.getString("Entire",""); // Moims 셰어드에서 Key값 "Entire"로(전체 모임아이템) Value값 JSONArray(String형) 불러옴

                JSONArray eventsJSONArray = null; // value값 JSONArray를 받아 저장하기 위해 빈 JSONArray 변수[*] 선언
                try {
                    eventsJSONArray = new JSONArray(eventsArray); // String형 JSONArray[eventsArray]를 JSONArray 객체로 형변환해 앞서 선언한 변수[*]에 저장
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                assert eventsJSONArray != null; // JSONArray가 null 값 아닐 때 (조건)
                for(int i=0 ; i <= (eventsJSONArray.length()-1) ; i++) { // JSONArray에 담긴 모든 값을 꺼내기 위한 반복문. i <= JSONArray 마지막 인덱스까지 반복하도록 함
                    try {
                        JSONObject object = eventsJSONArray.getJSONObject(i); // 앞서 형변환한 JSONArray에서 JSONObject를 i개 꺼냄

                        if(i == position) { // 어댑터에서 받아온 position값에 일치하는 아이템 읽어오기
                            String memberUID = object.getString("MemberUID");

                            String newmemberUID = memberUID + "," +email;

                            object.put("MemberUID", newmemberUID);

                            eventsJSONArray.put(object);
                            editor.putString("Entire", eventsJSONArray.toString());
                            editor.commit();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }   */

                // 파베에서 바꾸는 코드. 트랜잭션 이용
                final DocumentReference docRef = db.collection("Moim").document(postkey); // "Moim" 콜렉션에서 문서구분 키값에 해당하는 문서 가져오기
                Log.e("Transaction postkey ", postkey);
                db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(docRef);

                        // 참여멤버 UID 추가하기 위해 MemberUID 어레이 가져옴
                        ArrayList newarray = (ArrayList) snapshot.get("MemberUID");


                        if(newarray.contains(UID)){ // 어레이에 내 UID값 있는 경우
/*                            // UI 스레드가 없는 곳에서 토스트를 사용할 수 없음, 메인스레드가 있는 UI에서 사용해야 하는데 핸들러로 감싸고 메인루퍼 가져와보겠음
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "이미 참여한 모임입니다", Toast.LENGTH_SHORT);
                                }
                            }, 0); 안됨 */

                        }else{ // 없는 경우
                            // 어레이리스트에 내 UID 추가
                            newarray.add(UID);
                            transaction.update(docRef, "MemberUID", newarray);

                            // 현재 인원수 추가
                            int newCurrentMember = ((Long) snapshot.get("CurrentMember")).intValue() + 1;
                            transaction.update(docRef, "CurrentMember", newCurrentMember);
                        }

/* 틀림                        for(int i = 0; i < newarray.size(); i++) { // 모임에 참여하는 멤버 UID값 다 가져와서 있는지 없는지 비교 (이미 참여한 모임인 경우 Toast.이미 참여한 모임입니다 띄우기)
                            final String UIDd = (String) newarray.get(i); // 1멤버 UID값 전부 다 꺼낸다

                            if(UIDd.equals(UID)){ // 가져온 UID값들이 내 UID와 일치하는 경우와 아닌 경우(else)
                                Toast.makeText(context, "이미 참여한 모임입니다", Toast.LENGTH_SHORT);

                            }else{ // 참여한 멤버 UID값에 내 UID 없을 때만 코드 작동하도록 함

                                // 어레이리스트에 내 UID 추가
                                newarray.add(UID);
                                transaction.update(docRef, "MemberUID", newarray);

                                // 현재 인원수 추가
                                int newCurrentMember = ((Long) snapshot.get("CurrentMember")).intValue() + 1;
                                transaction.update(docRef, "CurrentMember", newCurrentMember);

                                Log.e("apply ", "Success "); // Success
                            }
                        } */
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
                dlg.dismiss(); // 커스텀 다이얼로그를 종료한다.
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "소모임 참여요청을 취소했습니다.", Toast.LENGTH_SHORT).show();
                dlg.dismiss(); // 커스텀 다이얼로그를 종료한다.
            }
        });
    }

}