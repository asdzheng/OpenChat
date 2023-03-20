package com.asdzheng.openchat.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asdzheng.openchat.R;
import com.asdzheng.openchat.db.model.ChatMessage;
import com.asdzheng.openchat.ui.view.MarkedView;
import com.bluewhaleyt.component.dialog.DialogUtil;
import com.unfbx.chatgpt.entity.chat.Message.Role;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

public class MessageAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<ChatMessage> mChatMessages;

    public MessageAdapter(List<ChatMessage> chatMessages) {
        this.mChatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        if(viewType == R.layout.layout_message_list_item) {
            return new ChatMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new UserMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        ChatMessage chatMessage = mChatMessages.get(position);
        if(holder instanceof ChatMessageViewHolder) {
            ((ChatMessageViewHolder)holder).chatMessageView.setMDText(chatMessage.getContent());
        } else if (holder instanceof UserMessageViewHolder){
            ((UserMessageViewHolder)holder).userMessageView.setText(chatMessage.getContent());
        }
        itemLongClick(holder, position);
    }

    @Override
    public int getItemCount() {
        return mChatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = mChatMessages.get(position);
        if(Role.ASSISTANT.name().equals(chatMessage.getRole())) {
            return R.layout.layout_message_list_item;
        } else {
            return R.layout.layout_message_user_input;
        }
    }

    private void itemLongClick(ViewHolder holder, int position) {
        holder.itemView.setOnLongClickListener(v -> {
            Context context = holder.itemView.getContext();
            DialogUtil dialog = new DialogUtil(context);
            dialog.setTitle(context.getString(R.string.delete));
            dialog.setMessage(mChatMessages.get(position).getContent());
            dialog.setPositiveButton(android.R.string.ok, ((d, i) -> {
                removeMessage(context, position);
            }));
            dialog.setNegativeButton(android.R.string.cancel, null);
            dialog.create();
            dialog.show();
            return true;
        });
    }

    private void removeMessage(Context context, int position) {
        mChatMessages.remove(position);
        notifyItemRemoved(position);
    }
    public boolean isEmpty() {
        return mChatMessages.isEmpty();
    }

    static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        MarkedView chatMessageView;
        public ChatMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            chatMessageView = itemView.findViewById(R.id.tv_chat_reply_markdown_message);
        }
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView userMessageView;
        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessageView = itemView.findViewById(R.id.tv_user_input_message);
        }
    }

    public void setMessageList(List<ChatMessage> chatMessageList) {
        mChatMessages = chatMessageList;
        notifyDataSetChanged();
    }

}
