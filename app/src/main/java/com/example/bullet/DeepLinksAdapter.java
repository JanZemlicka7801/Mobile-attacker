package com.example.bullet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * RecyclerView Adapter for displaying deep links.
 */
public class DeepLinksAdapter extends RecyclerView.Adapter<DeepLinksAdapter.ViewHolder> {

    // List that holds the deep links to be displayed
    private final List<String> deepLinks;

    // Constructor to initialize the deep links list
    public DeepLinksAdapter(List<String> deepLinks) {
        this.deepLinks = deepLinks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind the deep link text to the TextView in each item
        holder.textView.setText(deepLinks.get(position));
    }

    @Override
    public int getItemCount() {
        // Return the total number of deep links
        return deepLinks.size();
    }

    /**
     * ViewHolder class that represents each item in the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // TextView to display the deep link
        public TextView textView;

        // Constructor to initialize the TextView
        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);  // Reference to the TextView in the layout
        }
    }
}
