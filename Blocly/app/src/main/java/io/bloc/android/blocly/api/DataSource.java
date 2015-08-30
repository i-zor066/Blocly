package io.bloc.android.blocly.api;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.BuildConfig;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.api.model.database.DatabaseOpenHelper;
import io.bloc.android.blocly.api.model.database.table.RssFeedTable;
import io.bloc.android.blocly.api.model.database.table.RssItemTable;
import io.bloc.android.blocly.api.network.GetFeedsNetworkRequest;

/**
 * Created by igor on 13/8/15.
 */
public class DataSource {
    private DatabaseOpenHelper databaseOpenHelper;
    private RssFeedTable rssFeedTable;
    private RssItemTable rssItemTable;
    private List<RssFeed> feeds;
    private List<RssItem> items;

    public DataSource() {
        rssFeedTable = new RssFeedTable();
        rssItemTable = new RssItemTable();
        databaseOpenHelper = new DatabaseOpenHelper(BloclyApplication.getSharedInstance(),
                rssFeedTable, rssItemTable);
        feeds = new ArrayList<RssFeed>();
        items = new ArrayList<RssItem>();
        createFakeData();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG && false) {
                    BloclyApplication.getSharedInstance().deleteDatabase("blocly_db");
                }
                SQLiteDatabase writableDatabase = databaseOpenHelper.getWritableDatabase();
                List<GetFeedsNetworkRequest.FeedResponse> responseFeeds =  new GetFeedsNetworkRequest("http://feeds.feedburner.com/androidcentral?format=xml").performRequest();

                for (GetFeedsNetworkRequest.FeedResponse feedResponse : responseFeeds) {

                    String feedTitle = feedResponse.channelTitle;

                    Cursor cursor = writableDatabase.rawQuery("SELECT * FROM rss_feeds WHERE title ='" + feedTitle + "'", null);

                    if (cursor.getCount() <= 0) {
                        String feedDescription = feedResponse.channelDescription;
                        String channelURL = feedResponse.channelURL;
                        String feedUrlString = feedResponse.channelFeedURL;

                        ContentValues values = new ContentValues();

                        values.put("link", channelURL);
                        values.put("title", feedTitle);
                        values.put("description", feedDescription);
                        values.put("feed_url", feedUrlString);

                        writableDatabase.insert("rss_feeds", null, values );
                    }

                    cursor.close();

                    List<GetFeedsNetworkRequest.ItemResponse> channelItems = feedResponse.channelItems;

                    for (GetFeedsNetworkRequest.ItemResponse itemResponse : channelItems) {

                        String guid = itemResponse.itemGUID;

                        Cursor cursor2 = writableDatabase.rawQuery("SELECT * FROM rss_items WHERE guid ='" + guid + "'", null);

                        if (cursor2.getCount() <= 0) {


                            String title = itemResponse.itemTitle;
                            String description = itemResponse.itemDescription;
                            String url = itemResponse.itemURL;
                            String imageUrl = itemResponse.itemEnclosureURL;
                            String itemEnclosureMIMEType = itemResponse.itemEnclosureMIMEType;
                            long rssFeedId = 0;

                            long datePublished = System.currentTimeMillis();
                            DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z", Locale.ENGLISH);
                            try {
                                datePublished = dateFormat.parse(itemResponse.itemPubDate).getTime();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            boolean favorite = false;
                            boolean archived = false;

                            ContentValues values = new ContentValues();

                            values.put("link", url);
                            values.put("title", title);
                            values.put("description", description);
                            values.put("guid", guid);
                            values.put("pub_date", datePublished);
                            values.put("enclosure", imageUrl);
                            values.put("mime_type", itemEnclosureMIMEType);
                            values.put("rss_feed", rssFeedId);
                            values.put("is_favorite", favorite);
                            values.put("is_archived", archived);

                            writableDatabase.insert("rss_items", null, values);
                        }

                        cursor2.close();

                    }

                }


            }


        }).start();
    }

    public List<RssFeed> getFeeds() {
        return feeds;
    }

    public List<RssItem> getItems() {
        return items;
    }



    void createFakeData() {
        feeds.add(new RssFeed("My Favorite Feed",
                "This feed is just incredible, I can't even begin to tell you…",
                "http://favoritefeed.net", "http://feeds.feedburner.com/favorite_feed?format=xml"));
        for (int i = 0; i < 10; i++) {
            items.add(new RssItem(String.valueOf(i),
                    BloclyApplication.getSharedInstance().getString(R.string.placeholder_headline) + " " + i,
                    BloclyApplication.getSharedInstance().getString(R.string.placeholder_content),
                    "http://favoritefeed.net?story_id=an-incredible-news-story",
                    "http://rs1img.memecdn.com/silly-dog_o_511213.jpg",
                    0, System.currentTimeMillis(), false, false));
        }
    }


}
