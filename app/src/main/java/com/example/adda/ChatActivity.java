package com.example.adda;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId, messageReceiverName, messageReceiverImage, messageSenderId;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;
    private ImageButton sendMessageButton, sendFileButton;
    private ImageView logo;
    private EditText messageInputText;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private String saveCurrentTime, saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_user_image").toString();

        initializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        displayLastSeen();
    }

    private void initializeControllers() {

        chatToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        logo = (ImageView) chatToolbar.findViewById(R.id.logo);
        logo.setVisibility(View.GONE);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        logo = (ImageView) findViewById(R.id.logo);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        sendFileButton = (ImageButton) findViewById(R.id.send_file_button);
        messageInputText = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }


    private void displayLastSeen(){

        rootRef.child("users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("user_state").hasChild("state")){
                    String state = dataSnapshot.child("user_state").child("state").getValue().toString();
                    String date = dataSnapshot.child("user_state").child("date").getValue().toString();
                    String time = dataSnapshot.child("user_state").child("time").getValue().toString();

                    if(state.equals("online")){
                        userLastSeen.setText("online");
                    }
                    else if(state.equals("offline")){
                        userLastSeen.setText("Last Seen: " + time + " on " + date);
                    }
                }
                else{
                    userLastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage()
    {
        String messageText = messageInputText.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Write your message first", Toast.LENGTH_SHORT).show();
        }
        else{
            String messageSenderRef = "messages/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = "messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("messages").child(messageSenderId).child(messageReceiverId).push();

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);
            messageTextBody.put("to", messageReceiverId);
            messageTextBody.put("messageId", messagePushId);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);
            
            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "message sent successfully", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "we couldn't send the message", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });
        }
    }
}
