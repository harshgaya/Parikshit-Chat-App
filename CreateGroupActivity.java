package com.parikshit.parikshitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity {

    private Button submitBtn;
    private EditText groupEt, groupdescriptionEt;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        submitBtn = findViewById(R.id.submitBtn);
        groupEt = findViewById(R.id.groupEt);
        groupdescriptionEt = findViewById(R.id.groupdescriptionEt);
        loadingBar = new ProgressDialog(this);

        groupEt.addTextChangedListener(new TextWatcher() {
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
        groupdescriptionEt.addTextChangedListener(new TextWatcher() {
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


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadingBar.setTitle("Group Create");
                loadingBar.setMessage("Please wait ,while we are creating Group "+groupEt.getText().toString());
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                createGroup();
            }
        });
    }

    private void createGroup() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String timestamp=""+System.currentTimeMillis();

        String token = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> hashMapp = new HashMap<>();

        hashMapp.put("role", "" + "admin");
        hashMapp.put("timestamp",timestamp);
        hashMapp.put("uid", "" + uid);
        hashMapp.put("token", "" + token);
        DatabaseReference refff = FirebaseDatabase.getInstance().getReference("Groups");
        refff.child(groupEt.getText().toString()).child("Participants").child(uid)
                .setValue(hashMapp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        CreateGroup();

                        //Toast.makeText(GroupChatActivity.this, "Success ..", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateGroupActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });



    }

    private void CreateGroup(){
        HashMap<String, Object> hashMapp2 = new HashMap<>();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String timestamp=""+System.currentTimeMillis();

        hashMapp2.put("createdBy", uid);
        hashMapp2.put("groupDescription",groupdescriptionEt.getText().toString());
        hashMapp2.put("groupIcon", "");
        hashMapp2.put("groupId", groupEt.getText().toString());
        hashMapp2.put("groupTitle", groupEt.getText().toString());
        hashMapp2.put("timestamp", timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");

        ref.child(groupEt.getText().toString()).updateChildren(hashMapp2).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                groupdescriptionEt.setText("");
                groupEt.setText("");
                Toast.makeText(CreateGroupActivity.this, "Group Created Successfully..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(groupEt.getText())) {
            if (!TextUtils.isEmpty(groupdescriptionEt.getText())) {

                submitBtn.setEnabled(true);
                submitBtn.setTextColor(Color.rgb(255, 255, 255));

            } else {
                submitBtn.setEnabled(false);
                submitBtn.setTextColor(Color.argb(50, 255, 255, 255));
            }
        } else {
            submitBtn.setEnabled(false);
            submitBtn.setTextColor(Color.argb(50, 255, 255, 255));
        }
    }
}