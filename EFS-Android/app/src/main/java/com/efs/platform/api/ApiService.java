package com.efs.platform.api;

import com.efs.platform.models.*;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;
import okhttp3.RequestBody;

public interface ApiService {
    
    // Auth endpoints
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    
    @POST("auth/register")
    Call<ApiResponse> register(@Body RegisterRequest request);
    
    // Dashboard
    @GET("dashboard/summary")
    Call<DashboardData> getDashboardSummary(@Header("Authorization") String token);
    
    // Courses
    @GET("courses")
    Call<List<Course>> getAllCourses(@Header("Authorization") String token);
    
    @GET("courses/{code}")
    Call<CourseDetail> getCourse(@Header("Authorization") String token, @Path("code") String code);
    
    // Group Formation
    @GET("groups/requests")
    Call<List<GroupRequest>> getGroupRequests(@Header("Authorization") String token);
    
    @POST("groups/requests")
    Call<ApiResponse> createGroupRequest(@Header("Authorization") String token, @Body GroupRequest request);
    
    @POST("groups/requests/{id}/invite")
    Call<ApiResponse> sendInvitation(@Header("Authorization") String token, @Path("id") String id, @Body InviteRequest request);
    
    @DELETE("groups/requests/{id}")
    Call<ApiResponse> deleteGroupRequest(@Header("Authorization") String token, @Path("id") String id);
    
    // Materials
    @GET("materials")
    Call<List<Material>> getAllMaterials(@Header("Authorization") String token);
    
    @Multipart
    @POST("materials/upload")
    Call<ApiResponse> uploadMaterial(
        @Header("Authorization") String token,
        @Part("courseCode") RequestBody courseCode,
        @Part("name") RequestBody name,
        @Part("description") RequestBody description,
        @Part MultipartBody.Part file
    );
    
    @GET("materials/{id}/download")
    Call<ResponseBody> downloadMaterial(@Header("Authorization") String token, @Path("id") String id);
    
    // Questionnaire
    @GET("questionnaires")
    Call<List<Questionnaire>> getAllQuestionnaires(@Header("Authorization") String token);
    
    @GET("questionnaires/my")
    Call<List<Questionnaire>> getMyQuestionnaires(@Header("Authorization") String token);
    
    @POST("questionnaires")
    Call<ApiResponse> createQuestionnaire(@Header("Authorization") String token, @Body Questionnaire request);
    
    @POST("questionnaires/{id}/fill")
    Call<ApiResponse> fillQuestionnaire(@Header("Authorization") String token, @Path("id") String id);
    
    // Profile
    @GET("profile/me")
    Call<UserProfile> getProfile(@Header("Authorization") String token);
    
    @PUT("profile/update")
    Call<ApiResponse> updateProfile(@Header("Authorization") String token, @Body UserProfile profile);
    
    // Admin
    @GET("admin/pending-accounts")
    Call<List<PendingAccount>> getPendingAccounts(@Header("Authorization") String token);
    
    @POST("admin/accounts/{sid}/approve")
    Call<ApiResponse> approveAccount(@Header("Authorization") String token, @Path("sid") String sid);
    
    @POST("admin/accounts/{sid}/reject")
    Call<ApiResponse> rejectAccount(@Header("Authorization") String token, @Path("sid") String sid, @Body RejectRequest request);
    
    @GET("admin/stats")
    Call<AdminStats> getAdminStats(@Header("Authorization") String token);
}
