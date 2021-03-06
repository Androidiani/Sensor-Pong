package it.unina.is2project.sensorball.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import it.unina.is2project.sensorball.R;

public class FirstAccessActivity extends Activity {

    private final String TAG = "FirstAccess";

    // Views on screen declaration
    private TextView helloView;
    private TextView appNameView;
    private EditText nickname;
    private Button okButton;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_access);

        // Set the fullscreen window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Find view
        findViews();
        // Set-up typeface
        setTypeface();
        // Set-up listners
        setListners();
        // Get Screen Dims for nickname form width
        Point p = getScreenDimensions();
        Log.d(TAG, "Screen dimensions: " + p.x + ", " + p.y);
        nickname.getLayoutParams().width = (int) (0.7f * p.x);
    }

    /**
     * Find views in activity_main.xml
     */
    private void findViews() {
        helloView = (TextView) findViewById(R.id.helloView);
        appNameView = (TextView) findViewById(R.id.nameFirstAccess);
        nickname = (EditText) findViewById(R.id.nicknameForm);
        okButton = (Button) findViewById(R.id.btnSubmit);
    }

    /**
     * Set the typeface
     */
    private void setTypeface() {
        // Load the font
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "font/secrcode.ttf");
        // Set the typeface
        helloView.setTypeface(typeFace);
        appNameView.setTypeface(typeFace);
        nickname.setTypeface(typeFace);
    }

    /**
     * Set listners for buttons
     */
    private void setListners() {
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOkClick();
            }
        });
    }

    /**
     * Get the screen dimensions
     */
    private Point getScreenDimensions() {
        Point mPoint = new Point();
        // Get screen dimensions
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(mPoint);

        return mPoint;
    }

    /**
     * Manage click on onePlayer button.
     */
    private void btnOkClick() {
        // Save shared preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(TAG, "Nickname found " + nickname.getText().toString());
        if (nickname.getText().toString().compareTo("") != 0) {
            sharedPreferences.edit().putString("prefNickname", nickname.getText().toString()).apply();
            Log.d(TAG, "Nickname saved as " + sharedPreferences.getString("prefNickname", getString(R.string.txt_no_name)));
            // Intent to MainActivity
            Intent i = new Intent(FirstAccessActivity.this, MainActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            finish();
        } else {
            if (toast != null) toast.cancel();
            toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.text_no_user_input), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
