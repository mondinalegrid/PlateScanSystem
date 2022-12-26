package com.mondin.platescansystem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.List;

import javax.annotation.RegEx;

public class testScanner extends AppCompatActivity {

    Button startCamera,startScanner,cropImage;
    TextView textResults;
    FirebaseFirestore firebaseFirestore;
    private final String SAMPLE_CROPPED_IMG_NAME = "pic";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_scanner);
        startCamera=findViewById(R.id.capCameraBtn);
        startScanner=findViewById(R.id.startScannerBtn);
        cropImage=findViewById(R.id.cropImgBtn);
        startCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (testScanner.this, CameraActivity.class);
                intent.putExtra("TESTSCAN", "TESTSCAN");
                startActivity(intent);
            }
        });
        startScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri filepath=Uri.fromFile(new File(getExternalFilesDir(null), "pic.jpg"));
                String path= filepath.getPath();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap bitmap = BitmapFactory.decodeFile(path,options);
                detectImage(bitmap);
            }
        });
        cropImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCrop(Uri.fromFile(new File(getExternalFilesDir(null), "pic.jpg")));
            }
        });
        textResults=findViewById(R.id.mlkitResults);
        firebaseFirestore = FirebaseFirestore.getInstance();
    }
    private void detectImage(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTxt(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("No Text Detected");
            }
        });
    }
    private void processTxt(FirebaseVisionText text) {
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        if (blocks.size() == 0) {
            //String Result = "No Text Detected";
            showToast("No Text Detected");
            //textView.setText(Result);
            return;
        }
        textResults.setText("");
        for (final FirebaseVisionText.TextBlock block : text.getTextBlocks()) {
            //String txt = block.getText();
            //textView.setTextSize(24);
            if(block.getText().matches("(?i)\\b([A-Z]{3} ?[0-9]{3,4}|[A-Z]{2} ?[0-9]{4}|[0-9]{4} ?[A-Z]{2})\\b")){
                //textResults.append(block.getText());
                firebaseFirestore.collection("Plate Number").document(block.getText().toUpperCase().replace(" ","").trim())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()){
                                showToast("Plate Number Detected"+block.getText());
                                textResults.append(block.getText());
                              }
                        }else{
                            textResults.append(block.getText());
                            showToast("No Plate Number Detected");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast(e.getMessage());
                    }
                });
            }else{
                textResults.append(block.getText());
                showToast("No Plate Number Detected");
            }
            //textResults.append(" " + block.getText());

        }
    }
    private void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK){
            Uri imageUriResultCrop = UCrop.getOutput(data);

            if(imageUriResultCrop!=null){
                //plateImage.setImageURI(imageUriResultCrop);
            }
        }
    }
    private void startCrop(@NonNull Uri uri){
        String destinationFileName = SAMPLE_CROPPED_IMG_NAME;
        destinationFileName += ".jpg";
        //getCacheDir
        UCrop uCrop = UCrop.of(uri,Uri.fromFile(new File(getExternalFilesDir(null),destinationFileName)));
        uCrop.withAspectRatio(1,1);
        //uCrop.withAspectRatio(3,4);
        //uCrop.useSourceImageAspectRatio();
        //uCrop.withAspectRatio(2,3);
        //uCrop.withAspectRatio(16,9);
        uCrop.withMaxResultSize(450,450);
        uCrop.withOptions(getCropOptions());
        uCrop.start(testScanner.this);
    }

    private UCrop.Options getCropOptions(){
        UCrop.Options options = new UCrop.Options();
        //options.setCompressionQuality(100);
        //options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        //options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true);
        options.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        options.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        options.setToolbarTitle("Crop image");
        return options;
    }
}
