package io.bloc.android.blocly.ui.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.DataSource;
import io.bloc.android.blocly.api.model.RssItem;

/**
 * Created by igor on 6/9/15.
 */
public class RssItemDetailFragment extends Fragment implements ImageLoadingListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String BUNDLE_EXTRA_RSS_ITEM = RssItemDetailFragment.class.getCanonicalName().concat(".EXTRA_RSS_ITEM");

    private static String TAG = RssItemDetailFragment.class.getSimpleName();

    public static RssItemDetailFragment detailFragmentForRssItem(RssItem rssItem) {
        Bundle arguments = new Bundle();
        arguments.putLong(BUNDLE_EXTRA_RSS_ITEM, rssItem.getRowId());
        RssItemDetailFragment rssItemDetailFragment = new RssItemDetailFragment();
        rssItemDetailFragment.setArguments(arguments);
        return rssItemDetailFragment;
    }

    ImageView headerImage;
    TextView title;
    TextView content;
    ProgressBar progressBar;
    CheckBox archiveCheckboxTablet;
    CheckBox favoriteCheckboxTablet;
    TextView visitSiteTablet;
    ImageButton shareItemTablet;
    String url;
    RssItem itemDetailFrag;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        Bundle arguments = getArguments();
        if (arguments != null) {
            long rssItemId = arguments.getLong(BUNDLE_EXTRA_RSS_ITEM);
            BloclyApplication.getSharedDataSource().fetchRSSItemWithId(rssItemId, new DataSource.Callback<RssItem>() {
                @Override
                public void onSuccess(RssItem rssItem) {
                    if (getActivity() == null) {
                        return;
                    }
                    title.setText(rssItem.getTitle());
                    content.setText(rssItem.getDescription());
                    ImageLoader.getInstance().loadImage(rssItem.getImageUrl(), RssItemDetailFragment.this);
                    url = rssItem.getUrl();
                    itemDetailFrag = rssItem;
                }

                @Override
                public void onError(String errorMessage) {
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_rss_item_detail, container, false);
        archiveCheckboxTablet = (CheckBox) inflate.findViewById(R.id.fa_rss_item_check_mark);
        favoriteCheckboxTablet = (CheckBox) inflate.findViewById(R.id.fa_rss_item_favorite_star);
        shareItemTablet = (ImageButton) inflate.findViewById(R.id.fa_action_share);
        visitSiteTablet = (TextView) inflate.findViewById(R.id.fa_rss_item_visit_site);
        headerImage = (ImageView) inflate.findViewById(R.id.iv_fragment_rss_item_detail_header);
        progressBar = (ProgressBar) inflate.findViewById(R.id.pb_fragment_rss_item_detail_header);
        title = (TextView) inflate.findViewById(R.id.tv_fragment_rss_item_detail_title);
        content = (TextView) inflate.findViewById(R.id.tv_fragment_rss_item_detail_content);
        archiveCheckboxTablet.setOnCheckedChangeListener(this);
        favoriteCheckboxTablet.setOnCheckedChangeListener(this);
        shareItemTablet.setOnClickListener(this);
        visitSiteTablet.setOnClickListener(this);
        return inflate;
    }

     /*
      * ImageLoadingListener
      */

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        progressBar.animate()
                .alpha(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(getActivity().getResources().getInteger(android.R.integer.config_shortAnimTime))
                .start();
        headerImage.animate()
                .alpha(0f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(getActivity().getResources().getInteger(android.R.integer.config_shortAnimTime))
                .start();
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        progressBar.animate()
                .alpha(0f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(getActivity().getResources().getInteger(android.R.integer.config_shortAnimTime))
                .start();
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        progressBar.animate()
                .alpha(0f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(getActivity().getResources().getInteger(android.R.integer.config_shortAnimTime))
                .start();
        headerImage.setImageBitmap(loadedImage);
        headerImage.animate()
                .alpha(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(getActivity().getResources().getInteger(android.R.integer.config_shortAnimTime))
                .start();
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
    }



    /*
     * OnCheckedChangedListener
     */

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.v(TAG, "Checked changed to: " + isChecked);

    }

    /*
     * OnClick Listener
     */


    @Override
    public void onClick(View view) {
        if (view == visitSiteTablet ) {
            Intent visitSite = new Intent(Intent.ACTION_VIEW, Uri.parse(itemDetailFrag.getUrl()));
            startActivity(visitSite);
        } else {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_TEXT, String.format("%s (%s)", itemDetailFrag.getTitle(), itemDetailFrag.getUrl()));
            share.setType("text/plain");
            Intent chooser = Intent.createChooser(share, getString(R.string.share_chooser_title));
            startActivity(chooser);
        }
    }
}