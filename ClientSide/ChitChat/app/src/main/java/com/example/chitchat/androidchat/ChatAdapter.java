package com.example.chitchat.androidchat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.Holder> {
    private List<Messaging> MessageList;

    public class Holder extends RecyclerView.ViewHolder {
        public TextView nickname;
        public TextView message;


        public Holder(View view) {
            super(view);
            nickname = (TextView) view.findViewById(R.id.nickname);
            message = (TextView) view.findViewById(R.id.message);
        }
    }
    public ChatAdapter(List<Messaging>MessagesList) {
        this.MessageList = MessagesList;
    }

    @Override
    public int getItemCount() {
        return MessageList.size();
    }
    @Override
    public ChatAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new ChatAdapter.Holder(itemView);
    }

    @Override
    public void onBindViewHolder(final ChatAdapter.Holder holder, final int position) {
        final Messaging m = MessageList.get(position);
        holder.nickname.setText(m.getName() +" : ");
        holder.message.setText(m.getMessage() );
    }



}