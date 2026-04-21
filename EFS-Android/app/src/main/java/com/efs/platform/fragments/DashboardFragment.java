package com.efs.platform.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.efs.platform.R;
import com.efs.platform.api.ApiClient;
import com.efs.platform.api.ApiService;
import com.efs.platform.models.Models;
import com.efs.platform.utils.SessionManager;
import com.efs.platform.adapters.QuickActionAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {
    
    private TextView tvSid, tvCredits, tvMajor, tvCourses, tvGroups, tvSurveys, tvMaterials;
    private CardView cardAdminAlert;
    private TextView tvPendingApprovals;
    private RecyclerView rvQuickActions;
    private SessionManager sessionManager;
    private ApiService apiService;
    private Models.User user;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        sessionManager = new SessionManager(getContext());
        apiService = ApiClient.getClient().create(ApiService.class);
        user = sessionManager.getUser();
        
        initViews(view);
        loadDashboardData();
        
        return view;
    }
    
    private void initViews(View view) {
        tvSid = view.findViewById(R.id.tvSid);
        tvCredits = view.findViewById(R.id.tvCredits);
        tvMajor = view.findViewById(R.id.tvMajor);
        tvCourses = view.findViewById(R.id.tvCourses);
        tvGroups = view.findViewById(R.id.tvGroups);
        tvSurveys = view.findViewById(R.id.tvSurveys);
        tvMaterials = view.findViewById(R.id.tvMaterials);
        cardAdminAlert = view.findViewById(R.id.cardAdminAlert);
        tvPendingApprovals = view.findViewById(R.id.tvPendingApprovals);
        rvQuickActions = view.findViewById(R.id.rvQuickActions);
        
        if (user != null) {
            tvSid.setText(user.sid);
            tvCredits.setText(String.valueOf(user.credits));
            tvMajor.setText(user.major != null ? user.major : "Student");
        }
        
        setupQuickActions();
    }
    
    private void setupQuickActions() {
        QuickActionAdapter adapter = new QuickActionAdapter(getContext(), user);
        rvQuickActions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvQuickActions.setAdapter(adapter);
    }
    
    private void loadDashboardData() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        apiService.getDashboardSummary(token).enqueue(new Callback<Models.DashboardData>() {
            @Override
            public void onResponse(Call<Models.DashboardData> call, Response<Models.DashboardData> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                }
            }
            
            @Override
            public void onFailure(Call<Models.DashboardData> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load dashboard", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI(Models.DashboardData data) {
        if (data.stats != null) {
            tvCourses.setText(String.valueOf(data.stats.courses));
            tvGroups.setText(String.valueOf(data.stats.myGroupRequests));
            tvSurveys.setText(String.valueOf(data.stats.myQuestionnaires));
            tvMaterials.setText(String.valueOf(data.stats.myMaterials));
            
            if ("admin".equals(user.role) && data.stats.pendingApprovals > 0) {
                cardAdminAlert.setVisibility(View.VISIBLE);
                tvPendingApprovals.setText(data.stats.pendingApprovals + " pending approval(s)");
                cardAdminAlert.setOnClickListener(v -> {
                    // Navigate to admin panel
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).onNavigationItemSelected(
                            ((MainActivity) getActivity()).findViewById(R.id.nav_admin));
                    }
                });
            } else {
                cardAdminAlert.setVisibility(View.GONE);
            }
        }
    }
}
