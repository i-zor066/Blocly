package io.bloc.android.blocly.api.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by igor on 13/8/15.
 */
public class RssItem extends Model implements Parcelable {
    private String guid;
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private long rssFeedId;
    private long datePublished;
    private boolean favorite;
    private boolean archived;

    public RssItem(long rowId, String guid, String title, String description, String url, String imageUrl, long rssFeedId, long datePublished, boolean favorite, boolean archived) {
        super(rowId);
        this.guid = guid;
        this.title = title;
        this.description = description;
        this.url = url;
        this.imageUrl = imageUrl;
        this.rssFeedId = rssFeedId;
        this.datePublished = datePublished;
        this.favorite = favorite;
        this.archived = archived;
    }

    public String getGuid() {
        return guid;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getRssFeedId() {
        return rssFeedId;
    }

    public long getDatePublished() {
        return datePublished;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public boolean isArchived() {
        return archived;
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
        bundle.putString("guid", guid);
        bundle.putString("title", title);
        bundle.putString("description", description);
        bundle.putString("url", url);
        bundle.putString("imageUrl", imageUrl);
        bundle.putLong("rssFeedId", rssFeedId);
        bundle.putLong("datePublished", datePublished);
        bundle.putBoolean("favorite", favorite);
        bundle.putBoolean("archived", archived);

        dest.writeBundle(bundle);

    }

    public static final Parcelable.Creator<RssItem> CREATOR = new Parcelable.Creator<RssItem>() {
        public RssItem createFromParcel(Parcel source){
            Bundle bundle = source.readBundle();

            return new RssItem(bundle.getLong("rowId"),
                    bundle.getString("guid"),
                    bundle.getString("title"),
                    bundle.getString("description"),
                    bundle.getString("url"),
                    bundle.getString("imageUrl"),
                    bundle.getLong("rssFeedId"),
                    bundle.getLong("datePublished"),
                    bundle.getBoolean("favorite"),
                    bundle.getBoolean("archived"));
        }

        public RssItem[] newArray(int size) {
            return new RssItem[size];
        }
    };

}
