package com.example.roy_2207099_cricktrack_android;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.List;

public class UserMatchAdapter extends RecyclerView.Adapter<UserMatchAdapter.ViewHolder> {
    private List<MatchRow> list;
    private String userId;
    private OnMatchClickListener listener;

    public interface OnMatchClickListener { void onReqest(MatchRow m); void onView(MatchRow m); }

    public UserMatchAdapter(List<MatchRow> list, String userId, OnMatchClickListener listener) {
        this.list = list; this.userId = userId; this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_match_user, p, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchRow m = list.get(position);

        holder.tvTeams.setText(m.team_a + " vs " + m.team_b);

        String venueStr = (m.stadium != null) ? m.stadium : "No Venue";
        String dateStr = (m.date != null) ? m.date : "No Date";
        holder.tvDetails.setText(dateStr + " | " + venueStr);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.child("permissions").child(m.id).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) {
                    setButtonStyle(holder.btn, "View Match", "#388E3C", true);
                    holder.btn.setOnClickListener(v -> listener.onView(m));
                } else {
                    ref.child("pending_requests").child(m.id + "_" + userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap2) {
                            if (snap2.exists()) {
                                setButtonStyle(holder.btn, "Pending...", "#FFA000", false);
                            } else {
                                setButtonStyle(holder.btn, "Request Access", "#1976D2", true);
                                holder.btn.setOnClickListener(v -> listener.onReqest(m));
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void setButtonStyle(Button btn, String txt, String color, boolean enabled) {
        btn.setText(txt);
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
        btn.setEnabled(enabled);
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeams, tvDetails;
        Button btn;

        ViewHolder(View v) {
            super(v);
            tvTeams = v.findViewById(R.id.tvTeams);
            tvDetails = v.findViewById(R.id.tvDetails);
            btn = v.findViewById(R.id.btnAction);
        }
    }
}