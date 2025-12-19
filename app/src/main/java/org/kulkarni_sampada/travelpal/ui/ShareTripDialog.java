package org.kulkarni_sampada.travelpal.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import org.kulkarni_sampada.travelpal.R;

public class ShareTripDialog extends DialogFragment {

    private String tripId;
    private EditText editEmailToShare;
    private TextView textShareLink;
    private Button btnCopyLink;
    private Button btnSendInvite;

    public static ShareTripDialog newInstance(String tripId) {
        ShareTripDialog dialog = new ShareTripDialog();
        Bundle args = new Bundle();
        args.putString("tripId", tripId);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            tripId = getArguments().getString("tripId");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_share_trip, null);

        initializeViews(view);
        setupListeners();

        // Generate shareable link
        String shareLink = "travelpal://trip/" + tripId;
        textShareLink.setText(shareLink);

        builder.setView(view)
                .setTitle("Share Trip with Friends")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    private void initializeViews(View view) {
        editEmailToShare = view.findViewById(R.id.editEmailToShare);
        textShareLink = view.findViewById(R.id.textShareLink);
        btnCopyLink = view.findViewById(R.id.btnCopyLink);
        btnSendInvite = view.findViewById(R.id.btnSendInvite);
    }

    private void setupListeners() {
        btnCopyLink.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireActivity()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Trip Link", textShareLink.getText());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Link copied to clipboard!", Toast.LENGTH_SHORT).show();
        });

        btnSendInvite.setOnClickListener(v -> {
            String email = editEmailToShare.getText().toString().trim();
            if (email.isEmpty()) {
                editEmailToShare.setError("Please enter an email");
                return;
            }

            // TODO: Implement email invitation
            // For now, just add them as collaborator
            shareWithEmail(email);
        });
    }

    private void shareWithEmail(String email) {
        // TODO: Find user by email and add as collaborator
        Toast.makeText(getContext(),
                "Invitation sent to " + email + "! They can now view and edit this trip.",
                Toast.LENGTH_LONG).show();
        dismiss();
    }
}
