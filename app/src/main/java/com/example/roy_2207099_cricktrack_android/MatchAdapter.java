package com.example.roy_2207099_cricktrack_android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    private List<MatchRow> matchList;
    private OnMatchClickListener listener;

    public interface OnMatchClickListener {
        void onMatchClick(String matchId);
    }

    public MatchAdapter(List<MatchRow> matchList, OnMatchClickListener listener) {
        this.matchList = matchList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match_row, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchRow match = matchList.get(position);
        holder.tvTeams.setText(match.getTeamsText());
        holder.tvVenueDate.setText(match.stadium + " | " + match.date);

        String res = match.result;
        if (res == null || res.isEmpty()) res = "Match In Progress / No Result";
        holder.tvResult.setText(res);

        holder.btnView.setOnClickListener(v -> listener.onMatchClick(match.id));
    }

    @Override
    public int getItemCount() { return matchList.size(); }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeams, tvVenueDate, tvResult;
        Button btnView;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeams = itemView.findViewById(R.id.tvMatchTeams);
            tvVenueDate = itemView.findViewById(R.id.tvVenueDate);
            tvResult = itemView.findViewById(R.id.tvResult);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}