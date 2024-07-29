package com.example.bullet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class IPCAdapter extends RecyclerView.Adapter<IPCAdapter.ViewHolder> {

    private ArrayList<String> ipcList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String selectedItem);
    }

    public IPCAdapter(ArrayList<String> ipcList, OnItemClickListener onItemClickListener) {
        this.ipcList = ipcList;
        this.onItemClickListener = onItemClickListener;
    }

    public void updateIPCList(ArrayList<String> ipcList) {
        this.ipcList = ipcList;
        notifyDataSetChanged();  // Consider using DiffUtil for better performance with large lists
    }

    @Override
    public IPCAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String item = ipcList.get(position);
        if (item != null) {
            holder.textView.setText(item);
            holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return ipcList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
