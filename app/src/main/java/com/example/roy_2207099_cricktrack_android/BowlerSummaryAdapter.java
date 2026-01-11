package com.example.roy_2207099_cricktrack_android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BowlerSummaryAdapter extends RecyclerView.Adapter<BowlerSummaryAdapter.ViewHolder> {
    private List<ScoreUpdate.Bowler> list;

    public BowlerSummaryAdapter(List<ScoreUpdate.Bowler> list) { this.list = list; }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_bowler_summary, p, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        ScoreUpdate.Bowler b = list.get(pos);
        h.name.setText(b.getName());
        h.balls.setText(String.valueOf(b.getBallsBowled()));
        h.runs.setText(String.valueOf(b.getRuns()));
        h.wkts.setText(String.valueOf(b.getWickets()));
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, balls, runs, wkts;
        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tvName);
            balls = v.findViewById(R.id.tvBalls);
            runs = v.findViewById(R.id.tvRuns);
            wkts = v.findViewById(R.id.tvWkts);
        }
    }
}