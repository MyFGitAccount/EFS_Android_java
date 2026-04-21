package com.efs.platform.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.efs.platform.adapters.CourseSearchAdapter;
import com.efs.platform.adapters.TimetableAdapter;
import com.efs.platform.models.Models;
import com.efs.platform.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarFragment extends Fragment {
    
    private EditText etSearch;
    private Button btnSearch;
    private RecyclerView rvSearchResults, rvTimetable;
    private ProgressBar progressBar;
    private LinearLayout emptyTimetableView;
    private ChipGroup chipGroupFilters;
    private TextView tvSessionCount;
    private MaterialButton btnSave, btnClear, btnExport, btnImport;
    
    private SessionManager sessionManager;
    private ApiService apiService;
    private CourseSearchAdapter searchAdapter;
    private TimetableAdapter timetableAdapter;
    private List<SavedSession> selectedSessions = new ArrayList<>();
    private List<Models.Course> allCourses = new ArrayList<>();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        
        sessionManager = new SessionManager(getContext());
        apiService = ApiClient.getClient().create(ApiService.class);
        
        initViews(view);
        loadSavedTimetable();
        loadCourses();
        setupListeners();
        
        return view;
    }
    
    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        rvTimetable = view.findViewById(R.id.rvTimetable);
        progressBar = view.findViewById(R.id.progressBar);
        emptyTimetableView = view.findViewById(R.id.emptyTimetableView);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        tvSessionCount = view.findViewById(R.id.tvSessionCount);
        btnSave = view.findViewById(R.id.btnSave);
        btnClear = view.findViewById(R.id.btnClear);
        btnExport = view.findViewById(R.id.btnExport);
        btnImport = view.findViewById(R.id.btnImport);
        
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTimetable.setLayoutManager(new LinearLayoutManager(getContext()));
        
        searchAdapter = new CourseSearchAdapter(getContext(), this::addClassToTimetable);
        rvSearchResults.setAdapter(searchAdapter);
        
        timetableAdapter = new TimetableAdapter(getContext(), this::removeClassFromTimetable);
        rvTimetable.setAdapter(timetableAdapter);
    }
    
    private void setupListeners() {
        btnSearch.setOnClickListener(v -> searchCourses());
        btnSave.setOnClickListener(v -> saveTimetable());
        btnClear.setOnClickListener(v -> clearTimetable());
        btnExport.setOnClickListener(v -> exportTimetable());
        btnImport.setOnClickListener(v -> importTimetable());
    }
    
    private void loadCourses() {
        String token = sessionManager.getAuthHeader();
        if (token == null) return;
        
        apiService.getAllCourses(token).enqueue(new Callback<List<Models.Course>>() {
            @Override
            public void onResponse(Call<List<Models.Course>> call, Response<List<Models.Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCourses = response.body();
                }
            }
            
            @Override
            public void onFailure(Call<List<Models.Course>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void searchCourses() {
        String query = etSearch.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) return;
        
        progressBar.setVisibility(View.VISIBLE);
        
        List<Models.Course> results = new ArrayList<>();
        for (Models.Course course : allCourses) {
            if (course.code.toLowerCase().contains(query) || 
                (course.title != null && course.title.toLowerCase().contains(query))) {
                results.add(course);
            }
        }
        
        // Load detailed course info with timetable
        loadCourseDetails(results);
    }
    
    private void loadCourseDetails(List<Models.Course> courses) {
        String token = sessionManager.getAuthHeader();
        List<CourseWithSessions> coursesWithSessions = new ArrayList<>();
        
        for (Models.Course course : courses) {
            apiService.getCourse(token, course.code).enqueue(new Callback<Models.CourseDetail>() {
                @Override
                public void onResponse(Call<Models.CourseDetail> call, Response<Models.CourseDetail> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        CourseWithSessions cws = new CourseWithSessions();
                        cws.course = course;
                        cws.sessions = response.body().timetable;
                        coursesWithSessions.add(cws);
                        
                        if (coursesWithSessions.size() == courses.size()) {
                            progressBar.setVisibility(View.GONE);
                            searchAdapter.setCoursesWithSessions(coursesWithSessions);
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<Models.CourseDetail> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
    
    private void addClassToTimetable(Models.Course course, Models.TimetableSlot session) {
        String classId = course.code + "-" + session.classNo;
        
        // Check if already added
        for (SavedSession ss : selectedSessions) {
            if (ss.id.startsWith(classId)) {
                Toast.makeText(getContext(), "Class already in timetable", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        SavedSession saved = new SavedSession();
        saved.id = classId + "-" + session.startTime;
        saved.code = course.code;
        saved.title = course.title;
        saved.classNo = session.classNo;
        saved.startTime = session.startTime;
        saved.endTime = session.endTime;
        saved.weekday = session.weekday;
        saved.room = session.room;
        saved.day = getDayString(session.weekday);
        saved.time = session.startTime + "-" + session.endTime;
        
        selectedSessions.add(saved);
        updateTimetableDisplay();
    }
    
    private void removeClassFromTimetable(String classId) {
        selectedSessions.removeIf(s -> s.id.startsWith(classId));
        updateTimetableDisplay();
    }
    
    private void updateTimetableDisplay() {
        timetableAdapter.setSessions(selectedSessions);
        tvSessionCount.setText(selectedSessions.size() + " session(s)");
        
        if (selectedSessions.isEmpty()) {
            emptyTimetableView.setVisibility(View.VISIBLE);
            rvTimetable.setVisibility(View.GONE);
        } else {
            emptyTimetableView.setVisibility(View.GONE);
            rvTimetable.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadSavedTimetable() {
        android.content.SharedPreferences prefs = getContext().getSharedPreferences("timetable", android.content.Context.MODE_PRIVATE);
        String json = prefs.getString("sessions", null);
        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                selectedSessions.clear();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    SavedSession session = new SavedSession();
                    session.id = obj.getString("id");
                    session.code = obj.getString("code");
                    session.title = obj.getString("title");
                    session.classNo = obj.getString("classNo");
                    session.startTime = obj.getString("startTime");
                    session.endTime = obj.getString("endTime");
                    session.weekday = obj.getInt("weekday");
                    session.room = obj.getString("room");
                    session.day = obj.getString("day");
                    session.time = obj.getString("time");
                    selectedSessions.add(session);
                }
                updateTimetableDisplay();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void saveTimetable() {
        try {
            JSONArray array = new JSONArray();
            for (SavedSession session : selectedSessions) {
                JSONObject obj = new JSONObject();
                obj.put("id", session.id);
                obj.put("code", session.code);
                obj.put("title", session.title);
                obj.put("classNo", session.classNo);
                obj.put("startTime", session.startTime);
                obj.put("endTime", session.endTime);
                obj.put("weekday", session.weekday);
                obj.put("room", session.room);
                obj.put("day", session.day);
                obj.put("time", session.time);
                array.put(obj);
            }
            
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("timetable", android.content.Context.MODE_PRIVATE);
            prefs.edit().putString("sessions", array.toString()).apply();
            Toast.makeText(getContext(), "Timetable saved", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Save failed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clearTimetable() {
        new AlertDialog.Builder(getContext())
            .setTitle("Clear Timetable")
            .setMessage("Are you sure you want to clear all courses?")
            .setPositiveButton("Clear", (dialog, which) -> {
                selectedSessions.clear();
                updateTimetableDisplay();
                saveTimetable();
                Toast.makeText(getContext(), "Timetable cleared", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void exportTimetable() {
        // Implementation for JSON export
        Toast.makeText(getContext(), "Export feature - save JSON file", Toast.LENGTH_SHORT).show();
    }
    
    private void importTimetable() {
        // Implementation for JSON import
        Toast.makeText(getContext(), "Import feature - load JSON file", Toast.LENGTH_SHORT).show();
    }
    
    private String getDayString(int weekday) {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        return days[weekday - 1];
    }
    
    static class SavedSession {
        String id, code, title, classNo, startTime, endTime, room, day, time;
        int weekday;
    }
    
    static class CourseWithSessions {
        Models.Course course;
        List<Models.TimetableSlot> sessions;
    }
}
