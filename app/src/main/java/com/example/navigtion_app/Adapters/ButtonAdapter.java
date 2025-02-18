package com.example.navigtion_app.Adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.navigtion_app.R;
import com.example.navigtion_app.models.ButtonItem;

import java.util.List;

public class ButtonAdapter extends RecyclerView.Adapter<ButtonAdapter.ViewHolder> {

    private List<ButtonItem> buttonList;
    private Context context;

    public ButtonAdapter(Context context, List<ButtonItem> buttonList) {
        this.context = context;
        this.buttonList = buttonList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_button, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ButtonItem item = buttonList.get(position);
        holder.buttonText.setText(item.getButtonText());
        holder.buttonIcon.setImageResource(item.getIconResource());
        holder.cardView.setCardBackgroundColor(item.getBackgroundColor());

        // ניווט ל-Fragment בעת לחיצה
        holder.itemView.setOnClickListener(v -> {
            try {
                Fragment fragment = item.getTargetFragment().newInstance();
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragmentContainerView, fragment); // ודא שיש לך FrameLayout עם ID זה ב-XML
                transaction.addToBackStack(null);
                transaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return buttonList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView buttonText;
        ImageView buttonIcon;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            buttonText = itemView.findViewById(R.id.buttonText);
            buttonIcon = itemView.findViewById(R.id.buttonIcon);
            cardView = (CardView) itemView;
        }
    }
}
