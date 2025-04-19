package com.example.signuploginfirebase;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ServiceProviderAdapter extends RecyclerView.Adapter<ServiceProviderAdapter.ViewHolder> {

    private Context context;
    private List<String> categories;

    public ServiceProviderAdapter(Context context, List<String> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.categoryName.setText(category);

        // Convert category name to lowercase and replace spaces with underscores to match drawable filenames
        String imageName = category.toLowerCase().replace(" ", "_");

        // Debugging log to check which image name is being generated
        Log.d("ImageDebug", "Category: " + category + ", Image Name: " + imageName);

        // Get the corresponding drawable resource ID
        int imageResId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());

        // If the image exists, set it; otherwise, use a default image
        if (imageResId != 0) {
            holder.categoryImage.setImageResource(imageResId);
        } else {
            Log.e("ImageError", "Image not found for: " + imageName);
            holder.categoryImage.setImageResource(R.drawable.carrepair); // Default fallback image
        }

        // ✅ Make item clickable and navigate to ServiceProviderListActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ServiceProviderListActivity.class);
            intent.putExtra("serviceType", category.trim()); // Pass category to next screen
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView categoryImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            categoryImage = itemView.findViewById(R.id.categoryImage);
        }
    }
}
