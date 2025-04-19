package com.example.signuploginfirebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
    private Context context;
    private List<Booking> bookings;
    private FirebaseFirestore db;

    public BookingAdapter(Context context, List<Booking> bookings) {
        this.context = context;
        this.bookings = bookings;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.nameTextView.setText("Provider: " + booking.getServiceProviderName());
        holder.typeTextView.setText("Service: " + booking.getServiceType());
        holder.dateTextView.setText("Date: " + booking.getSelectedDateTime());
        holder.statusTextView.setText("Status: " + booking.getStatus()); // Show status

        holder.cancelButton.setOnClickListener(v -> showCancelDialog(booking, position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, typeTextView, dateTextView, statusTextView;
        Button cancelButton;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView); // Fixed missing statusTextView
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }

    // ✅ Fix: Ensure this method exists
    private void showCancelDialog(Booking booking, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> cancelBooking(booking, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelBooking(Booking booking, int position) {
        db.collection("Bookings")
                .document(booking.getId())
                .update("status", "Cancelled") // Instead of deleting, update status
                .addOnSuccessListener(aVoid -> {
                    bookings.get(position).setStatus("Cancelled");
                    notifyItemChanged(position);
                    Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to cancel booking", Toast.LENGTH_SHORT).show());
    }
}
