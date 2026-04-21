package com.efs.platform;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import com.efs.platform.api.ApiClient;
import com.efs.platform.api.ApiService;
import com.efs.platform.models.Models;
import com.efs.platform.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.util.Base64;

public class LoginActivity extends AppCompatActivity {
    
    private TextInputEditText etEmail, etPassword, etSid, etConfirmPassword;
    private TextInputLayout tilSid, tilConfirmPassword;
    private MaterialButton btnSubmit, btnToggleMode;
    private ProgressBar progressBar;
    private TextView tvPhotoStatus;
    private Button btnUploadPhoto;
    
    private ApiService apiService;
    private SessionManager sessionManager;
    private boolean isRegisterMode = false;
    private String photoBase64 = null;
    private String photoFileName = null;
    
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST = 2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);
        
        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }
        
        initViews();
        setupListeners();
    }
    
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etSid = findViewById(R.id.etSid);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilSid = findViewById(R.id.tilSid);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnToggleMode = findViewById(R.id.btnToggleMode);
        progressBar = findViewById(R.id.progressBar);
        tvPhotoStatus = findViewById(R.id.tvPhotoStatus);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
    }
    
    private void setupListeners() {
        btnToggleMode.setOnClickListener(v -> {
            isRegisterMode = !isRegisterMode;
            updateUIMode();
        });
        
        btnUploadPhoto.setOnClickListener(v -> {
            if (checkPermissions()) {
                openFileChooser();
            }
        });
        
        btnSubmit.setOnClickListener(v -> {
            if (isRegisterMode) {
                attemptRegister();
            } else {
                attemptLogin();
            }
        });
    }
    
    private void updateUIMode() {
        if (isRegisterMode) {
            tilSid.setVisibility(android.view.View.VISIBLE);
            tilConfirmPassword.setVisibility(android.view.View.VISIBLE);
            btnUploadPhoto.setVisibility(android.view.View.VISIBLE);
            btnSubmit.setText("Register");
            btnToggleMode.setText("Already have an account? Login");
        } else {
            tilSid.setVisibility(android.view.View.GONE);
            tilConfirmPassword.setVisibility(android.view.View.GONE);
            btnUploadPhoto.setVisibility(android.view.View.GONE);
            tvPhotoStatus.setVisibility(android.view.View.GONE);
            btnSubmit.setText("Login");
            btnToggleMode.setText("Need an account? Register");
        }
    }
    
    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST);
            return false;
        }
        return true;
    }
    
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Student Card"), PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                byte[] imageBytes = baos.toByteArray();
                photoBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                photoFileName = "student_card.jpg";
                tvPhotoStatus.setText("Photo selected: " + photoFileName);
                tvPhotoStatus.setVisibility(android.view.View.VISIBLE);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setLoading(true);
        
        Models.LoginRequest request = new Models.LoginRequest(email, password);
        Call<Models.LoginResponse> call = apiService.login(request);
        call.enqueue(new Callback<Models.LoginResponse>() {
            @Override
            public void onResponse(Call<Models.LoginResponse> call, Response<Models.LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    sessionManager.saveAuthToken(response.body().token);
                    sessionManager.saveUser(response.body().user);
                    navigateToMain();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Models.LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void attemptRegister() {
        String sid = etSid.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        if (sid.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (photoBase64 == null) {
            Toast.makeText(this, "Please upload your student card", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setLoading(true);
        
        Models.RegisterRequest request = new Models.RegisterRequest();
        request.sid = sid;
        request.email = email;
        request.password = password;
        request.photoData = photoBase64;
        request.fileName = photoFileName;
        
        Call<Models.ApiResponse> call = apiService.register(request);
        call.enqueue(new Callback<Models.ApiResponse>() {
            @Override
            public void onResponse(Call<Models.ApiResponse> call, Response<Models.ApiResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(LoginActivity.this, 
                        "Registration submitted! Please wait for admin approval.", 
                        Toast.LENGTH_LONG).show();
                    isRegisterMode = false;
                    updateUIMode();
                    clearForm();
                } else {
                    String error = response.body() != null ? response.body().error : "Registration failed";
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Models.ApiResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void clearForm() {
        etEmail.setText("");
        etPassword.setText("");
        etSid.setText("");
        etConfirmPassword.setText("");
        photoBase64 = null;
        photoFileName = null;
        tvPhotoStatus.setVisibility(android.view.View.GONE);
    }
    
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        btnSubmit.setEnabled(!loading);
        btnToggleMode.setEnabled(!loading);
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
