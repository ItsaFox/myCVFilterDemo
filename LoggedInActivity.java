package d.blueshoestring.mycvdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import d.blueshoestring.mycvdemo.fragments.EntryDialog;
import d.blueshoestring.mycvdemo.fragments.FeedbackDialog;

public class LoggedInActivity extends AppCompatActivity implements RecycleViewAdapterEntry.EntryListener{
    private FirebaseAuth mAuth;
    public String version;
    public TextView VersionTextView, YearsTextField, ExplanationTextField;
    public ArrayList<CVEntry> myEntryList;
    public ArrayList<String> entryIDList;
    public ProgressBar LoggedInProgressBar;
    public BottomNavigationView bottomNavigationView;
    public ConstraintLayout layout;
    public ImageView TopImage;

    public RecyclerView EntryList;
    public RecyclerView.Adapter rAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        mAuth = FirebaseAuth.getInstance();

        TopImage = findViewById(R.id.TopImage);
        VersionTextView = findViewById(R.id.VersionTextView);
        YearsTextField = findViewById(R.id.YearsTextField);
        ExplanationTextField = findViewById(R.id.ExplanationTextField);
        LoggedInProgressBar = findViewById(R.id.LoggedInProgressBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        layout = findViewById(R.id.logged_in);
        EntryList = findViewById(R.id.FinalCollection);

        LoggedInProgressBar.setVisibility(View.VISIBLE); //Is set to GONE again when updateEntryList() connects to Firebase

        Bundle extras = getIntent().getExtras();
        version = extras.getString("version");
        versionCheck(version);                          //Changes the look depending on version chosen

        myEntryList = new ArrayList<>();                    // for the list of entries for the RecyclerView
        entryIDList = new ArrayList<>();                     // list of IDs of the entries
        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(this);
        EntryList.setLayoutManager(rLayoutManager);
        rAdapter = new RecycleViewAdapterEntry(myEntryList, this, version); //version alters the output
        EntryList.setAdapter(rAdapter);
        updateEntryList();                                  //If there are entries it creates the list of entries shown.

        bottomNavigationView.getMenu().getItem(0).setCheckable(false);  //The bottom menu and it's buttons.
        bottomNavigationView.setOnItemSelectedListener(bottomItem -> {
            int id = bottomItem.getItemId();
            if (id == R.id.MakeACV){                                          //The "Create selection" button.
                DatabaseReference reference;
                reference = FirebaseDatabase.getInstance("https://mycvdemo-43a89-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                if(mAuth.getUid() != null){
                    reference.child("users").child(mAuth.getUid()).removeValue().addOnCompleteListener(task -> { //Removes previous selection.
                        if (task.isSuccessful()) {
                            String[] topics = getResources().getStringArray(R.array.topic_array);
                            for (String topic : topics) {
                                reference.child("template").child(topic).addListenerForSingleValueEvent(new ValueEventListener() { //Picks up the template
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int j = 0;
                                        String snap = snapshot.getKey();
                                        if (snap != null) {
                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                j++;
                                                CVEntry entry = dataSnapshot.getValue(CVEntry.class); //And adds the template as the new selection.
                                                reference.child("users").child(mAuth.getUid()).child(snap).child(Integer.toString(j)).setValue(entry).addOnCompleteListener(task -> {

                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(LoggedInActivity.this, "Template database unavailable", Toast.LENGTH_SHORT).show();
                                    }

                                });
                            }
                            Intent intent = new Intent(LoggedInActivity.this, SelectActivity.class);
                            intent.putExtra("version", version);
                            startActivity(intent);      //Sends us to the selection activity with the version previously chosen.
                        }
                    });
                }

            }else if(id == R.id.Feedback){      //The feedback button.
                FeedbackDialog dialog = new FeedbackDialog();
                Bundle bundle = new Bundle();
                bundle.putString("Version", version);                           //Version is communicated
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "FeedbackDialog"); //Initiates the FeedbackDialog fragment.
            }
            return true;
        });

    }

    public void versionCheck(String version) {
        if (version != null) {
            if (mAuth.getUid() != null) {
                if (version.equals("Happy")) {          //Things are a lot more cheerful.
                    VersionTextView.setText(getResources().getString(R.string.Happy));
                    layout.setBackgroundColor(0xFFC4F5F4);
                    ExplanationTextField.setBackgroundResource(R.drawable.roundyellow2);
                    TopImage.setColorFilter(0xFFFF78E2);
                    bottomNavigationView.setBackgroundColor(0xFFFF78E2);
                    bottomNavigationView.getMenu().getItem(1).setTitle(R.string.MakeACV2);
                    bottomNavigationView.getMenu().getItem(0).setTitle(R.string.NotHappy);
                    ExplanationTextField.setText(R.string.ExplanationText2);
                } else {                                //Not as cheerful
                    VersionTextView.setText(getResources().getString(R.string.Boring));
                    ExplanationTextField.setBackgroundResource(R.drawable.roundgray);
                }
            }
        }
    }

    @Override
    public void onEntryClick(int position) {
        EntryDialog dialog = new EntryDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable("Entry", myEntryList.get(position));       //An CVEntry object, it's ID and the version is communicated.
        bundle.putString("EntryID", entryIDList.get(position));
        bundle.putString("Version", version);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "EntryDialog");    //Initiates the EntryDialog fragment.
    }

    public void updateEntryList(){
        myEntryList.clear();                //The lists are cleared.
        entryIDList.clear();
        rAdapter.notifyDataSetChanged();    //So that the adapter can be reset with an empty list.
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance("https://mycvdemo-43a89-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        String[] topics = getResources().getStringArray(R.array.topic_array);
        if(mAuth.getUid() != null){
            for(int i = 0; i < topics.length; i++){
                reference.child("users").child(mAuth.getUid()).child("Final").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) { //So that the ValueEventListener starts filling all the entries into an empty list.
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CVEntry entry = dataSnapshot.getValue(CVEntry.class);
                            if (entry != null) {
                                int count = 0;
                                for (int i = 0; i < entryIDList.size(); i++) {
                                    if (entryIDList.get(i).equals(dataSnapshot.getKey())) { //Double checks that this entry is not already in the list.
                                        count++;
                                    }
                                }
                                if (count == 0) {                                           //If it isn't the entry is added to the arraylist.
                                    myEntryList.add(entry);
                                    entryIDList.add(dataSnapshot.getKey());
                                    rAdapter.notifyItemInserted(myEntryList.size());
                                }
                                ExplanationTextField.setVisibility(View.GONE);              //If there are entries the explanation text is hidden.
                            }
                        }
                        LoggedInProgressBar.setVisibility(View.GONE);                       //If data is accessed from Firebase the progressBar is removed from view.
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                                                                                            //If there is no selection then nothing should be shown.
                    }
                });
            }
            reference.child("users").child(mAuth.getUid()).child("TotalMonths").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {  //Picks up and updates the shown value of months of working experience.
                    Long totalMonths = snapshot.getValue(Long.class);
                    if(totalMonths != null){
                        if(version.equals("Happy")){                        //Output slightly different depending on version.
                            YearsTextField.setText(getResources().getString(R.string.WorkExpText2) + " " + totalMonths);
                        }else {
                            YearsTextField.setText(getResources().getString(R.string.WorkExpText) + " " + totalMonths);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                                                                            //If there is no value then nothing should be shown.
                }
            });
        }

    }
}