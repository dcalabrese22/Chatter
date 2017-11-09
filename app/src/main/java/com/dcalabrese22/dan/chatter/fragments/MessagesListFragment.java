package com.dcalabrese22.dan.chatter.fragments;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dcalabrese22.dan.chatter.ConversationViewHolder;
import com.dcalabrese22.dan.chatter.MultiSelectFirebaseRecyclerAdapter;
import com.dcalabrese22.dan.chatter.Objects.Conversation;
import com.dcalabrese22.dan.chatter.Objects.SelectedConversation;
import com.dcalabrese22.dan.chatter.PbAppWidget;
import com.dcalabrese22.dan.chatter.R;
import com.dcalabrese22.dan.chatter.helpers.RecyclerItemClickListener;
import com.dcalabrese22.dan.chatter.interfaces.MessageExtrasListener;
import com.dcalabrese22.dan.chatter.interfaces.OnRecyclerItemClickListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;


public class MessagesListFragment extends Fragment {

    private String mUserId;
    private FirebaseRecyclerAdapter mAdapter;
    private MessageExtrasListener mListener;
    private boolean mIsMultiSelectMode = false;
    private RecyclerView mRecyclerView;
    private ActionMode mActionMode;
    private ArrayList<SelectedConversation> mSelectedConversations = new ArrayList<>();
    private ArrayList<Conversation> mSelectedPbConversations = new ArrayList<>();

    public MessagesListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (MessageExtrasListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_messages_list, container, false);

        final ProgressBar progressBar = rootView.findViewById(R.id.progress_loading_messages);
        progressBar.setVisibility(View.VISIBLE);
        final FloatingActionButton fab = rootView.findViewById(R.id.message_list_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewMessageFragment fragment = new NewMessageFragment();
                FragmentTransaction transaction = getActivity()
                        .getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        final Context context = getContext();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mUserId = user.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference conversationRef = reference.child("conversations")
                .child(mUserId);

        mRecyclerView = rootView.findViewById(R.id.rv_conversations);

        mAdapter = new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(
                Conversation.class,
                R.layout.fragment_messages_list,
                ConversationViewHolder.class,
                conversationRef
        ) {
            @Override
            public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.conversation, parent, false);
                return new ConversationViewHolder(view);
            }

            @Override
            protected void populateViewHolder(ConversationViewHolder viewHolder, Conversation model, int position) {
                viewHolder.setLastMessage(model.getLastMessage());
                viewHolder.setUser(model.getUser2());
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                progressBar.setVisibility(View.INVISIBLE);
            }
        };

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), mRecyclerView,
                new OnRecyclerItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        if (mIsMultiSelectMode) {
                            multiSelect(view, position);
                        } else {
                            Conversation itemClicked = (Conversation) mAdapter.getItem(position);
                            String conversationId = itemClicked.getConversationId();

                            mListener.getConversationId(conversationId);

                        }
                    }

                    @Override
                    public void OnItemLongClick(View view, int position) {

                        if (!mIsMultiSelectMode) {
                            mIsMultiSelectMode = true;

                            if (mActionMode == null) {
                                mActionMode = getActivity().startActionMode(mActionModeCallBack);
                            }
                        }
                        multiSelect(view, position);

                    }
                }));
        LinearLayoutManager ll = new LinearLayoutManager(getContext());

        mRecyclerView.setLayoutManager(ll);
        ll.setReverseLayout(true);
        mRecyclerView.setAdapter(mAdapter);


        return rootView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    private Menu mContextMenu;
    private ActionMode.Callback mActionModeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_menu, menu);
            mContextMenu = menu;
            mSelectedConversations.clear();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
                    for (SelectedConversation selectedConversation : mSelectedConversations) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                .child("conversations")
                                .child(mUserId);

                        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference()
                                .child("messages");


                        Intent intent = new Intent(getContext(), PbAppWidget.class);
                        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

                        AppWidgetManager manager = AppWidgetManager.getInstance(getContext());
                        int[] ids = manager.getAppWidgetIds(new ComponentName(getContext()
                                .getPackageName(), PbAppWidget.class.getName()));
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                        getActivity().sendBroadcast(intent);

                    }
                    mSelectedConversations.clear();
                    mActionMode.setTitle("" + mSelectedConversations.size());
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mIsMultiSelectMode = false;
            deselectAll();
        }
    };

    public void multiSelect(View view, int position) {
        ConversationViewHolder viewHolder = (ConversationViewHolder) mRecyclerView
                .getChildViewHolder(view);
        Conversation selected = (Conversation) mAdapter.getItem(position);
        SelectedConversation selectedConversation = new SelectedConversation(view, viewHolder,
                position, selected);
        if (mActionMode != null) {
            if (mSelectedPbConversations.contains(selected)) {
                viewHolder.flipAvatar(view);
                view.setActivated(false);
                mSelectedPbConversations.remove(selected);
                for (Iterator<SelectedConversation> i = mSelectedConversations.listIterator(); i.hasNext();) {
                    Conversation c = i.next().getConversation();
                    if (c.equals(selected)) {
                        i.remove();
                    }
                }
            } else {
                mSelectedPbConversations.add(selected);
                mSelectedConversations.add(selectedConversation);
                viewHolder.flipAvatar(view);
                view.setActivated(true);
            }
            mActionMode.setTitle("" + mSelectedConversations.size());
        }
    }


    public void deselectAll() {
        for (SelectedConversation selected : mSelectedConversations) {
            selected.getViewHolder().flipAvatar(selected.getSelectedView());
            selected.getSelectedView().setActivated(false);
        }
        mSelectedConversations.clear();
        mSelectedPbConversations.clear();
    }


}
