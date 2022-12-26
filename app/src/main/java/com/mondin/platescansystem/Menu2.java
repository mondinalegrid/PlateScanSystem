package com.mondin.platescansystem;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;

public class Menu2 extends Fragment {

    private ArrayList<String> mPlateNo = new ArrayList<>();
    private ArrayList<String> mPlateNoLink = new ArrayList<>();
    private ArrayList<String> mDateTime = new ArrayList<>();
    private ArrayList<String> docID = new ArrayList<>();
    FirebaseFirestore firebaseFirestore;
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return inflater.inflate(R.layout.fragment_menu_2, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Menu 1");
        initImageBitmaps(view);
    }

    private void initImageBitmaps(View view){
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d(TAG, document.getId() + " => " + document.getData());
                                mPlateNo.add(document.getString("Plate Number"));
                                docID.add(document.getId());
                                mPlateNoLink.add(new File(getActivity().getExternalFilesDir(null),document.getString("Photo")).getPath());
                                mDateTime.add(document.getString("Date")+" "+document.getString("Time"));
                            }
                        } else {
                            //Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        mPlateNo.add("Washington");

        initRecyclerView(view);
    }

    private void initRecyclerView(View view){
        recyclerView = view.findViewById(R.id.recyclerv_view);
        adapter = new RecyclerViewAdapter(getActivity(), mPlateNo,mPlateNoLink,mDateTime,docID);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        recyclerView=null;
        adapter=null;
    }
}
