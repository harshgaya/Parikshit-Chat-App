package com.parikshit.parikshitchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.parikshit.parikshitchat.Adapter.AdapterGroupChat;
import com.parikshit.parikshitchat.Model.ModelGroupChat;
import com.parikshit.parikshitchat.Model.ModelUser;
import com.parikshit.parikshitchat.Notification2.Data2;
import com.parikshit.parikshitchat.Notification2.Sender2;
import com.parikshit.parikshitchat.notifications.APIService;
import com.parikshit.parikshitchat.notifications.Client;
import com.parikshit.parikshitchat.notifications.Data;
import com.parikshit.parikshitchat.notifications.Response;
import com.parikshit.parikshitchat.notifications.Sender;
import com.parikshit.parikshitchat.notifications.Token;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

public class GroupChatActivity extends AppCompatActivity {

    private ArrayList<ModelUser> userList;

    private String groupId, myGroupRole="";
    private Toolbar toolbar;
    private CircleImageView groupIconIv;
    private TextView groupTitleTv;
    private EditText messageEt;
    private ImageButton attachBtn, sendBtn;
    private RecyclerView chatRv;

    private ArrayList<ModelGroupChat> groupChatList;
    private AdapterGroupChat adapterGroupChat;
    private String audioFileName = null;


    private FirebaseAuth firebaseAuth;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;

    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;

    private String[] cameraPermission;
    private String[] storagePermission;

    private ImageButton recordBtn, groupInfo;
    private Dialog audioRecordDialog;

    private Uri image_uri = null;
    MediaRecorder mediaRecorder;
    String pathSave = "";
    final int REQUEST_PERMISSION_CODE = 1000;

    private StorageReference storageReference;
    private Chronometer chronometer;

    APIService apiService;
    boolean notify = false;
    final String uid = "nPcxnPSdifNklKwFegHbTRYEEwa2";

    private GestureDetector gestureDetector;
    private ArrayList<String> userid=new ArrayList<>();
    private ArrayList<String> tokens=new ArrayList();



    public  ArrayList<String> getTokens(){
        return tokens;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);


        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
        final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        if (!checkPermissionFromDevice()) {
            requestPermission();
        }

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        toolbar = findViewById(R.id.toolbar);
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        attachBtn = findViewById(R.id.attachBtn);
        chatRv = findViewById(R.id.chatRv);
        chronometer = findViewById(R.id.chronometer);

        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);


        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        setSupportActionBar(toolbar);


        cameraPermission = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        storagePermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();
        updateToken();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEt.getText().toString().trim();

                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(GroupChatActivity.this, "Can't send empty message...", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);
                }
                //messageEt.setText("");
            }
        });
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageImportDialog();

            }
        });

        recordBtn = findViewById(R.id.recordBtn);
        audioRecordDialog = new Dialog(GroupChatActivity.this);
        //audioRecordDialog.setContentView(R.layout.audio_record_layout);
        audioRecordDialog.setCancelable(false);
        audioRecordDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        recordBtn.setOnClickListener(null);
