package com.example.navigtion_app.frams;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navigtion_app.Adapters.BarberAdapter;
import com.example.navigtion_app.Adapters.DateAdapter;
import com.example.navigtion_app.Adapters.TimeAdapter;
import com.example.navigtion_app.R;
import com.example.navigtion_app.activity.MainActivity;
import com.example.navigtion_app.models.Appointment;
import com.example.navigtion_app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.content.ClipboardManager;
import android.content.ClipData;


public class new_apointment extends Fragment {

    private RecyclerView dateRecyclerView;
    private RecyclerView timeRecyclerView;
    private DateAdapter dateAdapter;
    private TimeAdapter timeAdapter;
    private List<String> dateList;
    private List<String> timeList;
    private String selectedDate;
    private String selectedTime;
    private Button bookAppointmentButton;
    private RecyclerView barberRecyclerView;
    private BarberAdapter barberAdapter;
    private List<User> barberList;
    private DatabaseReference usersRef;
    private String selectedBarberId;
    private String hairType;
    private TextView nameText;
    private TextView typeText;
    private TextView phoneText;
    private TextView mailText;
    private TextView barberNameCircle;
    private List<Appointment> appointmentsList = new ArrayList<>();

    // נשמור את הערך הנוכחי של ה-favorite של המשתמש
    private String userFavorite = null;

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
        if (getArguments() != null) {
            hairType = getArguments().getString("hairType");
        }

        View view = inflater.inflate(R.layout.fragment_new_apointment, container, false);
        nameText = view.findViewById(R.id.name);
        typeText = view.findViewById(R.id.type);
        phoneText = view.findViewById(R.id.Phone);
        mailText = view.findViewById(R.id.Mail);
        barberNameCircle = view.findViewById(R.id.barberNameCircle);

