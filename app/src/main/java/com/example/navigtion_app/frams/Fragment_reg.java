package com.example.navigtion_app.frams;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navigtion_app.R;
import com.example.navigtion_app.activity.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_reg#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_reg extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Fragment_reg() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_reg.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_reg newInstance(String param1, String param2) {
        Fragment_reg fragment = new Fragment_reg();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_reg, container, false);
        EditText password = view.findViewById(R.id.password);
        EditText repass = view.findViewById(R.id.repass);
        Button register = view.findViewById(R.id.register);
        EditText Email = view.findViewById(R.id.Email);
        EditText phone = view.findViewById(R.id.phone);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity) getActivity();
                assert mainActivity != null;

                mainActivity.register(new MainActivity.RegisterCallback() {
                    @Override
                    public void onRegisterResult(boolean isSuccess, String message) {
                        if (isSuccess) {
                            Navigation.findNavController(view).navigate(R.id.action_fragment_reg_to_fragment_login);
                        } else {
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


        return view;
    }
}