package com.example.navigtion_app.frams;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.navigtion_app.R;


public class gender extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public gender() {
    }


    public static gender newInstance(String param1, String param2) {
        gender fragment = new gender();
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

        View view=inflater.inflate(R.layout.fragment_gender, container, false);

        LinearLayout shortHair=view.findViewById(R.id.button_man);
        shortHair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("hairType", "shortHair");
                Navigation.findNavController(view).navigate(R.id.action_gender_to_new_apointment, bundle);
            }
        });

        LinearLayout longHair=view.findViewById(R.id.button_woman);
        longHair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("hairType", "longHair");
                Navigation.findNavController(view).navigate(R.id.action_gender_to_new_apointment, bundle);
            }
        });

        return view;
    }
}