        // לאחר איתחול המשתנה phoneText, הוסיפו את הקוד הבא:
        phoneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Phone", phoneText.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Copied: " + phoneText.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        mailText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Mail", mailText.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Copied: " + mailText.getText(), Toast.LENGTH_SHORT).show();
            }
        });


        ImageView backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_new_apointment_to_gender));

        // הגדרת ה־RecyclerViews ותפריטי הבחירה
        dateRecyclerView = view.findViewById(R.id.DateSelect);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        timeRecyclerView = view.findViewById(R.id.TimeSelect);
        timeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        dateList = new ArrayList<>();
        timeList = new ArrayList<>();

        dateAdapter = new DateAdapter(dateList, date -> {
            selectedDate = date;
            selectedTime = null; // איפוס בחירת השעה
            if (timeAdapter != null) {
                timeAdapter.resetSelection();
            }
            updateTimeSlotsForSelectedDate(selectedDate);
        });
        timeAdapter = new TimeAdapter(timeList, time -> selectedTime = time);

        dateRecyclerView.setAdapter(dateAdapter);
        timeRecyclerView.setAdapter(timeAdapter);

        // כפתור קביעת התור
        bookAppointmentButton = view.findViewById(R.id.button);
        bookAppointmentButton.setOnClickListener(v -> bookAppointment());

        // רשימת הברברים
        barberRecyclerView = view.findViewById(R.id.barberList);
        barberRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        loadBarbers();
        dateList.addAll(getNextTwoWeeksDates());
        dateAdapter.notifyDataSetChanged();

        // טיפול ב-favorite - הגדרת האייקון והלוגיקה
        final ImageView favoriteImageView = view.findViewById(R.id.imageView6);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String clientId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (clientId != null) {
            DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference("users").child(clientId);
            // קריאת ערך ה-favorite של המשתמש ושמירתו ב-userFavorite
            clientRef.child("favorite").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userFavorite = snapshot.getValue(String.class);
                    // עדכון צבע האייקון – אדום רק אם userFavorite קיים ותואם ל-selectedBarberId
                    updateFavoriteIconColor(favoriteImageView);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // טיפול בשגיאה במידת הצורך
                }
            });

            // לחיצה על האייקון – עדכון שדה favorite למזהה הספר הנבחר
            favoriteImageView.setOnClickListener(v -> {
                if (selectedBarberId == null) {
                    Toast.makeText(getContext(), "Please select a barber first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // אם ה-favorite הנוכחי שווה ל-selectedBarberId, נעדכן ל-null
                if (selectedBarberId.equals(userFavorite)) {
                    clientRef.child("favorite").setValue(null)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    userFavorite = null;
                                    updateFavoriteIconColor(favoriteImageView);
                                    Toast.makeText(getContext(), "Favorite cleared!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to clear favorite", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // אחרת, נעדכן את ה-favorite למזהה הספר הנבחר
                    clientRef.child("favorite").setValue(selectedBarberId)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    userFavorite = selectedBarberId;
                                    updateFavoriteIconColor(favoriteImageView);
                                    Toast.makeText(getContext(), "Favorite updated!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to update favorite", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });


        }
        return view;
    }

    // פונקציה לעדכון צבע האייקון בהתאם לערך userFavorite ול-selectedBarberId
    private void updateFavoriteIconColor(ImageView favoriteImageView) {
        if (userFavorite != null && selectedBarberId != null && userFavorite.equals(selectedBarberId)) {
            favoriteImageView.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            favoriteImageView.clearColorFilter();
        }
    }

    private void loadBarbers() {
        barberList = new ArrayList<>();

        usersRef.orderByChild("type").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                barberList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        if ("longHair".equals(hairType) && "Long Hair".equals(user.getType())) {
                            barberList.add(user);
                        } else if ("shortHair".equals(hairType) && "Short Hair".equals(user.getType())) {
                            barberList.add(user);
                        }
                    }
                }

                // Set adapter after fetching data
                barberAdapter = new BarberAdapter(barberList, barber -> {
                    selectedBarberId = barber.getId();
                    // עדכון פרטי הספר
                    nameText.setText(barber.getFullName());
                    typeText.setText(barber.getType());
                    phoneText.setText(barber.getPhone());
                    mailText.setText(barber.getEmail());
                    barberNameCircle.setText(barber.getFullName());

                    if (selectedBarberId != null) {
                        Toast.makeText(getContext(), "Selected: " + barber.getFullName(), Toast.LENGTH_SHORT).show();
                        resetDateAndTimeSelection();
                        fetchAppointmentsForSelectedBarber(selectedBarberId);
                        // עדכון צבע האייקון בהתאם – אם הספר הנבחר תואם ל-favorite
                        updateFavoriteIconColor(getView().findViewById(R.id.imageView6));
                    } else {
                        Toast.makeText(getContext(), "Failed to get Barber ID.", Toast.LENGTH_SHORT).show();
                    }
                });
                barberRecyclerView.setAdapter(barberAdapter);

                // אם הרשימה לא ריקה, בחר אוטומטית את הברבר הראשון
                if (!barberList.isEmpty()) {
                    selectedBarberId = barberList.get(0).getId();
                    nameText.setText(barberList.get(0).getFullName());
                    typeText.setText(barberList.get(0).getType());
                    phoneText.setText(barberList.get(0).getPhone());
                    mailText.setText(barberList.get(0).getEmail());
                    barberNameCircle.setText(barberList.get(0).getFullName());

                    Toast.makeText(getContext(), "Selected: " + barberList.get(0).getFullName(), Toast.LENGTH_SHORT).show();
                    barberAdapter.setSelectedPosition(0);
                    resetDateAndTimeSelection();
                    fetchAppointmentsForSelectedBarber(selectedBarberId);
                    updateFavoriteIconColor(getView().findViewById(R.id.imageView6));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load barbers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetDateAndTimeSelection() {
        selectedDate = null;
        selectedTime = null;
        dateList.clear();
        timeList.clear();

        dateAdapter.resetSelection();
        timeAdapter.resetSelection();

        dateList.addAll(getNextTwoWeeksDates());
        dateAdapter.notifyDataSetChanged();
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

    private void updateTimeSlotsForSelectedDate(String selectedDate) {
        if (selectedDate == null) return;
        // נרמל את התאריך (מסיר "\n" ומרווחים מיותרים)
        String normalizedDate = selectedDate.replace("\n", " ").trim();
        timeList.clear();
        timeList.addAll(generateTimeSlots(normalizedDate));
        timeAdapter.notifyDataSetChanged();
    }

    private List<String> generateTimeSlots(String normalizedSelectedDate) {
        List<String> times = new ArrayList<>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 17);
        endCalendar.set(Calendar.MINUTE, 0);

        while (calendar.before(endCalendar)) {
            String timeSlot = timeFormat.format(calendar.getTime());
            boolean taken = false;
            for (Appointment appointment : appointmentsList) {
                if (appointment.getDate().equals(normalizedSelectedDate) &&
                        appointment.getTime().equals(timeSlot)) {
                    taken = true;
                    break;
                }
            }
            if (!taken) {
                times.add(timeSlot);
            }

            if (hairType.equals("longHair")) {
                calendar.add(Calendar.MINUTE, 60);
            } else {
                calendar.add(Calendar.MINUTE, 30);
            }
        }
        return times;
    }

    private void bookAppointment() {
        if (getActivity() == null || !(getActivity() instanceof MainActivity)) {
            Toast.makeText(getContext(), "Error: Invalid activity context.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedBarberId == null) {
            Toast.makeText(getContext(), "Please select a barber first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == null || selectedTime == null) {
            Toast.makeText(getContext(), "Please select both date and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String clientId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (clientId != null) {
            ((MainActivity) getActivity()).AddAppointmentWithAutoID(clientId, selectedBarberId, selectedDate, selectedTime, success -> {
                if (success) {
                    Navigation.findNavController(getView()).navigate(R.id.action_new_apointment_to_fragment_main);
                }
            });
        } else {
            Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAppointmentsForSelectedBarber(String barberId) {
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        appointmentsRef.orderByChild("barberId").equalTo(barberId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentsList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Appointment appointment = data.getValue(Appointment.class);
                            if (appointment != null) {
                                appointmentsList.add(appointment);
                            }
                        }
                        if (selectedDate != null) {
                            updateTimeSlotsForSelectedDate(selectedDate);
                        }
                        Toast.makeText(getContext(), "נמצאו " + appointmentsList.size() + " תורים", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "שגיאה בטעינת התורים: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}