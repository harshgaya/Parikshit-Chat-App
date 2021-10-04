package com.parikshit.parikshitchat.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.parikshit.parikshitchat.ImageViewActivity;
import com.parikshit.parikshitchat.InternalURLSpan;
import com.parikshit.parikshitchat.Model.ModelChat;
import com.parikshit.parikshitchat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String ImageUrl;

    FirebaseUser fUser;
    private FirebaseAuth firebaseAuth;
    private Dialog audioPlayDialog;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        ImageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new MyHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        final String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("MMMM d, yyyy hh:mm aa", cal).toString();
        String messageType = chatList.get(position).getType();


        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(Long.parseLong(timeStamp));

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            String date = "Today " + DateFormat.format(timeFormatString, smsTime);
            holder.timeTv.setText(date);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            String date = "Yesterday " + DateFormat.format(timeFormatString, smsTime);
            holder.timeTv.setText(date);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            DateFormat.format(dateTimeFormatString, smsTime).toString();
            holder.timeTv.setText(dateTime);
        } else {
            //DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString();
            holder.timeTv.setText(dateTime);
        }


        //holder.messageTv.setText(message);

        try {
            Picasso.get().load(ImageUrl).placeholder(R.drawable.ic_baseline_face_24).into(holder.profileIv);
        } catch (Exception e) {

        }


        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(position);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });


        if (position == chatList.size() - 1) {
            if (chatList.get(position).isSeen()) {
                holder.isSeenTv.setText("Seen");
            } else {
                holder.isSeenTv.setText("Delivered");
            }
        } else {
            holder.isSeenTv.setVisibility(View.GONE);
        }

        if (messageType.equals("text")) {
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);
            holder.jcplayer.setVisibility(View.GONE);
        } else if (messageType.equals("image")) {
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);
            holder.jcplayer.setVisibility(View.GONE);
            try {
                Picasso.get().load(message).placeholder(R.drawable.placeholderimage).into(holder.messageIv);
            } catch (Exception e) {
                holder.messageIv.setImageResource(R.drawable.placeholderimage);
            }
        } else if (messageType.equals("audio")) {
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.GONE);
            holder.jcplayer.setVisibility(View.VISIBLE);


            //holder.jcplayer.initPlaylist(Collections.singletonList(jcAudio), null);
        }
        if (messageType.equals("image")) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ImageViewActivity.class);
                    intent.putExtra("url_of_image", message);
                    context.startActivity(intent);

                }
            });
        } else if (messageType.equals("text")) {
            setLinks(holder.messageTv, message, message);


        } else if (messageType.equals("audio")) {
            audioPlayDialog = new Dialog(context);
            audioPlayDialog.setContentView(R.layout.audio_palyer_layout);
            audioPlayDialog.setCancelable(true);
            audioPlayDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    audioPlayDialog.show();


                    JcPlayerView jcplayer = audioPlayDialog.findViewById(R.id.jcplayer);

                    ArrayList<JcAudio> jcAudios = new ArrayList<>();
                    jcAudios.add(JcAudio.createFromURL("", message));

                    jcplayer.initPlaylist(jcAudios, null);

                }
            });
            audioPlayDialog.dismiss();
        }
    }

    private void deleteMessage(int position) {

        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("sender").getValue().equals(myUID)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message has been deleted...");
                        //dataSnapshot.getRef().removeValue();
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "you can only delete your message...", Toast.LENGTH_SHORT).show();
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();

        String uis = FirebaseAuth.getInstance().getCurrentUser().getUid();


        if (chatList.get(position).getSender().equals(uis)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView profileIv;
        private ImageView messageIv;
        private LinearLayout jcplayer;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            jcplayer = itemView.findViewById(R.id.jcplayer);
            messageIv = itemView.findViewById(R.id.messageIv);
        }
    }

    void setLinks(TextView tv, String text, final String link) {
        String[] linkPatterns = {
                "([Hh][tT][tT][pP][sS]?:\\/\\/[^ ,'\">\\]\\)]*[^\\. ,'\">\\]\\)])"};
        //"#[\\w]+", "@[\\w]+" };
        for (String str : linkPatterns) {
            Pattern pattern = Pattern.compile(str);
            Matcher matcher = pattern.matcher(tv.getText());
            while (matcher.find()) {
                int x = matcher.start();
                int y = matcher.end();
                final android.text.SpannableString f = new android.text.SpannableString(
                        tv.getText());
                InternalURLSpan span = new InternalURLSpan();
                span.text = text.substring(x, y);
                f.setSpan(span, x, y,
                        android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(f);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(context, Uri.parse(link));
                    }
                });

            }
        }
        tv.setLinkTextColor(Color.BLUE);
        tv.setLinksClickable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setFocusable(false);
    }
}
