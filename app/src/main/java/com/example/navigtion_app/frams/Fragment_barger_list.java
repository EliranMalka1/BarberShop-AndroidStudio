
package com.example.navigtion_app.frams;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.navigtion_app.Adapters.UserAdapter;
import com.example.navigtion_app.R;
import com.example.navigtion_app.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Fragment_barger_list extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference usersRef;

    public Fragment_barger_list() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barger_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this::deleteUser);
        recyclerView.setAdapter(userAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        loadUsers();

        return view;
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && !user.getType().equalsIgnoreCase("Manager") && !user.getType().equalsIgnoreCase("client")) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(User user) {
        usersRef.child(user.getId()).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "User deleted", Toast.LENGTH_SHORT).show();
            userList.remove(user);
            userAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Failed to delete user", Toast.LENGTH_SHORT).show());
    }
}
