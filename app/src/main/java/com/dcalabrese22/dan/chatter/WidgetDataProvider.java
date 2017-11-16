package com.dcalabrese22.dan.chatter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.dcalabrese22.dan.chatter.Objects.Conversation;
import com.dcalabrese22.dan.chatter.Objects.WidgetListItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by dan on 10/9/17.
 */

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext = null;
    private ArrayList<WidgetListItem> mWidgetListItems = new ArrayList<>();
    private List<String> fakeData = new ArrayList<>();
    private AppWidgetManager mManager;
    private int[] mWidgetIds;
    private CountDownLatch mLatch;
    private String mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private  DatabaseReference mReference = FirebaseDatabase.getInstance().getReference()
            .child("conversations")
            .child(mUserId);

    public static final String CONVERSATION_FRAGMENT_VALUE = "conversation_fragment";
    public static final String WIDGET_CONVERSATION_ID_EXTRA = "conversation_id_extra";

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
        mManager = AppWidgetManager.getInstance(mContext);
        mWidgetIds = mManager.getAppWidgetIds(new ComponentName(mContext.getPackageName(), AppWidget.class.getName()));

    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        try {
            populateWidgetListView();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mWidgetListItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews view = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_conversation);
        Intent fillInIntent = new Intent();
        WidgetListItem listItem = mWidgetListItems.get(mWidgetListItems.size()-1-position);
        view.setTextViewText(R.id.widget_conversation_last_message,
                listItem.getLastMessage());
        view.setTextViewText(R.id.widget_conversation_user, listItem.getSender());
        fillInIntent.putExtra(AppWidget.WIDGET_INTENT_EXTRA, CONVERSATION_FRAGMENT_VALUE);
        fillInIntent.putExtra(WIDGET_CONVERSATION_ID_EXTRA, listItem.getConversationId());
        view.setOnClickFillInIntent(R.id.widget_conversation_row, fillInIntent);

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    public void makeFakeData() throws InterruptedException{
        mWidgetListItems.clear();
        for (int i = 0; i < 10; i++) {
            String value = String.valueOf(i);
            mWidgetListItems.add(new WidgetListItem(value, value, value));
        }
    }

    public void populateWidgetListView() throws InterruptedException {
        mWidgetListItems.clear();
        mLatch = new CountDownLatch(10);
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    WidgetListItem widgetListItem = new WidgetListItem();
                    Conversation conversation = child.getValue(Conversation.class);
                    widgetListItem.setSender(conversation.getUser2());
                    widgetListItem.setLastMessage(conversation.getLastMessage());
                    widgetListItem.setConversationId(conversation.getConversationId());
                    mWidgetListItems.add(widgetListItem);
                    mLatch.countDown();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mLatch.countDown();
            }
        });
        mLatch.await(7, TimeUnit.SECONDS);
    }
}
