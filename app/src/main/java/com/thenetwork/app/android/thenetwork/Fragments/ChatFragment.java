package com.thenetwork.app.android.thenetwork.Fragments;


import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.thenetwork.app.android.thenetwork.Adapters.ChatRecyclerAdapter;
import com.thenetwork.app.android.thenetwork.HelperClasses.Conv;
import com.thenetwork.app.android.thenetwork.HelperUtils.RecyclerItemTouchHelper;
import com.thenetwork.app.android.thenetwork.R;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    public static final String TAG = ChatFragment.class.getSimpleName();

    private RecyclerView mConvList;
    private View mMainView;
    private List<Conv> conv_list;
    private ChatRecyclerAdapter chatRecyclerAdapter;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    //firestore
    private FirebaseFirestore firebaseFirestore;
    private String mCurrent_user_id;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mConvDatabase;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chat, container, false);

        conv_list = new ArrayList<>();

        mConvList = mMainView.findViewById(R.id.conv_list);
        chatRecyclerAdapter = new ChatRecyclerAdapter(conv_list,getContext());
        mConvList.setLayoutManager(new LinearLayoutManager(container.getContext()));
        mConvList.setHasFixedSize(true);
        mConvList.setAdapter(chatRecyclerAdapter);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mConvList.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper
                (0, ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mConvList);

        mUser = FirebaseAuth.getInstance().getCurrentUser();


//        mConvList.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                Boolean reachedBottom = !recyclerView.canScrollVertically(1);
//
//                if (reachedBottom){
//                    loadMorePosts();
//                }
//
//            }
//        });

        if (isFirstPageFirstLoad) {
            conv_list.clear();
        }

        Query firstQuery =  firebaseFirestore.collection("Chat").document(mCurrent_user_id)
                .collection("usernames")
                .orderBy("timestamp", Query.Direction.DESCENDING);
        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if (!documentSnapshots.isEmpty()){


                    if (isFirstPageFirstLoad){
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    }

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges() ){

                        if (doc.getType() == DocumentChange.Type.ADDED ){

                            Conv conv = doc.getDocument().toObject(Conv.class);

                            if (isFirstPageFirstLoad){
                                conv_list.add(conv);
                                chatRecyclerAdapter.notifyItemInserted(conv_list.size());
                            } else {
                                conv_list.add(0,conv);
                                chatRecyclerAdapter.notifyItemInserted(0);
                            }

                            //chatRecyclerAdapter.notifyDataSetChanged();

                        }

                    }

                    isFirstPageFirstLoad = false;
                }
            }
        });


        return mMainView;
    }

    @Override
    public void onCreate(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.chat_menu,menu);

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
                chatRecyclerAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                chatRecyclerAdapter.getFilter().filter(query);
                return false;
            }
        });

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ChatRecyclerAdapter.ChatHolder){

            String otherUserId = conv_list.get(viewHolder.getAdapterPosition()).getUserId();

            final int deletedIndex = viewHolder.getAdapterPosition();
            chatRecyclerAdapter.removeItem(viewHolder.getAdapterPosition());

            //firebaseFirestore.collection("Chat/"+mCurrent_user_id+"/usernames/"+otherUserId).
            firebaseFirestore.collection("Chat")
                    .document(mCurrent_user_id).collection("usernames")
                    .document(otherUserId).delete();

        }
    }


    //    public void loadMorePosts(){
//
//        Query nextQuery = firebaseFirestore.collection("Posts")
//                .orderBy("timestamp",Query.Direction.DESCENDING)
//                .startAfter(lastVisible)
//                .limit(4);
//
//        nextQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
//
//                if (e != null) {
//                    Log.w(TAG, "listen:error", e);
//                    return;
//                }
//
//                if (!documentSnapshots.isEmpty()){
//                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
//                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()){
//                        if (doc.getType() == DocumentChange.Type.ADDED){
//
//                            Conv conv = doc.getDocument().toObject(Conv.class);
//                            conv_list.add(conv);
//                            chatRecyclerAdapter.notifyDataSetChanged();
//
//                        }
//                    }
//                }
//
//            }
//        });
//    }

}
