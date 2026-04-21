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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MaterialsAdapter extends RecyclerView.Adapter<MaterialsAdapter.ViewHolder> {
    
    private List<Models.Material> materials;
    private OnDownloadClickListener downloadListener;
    
    public interface OnDownloadClickListener {
        void onDownloadClick(String materialId, String fileName);
    }
    
    public MaterialsAdapter(List<Models.Material> materials, OnDownloadClickListener listener) {
        this.materials = materials;
        this.downloadListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_material, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Models.Material material = materials.get(position);
        
        holder.tvName.setText(material.name);
        holder.tvDescription.setText(material.description != null ? material.description : "No description");
        holder.tvCourse.setText(material.courseCode);
        
        if (material.size > 0) {
            String sizeStr = material.size / 1024 / 1024 > 0 
                ? String.format(Locale.getDefault(), "%.2f MB", material.size / 1024.0 / 1024.0)
                : String.format(Locale.getDefault(), "%.2f KB", material.size / 1024.0);
            holder.tvSize.setText(sizeStr);
        } else {
            holder.tvSize.setText("");
        }
        
        holder.btnDownload.setOnClickListener(v -> {
            if (downloadListener != null) {
                downloadListener.onDownloadClick(material.id, material.name);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return materials != null ? materials.size() : 0;
    }
    
    public void updateList(List<Models.Material> newList) {
        this.materials = newList;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvCourse, tvSize;
        Button btnDownload;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCourse = itemView.findViewById(R.id.tvCourse);
            tvSize = itemView.findViewById(R.id.tvSize);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}
