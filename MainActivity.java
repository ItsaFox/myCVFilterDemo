package d.blueshoestring.mycvdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public TextView mainText, orTextView, choiceText;
    public Button mainPasswordButton1, mainPasswordButton2;
    public EditText mainPasswordEntryField;
    public String version;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    DatabaseReference reference = FirebaseDatabase.getInstance("https://mycvdemo-43a89-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mainText = findViewById(R.id.mainText);
        orTextView = findViewById(R.id.orTextView);
        choiceText = findViewById(R.id.choiceText);
        mainPasswordButton1 = findViewById(R.id.mainPasswordButton1);
        mainPasswordButton2 = findViewById(R.id.mainPasswordButton2);
        mainPasswordEntryField = findViewById(R.id.mainPasswordEntryField);

        mainPasswordButton1.setOnClickListener(this);
        mainPasswordButton2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {   //The Boring or Happy buttons.
        int id = v.getId();
        if(id == R.id.mainPasswordButton1){
            version = "Boring";
            checkPassword();        //Initiates user identification and the need for a password.
        }else if(id == R.id.mainPasswordButton2){
            version = "Happy";
            checkPassword();
        }
    }

    public void checkPassword(){
        Intent intent = new Intent(MainActivity.this, LoggedInActivity.class);
        intent.putExtra("version", version);
        if(currentUser != null){    //If the user already has a token from before then this takes us to the LoggedInActivity.
            startActivity(intent);
        }else{                      //If the user does not have a token we need to check the entered password.
            String passwordEntered = mainPasswordEntryField.getText().toString().trim();
            if(passwordEntered.isEmpty()){
                mainPasswordEntryField.setError("Please enter the password");
                mainPasswordEntryField.requestFocus();
                return;
            }
            if(passwordEntered.length() < 16){
                mainPasswordEntryField.setError("The entered password is too short");
                mainPasswordEntryField.requestFocus();
                return;
            }                       //If the entered password passes the checks we check with the database.
            reference.child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String password = snapshot.getValue(String.class);
                    if(password != null && password.equals(passwordEntered)){
                        mAuth.signInAnonymously().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                startActivity(intent);  //The user is given a token and logged in anonymously.
                            }
                        });
                    }else{
                        mainPasswordEntryField.setError("This password is regularly updated. You might need to request a new one");
                        mainPasswordEntryField.requestFocus();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if(currentUser == null){        //If the user already has a token there is no need for the password field.
            mainPasswordEntryField.setVisibility(View.VISIBLE);
        }
    }

}