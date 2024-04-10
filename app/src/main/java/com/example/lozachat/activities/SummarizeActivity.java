package com.example.lozachat.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

import com.example.lozachat.R;
import com.example.lozachat.databinding.ActivitySummarizeBinding;

public class SummarizeActivity extends AppCompatActivity {
    private ActivitySummarizeBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        String countryList[] = {"India", "China", "australia", "Portugle", "America", "NewZealand"};
        ArrayList<String> listItems=new ArrayList<String>();
        ArrayAdapter<String> adapter;

        adapter = new ArrayAdapter<String>(this, R.layout.item_container_summarize, listItems);
        binding.summarizeList.setAdapter(adapter);
    }
}
