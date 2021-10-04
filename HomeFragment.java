package com.parikshit.parikshitchat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.parikshit.parikshitchat.Adapter.AdapterGroupChat;
import com.parikshit.parikshitchat.Adapter.AdapterGroupChatList;
import com.parikshit.parikshitchat.Model.ModelGroupChat;
import com.parikshit.parikshitchat.Model.ModelGroupChatList;
import com.parikshit.parikshitchat.Model.ModelUser;
import com.parikshit.parikshitchat.notifications.APIService;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {

    private RecyclerView groupsRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelGroupChatList> groupChatLists;
    private AdapterGroupChatList adapterGroupChatList;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;
    String mUID;

    private final String TAG = Context.class.getSimpleName();



    public HomeFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("Group Chat");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        groupsRv=view.findViewById(R.id.groupsRv);
        firebaseAuth= FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();

        RootRef = FirebaseDatabase.getInstance().getReference();

        loadGroupChatList();


        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_logout).setVisible(true);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);



        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.orderByChild(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String role=""+ds.child("role").getValue();

                    if (role.equals("admin")){
                        menu.findItem(R.id.action_create_group).setVisible(true);
                    }else {
                       menu.findItem(R.id.action_create_group).setVisible(false);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        super.onCreateOptionsMenu(menu,menuInflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }

        if (id==R.id.action_create_group){
            Intent intent=new Intent(getContext(),CreateGroupActivity.class);
            startActivity(intent);

        }
        if (id==R.id.action_profile){
            Intent intent=new Intent(getContext(),CreateGroupActivity.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){

        }else{
            startActivity(new Intent(getContext(),LoginActivity.class));
            getActivity().finish();
        }
    }

    private void loadGroupChatList() {
        groupChatLists=new ArrayList<>();


        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.keepSynced(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatLists.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        ModelGroupChatList model=ds.getValue(ModelGroupChatList.class);
                        groupChatLists.add(model);
                    }
                }
                adapterGroupChatList=new AdapterGroupChatList(getContext(),groupChatLists);
                groupsRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void searchGroupChatList(final String query) {
        groupChatLists=new ArrayList<>();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatLists.size();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if (ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
                            ModelGroupChatList model=ds.getValue(ModelGroupChatList.class);
                            groupChatLists.add(model);
                        }

                    }
                }
                adapterGroupChatList=new AdapterGroupChatList(getContext(),groupChatLists);
                groupsRv.setAdapter(adapterGroupChatList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}