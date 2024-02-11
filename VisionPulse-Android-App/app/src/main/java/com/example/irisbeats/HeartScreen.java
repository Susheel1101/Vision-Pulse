package com.example.irisbeats;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HeartScreen extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private static String fileName = null;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {android.Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ImageButton heartscreenrecordbutton;
    private TextView heartscreenheartrate;
    private TextView heartscreenconfidencescore;
    private TextView heartscreenresult;
    private ProgressBar loadingIndicator;
    private ImageButton heartscreenuploadbutton;
    private ImageButton heartscreenlistenbutton;
    private ImageButton heartscreenresetbutton;
    private ImageButton heartscreensubmitbutton;
    private boolean isRecording = false;
    private static final int REQUEST_PICK_AUDIO = 2002; // The request code
    private Uri audioUri;
    private ImageView gifImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heartscreen);
        gifImageView = findViewById(R.id.gif_image);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.heart_screen_background));
        }

        Glide.with(this)
                .asGif()
                .load(R.drawable.maingif) // Replace 'your_gif' with the GIF resource name
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)) // Skip disk cache for the splash gif
                .into(gifImageView);

        heartscreenlistenbutton = findViewById(R.id.heartscreenlistenbutton);
        heartscreensubmitbutton = findViewById(R.id.heartscreensubmitbutton);
        heartscreenrecordbutton = findViewById(R.id.heartscreemrecordbutton);
        heartscreenuploadbutton = findViewById(R.id.heartscreenuploadbutton);
        heartscreenresetbutton = findViewById(R.id.heartscreenresetbutton);
        heartscreenheartrate=findViewById(R.id.heartscreenheartrate);
        heartscreenconfidencescore=findViewById(R.id.heartscreenconfidencescore);
        heartscreenresult=findViewById(R.id.heartscreenresult);


        fileName = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        fileName += "/audiorecordtest.3gp";
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        resetRecording();
        heartscreenrecordbutton.setOnClickListener(v -> toggleRecording());
        heartscreenresetbutton.setOnClickListener(v -> resetRecording());
        heartscreenlistenbutton.setOnClickListener(v -> playRecording());
        heartscreenuploadbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the back button click
                showAudioSourceDialog(); // For example, close the current activity
            }
        });

        heartscreensubmitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the back button click
                File recordingFile = new File(fileName);
                if (recordingFile.exists()) {
                    loadingIndicator.setVisibility(View.VISIBLE);
                    Log.d("HeartScreen", "Recording file exists: " + fileName);
                    sendHttpRequestToFunction(fileName);
                } else {
                    Log.d("HeartScreen", "Recording file does not exist: " + fileName);
                }
                 // For example, close the current activity
            }
        });


    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder.start();
        // Enable the stop button and disable the record button
//        findViewById(R.id.btnStop).setEnabled(true);
//        findViewById(R.id.btnRecord).setEnabled(false);
    }

    private void toggleRecording() {
        if (isRecording) {
            // Stop recording
            stopRecording();
            isRecording = false;
            // Update the UI to reflect that recording has stopped
            // For example, change the button text or color
            heartscreenrecordbutton.setImageResource(R.drawable.heartrecordericon);

        } else {
            // Start recording
            startRecording();
            isRecording = true;
            // Update the UI to reflect that recording has started
            // For example, change the button text or color
            heartscreenrecordbutton.setImageResource(R.drawable.heartstoprecordingicon);
        }
    }

    private void showAudioSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Audio Source")
                .setItems(new CharSequence[]{"Choose from internal storage."}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which == 0) {
                            // User chose to Record
                            dispatchPickAudioIntent();
                        }
                    }
                })
                .show();
    }


    private void dispatchPickAudioIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*"); // MIME type for audio

        try {
            startActivityForResult(intent, REQUEST_PICK_AUDIO);
        } catch (ActivityNotFoundException e) {
            // No file manager available
            Toast.makeText(this, "No file manager available to select audio", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_AUDIO && resultCode == RESULT_OK && data != null) {
                audioUri = data.getData();
                // Use the URI to access and handle the audio file, e.g., play it or save its path
            }
            }
    }
    private void resetRecording() {
        // Attempt to delete the recording file if it exists
        File recordingFile = new File(fileName);
        if (recordingFile.exists()) {
            recordingFile.delete();
        }
        heartscreenconfidencescore.setText("");
        heartscreenresult.setText("");
        heartscreenheartrate.setText("");
        loadingIndicator.setVisibility(View.GONE);
    }
