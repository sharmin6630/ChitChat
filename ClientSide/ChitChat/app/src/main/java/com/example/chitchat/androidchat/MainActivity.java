package com.example.chitchat.androidchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {

    private Button btn;
    private EditText nickname;
    public static final String NICKNAME = "usernickname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.enterchat) ;
        nickname = (EditText) findViewById(R.id.nickname);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if(!nickname.getText().toString().isEmpty()){
                     Intent i  = new Intent(MainActivity.this, ChatActivity.class);
                     i.putExtra(NICKNAME,nickname.getText().toString());
                     startActivity(i);
                 }
            }
        });

    }
}
