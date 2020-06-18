package com.example.riding;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {

/*    public String uid;
    public String UIDforChat;  */

    public Map<String, Boolean> users = new HashMap<>(); // 채팅방 유저들
    public Map<String, Comment> comments = new HashMap<>(); // 채팅방의 대화내용

    public static class Comment {
        public String uid;
//        public String UIDforChat;
        public String message;
        public Object timestamp;
    }
}