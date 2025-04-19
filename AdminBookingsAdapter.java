package com.example.signuploginfirebase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminBookingsAdapter extends RecyclerView.Adapter<AdminBookingsAdapter.AdminBookingViewHolder> {
    private List<AdminBooking> adminBookingsList;

    public AdminBookingsAdapter(List<AdminBooking> adminBookingsList) {
        this.adminBookingsList = adminBookingsList;
    }

    @NonNull
    @Override
    public AdminBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_admin_bookings_adapter, parent, false);
        return new AdminBookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminBookingViewHolder holder, int position) {
        AdminBooking adminBooking = adminBookingsList.get(position);
        holder.userName.setText("User: " + adminBooking.getUserName());
        holder.serviceProviderName.setText("Provider: " + adminBooking.getServiceProviderName());
        holder.serviceType.setText("Service: " + adminBooking.getServiceType());
        holder.phone.setText("Phone: " + adminBooking.getPhone());
    }

    @Override
    public int getItemCount() {
        return adminBookingsList.size();
    }

    public static class AdminBookingViewHolder extends RecyclerView.ViewHolder {
        TextView userName, serviceProviderName, serviceType, phone;

        public AdminBookingViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            serviceProviderName = itemView.findViewById(R.id.serviceProviderName);
            serviceType = itemView.findViewById(R.id.serviceType);
            phone = itemView.findViewById(R.id.phone);
        }
    }
}
