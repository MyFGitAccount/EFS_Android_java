package com.efs.platform.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.efs.platform.R;
import com.efs.platform.fragments.CalendarFragment;
import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {
    
    private List<CalendarFragment.SavedSession> sessions;
    private OnRemoveClickListener removeListener;
    
    public interface OnRemoveClickListener {
        void onRemove(String classId);
    }
    
    public TimetableAdapter(List<CalendarFragment.SavedSession> sessions, OnRemoveClickListener listener) {
        this.sessions = sessions;
        this.removeListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable_session, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarFragment.SavedSession session = sessions.get(position);
        
        String classId = session.code + "-" + session.classNo;
        
        holder.tvCourse.setText(session.code + " " + session.classNo);
        holder.tvTitle.setText(session.title);
        holder.tvTime.setText(session.day + " " + session.time);
        holder.tvRoom.setText(session.room);
        
        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) removeListener.onRemove(classId);
        });
    }
    
    @Override
    public int getItemCount() {
        return sessions != null ? sessions.size() : 0;
    }
    
    public void setSessions(List<CalendarFragment.SavedSession> newSessions) {
        this.sessions = newSessions;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourse, tvTitle, tvTime, tvRoom;
        Button btnRemove;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvCourse = itemView.findViewById(R.id.tvCourse);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
