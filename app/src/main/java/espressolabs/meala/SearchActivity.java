package espressolabs.meala;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.savvyapps.togglebuttonlayout.Toggle;
import com.savvyapps.togglebuttonlayout.ToggleButtonLayout;

import io.apptik.widget.MultiSlider;
import kotlin.Unit;

public class SearchActivity extends AppCompatActivity {
    ToggleButtonLayout toggleDiet;
    MultiSlider caloriesSlider;
    MultiSlider timeSlider;
    MultiSlider ingredientsSlider;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        toggleDiet = findViewById(R.id.toggle_diet);
        caloriesSlider = findViewById(R.id.calories_slider);
        timeSlider = findViewById(R.id.time_slider);
        ingredientsSlider = findViewById(R.id.ingredients_slider);

        final TextView dietText = findViewById(R.id.diet_text);
        final TextView caloriesText = findViewById(R.id.calories_text);
        final TextView timeText = findViewById(R.id.time_text);
        final TextView ingredientsText = findViewById(R.id.ingredients_text);

        dietText.setText("None");
        caloriesText.setText("No limit");
        timeText.setText("Any amount of time");
        ingredientsText.setText("Any");

        toggleDiet.setOnToggledListener((toggle, selected) -> {
            if(selected) {
                dietText.setText(toggle.getTitle());
            } else {
                dietText.setText("None");
            }
            return Unit.INSTANCE;
        });

        caloriesSlider.setOnThumbValueChangeListener(new MultiSlider.SimpleChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int
                    thumbIndex, int value) {

                int min = multiSlider.getThumb(0).getValue();
                int max = multiSlider.getThumb(1).getValue();
                int maxValue = multiSlider.getThumb(1).getMax();
                if (min == 0) {
                    if (max == maxValue) {
                        caloriesText.setText("No limit");
                    } else {
                        caloriesText.setText("At most " + max);
                    }
                } else {
                    if (max == maxValue) {
                        caloriesText.setText("At least " + min);
                    } else {
                        caloriesText.setText(min + " - " + max);
                    }
                }
            }
        });

        timeSlider.setOnThumbValueChangeListener(new MultiSlider.SimpleChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int
                    thumbIndex, int value) {

                int min = multiSlider.getThumb(0).getValue();
                int max = multiSlider.getThumb(1).getValue();
                int maxValue = multiSlider.getThumb(1).getMax();
                if (min == 0) {
                    if (max == maxValue) {
                        timeText.setText("Any amount of time");
                    } else {
                        timeText.setText("At most " + max + " minutes");
                    }
                } else {
                    if (max == maxValue) {
                        timeText.setText("At least " + min + " minutes");
                    } else {
                        timeText.setText(min + " - " + max + "minutes");
                    }
                }
            }
        });

        ingredientsSlider.setOnThumbValueChangeListener(new MultiSlider.SimpleChangeListener() {
            @Override
            public void onValueChanged(MultiSlider multiSlider, MultiSlider.Thumb thumb, int
                    thumbIndex, int value) {
                if (value == 0) {
                    ingredientsText.setText("Any");
                } else {
                    ingredientsText.setText("At most " + String.valueOf(value));
                }
            }
        });
    }
}
