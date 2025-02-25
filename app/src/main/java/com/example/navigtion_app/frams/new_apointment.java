package com.example.navigtion_app.frams;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_apointment, container, false);
        nameText = view.findViewById(R.id.name);
        typeText = view.findViewById(R.id.type);
        phoneText = view.findViewById(R.id.Phone);
        mailText = view.findViewById(R.id.Mail);
        barberNameCircle = view.findViewById(R.id.barberNameCircle);
        ImageView favoriteImageView = view.findViewById(R.id.imageView6);

        //  החזרת פונקציונליות העתקת מספר טלפון ודוא"ל
        phoneText.setOnClickListener(v -> copyToClipboard("Phone", phoneText.getText().toString()));
        mailText.setOnClickListener(v -> copyToClipboard("Mail", mailText.getText().toString()));

        ImageView backBtn = view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_new_apointment_to_gender));

        dateRecyclerView = view.findViewById(R.id.DateSelect);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        timeRecyclerView = view.findViewById(R.id.TimeSelect);
        timeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        dateList = new ArrayList<>();
        timeList = new ArrayList<>();

        dateAdapter = new DateAdapter(dateList, date -> {
            selectedDate = date;
            selectedTime = null;
            if (timeAdapter != null) {
                timeAdapter.resetSelection();
            }
            updateTimeSlotsForSelectedDate(selectedDate);
        });
        timeAdapter = new TimeAdapter(timeList, time -> selectedTime = time);

        dateRecyclerView.setAdapter(dateAdapter);
        timeRecyclerView.setAdapter(timeAdapter);

        bookAppointmentButton = view.findViewById(R.id.button);
        bookAppointmentButton.setOnClickListener(v -> bookAppointment());

        barberRecyclerView = view.findViewById(R.id.barberList);
        barberRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // זיהוי אם המשתמש הגיע מכפתור "My Barber"
        NavController navController = NavHostFragment.findNavController(this);
        boolean isFavoriteFlow = false;

        NavBackStackEntry previousBackStackEntry = navController.getPreviousBackStackEntry();
        if (previousBackStackEntry != null && previousBackStackEntry.getDestination().getId() == R.id.fragment_main) {
            isFavoriteFlow = true;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String clientId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (isFavoriteFlow && clientId != null) {
            DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference("users").child(clientId);
            clientRef.child("favorite").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String favoriteBarberId = snapshot.getValue(String.class);
                    if (favoriteBarberId != null) {
                        Log.d("onCreateView", "Favorite barber found: " + favoriteBarberId);
                        selectedBarberId = favoriteBarberId;

                        //  שליפת סוג השיער והעדפת המשתמש לסימון הלב
                        DatabaseReference barberRef = FirebaseDatabase.getInstance().getReference("users").child(favoriteBarberId);
                        barberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User barber = snapshot.getValue(User.class);
                                if (barber != null) {
                                    hairType = barber.getType().equals("Long Hair") ? "longHair" : "shortHair";
                                    Log.d("onCreateView", " Determined hairType from favorite barber: " + hairType);
                                    userFavorite = favoriteBarberId; // סימון הספר המועדף
                                    updateFavoriteIconColor(favoriteImageView); // עדכון צבע הלב
                                } else {
                                    Log.e("onCreateView", " Could not determine hairType from favorite barber.");
                                }
                                loadBarbers(); // טען את רשימת הספרים לאחר קבלת סוג השיער
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("onCreateView", " Failed to get hairType: " + error.getMessage());
                                loadBarbers();
                            }
                        });
                    } else {
                        Log.d("onCreateView", "No favorite barber found, loading all barbers.");
                        determineHairTypeFromArguments();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("onCreateView", " Failed to get favorite barber: " + error.getMessage());
                    determineHairTypeFromArguments();
                }
            });
        } else {
            Log.d("onCreateView", "Regular booking flow, no favorite barber");
            determineHairTypeFromArguments();
        }

        dateList.addAll(getNextTwoWeeksDates());
        dateAdapter.notifyDataSetChanged();

        //  הוספת לחיצה על הלב כדי לשנות ספר מועדף
        if (clientId != null) {
            DatabaseReference clientRef = FirebaseDatabase.getInstance().getReference("users").child(clientId);
            clientRef.child("favorite").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userFavorite = snapshot.getValue(String.class);
                    updateFavoriteIconColor(favoriteImageView);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            favoriteImageView.setOnClickListener(v -> {
                if (selectedBarberId == null) {
                    Toast.makeText(getContext(), "Please select a barber first.", Toast.LENGTH_SHORT).show();
                    return;
                }
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

    /**
     *  פונקציה שמעתיקה טקסט ללוח
     */
    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Copied: " + text, Toast.LENGTH_SHORT).show();
    }


    /**
     *  פונקציה שמוודאת ש- hairType מוגדר במקרה של זרימה רגילה (ללא ספר מועדף)
     */
    private void determineHairTypeFromArguments() {
        if (getArguments() != null) {
            hairType = getArguments().getString("hairType");
        }

        if (hairType == null) {
            Log.e("determineHairType", "⚠ hairType is null, setting default value.");
            hairType = "longHair"; //  אפשר להגדיר כ-"shortHair" אם זו ברירת המחדל הרצויה
        }

        Log.d("determineHairType", "✅ Final hairType: " + hairType);
        loadBarbers();
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

        Log.d("loadBarbers", "Expected hairType from arguments: " + hairType); // נבדוק מה מתקבל ב- hairType

        usersRef.orderByChild("type").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("loadBarbers", "Loading barbers from Firebase...");
                barberList.clear();
                User favoriteBarber = null;
                int favoritePosition = -1;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        Log.d("loadBarbers", "Loaded Barber: " + user.getFullName() + " | ID: " + user.getId() + " | Type: " + user.getType());

                        //  **תיקון השוואת סוג השיער - נבדוק בדיוק מה יש בתוך hairType**
                        boolean isValidBarber = false;

                        if ("Long Hair".equals(user.getType()) && "longHair".equalsIgnoreCase(hairType)) {
                            isValidBarber = true;
                        } else if ("Short Hair".equals(user.getType()) && "shortHair".equalsIgnoreCase(hairType)) {
                            isValidBarber = true;
                        }

                        if (isValidBarber) {
                            Log.d("loadBarbers", " Barber " + user.getFullName() + " matches the filter.");
                            barberList.add(user); // הוספה רק אם הספר מתאים לסינון

                            // אם זה הספר המועדף, שמור את המיקום שלו
                            if (selectedBarberId != null && selectedBarberId.equals(user.getId())) {
                                Log.d("loadBarbers", " Found favorite barber in list: " + user.getFullName());
                                favoriteBarber = user;
                                favoritePosition = barberList.size() - 1; // שמירה על האינדקס
                            }
                        }
                    }
                }

                // אם הרשימה ריקה, נציג הודעה
                if (barberList.isEmpty()) {
                    Log.e("loadBarbers", " No barbers found matching the filter!");
                    Toast.makeText(getContext(), "No available barbers for this hair type.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // יצירת מתאם ועדכון ה- RecyclerView
                barberAdapter = new BarberAdapter(barberList, barber -> {
                    selectedBarberId = barber.getId();
                    updateUIForSelectedBarber(barber);
                    resetDateAndTimeSelection();
                    fetchAppointmentsForSelectedBarber(selectedBarberId);
                });

                barberRecyclerView.setAdapter(barberAdapter);

                // סימון הספר המועדף אם קיים
                if (favoriteBarber != null && favoritePosition != -1) {
                    Log.d("loadBarbers", " Favorite barber selected: " + favoriteBarber.getFullName());
                    updateUIForSelectedBarber(favoriteBarber);
                    barberAdapter.setSelectedPosition(favoritePosition);
                    fetchAppointmentsForSelectedBarber(favoriteBarber.getId());
                } else {
                    Log.d("loadBarbers", "ℹ No favorite barber found, selecting first in list.");
                    selectDefaultBarber(); // בחירת הספר הראשון כברירת מחדל
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("loadBarbers", " Error loading barbers: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load barbers.", Toast.LENGTH_SHORT).show();
            }
        });
    }






    // פונקציה לעדכון ממשק המשתמש בהתאם לספר שנבחר
    private void updateUIForSelectedBarber(User barber) {
        selectedBarberId = barber.getId();
        nameText.setText(barber.getFullName());
        typeText.setText(barber.getType());
        phoneText.setText(barber.getPhone());
        mailText.setText(barber.getEmail());
        barberNameCircle.setText(barber.getFullName());

        Toast.makeText(getContext(), "Selected: " + barber.getFullName(), Toast.LENGTH_SHORT).show();
        updateFavoriteIconColor(getView().findViewById(R.id.imageView6));
    }

    // פונקציה לבחירת הספר הראשון ברשימה אם אין ספר מועדף
    private void selectDefaultBarber() {
        if (!barberList.isEmpty()) {
            User defaultBarber = barberList.get(0);
            updateUIForSelectedBarber(defaultBarber);
            barberAdapter.setSelectedPosition(0);
            fetchAppointmentsForSelectedBarber(defaultBarber.getId());
        }
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_YEAR, 1); // מתחיל ממחר

        for (int i = 0; i < 14; i++) { // יצירת תאריכים לשבועיים הקרובים
            dates.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1); // מעבר ליום הבא
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