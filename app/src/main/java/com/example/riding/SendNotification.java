package com.example.riding;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

public class SendNotification {

    // MediaType import할 때 com.squareup.okhttp.MediaType으로 해줬어야 됨. 다른 거 해서 오류났음
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static void sendNotification(String regToken, String title, String messsage){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... parms) {
                try {

                    // OkHttp 라이브러리를 이용해서 서버에 Post 요청하는 방법
                    OkHttpClient client = new OkHttpClient(); // 1. OkHttpClient 객체를 정의함

                    // 2. Post 요청에 필요한 자료를 정의함 (JSON 객체로)
                    JSONObject dataJson = new JSONObject(); // 알림에 보낼 메세지 정의
                    dataJson.put("body", messsage);
                    dataJson.put("title", title);

                    JSONObject json = new JSONObject();
                    json.put("notification", dataJson); // 알림을 보낼
                    json.put("to", regToken);

                    String jsonst = json.toString();

                    // 요청을 보낼 서버의 주소객체를 정의함. 객체의 데이터형은 String 또는 HttpUrl임
                    // HttpUrl ht = new HttpUrl.Builder(); 이런 식으로 HttpUrl 객체를 정의할 수 있음

                    // RequestBody 객체를 정의하고 Post 요청에 필요한 자료(JSON 객체)를 RequestBody에 담음
                    RequestBody body = (RequestBody) RequestBody.create(JSON, json.toString());

                    // Request 객체를 정의하고 RequestBody 객체를 추가함
                    Request request = new Request.Builder()
                            .header("Authorization", "key=" + "AAAA-lEGYRM:APA91bGV0sHKxDxj_NqOH6NOTMzC72cq6IOs1y3C5nzDZI84JyWLPh9jGyf1iu16c_7s30uKev6Up8tOKDsWsZraBdOBN-dhjrq03-EQreN0FGpJR1gIPAJkg8b8t3gTU3ozcUCHAaRq")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();

                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                }catch (Exception e){
                    Log.d("error", e+"");
                }
                return  null;
            }
        }.execute();
    }

}