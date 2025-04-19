package com.example.signuploginfirebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.ViewHolder> {

    private Context context;
    private List<AdminUser> userList;

    public AdminUsersAdapter(Context context, List<AdminUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_admin_users_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminUser user = userList.get(position);
        holder.nameTextView.setText("Name: " + user.getName());
        holder.typeTextView.setText("Type: " + user.getServiceType());
        holder.userNameTextView.setText("UserName: " + user.getUserName());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, typeTextView, userNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.serviceProviderName);
            typeTextView = itemView.findViewById(R.id.serviceType);
            userNameTextView = itemView.findViewById(R.id.userName);
        }
    }
}
