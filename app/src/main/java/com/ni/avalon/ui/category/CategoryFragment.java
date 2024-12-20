package com.ni.avalon.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ni.avalon.R;
import com.ni.avalon.adapters.NavCategoryAdapter;
import com.ni.avalon.adapters.PopularAdapters;
import com.ni.avalon.model.NavCategoryModel;
import com.ni.avalon.model.PopularModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    List<NavCategoryModel> categoryModelList;
    NavCategoryAdapter navCategoryAdapter;
    RecyclerView categoryRec;
    FirebaseFirestore db;
    ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_category, container, false);
        db = FirebaseFirestore.getInstance();

        categoryRec = root.findViewById(R.id.cat_rec);

        // items mas populares
        categoryRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        categoryModelList = new ArrayList<>();
        navCategoryAdapter = new NavCategoryAdapter(getActivity(),categoryModelList);
        categoryRec.setAdapter(navCategoryAdapter);

        progressBar = root.findViewById(R.id.progressbarCat);
        progressBar.setVisibility(View.VISIBLE);
        categoryRec.setVisibility(View.GONE);


        // este codigo te lo genera el apartado cloud firestore cuando vas a importar las librerias de cloud store
        db.collection("NavCategory")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                NavCategoryModel navCategoryModel = document.toObject(NavCategoryModel.class);
                                categoryModelList.add(navCategoryModel);
                                navCategoryAdapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                                categoryRec.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return root;
    }

}