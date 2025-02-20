package com.example.navigtion_app.frams;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.navigtion_app.Adapters.DateAdapter;
import com.example.navigtion_app.Adapters.TimeAdapter;
import com.example.navigtion_app.R;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class new_apointment extends Fragment {

    private RecyclerView dateRecyclerView;
    private DateAdapter dateAdapter;
    private List<String> dateList;
    private RecyclerView timeRecyclerView;
    private TimeAdapter timeAdapter;
    private List<String> timeList;

    public new_apointment() {
        // Required empty public constructor
    }

    public static new_apointment newInstance(String param1, String param2) {
        new_apointment fragment = new new_apointment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_apointment, container, false);

        // Initialize RecyclerView
        dateRecyclerView = view.findViewById(R.id.DateSelect);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Generate dates for the next two weeks
        dateList = getNextTwoWeeksDates();
        dateAdapter = new DateAdapter(dateList);
        dateRecyclerView.setAdapter(dateAdapter);

        timeRecyclerView = view.findViewById(R.id.TimeSelect);
        timeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Generate time slots from 09:00 to 17:00
        timeList = generateTimeSlots();
        timeAdapter = new TimeAdapter(timeList);
        timeRecyclerView.setAdapter(timeAdapter);

        return view;
    }

    private List<String> getNextTwoWeeksDates() {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE\ndd/MM/yyyy", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 14; i++) {
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return dates;
    }

    private List<String> generateTimeSlots() {
        List<String> times = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);  // Start time: 09:00
        calendar.set(Calendar.MINUTE, 0);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 17);  // End time: 16:00
        endCalendar.set(Calendar.MINUTE, 0);

        while (calendar.before(endCalendar) || calendar.equals(endCalendar)) {
            times.add(timeFormat.format(calendar.getTime()));
            calendar.add(Calendar.MINUTE, 30);  // Add 30 minutes interval
        }

        return times;
    }
}
