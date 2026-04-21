package com.efs.platform.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Models {
    
    public static class LoginRequest {
        public String email;
        public String password;
        
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
    
    public static class LoginResponse {
        public String token;
        public User user;
        public boolean success;
    }
    
    public static class RegisterRequest {
        public String sid;
        public String email;
        public String password;
        public String photoData;
        public String fileName;
    }
    
    public static class ApiResponse {
        public boolean success;
        public String message;
        public String error;
    }
    
    public static class User {
        public String sid;
        public String email;
        public String role;
        public int credits;
        public String major;
        @SerializedName("year_of_study")
        public int yearOfStudy;
    }
    
    public static class DashboardData {
        public User user;
        public DashboardStats stats;
    }
    
    public static class DashboardStats {
        public int courses;
        public int myGroupRequests;
        public int myQuestionnaires;
        public int myMaterials;
        public int pendingApprovals;
    }
    
    public static class Course {
        public String code;
        public String title;
        public String description;
    }
    
    public static class CourseDetail extends Course {
        public List<TimetableSlot> timetable;
        public List<Material> materials;
    }
    
    public static class TimetableSlot {
        public String day;
        public String time;
        public String room;
        public String classNo;
        public int weekday;
        public String startTime;
        public String endTime;
    }
    
    public static class GroupRequest {
        @SerializedName("_id")
        public String id;
        public String sid;
        public String major;
        public String description;
        public String email;
        public String phone;
        public Double gpa;
        public String dse_score;
        public String desired_groupmates;
        public boolean isOwner;
    }
    
    public static class InviteRequest {
        public String message;
    }
    
    public static class Material {
        public String id;
        public String name;
        public String description;
        public String courseCode;
        public String courseName;
        public String uploadedBy;
        public long size;
        public int downloads;
        public String uploadedAt;
    }
    
    public static class Questionnaire {
        @SerializedName("_id")
        public String id;
        public String description;
        public String link;
        public String creatorSid;
        public int targetResponses;
        public int currentResponses;
        public String status;
        public List<String> filledBy;
    }
    
    public static class UserProfile extends User {
        public String phone;
        public Double gpa;
        public List<String> skills;
        public String about_me;
        public String photoFileId;
    }
    
    public static class PendingAccount {
        public String sid;
        public String email;
        public String createdAt;
        public String photoFileId;
    }
    
    public static class RejectRequest {
        public String reason;
        
        public RejectRequest(String reason) {
            this.reason = reason;
        }
    }
    
    public static class AdminStats {
        public int totalUsers;
        public int totalCourses;
        public int totalMaterials;
        public int pendingAccounts;
    }
}