//
        recordBtn.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (gestureDetector.onTouchEvent(motionEvent)) {
                    // single tap
                    Toast.makeText(GroupChatActivity.this, "Hold to record...", Toast.LENGTH_SHORT).show();
                    chronometer.setVisibility(View.INVISIBLE);
                    recordBtn.setBackgroundColor(Color.parseColor("#ffffff"));


                    vibe.vibrate(300);

                    return true;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    recordBtn.setBackgroundColor(Color.parseColor("#FF0000"));

                    chronometer.setVisibility(View.VISIBLE);
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    pathSave = Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + "/"
                            + UUID.randomUUID().toString() + "_audio_record.mp3";
                    audioFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
                    audioFileName += "/recorded_audio.mp3";
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                    mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                    mediaRecorder.setOutputFile(audioFileName);

                    Log.d("RecordView", "onStart");

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    recordBtn.setBackgroundColor(Color.parseColor("#ffffff"));

                    //
                    try {
                        chronometer.setVisibility(View.INVISIBLE);
                        mediaRecorder.reset();
                        mediaRecorder.stop();
                        mediaRecorder = null;
                    } catch (Exception e) {

                    }

                    chronometer.stop();
                    final ProgressDialog pd = new ProgressDialog(GroupChatActivity.this);
                    pd.setTitle("Please wait");
                    pd.setMessage("Sending Audio...");
                    pd.setCanceledOnTouchOutside(false);
                    pd.show();

                    String filenamePath = "Audio/" + System.currentTimeMillis();

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);
                    //StorageReference filepath=storageReference.child("Audio").child("new_audio.mp3");

                    Uri uri = Uri.fromFile(new File(audioFileName));

                    storageReference.putFile(uri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                    while (!p_uriTask.isSuccessful()) ;
                                    Uri p_downloadUri = p_uriTask.getResult();

                                    if (p_uriTask.isSuccessful()) {
                                        String timestamp = "" + System.currentTimeMillis();

                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("sender", "" + firebaseAuth.getUid());
                                        hashMap.put("message", "" + p_downloadUri);
                                        hashMap.put("timestamp", "" + timestamp);
                                        hashMap.put("type", "" + "audio");


                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                                        ref.child(groupId).child("Messages").child(timestamp)
                                                .setValue(hashMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        messageEt.setText("");
                                                        pd.dismiss();


                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            pd.dismiss();

                        }
                    });
                    return true;


                }


                return false;

            }

        });
    }

    private void showImageImportDialog() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which == 0) {
                            if (!checkCameraPermission()) {
                                requestCameraPermission();
                            } else {
                                pickCamera();
                            }
                        } else {
                            if (!checkStoragePermission()) {
                                requestStoragePermission();
                            } else {
                                pickGallery();
                            }
                        }
                    }
                }).show();
    }

    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "GroupImageTitle");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "GroupImageDescription");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);

    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);

    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            myGroupRole = "" + ds.child("role").getValue();
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void loadGroupMessages() {
        groupChatList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelGroupChat model = ds.getValue(ModelGroupChat.class);
                    groupChatList.add(model);
                }
                adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChatList);
                chatRv.setAdapter(adapterGroupChat);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String message) {
        notify = true;

        final String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + firebaseAuth.getUid());
        hashMap.put("message", "" + message);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("type", "" + "text");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        messageEt.setText("");

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        String msg=message;
        final DatabaseReference database= FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user=dataSnapshot.getValue(ModelUser.class);
                if (notify){
                    sendNotification(uid,user.getName(),message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



//        DatabaseReference reff = FirebaseDatabase.getInstance().getReference("Groups");
//        reff.child(groupId).child("Messages").addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                //notification(message);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

//        DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());
//        database.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                final ModelUser user=dataSnapshot.getValue(ModelUser.class);
//                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
//                ref1.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                            //ArrayList<String> ids = new ArrayList<String>();
//                            userid.add(ds.getValue().toString());
//                            //sendNotification(user.getName(),message);
//                            //Toast.makeText(GroupChatActivity.this, ""+message, Toast.LENGTH_SHORT).show();
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


    }
    private void sendNotification(final String hisUid, final String name, final String message){
        DatabaseReference allTokens= FirebaseDatabase.getInstance().getReference("Groups");

        allTokens.child(groupId).child("Participants").orderByChild("token")
                //Query query=allTokens.orderByKey().equalTo(hisUid);

                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            Token token=ds.getValue(Token.class);

                            Data2 data=new Data2(firebaseAuth.getUid(),name+": "+message,"New Message",R.drawable.groupplaceholder);

                            Sender2 sender=new Sender2(data,token.getToken());

                            apiService.sendNotification(sender)
                                    .enqueue(new Callback<Response>() {
                                        @Override
                                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                            Toast.makeText(GroupChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Call<Response> call, Throwable t) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

//    private void sendNotification(final String hisUid, final String name, final String message){
//        DatabaseReference allTokens=FirebaseDatabase.getInstance().getReference("Tokens");
//        Query query=allTokens.orderByKey().equalTo(hisUid);
//
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot ds: dataSnapshot.getChildren()){
//                    Token token=ds.getValue(Token.class);
//                    Data data=new Data(firebaseAuth.getUid(),name+": "+message,"New Message",hisUid,R.drawable.ic_group);
//
//                    Sender sender=new Sender(data,token.getToken());
//
//                    apiService.sendNotification(sender)
//                            .enqueue(new Callback<Response>() {
//                                @Override
//                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
//                                    Toast.makeText(GroupChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
//                                }
//
//                                @Override
//                                public void onFailure(Call<Response> call, Throwable t) {
//
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

//    private void sendNotification(final String name, final String message) {
//
//        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
//        refff.child(groupId).child("Participants").orderByChild("token").addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                if (dataSnapshot != null) {
//                    //Token token = ds.getValue(Token.class);
//                    tokens.add(dataSnapshot.child("token").getValue().toString());
//                }
//                Toast.makeText(GroupChatActivity.this, "" + tokens, Toast.LENGTH_SHORT).show();
//
//                Data data = new Data(firebaseAuth.getUid(),name + "  : " + message, "New Message ", R.drawable.ic_group, userid);
//
//                Sender sender = new Sender(data, tokens);
//                apiService.sendNotification(sender)
//                        .enqueue(new Callback<Response>() {
//                            @Override
//                            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
//                                Toast.makeText(GroupChatActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onFailure(Call<Response> call, Throwable t) {
//                                Toast.makeText(GroupChatActivity.this, "Unable to send notification..", Toast.LENGTH_SHORT).show();
//
//                            }
//                        });
//
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//        refff.child(groupId).child("Participants").orderByChild("token")
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//
//        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
//        Query query = allTokens.orderByKey().equalTo(uid);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String groupTitle = "" + ds.child("groupTitle").getValue();
                            String groupDescription = "" + ds.child("groupDescription").getValue();
                            String groupIcon = "" + ds.child("groupIcon").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String createdBy = "" + ds.child("createdBy").getValue();

                            groupTitleTv.setText(groupTitle);
                            try {
                                Picasso.get().load(groupIcon).placeholder(R.drawable.groupplaceholder).into(groupIconIv);
                            } catch (Exception e) {
                                groupIconIv.setImageResource(R.drawable.groupplaceholder);
                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendImageMessage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Please wait");
        pd.setMessage("Sending Image...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        String filenamePath = "ChatImages/" + System.currentTimeMillis();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);

        storageReference.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!p_uriTask.isSuccessful()) ;
                        Uri p_downloadUri = p_uriTask.getResult();

                        if (p_uriTask.isSuccessful()) {
                            String timestamp = "" + System.currentTimeMillis();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", "" + firebaseAuth.getUid());
                            hashMap.put("message", "" + p_downloadUri);
                            hashMap.put("timestamp", "" + timestamp);
                            hashMap.put("type", "" + "image");


                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                            ref.child(groupId).child("Messages").child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            messageEt.setText("");
                                            pd.dismiss();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //menu.findItem(R.id.main_settings_option).setVisible(false);
        getMenuInflater().inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);

        if (myGroupRole.equals("creator") || myGroupRole.equals("admin")) {
            menu.findItem(R.id.action_add_participant).setVisible(true);
        } else {
            menu.findItem(R.id.action_add_participant).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_participant) {
            Intent intent = new Intent(this, GroupParticipantAddActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                sendImageMessage();

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                sendImageMessage();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, "Camera and storage permission are required...", Toast.LENGTH_SHORT).show();
                    }


                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, "Storage permission required...", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            case REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0) {
                    Toast.makeText(this, "Permission granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO

        }, REQUEST_PERMISSION_CODE);
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_Audio_Result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_Audio_Result == PackageManager.PERMISSION_GRANTED;
    }
    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    private void updateToken(){

        String token= FirebaseInstanceId.getInstance().getToken();

        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .child("token").setValue(token);







    }

}