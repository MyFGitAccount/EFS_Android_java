package com.efs.platform.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.efs.platform.R;
import com.efs.platform.models.Models;
import java.util.List;

public class QuestionnaireAdapter extends RecyclerView.Adapter<QuestionnaireAdapter.ViewHolder> {
    
    private List<Models.Questionnaire> questionnaires;
    private OnFillClickListener fillListener;
    private boolean isMyQuestionnaire;
    
    public interface OnFillClickListener {
        void onFillClick(String questionnaireId);
    }
    
    public QuestionnaireAdapter(List<Models.Questionnaire> questionnaires, OnFillClickListener listener, boolean isMy) {
        this.questionnaires = questionnaires;
        this.fillListener = listener;
        this.isMyQuestionnaire = isMy;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_questionnaire, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.Questionnaire q = questionnaires.get(position);
        
        holder.tvDescription.setText(q.description);
        holder.tvCreator.setText("By: " + q.creatorSid);
        
        int percent = q.targetResponses > 0 
            ? (int) ((double) q.currentResponses / q.targetResponses * 100) 
            : 0;
        holder.progressBar.setProgress(percent);
        holder.tvProgress.setText(q.currentResponses + " / " + q.targetResponses);
        
        if ("completed".equals(q.status)) {
            holder.tvStatus.setText("Completed");
            holder.tvStatus.setBackgroundResource(R.color.green);
            holder.btnFill.setEnabled(false);
        } else {
            holder.tvStatus.setText("Active");
            holder.tvStatus.setBackgroundResource(R.color.blue);
            holder.btnFill.setEnabled(true);
        }
        
        if (isMyQuestionnaire) {
            holder.btnFill.setVisibility(View.GONE);
            holder.tvFilledBy.setText("Filled by: " + (q.filledBy != null ? q.filledBy.size() : 0) + " students");
        } else {
            holder.btnFill.setVisibility(View.VISIBLE);
            holder.tvFilledBy.setVisibility(View.GONE);
            holder.btnFill.setOnClickListener(v -> {
                if (fillListener != null) fillListener.onFillClick(q.id);
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return questionnaires != null ? questionnaires.size() : 0;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvCreator, tvProgress, tvStatus, tvFilledBy;
        ProgressBar progressBar;
        Button btnFill;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCreator = itemView.findViewById(R.id.tvCreator);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvFilledBy = itemView.findViewById(R.id.tvFilledBy);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnFill = itemView.findViewById(R.id.btnFill);
        }
    }
}
