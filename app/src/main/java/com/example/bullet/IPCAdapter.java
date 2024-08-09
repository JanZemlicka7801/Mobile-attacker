package com.example.bullet;

import android.annotation.SuppressLint;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * IPCAdapter class for managing a RecyclerView displaying a list of IPC components.
 */
public class IPCAdapter extends RecyclerView.Adapter<IPCAdapter.ViewHolder> {

    private List<SpannableString> ipcList;
    private final OnItemClickListener listener;

    /**
     * Interface definition for a callback to be invoked when an item in this Adapter has been clicked.
     */
    public interface OnItemClickListener {
        /**
         * Called when an item has been clicked.
         *
         * @param selectedItem The item that was clicked.
         */
        void onItemClick(SpannableString selectedItem);
    }

    /**
     * Constructor for IPCAdapter.
     *
     * @param ipcList  The list of IPC components to be displayed.
     * @param listener The listener for handling item click events.
     */
    public IPCAdapter(List<SpannableString> ipcList, OnItemClickListener listener) {
        this.ipcList = ipcList;
        this.listener = listener;
    }

    /**
     * Updates the IPC list and notifies the adapter of the data change.
     *
     * @param ipcList The new list of IPC components.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateIPCList(List<SpannableString> ipcList) {
        this.ipcList = ipcList;
        notifyDataSetChanged();  // Consider using DiffUtil for better performance with large lists
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the list
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the ViewHolder's itemView to reflect the item at the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpannableString item = ipcList.get(position);
        if (item != null) {
            holder.bind(item, listener);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return ipcList.size();
    }

    /**
     * ViewHolder class for holding the view of each item in the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        /**
         * Constructor for ViewHolder.
         *
         * @param itemView The view of the item.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }

        /**
         * Binds the data to the view and sets the click listener for the item.
         *
         * @param item     The SpannableString item to be displayed.
         * @param listener The click listener for handling item click events.
         */
        public void bind(final SpannableString item, final OnItemClickListener listener) {
            textView.setText(item);
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
