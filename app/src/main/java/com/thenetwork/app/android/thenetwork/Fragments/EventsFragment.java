package com.thenetwork.app.android.thenetwork.Fragments;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.thenetwork.app.android.thenetwork.Activities.NewEventActivity;
import com.thenetwork.app.android.thenetwork.Adapters.BlogRecyclerAdapter;
import com.thenetwork.app.android.thenetwork.Adapters.EventRecyclerAdapter;
import com.thenetwork.app.android.thenetwork.HelperClasses.BlogPost;
import com.thenetwork.app.android.thenetwork.HelperClasses.Event;
import com.thenetwork.app.android.thenetwork.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventsFragment extends Fragment {

    public static final String TAG = EventsFragment.class.getSimpleName();

    private FloatingActionButton addEvent;
    private RecyclerView eventListView;
    private List<Event> event_list;
    private EventRecyclerAdapter eventRecyclerAdapter;

    private boolean isOpen = false;


    //firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    public EventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        addEvent = view.findViewById(R.id.fab_add_event);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), NewEventActivity.class);
                startActivity(intent);
            }
        });

        RequestManager glide = Glide.with(getContext());

        event_list = new ArrayList<>();
        eventListView = view.findViewById(R.id.event_list_view);
        eventRecyclerAdapter = new EventRecyclerAdapter(getContext(),event_list, glide);

        eventListView.setLayoutManager(new LinearLayoutManager(container.getContext()));

        eventListView.setAdapter(eventRecyclerAdapter);

        mAuth = FirebaseAuth.getInstance();

        firebaseFirestore = FirebaseFirestore.getInstance();

        eventListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                if (reachedBottom){
                    loadMorePosts();
                }

            }
        });

        Query firstQuery = firebaseFirestore.collection("Events")
                .orderBy("timestamp",Query.Direction.DESCENDING).limit(4);

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

                            String eventId = doc.getDocument().getId();

                            Event event = doc.getDocument().toObject(Event.class).withId(eventId);

                            if (isFirstPageFirstLoad){
                                event_list.add(event);
                                eventRecyclerAdapter.notifyItemInserted(event_list.size());
                            } else {
                                event_list.add(0,event);
                                eventRecyclerAdapter.notifyItemInserted(0);
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

        Query nextQuery = firebaseFirestore.collection("Events")
                .orderBy("timestamp",Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(5);

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
                            String eventId = doc.getDocument().getId();
                            Event event = doc.getDocument().toObject(Event.class).withId(eventId);
                            event_list.add(event);
                            eventRecyclerAdapter.notifyItemInserted(event_list.size());
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
                eventRecyclerAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                eventRecyclerAdapter.getFilter().filter(query);
                return false;
            }
        });
    }


}
