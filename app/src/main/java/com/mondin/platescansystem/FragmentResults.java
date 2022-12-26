package com.mondin.platescansystem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class FragmentResults extends Fragment {
    EditText plateNoView,makeView,modelView,yearView,dateView,ltoView,ltoAlarmView,colorView,bodyTypeView;
    TextView editPlate,editMake,editModel,editYear,editDate,editLto,editLtoAlarm,editColor,editBodyType;
    ImageView plateImage;
    FirebaseFirestore firebaseFirestore;
    ImageView backFab;
    ProgressBar progressBar;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View view=inflater.inflate(R.layout.fragment_results,container,false);
           final Bundle bundle = getArguments();
            plateImage = view.findViewById(R.id.plateNoImage);
            plateNoView = view.findViewById(R.id.plateNumberView);
            makeView = view.findViewById(R.id.makeView);
            modelView = view.findViewById(R.id.modelView);
            yearView = view.findViewById(R.id.yearView);
            dateView = view.findViewById(R.id.dateView);
            ltoView = view.findViewById(R.id.ltoView);
            progressBar = view.findViewById(R.id.progBarResult);
            progressBar.setVisibility(View.VISIBLE);
        editPlate = view.findViewById(R.id.editPlate);
        editMake = view.findViewById(R.id.editMake);
        editModel = view.findViewById(R.id.editModel);
        editYear = view.findViewById(R.id.editYear);
        editDate = view.findViewById(R.id.ltoRegEdit);
        editLto = view.findViewById(R.id.ltoEdit);
        ltoAlarmView = view.findViewById(R.id.ltoAlarmNew);
        colorView = view.findViewById(R.id.colorView);
        bodyTypeView = view.findViewById(R.id.bodyTypeView);
        editLtoAlarm = view.findViewById(R.id.ltoAlarmEditNew);
        editColor = view.findViewById(R.id.colorEdit);
        editBodyType = view.findViewById(R.id.bodyTypeEdit);
            //backFab.setAlpha(0.60f);
        firebaseFirestore = FirebaseFirestore.getInstance();
        backFab = view.findViewById(R.id.backFab);
            backFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        if(bundle.getString("BACK") != null) {
                            Fragment fragment;
                            fragment = new HistoryList();
                            if (fragment != null) {
                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                ft.replace(R.id.content_frame, fragment);
                                ft.commit();
                            }
                        }else{
                            startActivity(new Intent(getActivity(),MainActivity.class));

                        }
                }
            });

            if(bundle != null){
                final String plateNo = bundle.getString("PLATENUMBER");
                final String imagefilepath = bundle.getString("IMAGEFILEPATH");
                final Uri filepath = Uri.parse(imagefilepath);

                //BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inSampleSize = 8;
                //Bitmap bitmap = BitmapFactory.decodeFile(imagefilepath,options);

                firebaseFirestore.collection("Plate Number").document(plateNo.replace(" ",""))
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot != null && documentSnapshot.exists()){

                                if(progressBar!=null){
                                    progressBar.setVisibility(View.GONE);
                                    backFab.setVisibility(View.GONE);
                                    plateImage.setVisibility(View.VISIBLE);
                                    editPlate.setVisibility(View.VISIBLE);
                                    plateNoView.setVisibility(View.VISIBLE);
                                    editMake.setVisibility(View.VISIBLE);
                                    makeView.setVisibility(View.VISIBLE);
                                    editModel.setVisibility(View.VISIBLE);
                                    modelView.setVisibility(View.VISIBLE);
                                    editYear.setVisibility(View.VISIBLE);
                                    yearView.setVisibility(View.VISIBLE);
                                    editDate.setVisibility(View.VISIBLE);
                                    dateView.setVisibility(View.VISIBLE);
                                    editLto.setVisibility(View.VISIBLE);
                                    ltoView.setVisibility(View.VISIBLE);
                                    ltoAlarmView.setVisibility(View.VISIBLE);
                                    colorView.setVisibility(View.VISIBLE);
                                    bodyTypeView.setVisibility(View.VISIBLE);
                                    editLtoAlarm.setVisibility(View.VISIBLE);
                                    editColor.setVisibility(View.VISIBLE);
                                    editBodyType.setVisibility(View.VISIBLE);

                                    plateNoView.setText(plateNo);

                                    String path= filepath.getPath();
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inSampleSize = 2;
                                    Bitmap bitmap = BitmapFactory.decodeFile(path,options);
                                    plateImage.setImageBitmap(bitmap);
                                    //plateImage.setImageURI(filepath);
                                    makeView.setText(documentSnapshot.getString("Make"));
                                    bodyTypeView.setText(documentSnapshot.getString("Body Type"));
                                    modelView.setText(documentSnapshot.getString("Series"));
                                    colorView.setText(documentSnapshot.getString("Color"));
                                    yearView.setText(documentSnapshot.getString("Year Model"));
                                    dateView.setText(documentSnapshot.getString("Date"));
                                    ltoAlarmView.setText(documentSnapshot.getString("LTO Alarm"));
                                    ltoView.setText(documentSnapshot.getString("LTO Apprehension"));
                                    progressBar.setVisibility(View.GONE);
                                }
                            }else{
                                //Toast.makeText(getActivity(), "Document Snapshot is Null", Toast.LENGTH_SHORT).show();
                                Toast.makeText(getActivity(), "No Matching Plate Number Found", Toast.LENGTH_SHORT).show();
                                Fragment fragment;
                                fragment = new HistoryList();
                                if (fragment != null) {
                                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                    ft.replace(R.id.content_frame, fragment);
                                    ft.commit();
                                }
                            }
                        }else{
                            Toast.makeText(getActivity(),"ERROR : "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            Log.d("FIRE_LOG", "ERROR : "+task.getException().getMessage());
                        }
                    }
                });


            }


        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        /*
        plateNoView.setText(null);
        plateImage.setImageBitmap(null);
        makeView.setText(null);
        modelView.setText(null);
        yearView.setText(null);
        dateView.setText(null);
        ltoView.setText(null);
        progressBar.setVisibility(View.GONE);
        firebaseFirestore=null;
        */
        firebaseFirestore=null;
        plateImage=null;
        backFab=null;
        progressBar=null;
        plateNoView=null;
        makeView=null;
        modelView=null;
        yearView=null;
        dateView=null;
        ltoView=null;
        editPlate=null;
        editMake=null;
        editModel=null;
        editYear=null;
        editDate=null;
        editLto=null;
        ltoAlarmView=null;
        colorView=null;
        bodyTypeView=null;
        editLtoAlarm=null;
        editColor=null;
        editBodyType=null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Results");
        //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActivity().getActionBar().setDisplayShowHomeEnabled(true);
        //((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).changeToolbar();
    }


}
