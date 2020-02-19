package com.example.adda;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.renderscript.Sampler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requsetsFragmentView;
    private RecyclerView myRequestList;
    private DatabaseReference chatRequestRef, usersRef, contactsRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requsetsFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("chat_requests");
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");

        myRequestList = (RecyclerView) requsetsFragmentView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requsetsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRequestRef.child(currentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, requestViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, requestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final requestViewHolder requestViewHolder, int i, @NonNull Contacts contacts) {
                requestViewHolder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                requestViewHolder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                final String list_user_id = getRef(i).getKey();

                DatabaseReference getTypeRef = getRef(i).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String type = dataSnapshot.getValue().toString();
                            if(type.equals("received")) {
                                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image")){
                                            final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(requestViewHolder.profileImage);
                                        }

                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                        requestViewHolder.userName.setText(requestUserName);
                                        requestViewHolder.userStatus.setText("wants to connect with you");

                                        requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[]{
                                                     "Accept",
                                                     "Cancel"
                                                };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Chat Request from " + requestUserName);

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if(which == 0){
                                                            contactsRef.child(currentUserId).child(list_user_id).child("contacts").setValue("saved")
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){

                                                                        contactsRef.child(list_user_id).child(currentUserId).child("contacts").setValue("saved")
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful()){
                                                                                            chatRequestRef.child(currentUserId).child(list_user_id).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){

                                                                                                                chatRequestRef.child(list_user_id).child(currentUserId).removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if(task.isSuccessful()){
                                                                                                                                    Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });

                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });

                                                                    }
                                                                }
                                                            });
                                                        }
                                                        else if(which == 1){
                                                            chatRequestRef.child(currentUserId).child(list_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){

                                                                                chatRequestRef.child(list_user_id).child(currentUserId).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), "Chat Request Deleted", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });

                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            else if(type.equals("sent")){
                                Button request_sent_button = requestViewHolder.itemView.findViewById(R.id.request_accept_button);
                                request_sent_button.setText("Requset Sent");
                                requestViewHolder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.INVISIBLE);



                                usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("image")){
                                            final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(requestViewHolder.profileImage);
                                        }

                                        final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                        final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                        requestViewHolder.userName.setText(requestUserName);
                                        requestViewHolder.userStatus.setText("You have sent a request to " + requestUserName);

                                        requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "Cancel Chat Request"
                                                };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Already Sent Request");

                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if(which == 0){
                                                            chatRequestRef.child(currentUserId).child(list_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){

                                                                                chatRequestRef.child(list_user_id).child(currentUserId).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){
                                                                                                    Toast.makeText(getContext(), "You have canceller the chat request", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });

                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });



                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public requestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                requestViewHolder holder = new requestViewHolder(view);
                return holder;
            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class requestViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, cancelButton;

        public requestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_button);
            cancelButton = itemView.findViewById(R.id.request_cancel_button);

        }
    }
}
