package com.efs.platform.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.efs.platform.MainActivity;
import com.efs.platform.R;
import com.efs.platform.models.Models;
import java.util.ArrayList;
import java.util.List;

public class QuickActionAdapter extends RecyclerView.Adapter<QuickActionAdapter.ViewHolder> {
    
    private Context context;
    private List<QuickAction> actions;
    
    public QuickActionAdapter(Context context, Models.User user) {
        this.context = context;
        this.actions = new ArrayList<>();
        
        actions.add(new QuickAction(R.drawable.ic_calendar, "Timetable", "calendar"));
        actions.add(new QuickAction(R.drawable.ic_group, "Group", "group"));
        actions.add(new QuickAction(R.drawable.ic_questionnaire, "Surveys", "survey"));
        actions.add(new QuickAction(R.drawable.ic_materials, "Materials", "materials"));
        
        if (user != null && "admin".equals(user.role)) {
            actions.add(new QuickAction(R.drawable.ic_admin, "Admin", "admin"));
        }
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quick_action, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        QuickAction action = actions.get(position);
        holder.icon.setImageResource(action.iconRes);
        holder.title.setText(action.title);
        
        holder.card.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                int menuId = getMenuId(action.action);
                if (menuId != -1) {
                    ((MainActivity) context).onNavigationItemSelected(
                        ((MainActivity) context).findViewById(menuId));
                }
            }
        });
    }
    
    private int getMenuId(String action) {
        switch (action) {
            case "calendar": return R.id.nav_calendar;
            case "group": return R.id.nav_group;
            case "survey": return R.id.nav_questionnaire;
            case "materials": return R.id.nav_materials;
            case "admin": return R.id.nav_admin;
            default: return -1;
        }
    }
    
    @Override
    public int getItemCount() {
        return actions.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        ImageView icon;
        TextView title;
        
        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardAction);
            icon = itemView.findViewById(R.id.ivIcon);
            title = itemView.findViewById(R.id.tvTitle);
        }
    }
    
    static class QuickAction {
        int iconRes;
        String title;
        String action;
        
        QuickAction(int iconRes, String title, String action) {
            this.iconRes = iconRes;
            this.title = title;
            this.action = action;
        }
    }
}
