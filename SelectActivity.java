package d.blueshoestring.mycvdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import d.blueshoestring.mycvdemo.fragments.EntryDialog;

public class SelectActivity extends AppCompatActivity implements RecycleViewAdapterEntry.EntryListener{
    public int step;
    public int[] stepChange;
    public String version;
    private FirebaseAuth mAuth;
    public ArrayList<CVEntry> myEntryList;
    public ArrayList<String> entryIDList;
    public TextView ChoiceText;
    public ProgressBar SelectProgressBar;
    public BottomNavigationView bottomNavigationView, topNavigationView;
    public RecyclerView EntryList;
    public RecyclerView.Adapter rAdapter;
    public boolean highlightTime;
    public ValueEventListener listener;
    public DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        Bundle extras = getIntent().getExtras();
        version = extras.getString("version");

        step = 0;                                       //Regulates which topic is currently being shown
        highlightTime = false;
        stepChange = new int[]{0,0,0,0};                //Keeps track of if any changes have been made to the selection
        mAuth = FirebaseAuth.getInstance();
        myEntryList = new ArrayList<>();                    // for the list of entries for the RecyclerView
        entryIDList = new ArrayList<>();                     // list of IDs of the entries

        ChoiceText = findViewById(R.id.ChoiceText);
        EntryList = findViewById(R.id.ChoiceList);
        bottomNavigationView = findViewById(R.id.NavBarSelect);
        topNavigationView = findViewById(R.id.TopNavBarSelect);
        SelectProgressBar = findViewById(R.id.SelectProgressBar);

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(EntryList);
        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(this);
        EntryList.setLayoutManager(rLayoutManager);
        rAdapter = new RecycleViewAdapterEntry(myEntryList, this, version);
        EntryList.setAdapter(rAdapter);

        updateChoiceText();                             //Shows an instruction text on the first page
        updateEntryList();                              //Updates the list of entries
        versionCheck(version);                          //Updates the design depending on version selected

