package org.kulkarni_sampada.neuquest.firebase.repository.storage;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.kulkarni_sampada.neuquest.firebase.StorageConnector;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class UserProfileRepository {
    private final StorageReference userProfileRef;
    private Thread workerThread;

    public UserProfileRepository(String userId) {
        userProfileRef = StorageConnector.getInstance().getUsersReference();
    }

    public StorageReference getUserProfileRef() {
        return userProfileRef;
    }

    public void uploadProfileImage(Uri imageUri, String user_image) {

        if (workerThread == null || !workerThread.isAlive()) {
            workerThread = new Thread(() -> {
                try {
                    // Upload the file to Firebase Storage
                    UploadTask uploadTask = userProfileRef.child(user_image).putFile(imageUri);

                    // Wait for the upload to complete
                    Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }

                        // Continue with the task to get the download URL
                        return userProfileRef.getDownloadUrl();

                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            // Use the download URL as needed
                            System.out.println("Image upload successful. Download URL: " + downloadUri.toString());
                        } else {
                            // Handle any errors
                            Exception exception = task.getException();
                            System.err.println("Error uploading image: " + exception.getMessage());
                        }
                    });

                    Tasks.await(urlTask);
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error uploading image: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            });
            workerThread.start();
        }
    }

    public Uri getProfileImage(String user_image) {
        Task<Uri> urlTask = userProfileRef.child(user_image).getDownloadUrl();
        while (!urlTask.isSuccessful());
        return urlTask.getResult();
    }
}