package com.parikshit.parikshitchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parikshit.parikshitchat.GroupChatActivity;
import com.parikshit.parikshitchat.Model.ModelGroupChatList;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import com.parikshit.parikshitchat.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList>{

    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_groupchats_list,parent,false);


        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {
        ModelGroupChatList model=groupChatLists.get(position);
        final String groupId=model.getGroupId();
        String groupIcon=model.getGroupIcon();
        String groupTitle=model.getGroupTitle();


        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");

        // load last message and date

        loadLastMessage(model,holder);

        holder.groupTitleTv.setText(groupTitle);
        try{
            Picasso.get().load(groupIcon).placeholder(R.drawable.groupplaceholder).into(holder.groupIconIv);
        }
        catch (Exception e){
            holder.groupIconIv.setImageResource(R.drawable.groupplaceholder);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId",groupId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });

    }

    private void loadLastMessage(ModelGroupChatList model, final HolderGroupChatList holder) {
        // get last message from group
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                String message=""+ds.child("message").getValue();
                                String timestamp=""+ds.child("timestamp").getValue();
                                String sender=""+ds.child("sender").getValue();
                                String messageType=""+ds.child("type").getValue();

                                Calendar cal= Calendar.getInstance(Locale.ENGLISH);

                                cal.setTimeInMillis(Long.parseLong(timestamp));
                                String dateTime= DateFormat.format("MMMM d, yyyy hh:mm aa",cal).toString();


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
                                if (messageType.equals("image")){
                                    holder.messageTv.setText("Sent Photo");
                                }else if (messageType.equals("text")){
                                    holder.messageTv.setText(message);
                                }else if (messageType.equals("audio")){
                                    holder.messageTv.setText("Sent Audio");
                                }


                                //holder.timeTv.setText(dateTime);

                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                                ref.orderByChild("uid").equalTo(sender)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                                    String name=""+ds.child("name").getValue();
                                                    holder.nameTv.setText(name);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                            }
                        }else{
                            holder.nameTv.setText("Tap to chat");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    class HolderGroupChatList extends RecyclerView.ViewHolder{
        private CircleImageView groupIconIv;
        private TextView groupTitleTv,nameTv,messageTv,timeTv;


        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);
            groupIconIv=itemView.findViewById(R.id.groupIconIv);
            groupTitleTv=itemView.findViewById(R.id.groupTitleTv);
            nameTv=itemView.findViewById(R.id.nameTv);
            messageTv=itemView.findViewById(R.id.messageTv);
            timeTv=itemView.findViewById(R.id.timeTv);





        }
    }
}
