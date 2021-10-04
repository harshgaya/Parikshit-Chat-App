package com.parikshit.parikshitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActionBar actionBar;

    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;
    private String currentUserID;
    private StorageReference UserProfileImageRef;
    private FirebaseAuth mAuth;
    private static final int GalleryPick=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Parikshit Chat");

        mAuth = FirebaseAuth.getInstance();

        mAuth= FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");


        BottomNavigationView navigationView=findViewById(R.id.navigation);

        //actionBar.setTitle("Home");
        HomeFragment homeFragment=new HomeFragment();
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content,homeFragment,"").commit();

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:

                        ///home fragment

                        getSupportActionBar().setTitle("Parikshit Chat");
                        HomeFragment homeFragment=new HomeFragment();
                        FragmentTransaction fragmentTransaction1=getSupportFragmentManager().beginTransaction();
                        fragmentTransaction1.replace(R.id.content,homeFragment,"").commit();
                        return true;
//                    case  R.id.nav_profile:
//                        ///profile fragment
//                        getSupportActionBar().setTitle("Profile");
//                        ProfileFragment profileFragment=new ProfileFragment();
//                        FragmentTransaction fragmentTransaction2=getSupportFragmentManager().beginTransaction();
//                        fragmentTransaction2.replace(R.id.content,profileFragment,"").commit();
//                        return true;
                    case R.id.nav_users:
                        //users fragment
                        getSupportActionBar().setTitle("Users");
                        UsersFragment usersFragment=new UsersFragment();
                        FragmentTransaction fragmentTransaction3=getSupportFragmentManager().beginTransaction();
                        fragmentTransaction3.replace(R.id.content,usersFragment,"").commit();
                        return true;
                    //case R.id.nav_chat:
                        //users fragment
//                        getSupportActionBar().setTitle("Chats");
//                        ChatListFragment chatListFragment=new ChatListFragment();
//                        FragmentTransaction fragmentTransaction4=getSupportFragmentManager().beginTransaction();
//                        fragmentTransaction4.replace(R.id.content,chatListFragment,"").commit();
//                        return true;

                }
                return false;
            }
        });




    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("onlineStatus",status);

        dbRef.updateChildren(hashMap);
    }
    @Override
    protected void onStart() {
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp= String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}