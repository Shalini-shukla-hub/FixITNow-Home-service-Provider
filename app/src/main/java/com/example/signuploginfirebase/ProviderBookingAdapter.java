package com.example.signuploginfirebase;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProviderBookingAdapter extends RecyclerView.Adapter<ProviderBookingAdapter.BookingViewHolder> {
    private List<BookingModel> bookingList;
    private Context context;

    public ProviderBookingAdapter(Context context, List<BookingModel> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_provider_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingModel booking = bookingList.get(position);

        holder.userNameTextView.setText("Name: " + booking.getUserName());
        holder.serviceTypeTextView.setText("Service: " + booking.getServiceType());
        holder.locationTextView.setText("Location: " + booking.getLocation());
        holder.dateTimeTextView.setText("Date & Time: " + booking.getSelectedDateTime());
        holder.userPhoneTextView.setText("Phone: " + booking.getUserPhone());
        holder.issueDescriptionTextView.setText("Issue: " + booking.getIssueDescription());

        // Cancel booking handler
        holder.cancelButton.setOnClickListener(v -> showCancelConfirmationDialog(booking, position));

        // Complete booking handler
        holder.completeButton.setOnClickListener(v -> {
            if (completeListener != null) {
                completeListener.onCompleteClick(booking);
            }
        });
    }

    private void showCancelConfirmationDialog(BookingModel booking, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> cancelBooking(booking, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelBooking(BookingModel booking, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Bookings").document(booking.getBookingId())
                .update("status", "Canceled by Service Provider")
                .addOnSuccessListener(aVoid -> {
                    bookingList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, bookingList.size());
                    Toast.makeText(context, "Booking Canceled", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to cancel booking", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, serviceTypeTextView, locationTextView, dateTimeTextView, userPhoneTextView, issueDescriptionTextView;
        Button cancelButton, completeButton;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.textViewUserName);
            serviceTypeTextView = itemView.findViewById(R.id.textViewServiceType);
            locationTextView = itemView.findViewById(R.id.textViewLocation);
            dateTimeTextView = itemView.findViewById(R.id.textViewDateTime);
            userPhoneTextView = itemView.findViewById(R.id.textViewUserPhone);
            issueDescriptionTextView = itemView.findViewById(R.id.textViewIssueDescription);
            cancelButton = itemView.findViewById(R.id.buttonCancelBooking);
            completeButton = itemView.findViewById(R.id.btnComplete);
        }
    }

    public interface OnCompleteClickListener {
        void onCompleteClick(BookingModel booking);
    }

    private OnCompleteClickListener completeListener;

    public void setOnCompleteClickListener(OnCompleteClickListener listener) {
        this.completeListener = listener;
    }
}
