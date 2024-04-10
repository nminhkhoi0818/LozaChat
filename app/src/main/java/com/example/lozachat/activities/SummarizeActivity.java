package com.example.lozachat.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.lozachat.R;
import com.example.lozachat.databinding.ActivitySummarizeBinding;
import com.example.lozachat.models.Summary;
import com.example.lozachat.models.User;
import com.example.lozachat.utilities.Constants;
import com.example.lozachat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class SummarizeActivity extends AppCompatActivity {
    private ActivitySummarizeBinding binding;
    private PreferenceManager preferenceManager;
    private String receiverId, userId;
    private FirebaseFirestore database;
    private List<String> summaries;
    ArrayAdapter<String> adapter;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySummarizeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        receiverId = (String) getIntent().getSerializableExtra(Constants.KEY_RECEIVER_ID);
        userId = (String) getIntent().getSerializableExtra(Constants.KEY_USER_ID);

        init();
    }
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
    private void init() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        summaries = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_SUMMARY)
        .whereEqualTo(Constants.KEY_SENDER_ID, userId)
        .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot document = task.getResult();
                for (QueryDocumentSnapshot queryDocumentSnapshot : document) {
                    Summary summary = new Summary();
                    summary.senderId = queryDocumentSnapshot.getString(Constants.KEY_SENDER_ID);
                    summary.receiverId = queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_ID);
                    summary.summaryMessage = queryDocumentSnapshot.getString(Constants.KEY_SUMMARY_MESSAGE);
                    summary.dateTime = getReadableDateTime(queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP));
                    summary.dateObject = queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP);
                    summaries.add(summary.dateTime + "\n" + summary.summaryMessage);
                }
                adapter = new ArrayAdapter<String>(this, R.layout.item_container_summarize, R.id.message, summaries);
                binding.summarizeList.setAdapter(adapter);
            }
        });
    }
}
