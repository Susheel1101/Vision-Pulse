package com.example.irisbeats;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.yalantis.ucrop.UCrop;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

import androidx.appcompat.app.AppCompatDelegate;


public class CataractScreen extends AppCompatActivity {

    private ImageButton uploadButton;
    private ImageButton submitButton;
    private ImageView imageView;
    private ProgressBar loadingIndicator;
    private TextView confidenceScoreTextView;
    private TextView resultTextView;
    private ImageButton resetButton; // Declare the reset button
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_REQUEST = 1001; // You can use any unique number here
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int UCROP_REQUEST_CODE = 3;
    private String userImageFileName = "";
    private ImageView gifImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cataractscreen);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.cataract_screen_background));
        }
        gifImageView = findViewById(R.id.cataractgifimage);
        Glide.with(this)
                .asGif()
                .load(R.drawable.eyes) // Replace 'your_gif' with the GIF resource name
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)) // Skip disk cache for the splash gif
                .into(gifImageView);

        uploadButton = findViewById(R.id.uploadButton);
        submitButton = findViewById(R.id.submitButton);
        imageView = findViewById(R.id.imageView);
        confidenceScoreTextView = findViewById(R.id.confidenceScoreTextView);
        resultTextView = findViewById(R.id.resultTextView);

        loadingIndicator = findViewById(R.id.loadingIndicator);
        resetButton = findViewById(R.id.resetButton);


        // Assign a click listener to the uploadButton
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This method is called when the button is clicked
                showImageSourceDialog(); // Show a dialog for image source selection
            }
        });


        // Set an OnClickListener to the submitButton
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override // Override the onClick method from the View.OnClickListener interface
            public void onClick(View view) {
                // Get the bitmap from the ImageView named imageView
                Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                // Check if an image is selected
                if (imageBitmap != null) {
                    // Show a loading indicator (a View) when an image is selected and being processed
                    loadingIndicator.setVisibility(View.VISIBLE);

                    // Send the image to a Cloud Function or a server for processing
                    sendHttpRequestToFunction(imageBitmap);
                    submitButton.setEnabled(false);
                    uploadButton.setEnabled(false);
                } else {
                    // Show a toast message if no image is selected
                    // Toast is a small message that pops up at the bottom of the screen
                    // MainActivity.this is the context, "Please select an image first" is the message
                    // Toast.LENGTH_SHORT is the duration for which the toast message is shown
                    Toast.makeText(CataractScreen.this, "Please select an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });


        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAppToInitialState();
            }
        });

    }

    private void resetAppToInitialState() {
        // Reset the ImageView
        imageView.setImageBitmap(null);
        // Reset TextViews
        confidenceScoreTextView.setText("");
        resultTextView.setText("");
        // Reset any other UI elements or internal state as needed
        loadingIndicator.setVisibility(View.GONE);
    }

    private void handleUserFeedback(String feedback) {
        // You can perform actions based on user feedback here
        // For example, you might send the feedback to a server, log it, etc.
        Toast.makeText(CataractScreen.this, "User feedback: " + feedback, Toast.LENGTH_SHORT).show();
    }

    private void showImageSourceDialog() {
// Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image Source")
                .setItems(new CharSequence[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which == 0) {
                            // User chose Camera
                            dispatchTakePictureIntent();
                        } else {
                            // User chose Gallery
                            dispatchPickImageIntent();
                        }
                    }
                })
                .show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    private void dispatchPickImageIntent() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageIntent.setType("image/*");
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == UCROP_REQUEST_CODE && data != null) {
                final Uri resultUri = UCrop.getOutput(data);
                imageView.setImageURI(resultUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Uri tempUri = getUriFromBitmap(imageBitmap);
                startCropActivity(tempUri);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    startCropActivity(imageUri);
                }
            }
        }
    }


    private void sendHttpRequestToFunction(final Bitmap imageBitmap) {
        // Convert Bitmap to byte array
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        final byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Send the HTTP request to the Cloud Function
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Replace the URL with your actual Cloud Function URL
                    String functionUrl = "https://us-central1-catapred.cloudfunctions.net/predict";

                    // Create a connection
                    URL url = new URL(functionUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    try {
                        // Set up the connection
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=boundary"); // Set appropriate content type
                        urlConnection.setDoOutput(true);

                        // Write the image bytes to the connection's output stream
                        DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
                        outputStream.writeBytes("--boundary\r\n");
                        outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n");
                        outputStream.writeBytes("Content-Type: image/jpeg\r\n\r\n");
                        outputStream.write(byteArray);
                        outputStream.writeBytes("\r\n--boundary--\r\n");
                        outputStream.flush();
                        outputStream.close();

                        // Get the response from the Cloud Function
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        String functionResponse = response.toString();
                        JSONObject jsonResponse = new JSONObject(functionResponse);


                        handleFunctionResponse(response.toString());
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

                    // Set text color based on confidence score
//                    if (confidenceScore > 0.8) {
//                        confidenceScoreTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
//                    } else {
//                        confidenceScoreTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
//                    }
//
//                    // Set text color based on result string
//                    if ("Cataract Present".equals(result)) {
//                        resultTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
//                    } else {
//                        resultTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
//                    }

                    // Update TextViews with the values
                    confidenceScoreTextView.setText("Confidence Score: " + formattedConfidence);
                    resultTextView.setText("Result: " + result);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Hide loading indicator (if needed)
                loadingIndicator.setVisibility(View.GONE);
            }
        });
    }

    private Uri getUriFromBitmap(Bitmap bitmap) {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp_" + System.currentTimeMillis() + ".png");
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(file);
    }

    private void startCropActivity(@NonNull Uri uri) {
        String destinationFileName = "SampleCropImage.jpg";
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.withAspectRatio(1, 1);
        uCrop.withMaxResultSize(450, 450);
        uCrop.start(this, UCROP_REQUEST_CODE);
    }

}