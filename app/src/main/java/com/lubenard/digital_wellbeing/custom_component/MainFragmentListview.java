package com.lubenard.digital_wellbeing.custom_component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lubenard.digital_wellbeing.R;

/**
 * Custom View for main Fragment
 */
public class MainFragmentListview extends LinearLayout {

    /**
     * Set the text timer.
     * Convert integer timer in minutes to '12:50' format
     * @param timer Time to convert in minutes
     */
    //Todo: Maybe Compute time with percentage ?
    public void setTimer(int timer) {
        TextView app_usage = findViewById(R.id.app_usage_time);
        @SuppressLint("DefaultLocale") String text = String.format("%d:%02d", timer / 60, timer % 60);
        app_usage.setText(text);
    }

    /**
     * Set app icon
     * @param icon The icon to draw. This is a drawable
     */
    public void setIcon(Drawable icon) {
        ImageView id_imageView = findViewById(R.id.app_icon);
        id_imageView.setImageDrawable(icon);
    }

    /**
     * The app name
     * @param app_name
     */
    public void setApp_name(String app_name) {
        TextView id_app_name = findViewById(R.id.app_name);
        id_app_name.setText(app_name);
    }

    /**
     * Set the percentage bar progression
     * @param percentage In percent / 100
     */
    public void setPercentage(int percentage) {
        ProgressBar id_percentage = findViewById(R.id.app_usage_progress_bar);
        id_percentage.setProgress(percentage);
    }

    /**
     * Constructor
     * @param context
     */
    public MainFragmentListview(Context context) {
        super(context);
        init(context);
    }

    /**
     * Constructor
     * @param context
     * @param attrs
     */
    public MainFragmentListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Constructor
     * @param context
     * @param attrs
     * @param defStyle
     */
    public MainFragmentListview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * Inflate the Layout
     * @param context
     */
    private void init(Context context) {
        inflate(context, R.layout.main_fragment_app_listview_one_elem, this);
    }
}
