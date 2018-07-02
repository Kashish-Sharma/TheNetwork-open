package com.thenetwork.app.android.thenetwork.Fragments;


import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.thenetwork.app.android.thenetwork.Adapters.RequestRecyclerAdapter;
import com.thenetwork.app.android.thenetwork.HelperClasses.BlogPost;
import com.thenetwork.app.android.thenetwork.HelperClasses.RequestItem;
import com.thenetwork.app.android.thenetwork.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView reqListView;
    private List<RequestItem> req_list;
    private List<RequestItem> req_delete_list;
    private RequestRecyclerAdapter requestRecyclerAdapter;

    //firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private String mCurrentUserId;

    public static final String TAG = RequestsFragment.class.getSimpleName();


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        req_list = new ArrayList<>();
        reqListView = view.findViewById(R.id.request_list_view);
        requestRecyclerAdapter = new RequestRecyclerAdapter(req_list,getContext());

        reqListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        reqListView.setAdapter(requestRecyclerAdapter);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        reqListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                if (reachedBottom){
                    loadMorePosts();
                }

            }
        });

        Query firstQuery = firebaseFirestore.collection("Requests").document(mCurrentUserId)
                .collection("request-recieved").orderBy("timestamp",Query.Direction.DESCENDING).limit(10);

        firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){


                    if (isFirstPageFirstLoad){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    }

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()){
                        if (doc.getType() == DocumentChange.Type.ADDED){

                            String userId = doc.getDocument().getId();
                            RequestItem requestItem = doc.getDocument().toObject(RequestItem.class).withId(userId);

                            if (isFirstPageFirstLoad){
                                req_list.add(requestItem);
                                requestRecyclerAdapter.notifyItemInserted(req_list.size());
                            } else {
                                req_list.add(0,requestItem);
                                requestRecyclerAdapter.notifyItemInserted(0);
                            }

                        }

                    }

                    isFirstPageFirstLoad = false;
                }

            }
        });
        return view;
    }

    public void loadMorePosts(){

        Query nextQuery = firebaseFirestore.collection("Requests").document(mCurrentUserId)
                .collection("request-recieved")
                .orderBy("timestamp",Query.Direction.DESCENDING)
                .startAfter(lastVisible).limit(10);

        nextQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()){
                        if (doc.getType() == DocumentChange.Type.ADDED){

                            String userId = doc.getDocument().getId();
                            RequestItem requestItem = doc.getDocument().toObject(RequestItem.class).withId(userId);
                            req_list.add(requestItem);
                            requestRecyclerAdapter.notifyItemInserted(req_list.size());

                        }

                    }
                }

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home_menu,menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                Log.i("SearchInfo",query+"");
                requestRecyclerAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                requestRecyclerAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

}