//
    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        // Check if the file exists

    }

    private void playRecording() {
        if (player == null) {
            player = new MediaPlayer(); // Create MediaPlayer only if it doesn't exist to avoid leaks
        }

        try {
            // Reset the MediaPlayer to a clean state in case it was used before
            player.reset();

            if (audioUri != null) {
                player.setDataSource(this, audioUri); // Use the audio Uri if it's not null
            } else if (fileName != null && !fileName.isEmpty()) {
                player.setDataSource(fileName); // Use the file path if the Uri is null and fileName is not empty
            } else {
                Toast.makeText(this, "No audio file selected to play", Toast.LENGTH_SHORT).show();
                return; // Exit the method if both are null or empty
            }

            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException | SecurityException | IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing MediaPlayer", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }
    private void sendHttpRequestToFunction(final String audioFilePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                DataOutputStream outputStream = null;
                BufferedReader reader = null;
                try {
                    // Replace the URL with your actual Cloud Function URL
                    String functionUrl = "https://us-central1-visionpulse.cloudfunctions.net/predict";

                    // Read the audio file into a byte array
                    File audioFile = new File(audioFilePath);
                    if (!audioFile.exists()) {
                        throw new FileNotFoundException("Audio file not found at the specified path.");
                    }
                    FileInputStream fileInputStream = new FileInputStream(audioFile);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }
                    fileInputStream.close();
                    byte[] audioBytes = byteArrayOutputStream.toByteArray();

                    // Create a connection
                    URL url = new URL(functionUrl);
                    urlConnection = (HttpURLConnection) url.openConnection();

                    // Set up the connection
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=boundary");
                    urlConnection.setDoOutput(true);

                    // Write the audio bytes to the connection's output stream
                    outputStream = new DataOutputStream(urlConnection.getOutputStream());
                    outputStream.writeBytes("--boundary\r\n");
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + audioFile.getName() + "\"\r\n");
                    outputStream.writeBytes("Content-Type: audio/wav\r\n\r\n");
                    outputStream.write(audioBytes);
                    outputStream.writeBytes("\r\n--boundary--\r\n");
                    outputStream.flush();

                    // Get the response from the Cloud Function
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    String functionResponse = response.toString();
                    JSONObject jsonResponse = new JSONObject(functionResponse);

                    // Get Confidence Score and Result from the JSON
                    handleFunctionResponse(response.toString());
                } catch (FileNotFoundException e) {
                    Log.e("HttpRequestFunction", "File not found: " + e.getMessage());
                } catch (Exception e) {
                    Log.e("HttpRequestFunction", "Error sending HTTP request: " + e.getMessage());
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (reader != null) {
                            reader.close();
                        }
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    } catch (IOException e) {
                        Log.e("HttpRequestFunction", "Error closing streams: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    private void handleFunctionResponse(final String functionResponse) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Parse the JSON response
                    JSONObject jsonResponse = new JSONObject(functionResponse);

                    // Get Confidence Score and Result from the JSON
                    double confidenceScore = jsonResponse.getDouble("Confidence Score");
                    String result = jsonResponse.getString("Result");
                    String formattedConfidence = String.format("%.2f%%", confidenceScore * 100);
                    double heartrate = jsonResponse.getDouble("Heart Rate");
                    String formattedheartrate = String.format("%.2f", heartrate);

                    // Update TextViews with the values
                    if(result.equals("Artifact"))
                    {
                        heartscreenconfidencescore.setText("Confidence Score: " + formattedConfidence);
                        heartscreenresult.setText("Result: " + result);
                    }
                    else
                    {
                        heartscreenconfidencescore.setText("Confidence Score: " + formattedConfidence);
                        heartscreenresult.setText("Result: " + result);
                        heartscreenheartrate.setText("Heart Rate : "+ formattedheartrate);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                 Hide loading indicator (if needed)
                loadingIndicator.setVisibility(View.GONE);
            }
        });
    }

}































