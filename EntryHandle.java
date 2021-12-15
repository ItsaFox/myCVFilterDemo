package d.blueshoestring.mycvdemo;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EntryHandle {

    public EntryHandle() {
    }

    public static void delete(String logID, String topic, String userID) {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://mycvdemo-43a89-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        reference.child("users").child(userID).child(topic).child(logID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.e("Entry deletion", "successful");
            } else {
                Log.e("Entry deletion", "unsuccessful");
            }
        });
    }
}
