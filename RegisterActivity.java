package com.parikshit.parikshitchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.parikshit.parikshitchat.notifications.Token;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    String[] standardNames={"9th","10th","11th Board","12th Board","11th+IIT JEE","12th+IIT JEE","11th+Medical","12th+Medical","Target IIT","Target NEET"};
    private  String selectedStandard;

    private CircleImageView main_profile_image;
    private ImageView add_profile_icon;
    private EditText nameet, statuset, cityet, stateet,collegeet;
    private Button registerbtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog loadingBar;
    private FloatingActionButton camerabtn;
    private static final int GalleryPick=1;


    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;
    private StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //main_profile_image = findViewById(R.id.main_profile_image);
//        camerabtn = findViewById(R.id.camerabtn);






        nameet = findViewById(R.id.nameet);
        statuset = findViewById(R.id.statuset);
        cityet = findViewById(R.id.cityet);
        stateet = findViewById(R.id.stateet);
        collegeet = findViewById(R.id.collegeet);
        registerbtn = findViewById(R.id.registerbtn);
        firebaseAuth = FirebaseAuth.getInstance();

        mAuth= FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");


        Spinner spin = (Spinner) findViewById(R.id.simpleSpinner);
        spin.setOnItemSelectedListener(RegisterActivity.this);


        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,standardNames);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setAdapter(aa);



