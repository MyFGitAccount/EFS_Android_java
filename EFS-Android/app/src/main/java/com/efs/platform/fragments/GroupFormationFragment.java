package com.efs.platform.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.efs.platform.R;
import com.efs.platform.api.ApiClient;
import com.efs.platform.api.ApiService;
import com.efs.platform.adapters.GroupRequestAdapter;
import com.efs.platform.models.Models;
import com.efs.platform.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupFormationFragment extends Fragment {
    
    private RecyclerView rvRequests;
    private MaterialButton btnCreateRequest;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    
    private SessionManager sessionManager;
    private ApiService apiService;
    private GroupRequestAdapter adapter;
    private List<Models.GroupRequest> requests = new ArrayList<>();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_formation, container, false);
        
        sessionManager = new SessionManager(getContext());
        apiService = ApiClient.getClient().create(ApiService.class);
        
        initViews(view);
        loadRequests();
        setupListeners();
        
        return view;
    }
    
    private void initViews(View view) {
        rvRequests = view.findViewById(R.id.rvRequests);
        btnCreateRequest = view.findViewById(R.id.btnCreateRequest);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroupRequestAdapter(getContext(), requests, this::sendInvitation, this::deleteRequest);
        rvRequests.setAdapter(adapter);
    }
    
    private void setupListeners() {
        btnCreateRequest.setOnClickListener(v -> showCreateRequestDialog());
    }
    
    private void loadRequests() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.getGroupRequests(token).enqueue(new Callback<List<Models.GroupRequest>>() {
            @Override
            public void onResponse(Call<List<Models.GroupRequest>> call, Response<List<Models.GroupRequest>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    requests.clear();
                    requests.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    if (requests.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvRequests.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvRequests.setVisibility(View.VISIBLE);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<List<Models.GroupRequest>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load requests", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showCreateRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_group_request, null);
        
        TextInputEditText etMajor = view.findViewById(R.id.etMajor);
        TextInputEditText etDescription = view.findViewById(R.id.etDescription);
        TextInputEditText etDesiredGroupmates = view.findViewById(R.id.etDesiredGroupmates);
        TextInputEditText etEmail = view.findViewById(R.id.etEmail);
        TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        TextInputEditText etGpa = view.findViewById(R.id.etGpa);
        
        builder.setTitle("Create Group Request")
            .setView(view)
            .setPositiveButton("Create", (dialog, which) -> {
                Models.User user = sessionManager.getUser();
                Models.GroupRequest request = new Models.GroupRequest();
                request.major = etMajor.getText().toString();
                request.description = etDescription.getText().toString();
                request.desired_groupmates = etDesiredGroupmates.getText().toString();
                request.email = etEmail.getText().toString();
                request.phone = etPhone.getText().toString();
                if (!etGpa.getText().toString().isEmpty()) {
                    request.gpa = Double.parseDouble(etGpa.getText().toString());
                }
                
                createGroupRequest(request);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void createGroupRequest(Models.GroupRequest request) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.createGroupRequest(token, request).enqueue(new Callback<Models.ApiResponse>() {
            @Override
            public void onResponse(Call<Models.ApiResponse> call, Response<Models.ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(getContext(), "Request created", Toast.LENGTH_SHORT).show();
                    loadRequests();
                } else {
                    Toast.makeText(getContext(), "Failed to create request", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Models.ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void sendInvitation(Models.GroupRequest request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_send_invitation, null);
        
        EditText etMessage = view.findViewById(R.id.etMessage);
        etMessage.setText("Hi! I would like to form a study group with you. Let's coordinate our schedules!");
        
        builder.setTitle("Invite " + request.sid)
            .setView(view)
            .setPositiveButton("Send", (dialog, which) -> {
                Models.InviteRequest invite = new Models.InviteRequest();
                invite.message = etMessage.getText().toString();
                sendInvitationRequest(request.id, invite);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void sendInvitationRequest(String requestId, Models.InviteRequest invite) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.sendInvitation(token, requestId, invite).enqueue(new Callback<Models.ApiResponse>() {
            @Override
            public void onResponse(Call<Models.ApiResponse> call, Response<Models.ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(getContext(), "Invitation sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to send", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Models.ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void deleteRequest(String requestId) {
        new AlertDialog.Builder(getContext())
            .setTitle("Delete Request")
            .setMessage("Are you sure you want to delete this request?")
            .setPositiveButton("Delete", (dialog, which) -> {
                String token = sessionManager.getAuthHeader();
                if (token == null) return;
                
                progressBar.setVisibility(View.VISIBLE);
                
                apiService.deleteGroupRequest(token, requestId).enqueue(new Callback<Models.ApiResponse>() {
                    @Override
                    public void onResponse(Call<Models.ApiResponse> call, Response<Models.ApiResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Request deleted", Toast.LENGTH_SHORT).show();
                            loadRequests();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Models.ApiResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
