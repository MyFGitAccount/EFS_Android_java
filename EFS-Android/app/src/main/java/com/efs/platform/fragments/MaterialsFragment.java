package com.efs.platform.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.efs.platform.R;
import com.efs.platform.api.ApiClient;
import com.efs.platform.api.ApiService;
import com.efs.platform.adapters.MaterialsAdapter;
import com.efs.platform.models.Models;
import com.efs.platform.utils.SessionManager;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MaterialsFragment extends Fragment {
    
    private RecyclerView rvMaterials;
    private EditText etSearch;
    private Button btnSearch, btnUpload;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    
    private SessionManager sessionManager;
    private ApiService apiService;
    private MaterialsAdapter adapter;
    private List<Models.Material> materials = new ArrayList<>();
    private List<Models.Material> filteredMaterials = new ArrayList<>();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_materials, container, false);
        
        sessionManager = new SessionManager(getContext());
        apiService = ApiClient.getClient().create(ApiService.class);
        
        initViews(view);
        loadMaterials();
        setupListeners();
        
        return view;
    }
    
    private void initViews(View view) {
        rvMaterials = view.findViewById(R.id.rvMaterials);
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnUpload = view.findViewById(R.id.btnUpload);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        
        rvMaterials.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MaterialsAdapter(getContext(), filteredMaterials, this::downloadMaterial);
        rvMaterials.setAdapter(adapter);
        
        // Show upload button only for admin
        Models.User user = sessionManager.getUser();
        if (user == null || !"admin".equals(user.role)) {
            btnUpload.setVisibility(View.GONE);
        }
    }
    
    private void setupListeners() {
        btnSearch.setOnClickListener(v -> searchMaterials());
        btnUpload.setOnClickListener(v -> showUploadDialog());
    }
    
    private void loadMaterials() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.getAllMaterials(token).enqueue(new Callback<List<Models.Material>>() {
            @Override
            public void onResponse(Call<List<Models.Material>> call, Response<List<Models.Material>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    materials.clear();
                    materials.addAll(response.body());
                    filteredMaterials.clear();
                    filteredMaterials.addAll(materials);
                    adapter.notifyDataSetChanged();
                    
                    if (filteredMaterials.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvMaterials.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvMaterials.setVisibility(View.VISIBLE);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<List<Models.Material>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load materials", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void searchMaterials() {
        String query = etSearch.getText().toString().trim().toLowerCase();
        filteredMaterials.clear();
        
        if (query.isEmpty()) {
            filteredMaterials.addAll(materials);
        } else {
            for (Models.Material material : materials) {
                if (material.name.toLowerCase().contains(query) ||
                    (material.description != null && material.description.toLowerCase().contains(query)) ||
                    material.courseCode.toLowerCase().contains(query)) {
                    filteredMaterials.add(material);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        
        if (filteredMaterials.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvMaterials.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvMaterials.setVisibility(View.VISIBLE);
        }
    }
    
    private void showUploadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_upload_material, null);
        
        EditText etCourseCode = view.findViewById(R.id.etCourseCode);
        EditText etName = view.findViewById(R.id.etName);
        EditText etDescription = view.findViewById(R.id.etDescription);
        Button btnSelectFile = view.findViewById(R.id.btnSelectFile);
        TextView tvFileName = view.findViewById(R.id.tvFileName);
        
        final String[] selectedFilePath = {null};
        final String[] selectedFileName = {null};
        
        btnSelectFile.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, 200);
        });
        
        builder.setTitle("Upload Material")
            .setView(view)
            .setPositiveButton("Upload", (dialog, which) -> {
                if (selectedFilePath[0] == null) {
                    Toast.makeText(getContext(), "Please select a file", Toast.LENGTH_SHORT).show();
                    return;
                }
                uploadMaterial(etCourseCode.getText().toString(), 
                             etName.getText().toString(), 
                             etDescription.getText().toString(),
                             selectedFilePath[0],
                             selectedFileName[0]);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            // Handle file selection
            Toast.makeText(getContext(), "File selected", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void uploadMaterial(String courseCode, String name, String description, String filePath, String fileName) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        RequestBody courseCodeBody = RequestBody.create(MultipartBody.FORM, courseCode);
        RequestBody nameBody = RequestBody.create(MultipartBody.FORM, name);
        RequestBody descriptionBody = RequestBody.create(MultipartBody.FORM, description);
        
        // For actual file upload, you'd need to create a MultipartBody.Part
        // This is a simplified version
        Toast.makeText(getContext(), "Upload feature requires file handling implementation", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
    }
    
    private void downloadMaterial(String materialId, String fileName) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.downloadMaterial(token, materialId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    saveFileToDisk(response.body(), fileName);
                } else {
                    Toast.makeText(getContext(), "Download failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void saveFileToDisk(ResponseBody body, String fileName) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);
            
            InputStream inputStream = body.byteStream();
            FileOutputStream outputStream = new FileOutputStream(file);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.close();
            inputStream.close();
            
            Toast.makeText(getContext(), "Downloaded to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Save failed", Toast.LENGTH_SHORT).show();
        }
    }
}
