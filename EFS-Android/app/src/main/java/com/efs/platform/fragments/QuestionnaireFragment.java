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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.efs.platform.R;
import com.efs.platform.api.ApiClient;
import com.efs.platform.api.ApiService;
import com.efs.platform.adapters.QuestionnaireAdapter;
import com.efs.platform.models.Models;
import com.efs.platform.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionnaireFragment extends Fragment {
    
    private RecyclerView rvAvailable, rvMyQuestionnaires;
    private MaterialButton btnCreate;
    private ProgressBar progressBar;
    private TextView tvEmptyAvailable, tvEmptyMy;
    
    private SessionManager sessionManager;
    private ApiService apiService;
    private QuestionnaireAdapter availableAdapter, myAdapter;
    private List<Models.Questionnaire> availableList = new ArrayList<>();
    private List<Models.Questionnaire> myList = new ArrayList<>();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questionnaire, container, false);
        
        sessionManager = new SessionManager(getContext());
        apiService = ApiClient.getClient().create(ApiService.class);
        
        initViews(view);
        loadData();
        setupListeners();
        
        return view;
    }
    
    private void initViews(View view) {
        rvAvailable = view.findViewById(R.id.rvAvailable);
        rvMyQuestionnaires = view.findViewById(R.id.rvMyQuestionnaires);
        btnCreate = view.findViewById(R.id.btnCreate);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyAvailable = view.findViewById(R.id.tvEmptyAvailable);
        tvEmptyMy = view.findViewById(R.id.tvEmptyMy);
        
        rvAvailable.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMyQuestionnaires.setLayoutManager(new LinearLayoutManager(getContext()));
        
        availableAdapter = new QuestionnaireAdapter(getContext(), availableList, this::fillQuestionnaire, false);
        myAdapter = new QuestionnaireAdapter(getContext(), myList, null, true);
        
        rvAvailable.setAdapter(availableAdapter);
        rvMyQuestionnaires.setAdapter(myAdapter);
    }
    
    private void setupListeners() {
        btnCreate.setOnClickListener(v -> showCreateDialog());
    }
    
    private void loadData() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.getAllQuestionnaires(token).enqueue(new Callback<List<Models.Questionnaire>>() {
            @Override
            public void onResponse(Call<List<Models.Questionnaire>> call, Response<List<Models.Questionnaire>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableList.clear();
                    Models.User user = sessionManager.getUser();
                    for (Models.Questionnaire q : response.body()) {
                        if (!q.creatorSid.equals(user.sid) && !"completed".equals(q.status)) {
                            availableList.add(q);
                        }
                    }
                    availableAdapter.notifyDataSetChanged();
                    tvEmptyAvailable.setVisibility(availableList.isEmpty() ? View.VISIBLE : View.GONE);
                }
                loadMyQuestionnaires();
            }
            
            @Override
            public void onFailure(Call<List<Models.Questionnaire>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadMyQuestionnaires() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        apiService.getMyQuestionnaires(token).enqueue(new Callback<List<Models.Questionnaire>>() {
            @Override
            public void onResponse(Call<List<Models.Questionnaire>> call, Response<List<Models.Questionnaire>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    myList.clear();
                    myList.addAll(response.body());
                    myAdapter.notifyDataSetChanged();
                    tvEmptyMy.setVisibility(myList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            
            @Override
            public void onFailure(Call<List<Models.Questionnaire>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    
    private void showCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_questionnaire, null);
        
        EditText etDescription = view.findViewById(R.id.etDescription);
        EditText etLink = view.findViewById(R.id.etLink);
        EditText etTargetResponses = view.findViewById(R.id.etTargetResponses);
        TextView tvCreditInfo = view.findViewById(R.id.tvCreditInfo);
        
        Models.User user = sessionManager.getUser();
        tvCreditInfo.setText("Creating costs 1 credit. You have " + (user != null ? user.credits : 0) + " credits");
        
        builder.setTitle("Create Questionnaire")
            .setView(view)
            .setPositiveButton("Create", (dialog, which) -> {
                Models.Questionnaire q = new Models.Questionnaire();
                q.description = etDescription.getText().toString();
                q.link = etLink.getText().toString();
                q.targetResponses = Integer.parseInt(etTargetResponses.getText().toString());
                createQuestionnaire(q);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void createQuestionnaire(Models.Questionnaire q) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.createQuestionnaire(token, q).enqueue(new Callback<Models.ApiResponse>() {
            @Override
            public void onResponse(Call<Models.ApiResponse> call, Response<Models.ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(getContext(), "Questionnaire created! 1 credit deducted.", Toast.LENGTH_SHORT).show();
                    loadData();
                } else {
                    String error = response.body() != null ? response.body().error : "Creation failed";
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Models.ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void fillQuestionnaire(String questionnaireId) {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.fillQuestionnaire(token, questionnaireId).enqueue(new Callback<Models.ApiResponse>() {
            @Override
            public void onResponse(Call<Models.ApiResponse> call, Response<Models.ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(getContext(), "Questionnaire filled! You earned 1 credit!", Toast.LENGTH_SHORT).show();
                    loadData();
                } else {
                    Toast.makeText(getContext(), "Failed to fill", Toast.LENGTH_SHORT).show();
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
