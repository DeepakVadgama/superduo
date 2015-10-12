package barqsoft.footballscores;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LatestScoreIntentService extends IntentService {

    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 11;
    public static final int COL_ID = 10;
    public static final int COL_HOME_CREST = 8;
    public static final int COL_AWAY_CREST = 9;
    public static final int COL_MATCHTIME = 2;

    public LatestScoreIntentService() {
        super("LatestScoreIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                ScoreWidgetProvider.class));


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", getResources().getConfiguration().locale);
        final String date = formatter.format(new Date(System.currentTimeMillis()));
        Cursor cursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null, new String[]{date}, null);
        if (cursor == null) {
            return;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        // TODO: Find the latest completed match.

        // Perform this loop procedure for each widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_small;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setTextViewText(R.id.home_name, cursor.getString(COL_HOME));
            views.setTextViewText(R.id.away_name, cursor.getString(COL_AWAY));
            views.setTextViewText(R.id.data_textview, cursor.getString(COL_MATCHTIME));
            views.setTextViewText(R.id.score_textview, Utilies.getScores(cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));
            views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(cursor.getString(COL_HOME)));
            views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(cursor.getString(COL_AWAY)));

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, getString(R.string.cd_todays_latest_match_score));
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.icon, description);
    }

}
