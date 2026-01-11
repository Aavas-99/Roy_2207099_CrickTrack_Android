package com.example.roy_2207099_cricktrack_android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
    private List<AccessRequest> list;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener { void onApprove(AccessRequest req); void onReject(AccessRequest req); }

    public RequestAdapter(List<AccessRequest> list, OnRequestActionListener listener) {
        this.list = list; this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_request, p, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        AccessRequest req = list.get(pos);
        h.tvEmail.setText(req.userEmail);
        h.tvMatch.setText(req.matchName);
        h.btnApprove.setOnClickListener(v -> listener.onApprove(req));
        h.btnReject.setOnClickListener(v -> listener.onReject(req));
    }

    @Override public int getItemCount() { return list.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvMatch; Button btnApprove, btnReject;
        ViewHolder(View v) {
            super(v);
            tvEmail = v.findViewById(R.id.tvReqUserName);
            tvMatch = v.findViewById(R.id.tvReqMatchName);
            btnApprove = v.findViewById(R.id.btnApprove);
            btnReject = v.findViewById(R.id.btnReject);
        }
    }
}