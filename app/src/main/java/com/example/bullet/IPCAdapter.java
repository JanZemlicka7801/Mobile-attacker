package com.example.bullet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;

import java.util.List;

/**
 * RecyclerView Adapter for displaying IPC components.
 */
public class IPCAdapter extends RecyclerView.Adapter<IPCAdapter.ViewHolder> {

    private List<SpannableString> ipcList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SpannableString selectedItem);
    }

    public IPCAdapter(List<SpannableString> ipcList, OnItemClickListener listener) {
        this.ipcList = ipcList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpannableString item = ipcList.get(position);
        holder.textView.setText(item);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return ipcList.size();
    }

    public void updateIPCList(List<SpannableString> newData) {
        ipcList.clear();
        ipcList.addAll(newData);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
