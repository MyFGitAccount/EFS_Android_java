package com.efs.platform.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.efs.platform.R;
import com.efs.platform.models.Models;
import com.efs.platform.fragments.CalendarFragment;
import java.util.List;

public class CourseSearchAdapter extends RecyclerView.Adapter<CourseSearchAdapter.ViewHolder> {
    
    private List<CalendarFragment.CourseWithSessions> courses;
    private OnClassAddListener listener;
    
    public interface OnClassAddListener {
        void onAddClass(Models.Course course, Models.TimetableSlot session);
    }
    
    public CourseSearchAdapter(List<CalendarFragment.CourseWithSessions> courses, OnClassAddListener listener) {
        this.courses = courses;
        this.listener = listener;
    }
    
    public void setCoursesWithSessions(List<CalendarFragment.CourseWithSessions> courses) {
        this.courses = courses;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_search, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarFragment.CourseWithSessions cws = courses.get(position);
        holder.tvCourseCode.setText(cws.course.code);
        holder.tvCourseTitle.setText(cws.course.title);
        
        holder.llClasses.removeAllViews();
        
        if (cws.sessions != null && !cws.sessions.isEmpty()) {
            // Group by classNo
            for (Models.TimetableSlot session : cws.sessions) {
                View classView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.item_class_option, holder.llClasses, false);
                
                TextView tvClassNo = classView.findViewById(R.id.tvClassNo);
                TextView tvTime = classView.findViewById(R.id.tvTime);
                TextView tvRoom = classView.findViewById(R.id.tvRoom);
                Button btnAdd = classView.findViewById(R.id.btnAdd);
                
                tvClassNo.setText(session.classNo);
                tvTime.setText(session.startTime + "-" + session.endTime);
                tvRoom.setText(session.room);
                
                btnAdd.setOnClickListener(v -> listener.onAddClass(cws.course, session));
                
                holder.llClasses.addView(classView);
            }
        } else {
            TextView tvEmpty = new TextView(holder.itemView.getContext());
            tvEmpty.setText("No classes available");
            tvEmpty.setPadding(16, 8, 16, 8);
            holder.llClasses.addView(tvEmpty);
        }
    }
    
    @Override
    public int getItemCount() {
        return courses != null ? courses.size() : 0;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseCode, tvCourseTitle;
        LinearLayout llClasses;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            llClasses = itemView.findViewById(R.id.llClasses);
        }
    }
}
