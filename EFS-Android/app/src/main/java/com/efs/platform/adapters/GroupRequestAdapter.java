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

public class GroupRequestAdapter extends RecyclerView.Adapter<GroupRequestAdapter.ViewHolder> {
    
    private List<Models.GroupRequest> requests;
    private OnInviteClickListener inviteListener;
    private OnDeleteClickListener deleteListener;
    private String currentUserSid;
    
    public interface OnInviteClickListener {
        void onInviteClick(Models.GroupRequest request);
    }
    
    public interface OnDeleteClickListener {
        void onDeleteClick(String requestId);
    }
    
    public GroupRequestAdapter(List<Models.GroupRequest> requests, String currentUserSid,
                               OnInviteClickListener inviteListener, OnDeleteClickListener deleteListener) {
        this.requests = requests;
        this.currentUserSid = currentUserSid;
        this.inviteListener = inviteListener;
        this.deleteListener = deleteListener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_request, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.GroupRequest request = requests.get(position);
        
        holder.tvSid.setText(request.sid);
        holder.tvMajor.setText(request.major);
        holder.tvDescription.setText(request.description != null ? request.description : "No description");
        holder.tvEmail.setText(request.email);
        
        boolean isOwner = request.sid.equals(currentUserSid);
        holder.btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        
        holder.btnInvite.setOnClickListener(v -> {
            if (inviteListener != null) inviteListener.onInviteClick(request);
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(request.id);
        });
    }
    
    @Override
    public int getItemCount() {
        return requests != null ? requests.size() : 0;
    }
    
    public void updateList(List<Models.GroupRequest> newList) {
        this.requests = newList;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSid, tvMajor, tvDescription, tvEmail;
        Button btnInvite, btnDelete;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvSid = itemView.findViewById(R.id.tvSid);
            tvMajor = itemView.findViewById(R.id.tvMajor);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnInvite = itemView.findViewById(R.id.btnInvite);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
