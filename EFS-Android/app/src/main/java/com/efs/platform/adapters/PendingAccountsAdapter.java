package com.efs.platform.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.efs.platform.R;
import com.efs.platform.models.Models;
import java.util.List;

public class PendingAccountsAdapter extends RecyclerView.Adapter<PendingAccountsAdapter.ViewHolder> {
    
    private List<Models.PendingAccount> accounts;
    private OnApproveListener approveListener;
    private OnRejectListener rejectListener;
    private OnViewPhotoListener viewPhotoListener;
    
    public interface OnApproveListener {
        void onApprove(String sid);
    }
    
    public interface OnRejectListener {
        void onReject(String sid);
    }
    
    public interface OnViewPhotoListener {
        void onViewPhoto(Models.PendingAccount account);
    }
    
    public PendingAccountsAdapter(List<Models.PendingAccount> accounts,
                                  OnApproveListener approveListener,
                                  OnRejectListener rejectListener,
                                  OnViewPhotoListener viewPhotoListener) {
        this.accounts = accounts;
        this.approveListener = approveListener;
        this.rejectListener = rejectListener;
        this.viewPhotoListener = viewPhotoListener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_account, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.PendingAccount account = accounts.get(position);
        
        holder.tvSid.setText(account.sid);
        holder.tvEmail.setText(account.email);
        holder.tvCreatedAt.setText(account.createdAt != null ? 
            new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date(account.createdAt)) : "N/A");
        
        holder.btnViewPhoto.setOnClickListener(v -> {
            if (viewPhotoListener != null) viewPhotoListener.onViewPhoto(account);
        });
        
        holder.btnApprove.setOnClickListener(v -> {
            if (approveListener != null) approveListener.onApprove(account.sid);
        });
        
        holder.btnReject.setOnClickListener(v -> {
            if (rejectListener != null) rejectListener.onReject(account.sid);
        });
    }
    
    @Override
    public int getItemCount() {
        return accounts != null ? accounts.size() : 0;
    }
    
    public void updateList(List<Models.PendingAccount> newList) {
        this.accounts = newList;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSid, tvEmail, tvCreatedAt;
        Button btnViewPhoto, btnApprove, btnReject;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvSid = itemView.findViewById(R.id.tvSid);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnViewPhoto = itemView.findViewById(R.id.btnViewPhoto);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
