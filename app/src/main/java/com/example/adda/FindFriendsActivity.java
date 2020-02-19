package com.example.adda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar mToolbar;
    private RecyclerView findFriendsRecyclerList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        findFriendsRecyclerList = (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(usersRef, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, findFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, findFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull findFriendViewHolder findFriendViewHolder, final int i, @NonNull Contacts contacts) {
                        findFriendViewHolder.userName.setText(contacts.getName());
                        findFriendViewHolder.userStatus.setText(contacts.getStatus());
                        Picasso.get().load(contacts.getImage()).placeholder(R.drawable.profile_image).into(findFriendViewHolder.profileImage);

                        findFriendViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id  = getRef(i).getKey();

                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(profileIntent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public findFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        findFriendViewHolder viewHolder = new findFriendViewHolder(view);
                        return viewHolder;
                    }
                };

        findFriendsRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }

    public static class findFriendViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;

        public findFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
