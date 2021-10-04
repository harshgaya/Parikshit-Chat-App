package com.parikshit.parikshitchat.Notification2;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.parikshit.parikshitchat.notifications.Token;


public class FirebaseService2 extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        Log.e("New token",s);
        super.onNewToken(s);

        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        String tokenRefresh= FirebaseInstanceId.getInstance().getToken();
        if (user!=null){
            updateToken(tokenRefresh);
        }
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token=new Token();
        ref.child(user.getUid()).setValue(token);
    }
}
