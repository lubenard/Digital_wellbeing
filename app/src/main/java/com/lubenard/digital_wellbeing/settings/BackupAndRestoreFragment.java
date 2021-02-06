package com.lubenard.digital_wellbeing.settings;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.data.BarEntry;
import com.lubenard.digital_wellbeing.DbManager;
import com.lubenard.digital_wellbeing.R;
import com.lubenard.digital_wellbeing.Utils.Utils;
import com.lubenard.digital_wellbeing.Utils.XmlWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BackupAndRestoreFragment extends Activity {

    public static final String TAG = "BackupAndRestore";

    private String mode;
    private AlertDialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Must be set to true to avoid UI creation
        //int mode = getActivity().getIntent().getIntExtra("mode", -1);
        //if (mode == 1)
        startBackupIntoXML();
    }

    public boolean startBackupIntoXML() {
        if (!Utils.checkOrRequestPerm(this, this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            return false;
        mode = "backup";
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent dataToFileChooser = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            dataToFileChooser.setType("text/xml");
            try {
                startActivityForResult(dataToFileChooser, 1);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "Failed to open a file explorer. Save will be at default location");
                Toast.makeText(this, R.string.toast_error_custom_path_backup, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, "Failed to open a file explorer. Save will be at default location");
            Toast.makeText(this, R.string.toast_error_custom_path_backup, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    private void createAlertDialog() {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Saving datas");

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.loading_layout, null);
        builder.setView(customLayout);

        // create and show
        // the alert dialog
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null && data.getDataString() != null) {
            if (mode.equals("backup")) {
                Log.d(TAG, "ActivityResult backup at path: " + data.getDataString());
                try {
                    createAlertDialog();
                    OutputStream outputStream = getContentResolver().openOutputStream(Uri.parse(data.getDataString()));

                    XmlWriter xmlWriter = new XmlWriter(outputStream);

                    DbManager dbManager = new DbManager(this);
                    ArrayList<String> arrayOfApps = dbManager.getAllApps();

                    // Datas containing all saved datas
                    xmlWriter.writeEntity("datas");

                        // Contain number of unlocks
                        xmlWriter.writeEntity("numberOfUnlocks");
                            LinkedHashMap<String, Integer>  datas_numberOfUnlocks = dbManager.getAllUnclocks();
                            for (HashMap.Entry<String, Integer> one_data : datas_numberOfUnlocks.entrySet()) {
                                xmlWriter.writeEntity("item");
                                xmlWriter.writeAttribute("date", one_data.getKey());
                                xmlWriter.writeText(String.valueOf(one_data.getValue()));
                                xmlWriter.endEntity();
                            }
                        xmlWriter.endEntity();

                        // Contain number of screenTime
                        xmlWriter.writeEntity("screenTime");
                            LinkedHashMap<String, Integer>  datas_screenTime = dbManager.getAllScreenTime();
                            for (HashMap.Entry<String, Integer> one_data : datas_screenTime.entrySet()) {
                                xmlWriter.writeEntity("item");
                                xmlWriter.writeAttribute("date", one_data.getKey());
                                xmlWriter.writeText(String.valueOf(one_data.getValue()));
                                xmlWriter.endEntity();
                            }
                        xmlWriter.endEntity();

                        // Contain number of time per app
                        xmlWriter.writeEntity("appTime");

                            for (int i = 0; i != arrayOfApps.size(); i++) {
                                xmlWriter.writeEntity(arrayOfApps.get(i));
                                LinkedHashMap<String, Integer>  datas_appTime = dbManager.getDetailsForApp(arrayOfApps.get(i), 0, true);

                                for (HashMap.Entry<String, Integer> one_data : datas_appTime.entrySet()) {
                                    xmlWriter.writeEntity("item");
                                    xmlWriter.writeAttribute("date", one_data.getKey());
                                    xmlWriter.writeText(String.valueOf(one_data.getValue()));
                                    xmlWriter.endEntity();
                                }
                                xmlWriter.endEntity();
                            }
                        xmlWriter.endEntity();

                    xmlWriter.endEntity();

                    xmlWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, "Success to save the datas", Toast.LENGTH_LONG).show();
                dialog.dismiss();
                finish();
            }
        }
    }
}
