package com.parishod.wareply;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.parishod.wareply.model.CustomRepliesData;
import com.parishod.wareply.model.preferences.PreferencesManager;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_NOTIFICATION_LISTENER = 100;
    CardView autoReplyTextPreviewCard;
    TextView autoReplyTextPreview;
    CustomRepliesData customRepliesData;
    String autoReplyTextPlaceholder;
    Switch mainAutoReplySwitch;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        customRepliesData = CustomRepliesData.getInstance(this);
        preferencesManager = PreferencesManager.getPreferencesInstance(this);

        // Assign Views
        mainAutoReplySwitch = findViewById(R.id.mainAutoReplySwitch);
        autoReplyTextPreviewCard = findViewById(R.id.mainAutoReplyTextCardView);
        autoReplyTextPreview = findViewById(R.id.textView4);

        autoReplyTextPlaceholder = getResources().getString(R.string.mainAutoReplyTextPlaceholder);

        autoReplyTextPreviewCard.setOnClickListener(this::openCustomReplyEditorActivity);
        autoReplyTextPreview.setText(customRepliesData.getOrElse(autoReplyTextPlaceholder));
        mainAutoReplySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked && !isListenerEnabled(MainActivity.this, NotificationService.class)){
                launchNotificationAccessSettings();
            }else {
                preferencesManager.setServicePref(isChecked);
                if(isChecked){
                    enableService();
                    setSwitchState();
                }else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            restartActivity();
                        }
                    }, 100);
                }
            }
        });

        if(!preferencesManager.isServiceEnabled()){
            disableService();
        }
        setSwitchState();
    }

    private void setSwitchState(){
        mainAutoReplySwitch.setChecked(preferencesManager.isServiceEnabled());
    }

    //https://stackoverflow.com/questions/20141727/check-if-user-has-granted-notificationlistener-access-to-my-app/28160115
    //TODO: Use in UI to verify if it needs enabling or restarting
    public boolean isListenerEnabled(Context context, Class notificationListenerCls) {
        ComponentName cn = new ComponentName(context, notificationListenerCls);
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    private void openCustomReplyEditorActivity(View v) {
        Intent intent = new Intent(this, CustomReplyEditorActivity.class);
        startActivity(intent);
    }

    public void launchNotificationAccessSettings() {
        enableService();//we need to enable the service for it so show in settings

        final String NOTIFICATION_LISTENER_SETTINGS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1){
            NOTIFICATION_LISTENER_SETTINGS = ACTION_NOTIFICATION_LISTENER_SETTINGS;
        }else{
            NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
        }
        Intent i = new Intent(NOTIFICATION_LISTENER_SETTINGS);
        startActivityForResult(i, REQ_NOTIFICATION_LISTENER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_NOTIFICATION_LISTENER){
            if(isListenerEnabled(this, NotificationService.class)){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
                preferencesManager.setServicePref(true);
                setSwitchState();
            }else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void enableService(){
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(this, NotificationService.class);
        // enable dummyActivity (as it is disabled in the manifest.xml)
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }

    private void disableService(){
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(this, NotificationService.class);
        // enable dummyActivity (as it is disabled in the manifest.xml)
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

    }

    /*
     * Do not know why after disabling the service until activity restarted the service is still receiving the notifications
     * hence restarting the activity
     */
    private void restartActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Runtime.getRuntime().exit(0);
    }
}