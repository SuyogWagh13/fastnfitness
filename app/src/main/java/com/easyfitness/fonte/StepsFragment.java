package com.easyfitness.fonte;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.easyfitness.R;

public class StepsFragment extends Fragment implements SensorEventListener {


    private SensorManager mSensorManager;
    private Sensor stepSensor;
    private int totalSteps = 0;

    private int previousTotalSteps = 0;

    private TextView steps, goal;

    private Button reset;
    private ProgressBar progressBar;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static StepsFragment newInstance() {
        StepsFragment f = new StepsFragment();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_steps, container, false);
        steps = view.findViewById(R.id.text_view_total_steps);
        goal = view.findViewById(R.id.text_view_goal);
        progressBar = view.findViewById(R.id.progress_bar_steps);
        reset = view.findViewById(R.id.button_rest);
        loadData();
        saveDate();
        resetSteps();
        getActivity();
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        view.findViewById(R.id.image_view_edit_steps)
                .setOnClickListener(view1 -> {
                    showDialog();
                });
        return view;
    }

    private void saveDate() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("key1", totalSteps + previousTotalSteps);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        previousTotalSteps = sharedPreferences.getInt("key1", 0);
        goal.setText(sharedPreferences.getInt("key2", 5000) +"");
        steps.setText(String.valueOf(previousTotalSteps));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (stepSensor != null) {
            mSensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        } else steps.setText("Sensor not found");
    }

    @Override
    public void onPause() {
        super.onPause();
        saveDate();
        if (stepSensor != null) {
            mSensorManager.unregisterListener(this, stepSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            totalSteps = (int) sensorEvent.values[0];
            int currentSteps = totalSteps - previousTotalSteps;
            steps.setText(String.valueOf(currentSteps));
            progressBar.setProgress((currentSteps / 8000) * 100);
        }
    }


    private void resetSteps() {
        reset.setOnClickListener(view -> {
            Toast.makeText(requireContext(), "Long Press to reset !!", Toast.LENGTH_SHORT).show();
        });

        reset.setOnLongClickListener(view -> {
            previousTotalSteps = 0;
            totalSteps = 0;
            steps.setText("0");
            return true;
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Text");

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String userInput = input.getText().toString();
            goal.setText(userInput);
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("key2", Integer.parseInt(userInput));
            Toast.makeText(requireContext(), "Saved !!" + userInput, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        builder.show();
    }
}
