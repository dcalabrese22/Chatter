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
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dcalabrese22.dan.chatter.AppWidget;
import com.dcalabrese22.dan.chatter.ConversationViewHolder;
import com.dcalabrese22.dan.chatter.Objects.Conversation;
import com.dcalabrese22.dan.chatter.Objects.SelectedConversation;
import com.dcalabrese22.dan.chatter.R;
import com.dcalabrese22.dan.chatter.helpers.RecyclerItemClickListener;
import com.dcalabrese22.dan.chatter.interfaces.MessageExtrasListener;
import com.dcalabrese22.dan.chatter.interfaces.OnRecyclerItemClickListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Iterator;

//fragment for displaying a list of conversations
public class MessagesListFragment extends Fragment {

    private String mUserId;
    private Context mContext;
    private String mUser2;

    private FloatingActionButton mFab;
    private FirebaseRecyclerAdapter mAdapter;
    private MessageExtrasListener mListener;
    private boolean mIsMultiSelectMode = false;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ActionMode mActionMode;
    private ArrayList<SelectedConversation> mSelectedConversations = new ArrayList<>();
    private ArrayList<Conversation> mConversationsSelected = new ArrayList<>();
    private ArrayList<Integer> mSelectedPositions = new ArrayList<>();

    private final String SELECTED_POSITIONS_KEY = "selected_positions_key";

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

        View rootView = inflater.inflate(R.layout.fragment_messages_list, container,
                false);
        mContext = getContext();

        final ProgressBar progressBar = rootView.findViewById(R.id.progress_loading_messages);
        //show progress bar while data is loading
        progressBar.setVisibility(View.VISIBLE);

        mFab = rootView.findViewById(R.id.message_list_fab);
        //open the new message fragment when fab is clicked
        mFab.setOnClickListener(new View.OnClickListener() {
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

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mUserId = user.getUid();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query conversationRef = reference.child("conversations")
                .child(mUserId)
                .orderByChild("timeStamp");

        mRecyclerView = rootView.findViewById(R.id.rv_conversations);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        //firebase recycler adapter for displaying each conversation
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
            protected void populateViewHolder(final ConversationViewHolder viewHolder,
                                              final Conversation model, int position) {

                viewHolder.setLastMessage(model.getLastMessage());
                viewHolder.setUser(model.getUser2());
                viewHolder.setAvatar(mContext, model.getUser2ImageRef());
            }

            @Override
            public void onDataChanged() {
                //when the data is loaded, hide the progress bar
                progressBar.setVisibility(View.INVISIBLE);
                Log.d("child count:", String.valueOf(mLayoutManager.getChildCount()));
//                super.onDataChanged();
            }
        };

        mRecyclerView.setAdapter(mAdapter);

        //handles show and long press events for each item in the recyclerview
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), mRecyclerView,
                new OnRecyclerItemClickListener() {

                    //if the users is in select mode, which was activated by long pressing prior to
                    //keep selecting conversations. Otherwise, open the selected conversation
                    //to reply to a chat
                    @Override
                    public void onItemClick(View view, int position) {
                        if (mIsMultiSelectMode) {
//                            multiSelect(view, position);
                            view.setActivated(true);
                            mSelectedPositions.add(position);
                        } else {
                            Conversation itemClicked = (Conversation) mAdapter.getItem(position);
                            String conversationId = itemClicked.getConversationId();
                            mUser2 = itemClicked.getUser2();

                            mListener.getMessageExtras(conversationId, mUser2);

                        }
                    }

                    //when the user long presses a conversation view, conversation select mode
                    //is actived
                    @Override
                    public void OnItemLongClick(View view, int position) {

                        if (!mIsMultiSelectMode) {
                            mIsMultiSelectMode = true;

                            if (mActionMode == null) {
                                mActionMode = getActivity().startActionMode(mActionModeCallBack);
                            }
                        }
//                        multiSelect(view, position);
                        view.setActivated(true);
                    }
                }));


        if (savedInstanceState != null) {
            mSelectedPositions = savedInstanceState.getIntegerArrayList(SELECTED_POSITIONS_KEY);
            reselectViews(mSelectedPositions);
        }

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
            mFab.setVisibility(View.INVISIBLE);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        //handles functionality when the user presses the delete button when in action mode
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    //alert the user of removed conversations
                    Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
                    //remove each selected conversation from firebase for only this particular user
                    for (SelectedConversation selectedConversation : mSelectedConversations) {
                        String selectedId = selectedConversation.getConversation().getConversationId();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                .child("conversations")
                                .child(mUserId)
                                .child(selectedId);
                        reference.removeValue();

                        //update the widget
                        Intent intent = new Intent(getContext(), AppWidget.class);
                        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

                        AppWidgetManager manager = AppWidgetManager.getInstance(getContext());
                        int[] ids = manager.getAppWidgetIds(new ComponentName(getContext()
                                .getPackageName(), AppWidget.class.getName()));
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
            mFab.setVisibility(View.VISIBLE);
        }
    };

    public void reselectViews(ArrayList<Integer> positions) {
        for (Integer position : positions) {
            View view = mLayoutManager.findViewByPosition(position);
            view.setActivated(true);
        }
    }

    //handles selecting multiple conversations
    public void multiSelect(View view, int position) {
        //get the viewholder that was selected
        ConversationViewHolder viewHolder = (ConversationViewHolder) mRecyclerView
                .getChildViewHolder(view);
        //get the conversation that was selected
        Conversation selected = (Conversation) mAdapter.getItem(position);
        //create new selected conversation object
        SelectedConversation selectedConversation = new SelectedConversation(view, viewHolder,
                position, selected);
        //check if the conversation has alrady been selected and thus would need to be de-selected
        if (mActionMode != null) {
            if (mConversationsSelected.contains(selected)) {
                viewHolder.flipAvatar(view);
                view.setActivated(false);
                mConversationsSelected.remove(selected);
                for (Iterator<SelectedConversation> i = mSelectedConversations.listIterator(); i.hasNext();) {
                    Conversation c = i.next().getConversation();
                    if (c.equals(selected)) {
                        i.remove();
                    }
                }
                //if the conversation hasn't already been selected, add it to the list of selected
                //conversations and change the user avatar to a check mark
            } else {
                mConversationsSelected.add(selected);
                mSelectedConversations.add(selectedConversation);
                viewHolder.flipAvatar(view);
                view.setActivated(true);
            }
            //show the number of selected conversations
            mActionMode.setTitle("" + mSelectedConversations.size());
        }
    }

    //de-selects all previously selected conversations for when the user exits the action mode
    //and didn't delete the conversations
    public void deselectAll() {
        for (SelectedConversation selected : mSelectedConversations) {
            selected.getViewHolder().flipAvatar(selected.getSelectedView());
            selected.getSelectedView().setActivated(false);
        }
        mSelectedConversations.clear();
        mConversationsSelected.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putIntegerArrayList(SELECTED_POSITIONS_KEY, mSelectedPositions);
        super.onSaveInstanceState(outState);
    }
}
