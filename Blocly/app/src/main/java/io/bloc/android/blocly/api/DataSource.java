package io.bloc.android.blocly.api;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssFeed;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.api.network.GetFeedsNetworkRequest;

/**
 * Created by igor on 13/8/15.
 */
public class DataSource {
    private List<RssFeed> feeds;
    private List<RssItem> items;

    public DataSource() {
        feeds = new ArrayList<RssFeed>();
        items = new ArrayList<RssItem>();
        //createFakeData();
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GetFeedsNetworkRequest.FeedResponse> responseFeeds =  new GetFeedsNetworkRequest("http://feeds.feedburner.com/androidcentral?format=xml").performRequest();

                for (GetFeedsNetworkRequest.FeedResponse feedResponse : responseFeeds) {

                    String feedTitle = feedResponse.channelTitle;
                    String feedDescription = feedResponse.channelDescription;
                    String channelURL = feedResponse.channelURL;
                    String feedUrlString = feedResponse.channelFeedURL;

                    List<GetFeedsNetworkRequest.ItemResponse> channelItems = feedResponse.channelItems;

                    for (GetFeedsNetworkRequest.ItemResponse itemResponse : channelItems) {
                        String guid = itemResponse.itemGUID;
                        String title = itemResponse.itemTitle;
                        String description = itemResponse.itemDescription;
                        String url = itemResponse.itemURL;
                        String imageUrl = itemResponse.itemEnclosureURL;
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
                        items.add(new RssItem(guid, title, description, url, imageUrl, rssFeedId, datePublished, favorite, archived));


                    }
                    feeds.add(new RssFeed(feedTitle, feedDescription, channelURL, feedUrlString));

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
