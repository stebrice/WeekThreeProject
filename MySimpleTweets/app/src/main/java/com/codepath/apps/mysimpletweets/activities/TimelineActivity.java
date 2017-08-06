package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.adapters.TweetsAdapter;
import com.codepath.apps.mysimpletweets.customlistener.EndlessRecyclerViewScrollListener;
import com.codepath.apps.mysimpletweets.fragments.ComposeFragment;
import com.codepath.apps.mysimpletweets.fragments.ComposeFragment.ComposeDialogListener;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.utils.ItemClickSupport;
import com.codepath.apps.mysimpletweets.utils.SimpleDividerItemDecoration;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity implements ComposeDialogListener{

    private SwipeRefreshLayout swipeContainer;
    private FloatingActionButton fabCompose;
    private User user;
    private TwitterClient client;
    private Tweet newTweet;
    private ArrayList<Tweet> tweets;
    //private TweetsArrayAdapter aTweets;
    private TweetsAdapter aTweets;
    //private ListView lvTweets;
    private RecyclerView rvTweets;
    private long sinceID = 1;
    private long maxID = 1;
    private final int REQUEST_CODE = 10;
    Toolbar toolbar;

    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        //lvTweets = (ListView) findViewById(R.id.lvTweets);
        rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        tweets = new ArrayList<>();
        //aTweets = new TweetsArrayAdapter(this, tweets);
        aTweets = new TweetsAdapter(this, tweets);

        rvTweets.addItemDecoration(new SimpleDividerItemDecoration(this));

        rvTweets.setAdapter(aTweets);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);
        // That's all!
        client = TwitterApplication.getRestClient();
        populateTimeline();

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (maxID > 1){
                    populateTimelineFromMaxID(maxID);
                }
                return;
            }
        };
        rvTweets.addOnScrollListener(scrollListener);
        //PULL TO REFRESH
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                populateTimelineFromSinceID(sinceID);
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // get current user Account infos
        client.getAccountInformation(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //Toast.makeText(getApplicationContext(),"SUCCESS!", Toast.LENGTH_LONG).show();
                user = User.fromJSON(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(),errorResponse.toString(), Toast.LENGTH_LONG).show();
            }
        });

        fabCompose = (FloatingActionButton) findViewById(R.id.fabCompose);
        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCompose();
            }
        });

        ItemClickSupport.addTo(rvTweets).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Tweet currentTweet = aTweets.getItem(position);
                        showTweetDetails(currentTweet);
                        //Toast.makeText(getApplicationContext(), "ITEM CLICKED AT POSITION " + String.valueOf(position), Toast.LENGTH_LONG).show();
                    }
                }
        );

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_compose:
                //Toast.makeText(this, "Action Compose selected", Toast.LENGTH_SHORT).show();
                showCompose();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateTimeline(){
        client.getHomeTimeline(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                //Toast.makeText(getApplicationContext(),"SUCCESS!", Toast.LENGTH_LONG).show();
                ArrayList<Tweet> newItems = Tweet.fromJSONArray(response);
                int currentSize = aTweets.getItemCount();
                tweets.addAll(newItems);
                //aTweets.clear();
                //aTweets.addAll(tweets);
                aTweets.notifyItemRangeInserted(currentSize, newItems.size());
                maxID = tweets.get(tweets.size()-1).getUid() - 1;
                sinceID = tweets.get(0).getUid();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(),errorResponse.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateTimelineFromMaxID(long maxToUse){
        client.getHomeTimelineFromMaxID(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                //Toast.makeText(getApplicationContext(),"SUCCESS!", Toast.LENGTH_LONG).show();
                //tweets = Tweet.fromJSONArray(response);
                int currentSize = aTweets.getItemCount();
                ArrayList<Tweet> newItems = Tweet.fromJSONArray(response);
                tweets.addAll(newItems);

                //aTweets.addAll(tweets);
                aTweets.notifyItemRangeInserted(currentSize, newItems.size());
                maxID = tweets.get(tweets.size()-1).getUid() - 1;
                sinceID = tweets.get(0).getUid();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(),errorResponse.toString(), Toast.LENGTH_LONG).show();
            }
        }, maxToUse);
    }

    private void populateTimelineFromSinceID(long sinceToUse){
        client.getHomeTimelineFromSinceID(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                //Toast.makeText(getApplicationContext(),"SUCCESS!", Toast.LENGTH_LONG).show();
                //tweets = Tweet.fromJSONArray(response);
                int currentSize = 0;
                ArrayList<Tweet> newItems = Tweet.fromJSONArray(response);
                tweets.addAll(newItems);

                //aTweets.addAll(tweets);
                aTweets.notifyItemRangeInserted(currentSize, newItems.size());
                rvTweets.scrollToPosition(0);   // index 0 position
                maxID = tweets.get(tweets.size()-1).getUid() - 1;
                sinceID = tweets.get(0).getUid();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(),errorResponse.toString(), Toast.LENGTH_LONG).show();
            }
        }, sinceToUse);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Extract name value from result extras
            newTweet = (Tweet) Parcels.unwrap(data.getParcelableExtra("new_tweet"));
            insertNewTweetAndUpdateFeed(); //INSERT NEW TWEET AT CORRECT POSITION
            return;
        }
    }

    private void insertNewTweetAndUpdateFeed(){
        int indexToUse = 0;
        for (int i = 0; i < aTweets.getItemCount(); i++){
            Tweet retrievedTweet = aTweets.getItem(i);
            if (retrievedTweet.getUid() < newTweet.getUid()){
                indexToUse = i;
                break;
            }
        }
        //aTweets.insert(newTweet,indexToUse);
        tweets.add(indexToUse,newTweet);
        aTweets.notifyItemInserted(indexToUse);
    }

    public void showCompose() {
//        Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
//        i.putExtra("user", Parcels.wrap(user));
//        startActivityForResult(i, REQUEST_CODE);

        FragmentManager fm = getSupportFragmentManager();
        ComposeFragment composeFragment = ComposeFragment.newInstance(user);
        composeFragment.show(fm, "fragment_compose");
    }

    public void showTweetDetails(Tweet tweetToUse) {
        Intent i = new Intent(TimelineActivity.this, TweetDetailsActivity.class);
        i.putExtra("current_tweet", Parcels.wrap(tweetToUse));
        //startActivityForResult(i, REQUEST_CODE);
        startActivity(i);
    }


    @Override
    public void onFinishComposeDialog(Tweet tweetDefined) {
        this.newTweet = tweetDefined;
        if (newTweet.getUid() > 0)
            insertNewTweetAndUpdateFeed(); //INSERT NEW TWEET AT CORRECT POSITION
        return;
    }

}
