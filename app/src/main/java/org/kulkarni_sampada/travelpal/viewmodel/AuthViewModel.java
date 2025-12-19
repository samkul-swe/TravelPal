package org.kulkarni_sampada.travelpal.viewmodel;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;

import org.kulkarni_sampada.travelpal.repository.UserRepository;

public class AuthViewModel extends AndroidViewModel {
    private static final String TAG = "AuthViewModel";

    private final UserRepository userRepository;

    // LiveData
    private final LiveData<FirebaseUser> currentUser;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;
    private final MutableLiveData<String> successMessage;
    private final MutableLiveData<Boolean> isAuthenticated;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = UserRepository.getInstance();

        // Initialize LiveData
        this.currentUser = userRepository.getCurrentUser();
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
        this.successMessage = new MutableLiveData<>();
        this.isAuthenticated = new MutableLiveData<>(userRepository.isUserAuthenticated());

        // Observe current user changes
        this.currentUser.observeForever(user ->
                isAuthenticated.setValue(user != null)
        );
    }

    // Getters for LiveData
    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<Boolean> getIsAuthenticated() {
        return isAuthenticated;
    }

    /**
     * Sign up new user
     */
    public void signUp(String email, String password, String name) {
        if (!validateSignUpInput(email, password, name)) {
            return;
        }

        isLoading.setValue(true);

        userRepository.signUp(email, password, name, new UserRepository.OnAuthCompleteListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                isLoading.setValue(false);
                successMessage.setValue("Account created successfully! Welcome " + name);
                isAuthenticated.setValue(true);
                Log.d(TAG, "Sign up successful: " + email);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
                isAuthenticated.setValue(false);
                Log.e(TAG, "Sign up failed: " + error);
            }
        });
    }

    /**
     * Sign in existing user
     */
    public void signIn(String email, String password) {
        if (!validateSignInInput(email, password)) {
            return;
        }

        isLoading.setValue(true);

        userRepository.signIn(email, password, new UserRepository.OnAuthCompleteListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                isLoading.setValue(false);
                successMessage.setValue("Welcome back!");
                isAuthenticated.setValue(true);
                Log.d(TAG, "Sign in successful: " + email);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
                isAuthenticated.setValue(false);
                Log.e(TAG, "Sign in failed: " + error);
            }
        });
    }

    /**
     * Sign out current user
     */
    public void signOut() {
        userRepository.signOut();
        isAuthenticated.setValue(false);
        successMessage.setValue("Signed out successfully");
        Log.d(TAG, "User signed out");
    }

    /**
     * Reset password
     */
    public void resetPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Please enter your email address");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Please enter a valid email address");
            return;
        }

        isLoading.setValue(true);

        userRepository.resetPassword(email, new UserRepository.OnPasswordResetListener() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                successMessage.setValue("Password reset email sent to " + email);
                Log.d(TAG, "Password reset email sent: " + email);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
                Log.e(TAG, "Password reset failed: " + error);
            }
        });
    }

    /**
     * Validate sign up input
     */
    private boolean validateSignUpInput(String email, String password, String name) {
        if (name == null || name.trim().isEmpty()) {
            errorMessage.setValue("Please enter your name");
            return false;
        }

        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Please enter your email");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Please enter a valid email address");
            return false;
        }

        if (password == null || password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    /**
     * Validate sign in input
     */
    private boolean validateSignInInput(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Please enter your email");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Please enter a valid email address");
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Please enter your password");
            return false;
        }

        return true;
    }

    /**
     * Clear error message
     */
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    /**
     * Clear success message
     */
    public void clearSuccessMessage() {
        successMessage.setValue(null);
    }

    /**
     * Check if user is authenticated
     */
    public boolean isUserAuthenticated() {
        return userRepository.isUserAuthenticated();
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        return userRepository.getCurrentUserId();
    }
}
