package com.example.riding;

import android.app.Notification;

public class NotificationModel {

    public String to;
    public Notification notification = new Notification();

    public static class Notification{
        public String title;
        public String text;
    }
}