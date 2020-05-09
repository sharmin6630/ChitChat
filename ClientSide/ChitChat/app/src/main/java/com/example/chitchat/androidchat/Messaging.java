package com.example.chitchat.androidchat;

/*
this is for setting and getting messages for the clients.
 */

public class Messaging {

    private String nickname;
    private String message ;

    public Messaging(){

    }
    public Messaging(String nickname, String message) {
        this.nickname = nickname;
        this.message = message;
    }

    public String getName() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
