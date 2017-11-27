package com.dcalabrese22.dan.chatter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.dcalabrese22.dan.chatter.Objects.Conversation;
import com.dcalabrese22.dan.chatter.Objects.User;
import com.dcalabrese22.dan.chatter.Objects.WidgetListItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


//Class that provides the data for the widget
public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext = null;
    private ArrayList<WidgetListItem> mWidgetListItems = new ArrayList<>();
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
        mWidgetListItems.clear();
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
        try {
            view.setImageViewBitmap(R.id.widget_user_avatar, getUser2Avatar(listItem.getSender()));
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    /**
     * Populates a listview in the widget
     * @throws InterruptedException
     */
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

    /**
     * Gets the profile picture of the other user in the conversation
     * @param user2 Name of the other user
     * @return Bitmap of the image
     * @throws InterruptedException
     */
    public Bitmap getUser2Avatar(String user2) throws InterruptedException {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        final ArrayList<Bitmap> images = new ArrayList<>();
        mLatch = new CountDownLatch(1);
        reference.child("users").orderByChild("userName").equalTo(user2)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String userId = dataSnapshot.getKey();
                        User user2 = dataSnapshot.getValue(User.class);
                        Boolean hasUserImage = user2.getHasUserImage();
                        if (hasUserImage) {
                            StorageReference storageImagesRef =
                                    storage.getReference("images/" + userId +
                                            "/avatar.jpg");
                            final long ONE_MEGABYTE = 1024 * 1024;
                            storageImagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    images.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                    mLatch.countDown();
                                }
                            });
                        } else {
                            StorageReference storageImagesRef =
                                    storage.getReference("images/default/avatar.jpg");
                            final long ONE_MEGABYTE = 1024 * 1024;
                            storageImagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    images.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                    mLatch.countDown();
                                }
                            });

                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        mLatch.await();
        return getCircleBitmap(images.get(0));

    }

    /**
     * Creates a circular bitmap from a square one since widgets can't use custom imageviews
     * @param bitmap Bitmap to modify
     * @return New bitmap that is circular
     */
    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}
