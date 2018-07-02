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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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
import com.thenetwork.app.android.thenetwork.Activities.NewPostActivity;
import com.thenetwork.app.android.thenetwork.Adapters.BlogRecyclerAdapter;
import com.thenetwork.app.android.thenetwork.HelperClasses.BlogPost;
import com.thenetwork.app.android.thenetwork.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment{

    public static final String TAG = HomeFragment.class.getSimpleName();

    private RecyclerView blogListView;
    private List<BlogPost> blog_list;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    private FloatingActionButton openFabBtn;

    private boolean isOpen = false;


    //firebase
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);


        RequestManager glide = Glide.with(getContext());

        blog_list = new ArrayList<>();
        blogListView = view.findViewById(R.id.blog_list_view);
        blogRecyclerAdapter = new BlogRecyclerAdapter(getContext(),blog_list, glide);

        blogListView.setLayoutManager(new LinearLayoutManager(container.getContext()));

        blogListView.setAdapter(blogRecyclerAdapter);

        //whiteNotificationBar(blogListView);

        openFabBtn = view.findViewById(R.id.fab_plus);

        openFabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addPost = new Intent(getActivity(),NewPostActivity.class);
                startActivity(addPost);
            }
        });


        mAuth = FirebaseAuth.getInstance();

            firebaseFirestore = FirebaseFirestore.getInstance();

            blogListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom){
                        loadMorePosts();
                    }

                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts")
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

                                String blogPostId = doc.getDocument().getId();

                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                if (isFirstPageFirstLoad){
                                    blog_list.add(blogPost);
                                    blogRecyclerAdapter.notifyItemInserted(blog_list.size());
                                } else {
                                    blog_list.add(0,blogPost);
                                    blogRecyclerAdapter.notifyItemInserted(0);
                                }

                            }

                        }

                        isFirstPageFirstLoad = false;
                    }

                }
            });

        return view;
    }


//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//
//        if (blogRecyclerAdapter != null && blogListView != null) {
//            blogListView.setAdapter(null);
//            blogListView = null;
//        }
//
//    }

    public void loadMorePosts(){

            Query nextQuery = firebaseFirestore.collection("Posts")
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

                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                blog_list.add(blogPost);

                                blogRecyclerAdapter.notifyItemInserted(blog_list.size());

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
                blogRecyclerAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                blogRecyclerAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

}

