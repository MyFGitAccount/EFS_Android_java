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

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    
    private List<Models.User> users;
    private OnDeleteClickListener deleteListener;
    private String currentUserSid;
    
    public interface OnDeleteClickListener {
        void onDelete(String sid);
    }
    
    public UsersAdapter(List<Models.User> users, String currentUserSid, OnDeleteClickListener listener) {
        this.users = users;
        this.currentUserSid = currentUserSid;
        this.deleteListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.User user = users.get(position);
        
        holder.tvSid.setText(user.sid);
        holder.tvEmail.setText(user.email);
        holder.tvCredits.setText(String.valueOf(user.credits));
        
        String role = user.role != null ? user.role.toUpperCase() : "STUDENT";
        holder.tvRole.setText(role);
        holder.tvRole.setBackgroundResource("admin".equals(user.role) ? R.color.red : R.color.blue);
        
        boolean isCurrentUser = user.sid.equals(currentUserSid);
        holder.btnDelete.setEnabled(!isCurrentUser);
        holder.btnDelete.setAlpha(isCurrentUser ? 0.5f : 1.0f);
        
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null && !isCurrentUser) {
                deleteListener.onDelete(user.sid);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }
    
    public void updateList(List<Models.User> newList) {
        this.users = newList;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSid, tvEmail, tvCredits, tvRole;
        Button btnDelete;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvSid = itemView.findViewById(R.id.tvSid);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvCredits = itemView.findViewById(R.id.tvCredits);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
