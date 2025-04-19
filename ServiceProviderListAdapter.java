package com.example.signuploginfirebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import java.util.List;

public class ServiceProviderListAdapter extends RecyclerView.Adapter<ServiceProviderListAdapter.ViewHolder> {
    private List<ServiceProvider> serviceProviders;
    private Context context;
    private OnItemClickListener onItemClickListener;

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(ServiceProvider provider);
    }

    // Constructor to initialize adapter
    public ServiceProviderListAdapter(List<ServiceProvider> serviceProviders, Context context, OnItemClickListener listener) {
        this.serviceProviders = serviceProviders;
        this.context = context;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the item layout for each provider
        View view = LayoutInflater.from(context).inflate(R.layout.activity_service_provider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceProvider provider = serviceProviders.get(position);

        // Setting provider name, category, and experience
        holder.providerName.setText(provider.getName());
        holder.providerCategory.setText(provider.getServiceType());
        holder.providerExperience.setText("Experience: " + provider.getExperience()); // Binding experience

        // Load image using the image resource ID
        Glide.with(context)
                .load(provider.getImageRes())  // Load the image from the drawable resource ID
                .transform(new CircleCrop())   // Apply circle crop transformation to the image
                .into(holder.providerImage);  // Set the image into ImageView

        // Handle item click
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(provider));
    }

    @Override
    public int getItemCount() {
        return serviceProviders.size(); // Return the total number of service providers
    }

    // ViewHolder class to hold the views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView providerImage;       // ImageView for provider's image
        TextView providerName;         // TextView for provider's name
        TextView providerCategory;     // TextView for provider's service type
        TextView providerExperience;   // TextView for provider's experience (new field)

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initializing views
            providerImage = itemView.findViewById(R.id.providerImage);
            providerName = itemView.findViewById(R.id.providerName);
            providerCategory = itemView.findViewById(R.id.providerCategory);
            providerExperience = itemView.findViewById(R.id.providerExperience); // Initialize experience TextView
        }
    }
}
