package com.example.navigtion_app.frams;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.navigtion_app.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_main#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_main extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MediaPlayer mediaPlayer;
    public Fragment_main() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_main.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_main newInstance(String param1, String param2) {
        Fragment_main fragment = new Fragment_main();
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
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ImageView gifImageView = view.findViewById(R.id.gifImageView);
        // Load the GIF using Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.pedro) // Replace with your GIF resource
                .into(gifImageView);

        mediaPlayer = MediaPlayer.create(getContext(), R.raw.sound); // Replace 'sound' with your file name
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Release the MediaPlayer when the fragment is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}