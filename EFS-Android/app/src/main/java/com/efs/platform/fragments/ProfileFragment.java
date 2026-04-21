package com.efs.platform.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.efs.platform.R;
import com.efs.platform.api.ApiClient;
import com.efs.platform.api.ApiService;
import com.efs.platform.models.Models;
import com.efs.platform.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    
    private TextView tvSid, tvCredits, tvRole;
    private TextInputEditText etEmail, etPhone, etMajor, etYearOfStudy, etGpa, etAboutMe;
    private MaterialButton btnEdit, btnSave;
    private ProgressBar progressBar;
    
    private SessionManager sessionManager;
    private ApiService apiService;
    private Models.UserProfile profile;
    private boolean isEditing = false;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        sessionManager = new SessionManager(getContext());
        apiService = ApiClient.getClient().create(ApiService.class);
        
        initViews(view);
        loadProfile();
        setupListeners();
        
        return view;
    }
    
    private void initViews(View view) {
        tvSid = view.findViewById(R.id.tvSid);
        tvCredits = view.findViewById(R.id.tvCredits);
        tvRole = view.findViewById(R.id.tvRole);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etMajor = view.findViewById(R.id.etMajor);
        etYearOfStudy = view.findViewById(R.id.etYearOfStudy);
        etGpa = view.findViewById(R.id.etGpa);
        etAboutMe = view.findViewById(R.id.etAboutMe);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnSave = view.findViewById(R.id.btnSave);
        progressBar = view.findViewById(R.id.progressBar);
        
        setEditingEnabled(false);
    }
    
    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            isEditing = true;
            setEditingEnabled(true);
        });
        
        btnSave.setOnClickListener(v -> updateProfile());
    }
    
    private void setEditingEnabled(boolean enabled) {
        etEmail.setEnabled(enabled);
        etPhone.setEnabled(enabled);
        etMajor.setEnabled(enabled);
        etYearOfStudy.setEnabled(enabled);
        etGpa.setEnabled(enabled);
        etAboutMe.setEnabled(enabled);
        
        btnEdit.setVisibility(enabled ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }
    
    private void loadProfile() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.getProfile(token).enqueue(new Callback<Models.UserProfile>() {
            @Override
            public void onResponse(Call<Models.UserProfile> call, Response<Models.UserProfile> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    profile = response.body();
                    displayProfile();
                } else {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Models.UserProfile> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayProfile() {
        if (profile == null) return;
        
        tvSid.setText(profile.sid);
        tvCredits.setText(String.valueOf(profile.credits));
        tvRole.setText(profile.role != null ? profile.role.toUpperCase() : "STUDENT");
        etEmail.setText(profile.email);
        etPhone.setText(profile.phone);
        etMajor.setText(profile.major);
        etYearOfStudy.setText(String.valueOf(profile.yearOfStudy));
        etGpa.setText(profile.gpa != null ? String.valueOf(profile.gpa) : "");
        etAboutMe.setText(profile.about_me);
    }
    
    private void updateProfile() {
        if (profile == null) return;
        
        profile.email = etEmail.getText().toString();
        profile.phone = etPhone.getText().toString();
        profile.major = etMajor.getText().toString();
        profile.yearOfStudy = Integer.parseInt(etYearOfStudy.getText().toString());
        if (!etGpa.getText().toString().isEmpty()) {
            profile.gpa = Double.parseDouble(etGpa.getText().toString());
        }
        profile.about_me = etAboutMe.getText().toString();
        
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.updateProfile(token, profile).enqueue(new Callback<Models.ApiResponse>() {
            @Override
            public void onResponse(Call<Models.ApiResponse> call, Response<Models.ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    isEditing = false;
                    setEditingEnabled(false);
                    
                    // Update session user data
                    Models.User user = sessionManager.getUser();
                    if (user != null) {
                        user.email = profile.email;
                        user.major = profile.major;
                        user.yearOfStudy = profile.yearOfStudy;
                        sessionManager.saveUser(user);
                    }
                } else {
                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Models.ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
