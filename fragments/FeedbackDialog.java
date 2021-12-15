package d.blueshoestring.mycvdemo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import d.blueshoestring.mycvdemo.R;

public class FeedbackDialog extends DialogFragment implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    public Button FeedbackBackButton, SaveChangeButton;
    public TextView FeedbackTextField;
    public ConstraintLayout layout;
    public String version;
    public ProgressBar FeedbackProgressBar;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.alertdialog_feedback_dialog, container, false);
        layout = view.findViewById(R.id.FeedbackLayout);
        FeedbackTextField = view.findViewById(R.id.FeedbackTextField);
        FeedbackBackButton = view.findViewById(R.id.FeedbackBackButton);
        FeedbackBackButton.setOnClickListener(this);
        SaveChangeButton = view.findViewById(R.id.SaveChangeButton);
        SaveChangeButton.setOnClickListener(this);
        FeedbackProgressBar = view.findViewById(R.id.FeedbackProgressBar);
        FeedbackProgressBar.setVisibility(View.VISIBLE);

        assert getArguments() != null;
        version = getArguments().getString("Version");
        versionCheck(version);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getUid() != null){
            reference = FirebaseDatabase.getInstance("https://mycvdemo-43a89-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
            reference.child("feedback").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String feedback = snapshot.getValue(String.class);
                    if(feedback != null){
                        FeedbackTextField.setText(feedback);
                    }
                    FeedbackProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        return view;
    }

    private void versionCheck(String version) {
        if (version != null) {
            if (version.equals("Happy")) {
                layout.setBackgroundColor(0xFFFFFFB7);
                SaveChangeButton.setBackgroundColor(0xFFFF78E2);
                FeedbackBackButton.setBackgroundColor(0xFFFF78E2);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.FeedbackBackButton){
            dismiss();
        }else if(id == R.id.SaveChangeButton) {
            if (mAuth.getUid() != null) {
                reference = FirebaseDatabase.getInstance("https://mycvdemo-43a89-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                reference.child("feedback").child(mAuth.getUid()).setValue(FeedbackTextField.getText().toString()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Thank you for your feedback", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
            }
        }
    }
}
