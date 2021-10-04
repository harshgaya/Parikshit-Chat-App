package com.parikshit.parikshitchat.Adapter;

import android.app.Dialog;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parikshit.parikshitchat.ImageViewActivity;
import com.parikshit.parikshitchat.InternalURLSpan;
import com.parikshit.parikshitchat.Model.ModelGroupChat;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.parikshit.parikshitchat.R;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_right = 1;

    private Context context;
    private ArrayList<ModelGroupChat> modelGroupChatList;

    private FirebaseAuth firebaseAuth;
    private Dialog audioPlayDialog;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChatList) {
        this.context = context;
        this.modelGroupChatList = modelGroupChatList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_right) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
            return new HolderGroupChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new HolderGroupChat(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        ModelGroupChat model = modelGroupChatList.get(position);
        String timestamp = model.getTimestamp();
        final String message = model.getMessage();
        String senderUid = model.getSender();
        String messageType = model.getType();

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("MMMM d, yyyy hh:mm aa", cal).toString();

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
                Picasso.get().load(message).placeholder(R.drawable.avatarplaceholder).into(holder.messageIv);
            } catch (Exception e) {
                holder.messageIv.setImageResource(R.drawable.avatarplaceholder);
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


        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(Long.parseLong(timestamp));

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ) {
            String date=  "Today " + DateFormat.format(timeFormatString, smsTime);
            holder.timeTv.setText(date);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1  ){
            String date="Yesterday " + DateFormat.format(timeFormatString, smsTime);
            holder.timeTv.setText(date);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            ////DateFormat.format(dateTimeFormatString, smsTime).toString();
            holder.timeTv.setText(dateTime);
        } else {
            //DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString();
            holder.timeTv.setText(dateTime);
        }

        setUserName(model, holder);

    }

    private void setUserName(ModelGroupChat model, final HolderGroupChat holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();

                            holder.nameTv.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelGroupChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (modelGroupChatList.get(position).getSender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_right;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {

        private TextView nameTv, messageTv, timeTv;
        private ImageView messageIv;
        private LinearLayout jcplayer;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageIv = itemView.findViewById(R.id.messageIv);
            jcplayer = itemView.findViewById(R.id.jcplayer);

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
