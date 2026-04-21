package com.efs.platform.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.efs.platform.R;
import com.efs.platform.api.ApiClient;
import com.efs.platform.api.ApiService;
import com.efs.platform.adapters.PendingAccountsAdapter;
import com.efs.platform.models.Models;
import com.efs.platform.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPanelFragment extends Fragment {
    
    private TabLayout tabLayout;
    private RecyclerView rvPendingAccounts, rvUsers;
    private ProgressBar progressBar;
    private TextView tvTotalUsers, tvTotalCourses, tvTotalMaterials, tvPendingAccounts;
    
    private SessionManager sessionManager;
    private ApiService apiService;
    private PendingAccountsAdapter pendingAdapter;
    private UsersAdapter usersAdapter;
    private List<Models.PendingAccount> pendingAccounts = new ArrayList<>();
    private List<Models.User> users = new ArrayList<>();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_panel, container, false);
        
        sessionManager = new SessionManager(getContext());
        apiService = ApiClient.getClient().create(ApiService.class);
        
        initViews(view);
        loadData();
        setupListeners();
        
        return view;
    }
    
    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        rvPendingAccounts = view.findViewById(R.id.rvPendingAccounts);
        rvUsers = view.findViewById(R.id.rvUsers);
        progressBar = view.findViewById(R.id.progressBar);
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        tvTotalCourses = view.findViewById(R.id.tvTotalCourses);
        tvTotalMaterials = view.findViewById(R.id.tvTotalMaterials);
        tvPendingAccounts = view.findViewById(R.id.tvPendingAccounts);
        
        rvPendingAccounts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        
        pendingAdapter = new PendingAccountsAdapter(getContext(), pendingAccounts, 
            this::approveAccount, this::rejectAccount, this::viewStudentCard);
        usersAdapter = new UsersAdapter(getContext(), users, this::deleteUser);
        
        rvPendingAccounts.setAdapter(pendingAdapter);
        rvUsers.setAdapter(usersAdapter);
    }
    
    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvPendingAccounts.setVisibility(View.VISIBLE);
                    rvUsers.setVisibility(View.GONE);
                } else {
                    rvPendingAccounts.setVisibility(View.GONE);
                    rvUsers.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void loadData() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        // Load stats
        apiService.getAdminStats(token).enqueue(new Callback<Models.AdminStats>() {
            @Override
            public void onResponse(Call<Models.AdminStats> call, Response<Models.AdminStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvTotalUsers.setText(String.valueOf(response.body().totalUsers));
                    tvTotalCourses.setText(String.valueOf(response.body().totalCourses));
                    tvTotalMaterials.setText(String.valueOf(response.body().totalMaterials));
                    tvPendingAccounts.setText(String.valueOf(response.body().pendingAccounts));
                }
            }
            
            @Override
            public void onFailure(Call<Models.AdminStats> call, Throwable t) {}
        });
        
        // Load pending accounts
        apiService.getPendingAccounts(token).enqueue(new Callback<List<Models.PendingAccount>>() {
            @Override
            public void onResponse(Call<List<Models.PendingAccount>> call, Response<List<Models.PendingAccount>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pendingAccounts.clear();
                    pendingAccounts.addAll(response.body());
                    pendingAdapter.notifyDataSetChanged();
                }
                loadUsers();
            }
            
            @Override
            public void onFailure(Call<List<Models.PendingAccount>> call, Throwable t) {
                loadUsers();
            }
        });
    }
    
    private void loadUsers() {
        // For users list, you'd need an endpoint - this is a placeholder
        progressBar.setVisibility(View.GONE);
    }
    
    private void approveAccount(String sid) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.approveAccount(token, sid).enqueue(new Callback<Models.ApiResponse>() {
            @Override
            public void onResponse(Call<Models.ApiResponse> call, Response<Models.ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(getContext(), "Account approved", Toast.LENGTH_SHORT).show();
                    loadData();
                } else {
                    Toast.makeText(getContext(), "Approval failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Models.ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void rejectAccount(String sid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reject_reason, null);
        EditText etReason = view.findViewById(R.id.etReason);
        
        builder.setTitle("Reject Account")
            .setView(view)
            .setPositiveButton("Reject", (dialog, which) -> {
                String reason = etReason.getText().toString();
                Models.RejectRequest request = new Models.RejectRequest(reason);
                
                String token = sessionManager.getAuthHeader();
                if (token == null) return;
                
                progressBar.setVisibility(View.VISIBLE);
                
                apiService.rejectAccount(token, sid, request).enqueue(new Callback<Models.ApiResponse>() {
                    @Override
                    public void onResponse(Call<Models.ApiResponse> call, Response<Models.ApiResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Account rejected", Toast.LENGTH_SHORT).show();
                            loadData();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Models.ApiResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void viewStudentCard(Models.PendingAccount account) {
        // Show image dialog with student card photo
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_student_card, null);
        ImageView ivStudentCard = view.findViewById(R.id.ivStudentCard);
        TextView tvSid = view.findViewById(R.id.tvSid);
        TextView tvEmail = view.findViewById(R.id.tvEmail);
        
        tvSid.setText(account.sid);
        tvEmail.setText(account.email);
        
        // Load image from server
        String imageUrl = ApiClient.BASE_URL + "uploads/" + account.photoFileId;
        // Use Glide or Picasso to load image
        
        builder.setView(view)
            .setPositiveButton("Close", null)
            .show();
    }
    
    private void deleteUser(String sid) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete this user?")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Implement delete user API call
                Toast.makeText(getContext(), "Delete user: " + sid, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
