package com.example.cs412androidapp;

import android.content.ActivityNotFoundException;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;

/**
 * Opens the device camera through an intent (via the Activity Result API's TakePicture
 * contract) and shows the captured photo below the "Capture Image" button.
 */
public class ThirdActivity extends LifecycleLoggingActivity {

    private static final String TAG = "ThirdActivity";
    private static final String KEY_PENDING_URI = "pending_uri";
    private static final String KEY_DISPLAYED_URI = "displayed_uri";
    private static final int MAX_IMAGE_DIMENSION = 2048;

    private ImageView imageCaptured;
    /** Where the camera app is writing the photo currently being taken. */
    private Uri pendingUri;
    /** The photo currently shown in the ImageView. */
    private Uri displayedUri;

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && pendingUri != null) {
                    displayedUri = pendingUri;
                    showImage(displayedUri);
                } else {
                    Toast.makeText(this, R.string.image_capture_cancelled, Toast.LENGTH_SHORT).show();
                }
                pendingUri = null;
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_third);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageCaptured = findViewById(R.id.imageCaptured);

        if (savedInstanceState != null) {
            pendingUri = savedInstanceState.getParcelable(KEY_PENDING_URI);
            displayedUri = savedInstanceState.getParcelable(KEY_DISPLAYED_URI);
            if (displayedUri != null) {
                showImage(displayedUri);
            }
        }

        Button buttonCaptureImage = findViewById(R.id.buttonCaptureImage);
        buttonCaptureImage.setOnClickListener(v -> captureImage());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_PENDING_URI, pendingUri);
        outState.putParcelable(KEY_DISPLAYED_URI, displayedUri);
    }

    private void captureImage() {
        try {
            pendingUri = createImageUri();
        } catch (IOException e) {
            Log.e(TAG, "Could not create a file for the photo", e);
            return;
        }
        try {
            takePictureLauncher.launch(pendingUri);
        } catch (ActivityNotFoundException e) {
            pendingUri = null;
            Toast.makeText(this, R.string.no_camera_app, Toast.LENGTH_LONG).show();
        }
    }

    /** Creates an empty file in the app cache and returns a content:// URI the camera app may write to. */
    private Uri createImageUri() throws IOException {
        File imagesDir = new File(getCacheDir(), "images");
        if (!imagesDir.exists() && !imagesDir.mkdirs()) {
            throw new IOException("Could not create " + imagesDir);
        }
        // A unique name per capture, so the ImageView never shows a stale cached image.
        File imageFile = File.createTempFile("capture_", ".jpg", imagesDir);
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
    }

    private void showImage(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder applies the photo's EXIF rotation and lets us downsample large photos.
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                imageCaptured.setImageBitmap(ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                    int largestSide = Math.max(info.getSize().getWidth(), info.getSize().getHeight());
                    int sampleSize = 1;
                    while (largestSide / sampleSize > MAX_IMAGE_DIMENSION) {
                        sampleSize *= 2;
                    }
                    decoder.setTargetSampleSize(sampleSize);
                }));
            } catch (IOException e) {
                Log.e(TAG, "Could not decode the captured photo", e);
            }
        } else {
            imageCaptured.setImageURI(uri);
        }
    }
}
