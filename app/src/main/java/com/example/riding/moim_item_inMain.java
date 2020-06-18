package com.example.riding;

public class moim_item_inMain {

    private String Title;
    private String Time;
    private String Location;
    private String Member;

    public String getTitle(){
        return Title;
    }
    public String getTime(){
        return Time;
    }
    public String getLocation(){
        return Location;
    }
    public String getMember(){
        return Member;
    }

    public void setTitle(String title){
        Title = title;
    }
    public void setTime(String time){
        Time = time;
    }
    public void setLocation(String location){
        Location = location;
    }
    public void setMember(String member){ Member = member; }

 /*
    private String Filepath; /// 이미지 경로 넘기기 위한 변수 선언
    private String Imageexist; // 이미지 선택했는지 안했는지 확인하기 위한 변수
    private String LabelColor;
    private Uri uri;
    private Bitmap bitmap = null; // 늘 초기화

    private int itemViewType;

    public Uri getUri(){ return uri; }
    public Bitmap getBitmap(){
        return bitmap;
    }
    public String getFilepath(){ return Filepath; }
    public String getLabelColor(){
        return LabelColor;
    }
    public String getImageexist(){ return Imageexist; }

    public void setUri(Uri urii){
        uri = urii;
    }
    public void setBitmap(Bitmap bitmapp){ bitmap = bitmapp; }

    public moim_item_inMain(String title, String location, String currentMembers){
        Title = title;
        Location = location;
        CurrentMembers = currentMembers;
    }

    public int getItemViewType(){
        return itemViewType;
    }
    public void setItemViewType(){
        this.itemViewType = 0;  //// 피드 리사이클러뷰 아이템 뷰타입 세팅
    }  */
}