package com.barracudapff.hoobes.flatter.fragments.screens;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.barracudapff.hoobes.flatter.R;
import com.barracudapff.hoobes.flatter.activities.chat.ChatActivity;
import com.barracudapff.hoobes.flatter.activities.chat.NewChatActivity;
import com.barracudapff.hoobes.flatter.database.models.LastMessage;
import com.barracudapff.hoobes.flatter.database.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {
    public RecyclerView mRecyclerView;

    private DatabaseReference mDatabase;
    public FirebaseRecyclerAdapter<LastMessage, LastMessageViewHolder> adapter;

    public ChatListFragment() {
        // Required empty public constructor
    }

    public static ChatListFragment newInstance() {

        Bundle args = new Bundle();

        ChatListFragment fragment = new ChatListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Сообщения");

        mDatabase = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("talk")
                .child("last_messages")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mRecyclerView = view.findViewById(R.id.dialogs);
        mRecyclerView.setHasFixedSize(true);

        Query postsQuery = mDatabase;

        FirebaseRecyclerOptions<LastMessage> options = new FirebaseRecyclerOptions.Builder<LastMessage>()
                .setQuery(postsQuery, LastMessage.class)
                .build();

        LinearLayoutManager mManager = new LinearLayoutManager(getContext());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mManager);

        adapter = new FirebaseRecyclerAdapter<LastMessage, LastMessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull LastMessageViewHolder holder, int position, @NonNull LastMessage model) {
                final User[] userTo = new User[1];

                DatabaseReference reference = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child("users")
                        .child(getRef(position).getKey());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userTo[0] = dataSnapshot.getValue(User.class);
                        holder.bindOnUser(model, userTo[0], reference.getKey());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public LastMessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

                return new LastMessageViewHolder(inflater.inflate(R.layout.item_search_member, viewGroup, false));
            }
        };
        adapter.startListening();
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(adapter);

        setHasOptionsMenu(true);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_create_24dp));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Objects.requireNonNull(getActivity()).getMenuInflater().inflate(R.menu.add_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_last_party:
                // TODO: 06.10.2018
                Toast.makeText(getContext(), "TODO: Last party chat", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_add_chat:
                Intent i = new Intent(getActivity(), NewChatActivity.class);
                getActivity().startActivityForResult(i, 107);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class LastMessageViewHolder extends RecyclerView.ViewHolder {
        private final Transformation TRANSFORMATION;
        public ConstraintLayout layout;
        View mView;

        public LastMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            TRANSFORMATION = new RoundedTransformationBuilder()
                    .borderColor(Color.BLACK)
                    .borderWidthDp(1)
                    .cornerRadiusDp(32)
                    .oval(false)
                    .build();
            mView = itemView;
        }

        public void bindOnUser(LastMessage message, User user, String key) {
            layout = mView.findViewById(R.id.chat_list_search_layout);

            TextView user_name = mView.findViewById(R.id.chat_list_name);
            TextView user_status = mView.findViewById(R.id.chat_list_text);
            TextView user_date = mView.findViewById(R.id.chat_list_date);
            RoundedImageView user_image = mView.findViewById(R.id.chat_list_profile_image1);

            //Date
            Date date = new java.util.Date(message.last_message_timestamp);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", new Locale("ru", "RU"));
            user_date.setText(sdf.format(date));
            //UserName
            String name = user.first_name + " " + user.second_name;
            user_name.setText(name);
            user_status.setText(message.last_message);

            layout.setOnClickListener(view -> {
                ChatActivity.setUser(user);
                ChatActivity.setOtherUID(key);
                Intent i = new Intent(getActivity(), ChatActivity.class);
                getActivity().startActivityForResult(i, 108);
            });

            FirebaseStorage.getInstance().getReference()
                    .child("images")
                    .child("users")
                    .child(key)
                    .child(User.PROFILE_PHOTOS)
                    .child("0_low_25")
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> Picasso.get()
                            .load(uri)
                            .fit()
                            .centerCrop()
                            .error(R.color.lightGray)
                            .transform(TRANSFORMATION)
                            .into(user_image))
                    .addOnFailureListener(command -> Picasso.get()
                            .load(R.color.lightGray)
                            .fit()
                            .centerCrop()
                            .transform(TRANSFORMATION)
                            .into(user_image));

        }
    }
}