        bottomNavigationView.getMenu().getItem(0).setCheckable(false);  //Bottom menu
        bottomNavigationView.setOnItemSelectedListener(bottomItem -> {
            int id = bottomItem.getItemId();
            if (id == R.id.Forward){                    //Either moves to the next topic or finalizes selection.
                if(step == 3){
                    createFinal();
                    Intent intent = new Intent(SelectActivity.this, LoggedInActivity.class);
                    intent.putExtra("version", version);
                    startActivity(intent);
                }else {
                    if(mAuth.getUid() != null){
                        reference.child("users").child(mAuth.getUid()).child(stepToString(step)).removeEventListener(listener);
                    }
                    step++;
                    updateChoiceText();
                    updateEntryList();
                    bottomNavigationView.getMenu().getItem(1).setCheckable(false);
                }
            }else if(id == R.id.Back){                  //Either moves back to previous topic or to LoggedInActivity
                if(step == 0){
                    Intent intent = new Intent(SelectActivity.this, LoggedInActivity.class);
                    intent.putExtra("version", version);
                    startActivity(intent);
                }else{
                    if(mAuth.getUid() != null){
                        reference.child("users").child(mAuth.getUid()).child(stepToString(step)).removeEventListener(listener);
                    }
                    step--;
                    updateChoiceText();
                    updateEntryList();
                    bottomNavigationView.getMenu().getItem(0).setCheckable(false);
                }
            }else if(id == R.id.divider){               //Does Does nothing in the boring version. And mostly nothing in the happy one.
                if(version.equals("Happy")){
                    Toast.makeText(SelectActivity.this, "+1 JOY and happiness!", Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        });

        topNavigationView.getMenu().getItem(0).setCheckable(false); //Top menu
        topNavigationView.setOnItemSelectedListener(topItem -> {
            int topId = topItem.getItemId();
            if (topId == R.id.Highlight){
                if(highlightTime){                                      //Either enables or disables highlighting
                    highlightTime = false;
                    topNavigationView.getMenu().getItem(0).setCheckable(false);
                    for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                        bottomNavigationView.getMenu().getItem(i).setEnabled(true);
                    }
                    topNavigationView.getMenu().getItem(1).setEnabled(true);
                }else{
                    highlightTime = true;
                    topNavigationView.getMenu().getItem(0).setCheckable(true);
                    Toast.makeText(this, "Click the item you want to highlight", Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
                        bottomNavigationView.getMenu().getItem(i).setEnabled(false);
                    }
                    topNavigationView.getMenu().getItem(1).setEnabled(false);
                }

            }else if(topId == R.id.Reset){                              //Resets the current selection on this topic.
                if(stepChange[step] > 0){                               //Only actually resets if there is change registered.
                    SelectProgressBar.setVisibility(View.VISIBLE);      //Progressbar. Goes away once database has been accessed.
                    reference = FirebaseDatabase.getInstance("https://mycvdemo-43a89-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                    if(mAuth.getUid() != null){
                        reference.child("users").child(mAuth.getUid()).child(stepToString(step)).removeValue().addOnCompleteListener(task -> {
                            if(task.isSuccessful()){                    //First removes the old selection.
                                entryIDList.clear();                    //Clears the list for the RecycleView.
                                myEntryList.clear();
                                rAdapter.notifyDataSetChanged();        //Notifies the adapter that the lists are now empty
                                reference.child("template").child(stepToString(step)).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int j = 0;
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {  //Adds the template of the active topic as selection.
                                            j++;
                                            CVEntry entry = dataSnapshot.getValue(CVEntry.class);
                                            reference.child("users").child(mAuth.getUid()).child(stepToString(step)).child(Integer.toString(j)).setValue(entry).addOnCompleteListener(task -> {

                                            });
                                        }
                                        SelectProgressBar.setVisibility(View.GONE); //Takes the progress bar away.
                                        stepChange[step] = 0;                       //Resets change count for this topic.
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(SelectActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }
            }
            return true;
        });
    }

    @Override
    public void onEntryClick(int position) {
        if(highlightTime){          //If hightlight time is activated the entry is highlighted rather than opening the entry fragment.
            reference = FirebaseDatabase.getInstance("https://mycvdemo-43a89-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
            if(mAuth.getUid() != null){
                reference.child("users").child(mAuth.getUid()).child(stepToString(step)).child(entryIDList.get(position)).child("highlighted").setValue(true).addOnCompleteListener(task -> {
                    stepChange[step]++;                                               //Change on this topic is registered.
                    highlightTime = false;
                    for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) { //The interface is re-enabled.
                        bottomNavigationView.getMenu().getItem(i).setEnabled(true);
                    }
                    topNavigationView.getMenu().getItem(1).setEnabled(true);
                    topNavigationView.getMenu().getItem(0).setCheckable(false);
                    updateEntryList();                                                //The entry list is recreated to properly display the highlight based on the database.
                });
            }
        }else{                  //Otherwise the Entry dialog is created.
            EntryDialog dialog = new EntryDialog();
            Bundle bundle = new Bundle();
            bundle.putParcelable("Entry", myEntryList.get(position));
            bundle.putString("EntryID", entryIDList.get(position));
            bundle.putString("Version", version);
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), "EntryDialog");
        }
    }

    public void updateEntryList(){
        myEntryList.clear();                //The lists are cleared.
        entryIDList.clear();
        rAdapter.notifyDataSetChanged();    //So that the adapter can be reset with an empty list.
        reference = FirebaseDatabase.getInstance("https://mycvdemo-43a89-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        if(mAuth.getUid() != null){
            listener = reference.child("users").child(mAuth.getUid()).child(stepToString(step)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {      //So that the ValueEventListener starts filling all the entries into an empty list.
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        CVEntry entry = dataSnapshot.getValue(CVEntry.class);
                        if (entry != null) {
                            int count = 0;
                            for (int i = 0; i < entryIDList.size(); i++) {      //Double checks that this entry is not already in the list.
                                if (entryIDList.get(i).equals(dataSnapshot.getKey())) {
                                    count++;
                                }
                            }
                            if (count == 0) {                                   //If it isn't the entry is added to the arraylist.
                                myEntryList.add(entry);
                                entryIDList.add(dataSnapshot.getKey());
                                if(entry.isHighlighted()){
                                    stepChange[step]++;
                                }
                                rAdapter.notifyItemInserted(myEntryList.size());
                            }
                        }
                    }
                    SelectProgressBar.setVisibility(View.GONE);                 //If data is accessed from Firebase the progressBar is removed from view.
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Database", "not found");
                }
            });
        }
    }

    public void updateChoiceText(){ //The first topic page shows an instructions text
        String[] topics = getResources().getStringArray(R.array.topic_capital_array);
        if(step == 0){
            ChoiceText.setText(topics[step] + getResources().getString(R.string.instructionText));
        }else{
            ChoiceText.setText(topics[step] + getResources().getString(R.string.instructionText2));
        }
    }

    public void createFinal(){      //Creates the final selection upon continuing from topic 4.
        reference = FirebaseDatabase.getInstance("https://mycvdemo-43a89-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        if(mAuth.getUid() != null){
            reference.child("users").child(mAuth.getUid()).child("Final").removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {      //First removes any previous selection lurking about
                    reference.child("users").child(mAuth.getUid()).child("TotalMonths").setValue(0).addOnCompleteListener(task1 -> {
                        String[] topics = getResources().getStringArray(R.array.topic_array);
                        for (String topic : topics) {
                            reference.child("users").child(mAuth.getUid()).child(topic).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {  //Then adds the active selection as final.
                                        CVEntry entry = dataSnapshot.getValue(CVEntry.class);
                                        reference.child("users").child(mAuth.getUid()).child("Final").push().setValue(entry).addOnCompleteListener(task -> {
                                            if(task.isSuccessful()){
                                                if(entry != null && entry.getMonths() > 0){     //If there is an "Months" value for the entry (work) this value is added to the final work value.
                                                    reference.child("users").child(mAuth.getUid()).child("TotalMonths").runTransaction(new Transaction.Handler() {
                                                        @NonNull
                                                        @Override
                                                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                                            Long current = currentData.getValue(Long.class);
                                                            if(current == null){
                                                                return Transaction.success(currentData);
                                                            }
                                                            current = current + entry.getMonths();
                                                            currentData.setValue(current);
                                                            return Transaction.success(currentData);
                                                        }

                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(SelectActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { //Swiping entries to the right removes them.
            stepChange[step]++;                                                            //Change is registered.
            EntryHandle.delete(entryIDList.get(viewHolder.getAdapterPosition()), stepToString(step), mAuth.getUid());
            myEntryList.remove(viewHolder.getAdapterPosition());
            entryIDList.remove(viewHolder.getAdapterPosition());
            rAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());                    //Adapter is notified.
        }
    };

    public String stepToString(int in){  //Translating the step int to a String value.
        String stepString = "none";
        if(in == 0){
            stepString = "work";
        }else if(in == 1){
            stepString = "education";
        }else if(in == 2){
            stepString = "languages";
        }else if(in == 3){
            stepString = "skills";
        }
        return stepString;
    }

    public void versionCheck(String version) {  //Updates design if version is Happy.
        if (version != null) {
            if (mAuth.getUid() != null) {
                if (version.equals("Happy")) {
                    ConstraintLayout layout = findViewById(R.id.select);
                    layout.setBackgroundColor(0xFFC4F5F4);
                    bottomNavigationView.setBackgroundColor(0xFFF0FF59);
                    topNavigationView.setBackgroundColor(0xFFF0FF59);
                    bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.ic_baseline_auto_awesome_24);
                }
            }
        }
    }
}