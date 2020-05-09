package com.example.chitchat.androidchat;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;



public class ChatActivity extends AppCompatActivity {
    public RecyclerView myRecylerView ;
    public List<Messaging> MessageList ;
    public ChatAdapter chatBoxAdapter;
    public static String uniqueId;

    public  EditText messagetxt ;
    public  Button send ;
    public  Button colorPicker;

    private Socket socket; //socket object is declared

    public static final String TAG  = "MainActivity";
    public String Nickname ;

    private Thread thread2;
    private boolean startTyping = false;
    private int time = 2;


    @SuppressLint("HandlerLeak")
    Handler handler2=new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "handleMessage: typing stopped " + startTyping);
            if(time == 0){
                setTitle("ChitChat");
                Log.i(TAG, "handleMessage: typing stopped time is " + time);
                startTyping = false;
                time = 2;
            }

        }
    };

    // get the nickame of the user & this connects socket client to the server
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);

        messagetxt = (EditText) findViewById(R.id.message) ;
        send = (Button)findViewById(R.id.send);
        colorPicker = (Button)findViewById(R.id.colorPicker);

        Nickname= (String)getIntent().getExtras().getString(MainActivity.NICKNAME);
        uniqueId = UUID.randomUUID().toString();

        try {
            socket = IO.socket("http://192.168.43.118:3000"); //insert your local IP here
            socket.connect();
            socket.emit("load_previous_msg", Nickname);
            socket.emit("join", Nickname);
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
       // recyler set up
        MessageList = new ArrayList<>();
        myRecylerView = (RecyclerView) findViewById(R.id.messagelist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        myRecylerView.setLayoutManager(mLayoutManager);
        myRecylerView.setItemAnimator(new DefaultItemAnimator());
        onTypeButtonEnable();

        colorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialogBuilder
                        .with(view.getContext())
                        .setTitle("Choose color")
                        .initialColor(0xFFFF0000)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                Log.d(TAG, "onClick: Selected ColorL "+Integer.toHexString(selectedColor));
                                String color= Integer.toHexString(selectedColor);
                                myRecylerView.setBackgroundColor(Color.parseColor("#"+color));
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });
        // when send button is clicked
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // messagedetection event occurs
                if(!messagetxt.getText().toString().isEmpty()){
                    socket.emit("messagedetection",Nickname,messagetxt.getText().toString());

                    messagetxt.setText(" ");
                }


            }
        });

        //socket listeners : userJoined, userDisconnect, onTyping, message
        socket.on("userjoinedthechat", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        Toast.makeText(ChatActivity.this,data,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        socket.on("userdisconnect", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];

                        Toast.makeText(ChatActivity.this,data,Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        socket.on("on typing", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        Log.i(TAG, "run: " + args[0]);
                        try {
                            Boolean typingOrNot = data.getBoolean("typing");
                            String userName = data.getString("username") + " is Typing......";
                            String id = data.getString("uniqueId");

                            if(id.equals(uniqueId)){
                                typingOrNot = false;
                            }else {
                                setTitle(userName);
                            }
                            if(typingOrNot){

                                if(!startTyping){
                                    startTyping = true;
                                    thread2=new Thread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    while(time > 0) {
                                                        synchronized (this){
                                                            try {
                                                                wait(1000);
                                                                Log.i(TAG, "run: typing " + time);
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                            time--;
                                                        }
                                                        handler2.sendEmptyMessage(0);
                                                    }

                                                }
                                            }
                                    );
                                    thread2.start();
                                }else {
                                    time = 2;
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });


        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {

                            String nickname = data.getString("senderNickname");
                            String message = data.getString("message");
                            Messaging m = new Messaging(nickname,message);
                            MessageList.add(m);
                            chatBoxAdapter = new ChatAdapter(MessageList);
                            chatBoxAdapter.notifyDataSetChanged();
                            myRecylerView.setAdapter(chatBoxAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
            }
        });
    }


    public void onTypeButtonEnable(){
        messagetxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                JSONObject onTyping = new JSONObject();
                try {
                    onTyping.put("typing", true);
                    onTyping.put("username", Nickname);
                    onTyping.put("uniqueId", uniqueId);
                    socket.emit("on typing", onTyping);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (charSequence.toString().trim().length() > 0) {
                    send.setEnabled(true);
                } else {
                    send.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.emit("disvalue", Nickname);
        socket.disconnect();
  }
}
