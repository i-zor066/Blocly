package io.bloc.android.blocly.api.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by igor on 13/8/15.
 */
public class RssFeed extends Model implements Parcelable {
    private String title;
    private String description;
    private String siteUrl;
    private String feedUrl;

    public RssFeed(long rowId, String title, String description, String siteUrl, String feedUrl) {
        super(rowId);
        this.title = title;
        this.description = description;
        this.siteUrl = siteUrl;
        this.feedUrl = feedUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();

        long rowId = super.getRowId();

        bundle.putLong("rowId", rowId);
        bundle.putString("title", title);
        bundle.putString("description", description);
        bundle.putString("siteUrl", siteUrl);
        bundle.putString("feedUrl", feedUrl);

        dest.writeBundle(bundle);

    }

    public static final Parcelable.Creator<RssFeed> CREATOR = new Parcelable.Creator<RssFeed>() {
        public RssFeed createFromParcel(Parcel source){
            Bundle bundle = source.readBundle();

            return new RssFeed(bundle.getLong("rowId"),
                    bundle.getString("title"),
                    bundle.getString("description"),
                    bundle.getString("siteUrl"),
                    bundle.getString("feedUrl"));
        }

        public RssFeed[] newArray(int size) {
            return new RssFeed[size];
        }
    };


}