//        camerabtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent galleryIntent=new Intent();
//                galleryIntent.setAction(Intent.ACTION_PICK);
//                galleryIntent.setType("image/*");
//                startActivityForResult(galleryIntent,GalleryPick);
//
//
//
//            }
//        });

        loadingBar = new ProgressDialog(this);


        nameet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        collegeet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        statuset.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        cityet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        stateet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });




        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=nameet.getText().toString();
                String status=statuset.getText().toString();
                String city=cityet.getText().toString();
                String state=stateet.getText().toString();
                String college=collegeet.getText().toString();

                loadingBar.setTitle("Registering");
                loadingBar.setMessage("Please wait ,while we are registering..");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                if(selectedStandard=="9th"){
                    Addto9thClassGroup(name,college,status,city,state);

                }else if (selectedStandard=="10th"){
                    AddTo10thClassGroup(name,college,status,city,state);
                }else if (selectedStandard=="11th Board"){
                    AddTo11thBoardGroup(name,college,status,city,state);
                }else if (selectedStandard=="12th Board"){
                    AddTo12thBoardGroup(name,college,status,city,state);
                }else if (selectedStandard=="11th+IIT JEE"){
                    AddTo11PlusIITGroup(name,college,status,city,state);

                    //"9th","10th","11th Board","12th Board","11th+IIT JEE","12th+IIT JEE","11th+Medical","12th+Medical","Target IIT","Target NEET"
                }else if (selectedStandard=="12th+IIT JEE"){
                    AddTo12PlusIITGroup(name,college,status,city,state);
                }else if (selectedStandard=="11th+Medical"){
                    AddTo11PlusNEETGroup(name,college,status,city,state);
                }else if (selectedStandard=="12th+Medical"){
                    AddTo12PlusNEETGroup(name,college,status,city,state);
                }else if (selectedStandard=="Target IIT"){
                    AddToTargetIITGroup(name,college,status,city,state);
                }else if (selectedStandard=="Target NEET"){
                    AddToTargetNEETGroup(name,college,status,city,state);
                }

                setUserToUser(name,status,college);

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){

                loadingBar.setTitle("Set profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                final Uri resultUri=result.getUri();
                final StorageReference filePath=UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadUrl = uri.toString();

                                        Picasso.get().load(downloadUrl).into(main_profile_image);
                                        // complete the rest of your code

                                        RootRef.child("UsersImages").child(currentUserID).child("image")
                                                .setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Toast.makeText(RegisterActivity.this, "Image saved in database successfully ..", Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }else{
                                                            String message=task.getException().toString();
                                                            Toast.makeText(RegisterActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                    }
                                                });

                                    }
                                });
                                loadingBar.dismiss();

                            }
                        });

            }
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

        selectedStandard = standardNames[position];

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private void Addto9thClassGroup(String name,String college, String status, String city, String state) {

        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> hashMapp = new HashMap<>();

        hashMapp.put("name", "" + name);
        hashMapp.put("college", "" + college);
        hashMapp.put("status", "" + status);
        hashMapp.put("city", "" + city);
        hashMapp.put("state", "" + state);
        hashMapp.put("role", "" + "participant");
        hashMapp.put("uid", "" + firebaseAuth.getUid());
        hashMapp.put("token", "" + token);
        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child("9thClass").child("Participants").child(firebaseAuth.getUid())
                .setValue(hashMapp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(GroupChatActivity.this, "Success ..", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }
    private void AddTo10thClassGroup(String name,String college, String status, String city, String state) {

        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> hashMapp = new HashMap<>();

        hashMapp.put("name", "" + name);
        hashMapp.put("college", "" + college);
        hashMapp.put("status", "" + status);
        hashMapp.put("city", "" + city);
        hashMapp.put("state", "" + state);
        hashMapp.put("role", "" + "participant");
        hashMapp.put("uid", "" + firebaseAuth.getUid());
        hashMapp.put("token", "" + token);
        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child("10thClass").child("Participants").child(firebaseAuth.getUid())
                .setValue(hashMapp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(GroupChatActivity.this, "Success ..", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }
    private void AddTo11thBoardGroup(String name,String college, String status, String city, String state) {

        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> hashMapp = new HashMap<>();

        hashMapp.put("name", "" + name);
        hashMapp.put("college", "" + college);
        hashMapp.put("status", "" + status);
        hashMapp.put("city", "" + city);
        hashMapp.put("state", "" + state);
        hashMapp.put("role", "" + "participant");
        hashMapp.put("uid", "" + firebaseAuth.getUid());
        hashMapp.put("token", "" + token);
        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child("11thBoard").child("Participants").child(firebaseAuth.getUid())
                .setValue(hashMapp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(GroupChatActivity.this, "Success ..", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }
    private void AddTo12thBoardGroup(String name,String college, String status, String city, String state) {

        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> hashMapp = new HashMap<>();

        hashMapp.put("name", "" + name);
        hashMapp.put("college", "" + college);
        hashMapp.put("status", "" + status);
        hashMapp.put("city", "" + city);
        hashMapp.put("state", "" + state);
        hashMapp.put("role", "" + "participant");
        hashMapp.put("uid", "" + firebaseAuth.getUid());
        hashMapp.put("token", "" + token);
        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child("12thBoard").child("Participants").child(firebaseAuth.getUid())
                .setValue(hashMapp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(GroupChatActivity.this, "Success ..", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }
    private void AddTo11PlusIITGroup(String name,String college, String status, String city, String state) {

        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> hashMapp = new HashMap<>();

        hashMapp.put("name", "" + name);
        hashMapp.put("college", "" + college);
        hashMapp.put("status", "" + status);
        hashMapp.put("city", "" + city);
        hashMapp.put("state", "" + state);
        hashMapp.put("role", "" + "participant");
        hashMapp.put("uid", "" + firebaseAuth.getUid());
        hashMapp.put("token", "" + token);
        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child("11thPlusIIT").child("Participants").child(firebaseAuth.getUid())
                .setValue(hashMapp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(GroupChatActivity.this, "Success ..", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }
    private void AddTo12PlusIITGroup(String name,String college, String status, String city, String state) {

        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> hashMapp = new HashMap<>();

        hashMapp.put("name", "" + name);
        hashMapp.put("college", "" + college);
        hashMapp.put("status", "" + status);
        hashMapp.put("city", "" + city);
        hashMapp.put("state", "" + state);
        hashMapp.put("role", "" + "participant");
        hashMapp.put("uid", "" + firebaseAuth.getUid());
        hashMapp.put("token", "" + token);
        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child("12thPlusIIT").child("Participants").child(firebaseAuth.getUid())
                .setValue(hashMapp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(GroupChatActivity.this, "Success ..", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }
    private void AddTo11PlusNEETGroup(String name,String college, String status, String city, String state) {

        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> hashMapp = new HashMap<>();

        hashMapp.put("name", "" + name);
        hashMapp.put("college", "" + college);
        hashMapp.put("status", "" + status);
        hashMapp.put("city", "" + city);
        hashMapp.put("state", "" + state);
        hashMapp.put("role", "" + "participant");
        hashMapp.put("uid", "" + firebaseAuth.getUid());
        hashMapp.put("token", "" + token);
        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child("11thPlusNEET").child("Participants").child(firebaseAuth.getUid())
                .setValue(hashMapp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(GroupChatActivity.this, "Success ..", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }
    private void AddTo12PlusNEETGroup(String name,String college, String status, String city, String state) {

        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> hashMapp = new HashMap<>();

        hashMapp.put("name", "" + name);
        hashMapp.put("college", "" + college);
        hashMapp.put("status", "" + status);
        hashMapp.put("city", "" + city);
        hashMapp.put("state", "" + state);
        hashMapp.put("role", "" + "participant");
        hashMapp.put("uid", "" + firebaseAuth.getUid());
        hashMapp.put("token", "" + token);
        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child("12thPlusNEET").child("Participants").child(firebaseAuth.getUid())
                .setValue(hashMapp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        RegisterActivity.this.finish();
                        //Toast.makeText(GroupChatActivity.this, "Success ..", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }
    private void AddToTargetIITGroup(String name,String college, String status, String city, String state) {

        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> hashMapp = new HashMap<>();

        hashMapp.put("name", "" + name);
        hashMapp.put("college", "" + college);
        hashMapp.put("status", "" + status);
        hashMapp.put("city", "" + city);
        hashMapp.put("state", "" + state);
        hashMapp.put("role", "" + "participant");
        hashMapp.put("uid", "" + firebaseAuth.getUid());
        hashMapp.put("token", "" + token);
        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child("TargetIIT").child("Participants").child(firebaseAuth.getUid())
                .setValue(hashMapp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(GroupChatActivity.this, "Success ..", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }
    private void AddToTargetNEETGroup(String name,String college, String status, String city, String state) {

        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> hashMapp = new HashMap<>();

        hashMapp.put("name", "" + name);
        hashMapp.put("college", "" + college);
        hashMapp.put("status", "" + status);
        hashMapp.put("city", "" + city);
        hashMapp.put("state", "" + state);
        hashMapp.put("role", "" + "participant");
        hashMapp.put("uid", "" + firebaseAuth.getUid());
        hashMapp.put("token", "" + token);
        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child("TargetNEET").child("Participants").child(firebaseAuth.getUid())
                .setValue(hashMapp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        //Toast.makeText(GroupChatActivity.this, "Success ..", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });
    }


    private void setUserToUser(String name, String status,String college){

        HashMap<String, Object> profileMap=new HashMap<>();
        profileMap.put("uid",firebaseAuth.getUid());
        profileMap.put("name",name);
        profileMap.put("onlineStatus","online");
        profileMap.put("status",status);
        profileMap.put("college",college);
        profileMap.put("role","participant");
        profileMap.put("typingTo","noOne");
        profileMap.put("standard",selectedStandard);
        profileMap.put("email",firebaseAuth.getInstance().getCurrentUser().getEmail());
        profileMap.put("profile","");

        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Users");
        refff.child(firebaseAuth.getUid()).setValue(profileMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });

    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(nameet.getText())) {
            if (!TextUtils.isEmpty(collegeet.getText())) {
                if (!TextUtils.isEmpty(statuset.getText())) {
                    if (!TextUtils.isEmpty(cityet.getText())) {
                        if (!TextUtils.isEmpty(stateet.getText())) {
                            registerbtn.setEnabled(true);
                            registerbtn.setTextColor(Color.rgb(255, 255, 255));
                        } else {
                            registerbtn.setEnabled(false);
                            registerbtn.setTextColor(Color.argb(50,255, 255, 255));
                        }
                    } else {
                        registerbtn.setEnabled(false);
                        registerbtn.setTextColor(Color.argb(50,255, 255, 255));
                    }
                } else {
                    registerbtn.setEnabled(false);
                    registerbtn.setTextColor(Color.argb(50,255, 255, 255));
                }
            }else{
                registerbtn.setEnabled(false);
                registerbtn.setTextColor(Color.argb(50,255, 255, 255));
            }
        } else {
            registerbtn.setEnabled(false);
            registerbtn.setTextColor(Color.argb( 50,255, 255, 255));
        }
    }

    public void  updateToken(String Token){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Tokens");
        String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();


        Token mToken=new Token(Token);
        databaseReference.child(uid).setValue(mToken);
    }


}