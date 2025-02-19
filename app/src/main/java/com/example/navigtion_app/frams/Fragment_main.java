package com.example.navigtion_app.frams;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.navigtion_app.Adapters.ButtonAdapter;
import com.example.navigtion_app.R;
import com.example.navigtion_app.models.ButtonItem;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;


public class Fragment_main extends Fragment {

    private RecyclerView recyclerView;
    private ButtonAdapter buttonAdapter;
    private List<ButtonItem> buttonList;

    public Fragment_main() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // קישור ל-RecyclerView מה-XML
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // יצירת רשימת הכפתורים
        buttonList = new ArrayList<>();
        buttonList.add(new ButtonItem("New Haircut", R.drawable.ic_plus, ContextCompat.getColor(view.getContext(), R.color.blue), R.id.action_fragment_main_to_gender));
        buttonList.add(new ButtonItem("My Barber", R.drawable.ic_personal, ContextCompat.getColor(requireContext(), R.color.green), R.id.action_fragment_main_to_profile));
        buttonList.add(new ButtonItem("Future Haircuts", R.drawable.ic_future, ContextCompat.getColor(requireContext(), R.color.orange), R.id.action_fragment_main_to_profile));
        buttonList.add(new ButtonItem("History", R.drawable.ic_history, ContextCompat.getColor(requireContext(), R.color.purple2), R.id.action_fragment_main_to_profile));

// יצירת מתאם ושיוכו ל-RecyclerView
        buttonAdapter = new ButtonAdapter(requireActivity(), buttonList);
        recyclerView.setAdapter(buttonAdapter);



        ImageView logOut = view.findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Logout")
                                .setMessage("Are you sure you want to log out?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Navigation.findNavController(view).navigate(R.id.action_fragment_main_to_fragment_intro);
                                        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });

            }
        });

        ImageView updateInfo =view.findViewById(R.id.UpdateInfo);
        updateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_fragment_main_to_profile);
            }
        });
    }

}
