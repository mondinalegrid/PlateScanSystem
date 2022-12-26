package com.mondin.platescansystem;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;

public class HistoryList extends Fragment {

    private ArrayList<String> mPlateNo = new ArrayList<>();
    private ArrayList<String> mPlateNoLink = new ArrayList<>();
    private ArrayList<String> mDateTime = new ArrayList<>();
    private ArrayList<String> docID = new ArrayList<>();
    FirebaseFirestore firebaseFirestore;
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    TextView historyStatus;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        //fragment_menu_2 dati ung inflate kung mag error
        return inflater.inflate(R.layout.history_list, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("History");
        initImageBitmaps(view);
    }

    private void initImageBitmaps(final View view){
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("User Informations").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("History").orderBy("TimeStamp",Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().isEmpty()||task.getResult().size()<=0||task.getResult()==null){
                                historyStatus=view.findViewById(R.id.historyStatus);
                                recyclerView = view.findViewById(R.id.recyclerv_view);
                                historyStatus.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }else{
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    //Log.d(TAG, document.getId() + " => " + document.getData());
                                    if(mPlateNo!=null){

                                        mPlateNo.add(document.getString("Plate Number"));
                                        docID.add(document.getId());
                                        mPlateNoLink.add(new File(getActivity().getExternalFilesDir(null),document.getString("Photo")).getPath());
                                        mDateTime.add(document.getString("Date")+" "+document.getString("Time").replace("-",":"));
                                        initRecyclerView(view);
                                    }
                                }
                            }
                        } else {
                            //Log.w(TAG, "Error getting documents.", task.getException());
                            //no history
                            historyStatus=view.findViewById(R.id.historyStatus);
                            recyclerView = view.findViewById(R.id.recyclerv_view);
                            historyStatus.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            //Toast.makeText(getActivity(), "Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //mPlateNo.add("Washington");
    }

    private void initRecyclerView(View view){
        recyclerView = view.findViewById(R.id.recyclerv_view);
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        adapter = new RecyclerViewAdapter(getActivity(), mPlateNo,mPlateNoLink,mDateTime,docID);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(recyclerView!=null){
            recyclerView.setAdapter(null);
        }
        adapter=null;
        historyStatus=null;
        firebaseFirestore=null;
        mPlateNo=null;
        mDateTime=null;
        mPlateNoLink=null;
        docID=null;
    }

}
