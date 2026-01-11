package com.example.roy_2207099_cricktrack_android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BatsmanSummaryAdapter extends RecyclerView.Adapter<BatsmanSummaryAdapter.ViewHolder> {
    private List<ScoreUpdate.Batsman> list;

    public BatsmanSummaryAdapter(List<ScoreUpdate.Batsman> list) { this.list = list; }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_batsman_summary, p, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        ScoreUpdate.Batsman b = list.get(pos);
        h.name.setText(b.getName());
        h.runs.setText(String.valueOf(b.getRuns()));
        h.balls.setText(String.valueOf(b.getBalls()));
        h.status.setText(b.isOut() ? "Out" : "Not Out");
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, runs, balls, status;
        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tvName);
            runs = v.findViewById(R.id.tvRuns);
            balls = v.findViewById(R.id.tvBalls);
            status = v.findViewById(R.id.tvStatus);
        }
    }
}