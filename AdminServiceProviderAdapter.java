package com.example.signuploginfirebase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.List;

public class AdminServiceProviderAdapter extends RecyclerView.Adapter<AdminServiceProviderAdapter.AdminProviderViewHolder> {

    public interface OnVerificationActionListener {
        void onVerificationAction(AdminServiceProvider provider, boolean approved);
    }

    private final List<AdminServiceProvider> providers;
    private final OnVerificationActionListener listener;

    public AdminServiceProviderAdapter(List<AdminServiceProvider> providers,
                                       OnVerificationActionListener listener) {
        this.providers = providers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_admin_service_provider_adapter, parent, false);
        return new AdminProviderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProviderViewHolder holder, int position) {
        AdminServiceProvider provider = providers.get(position);
        holder.bind(provider, listener);
    }

    @Override
    public int getItemCount() {
        return providers.size();
    }

    static class AdminProviderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvEmail, tvPhone, tvServiceType, tvAadhar;
        private final Button btnApprove, btnReject;

        public AdminProviderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAdminProviderName);
            tvEmail = itemView.findViewById(R.id.tvAdminProviderEmail);
            tvPhone = itemView.findViewById(R.id.tvAdminProviderPhone);
            tvServiceType = itemView.findViewById(R.id.tvAdminProviderServiceType);
            tvAadhar = itemView.findViewById(R.id.tvAdminProviderAadhar);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        public void bind(AdminServiceProvider provider, OnVerificationActionListener listener) {
            tvName.setText(provider.getName());
            tvEmail.setText(provider.getEmail());
            tvPhone.setText(provider.getPhone());
            tvServiceType.setText(provider.getServiceType());
            tvAadhar.setText(formatAadharNumber(provider.getAadharNumber()));

            btnApprove.setOnClickListener(v -> {
                provider.setAdminApproved(true);
                provider.setVerified(true);
                provider.setVerificationDate(Timestamp.now());  // ✅ Firebase-compatible timestamp
                listener.onVerificationAction(provider, true);
            });

            btnReject.setOnClickListener(v -> {
                provider.setAdminApproved(false);
                provider.setVerified(false);
                listener.onVerificationAction(provider, false);
            });
        }

        private String formatAadharNumber(String aadhar) {
            if (aadhar == null || aadhar.length() != 12) return aadhar;
            return aadhar.replaceFirst("(\\d{4})(\\d{4})(\\d{4})", "$1 $2 $3");
        }
    }
}
