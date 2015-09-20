package io.bloc.android.blocly.ui.activity;

import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.CheckBox;

import io.bloc.android.blocly.BloclyApplication;
import io.bloc.android.blocly.R;
import io.bloc.android.blocly.api.model.RssItem;
import io.bloc.android.blocly.ui.adapter.ItemAdapter;

/**
 * Created by igor on 18/9/15.
 */
public class ActivityTest extends ActivityInstrumentationTestCase2<BloclyActivity> {

    BloclyActivity testBloclyActivity;
    RecyclerView testRecyclerView;


    public ActivityTest() {
        super(BloclyActivity.class);
    }

   @Override
    protected void setUp() throws Exception {
       super.setUp();

       setActivityInitialTouchMode(true);

       testBloclyActivity = getActivity();
       testRecyclerView = (RecyclerView) testBloclyActivity.findViewById(R.id.rv_fragment_rss_list);

   }

    public void testPreconditions() {
        assertNotNull("testBloclyActivity is null", testBloclyActivity );
        assertNotNull("testRecyclerView is null", testRecyclerView);
    }

    public void testFavouriteStarPresence() {
        int childCount = testRecyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            assertNotNull("Favourite star not present", testRecyclerView.getChildAt(i).findViewById(R.id.cb_rss_item_favorite_star));
        }

    }

    public void testFavouriteStarWorking() {
        int childCount = testRecyclerView.getChildCount();
        ItemAdapter itemAdapter = (ItemAdapter) testRecyclerView.getAdapter();
        assertNotNull("itemAdapter is null", itemAdapter);

        for (int i = 0; i < childCount; i++) {
            RssItem rssItem = itemAdapter.getDataSource().getRssItem(itemAdapter, i);
            CheckBox testFavStar = (CheckBox) testRecyclerView.getChildAt(i).findViewById(R.id.cb_rss_item_favorite_star);
            assertNotNull("testFavStar is null", testFavStar);
            assertFalse("rssItem IS favourite", rssItem.isFavorite());
            assertEquals("Variable not equal to DB", rssItem.isFavorite(), BloclyApplication.getSharedDataSource().getFavouriteStatus(rssItem));
            TouchUtils.clickView(this, testFavStar);
            assertTrue("rssItem IS NOT favourite", rssItem.isFavorite());
            assertEquals("Variable not equal to DB", rssItem.isFavorite(), BloclyApplication.getSharedDataSource().getFavouriteStatus(rssItem));

        }

    }

}
