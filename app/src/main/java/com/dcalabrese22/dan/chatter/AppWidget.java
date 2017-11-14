package com.dcalabrese22.dan.chatter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.dcalabrese22.dan.chatter.services.WidgetService;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    public static final String WIDGET_INTENT_EXTA = "widget_intent_extra";
    public static final String NEW_MESSAGE_FRAGMENT_VALUE = "new_message_fragment";
    public static final String CONVERSATION_FRAGMENT_VALUE = "conversation_fragment";
    private String mConversationId;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent newMessageIntent = new Intent(context, MainActivity.class);
        newMessageIntent.setData(Uri.parse(newMessageIntent.toUri(Intent.URI_INTENT_SCHEME)));
        newMessageIntent.putExtra(WIDGET_INTENT_EXTA,  NEW_MESSAGE_FRAGMENT_VALUE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                newMessageIntent, 0);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setOnClickPendingIntent(R.id.widget_new_message, pendingIntent);
        setRemoteAdapter(context, views);
        // Instruct the widget manager to update the widget

//        Intent chatIntent = new Intent(context, MainActivity.class);
//        PendingIntent chatPendingIntent = PendingIntent.getBroadcast(context, 0,
//                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        views.setPendingIntentTemplate(R.id.lv_widget_conversations, chatPendingIntent);
        
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private static void setRemoteAdapter(Context context, RemoteViews views) {
        views.setRemoteAdapter(R.id.lv_widget_conversations,
                new Intent(context, WidgetService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mConversationId = intent
                .getStringExtra(WidgetDataProvider.WIDGET_CONVERSATION_ID_EXTRA);
        Intent chatIntent = new Intent(context, MainActivity.class);
        chatIntent.putExtra(WidgetDataProvider.WIDGET_CONVERSATION_ID_EXTRA, mConversationId);
        context.startActivity(chatIntent);

        super.onReceive(context, intent);
    }
}

