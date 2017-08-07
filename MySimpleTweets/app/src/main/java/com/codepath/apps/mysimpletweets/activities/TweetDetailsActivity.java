package com.codepath.apps.mysimpletweets.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.fragments.ComposeFragment;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.utils.RoundedCornersTransformation;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TweetDetailsActivity extends AppCompatActivity implements ComposeFragment.ComposeDialogListener{

    private TextView tvScreenNameDt;
    private TextView tvUserNameDt;
    private TextView tvBodyDt;
    private TextView tvCreatedAtDt;
    private TextView tvRetweetsDt;
    private TextView tvLikesDt;
    private ImageView ivProfileImageDt;

    private Tweet currentTweet;
    private Tweet newTweet;
    private User connectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_details);
        //setSupportActionBar(toolbar);
        newTweet = new Tweet("",0,null,"",0,0);
        currentTweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra("current_tweet"));
        connectedUser = (User) Parcels.unwrap(getIntent().getParcelableExtra("connected_user"));

        setupViews();
    }

    private void setupViews() {
        tvScreenNameDt = (TextView) findViewById(R.id.tvScreenNameDt);
        tvUserNameDt = (TextView) findViewById(R.id.tvUserNameDt);
        tvBodyDt = (TextView) findViewById(R.id.tvBodyDt);
        ivProfileImageDt = (ImageView) findViewById(R.id.ivProfileImageDt);
        tvCreatedAtDt = (TextView) findViewById(R.id.tvCreatedAtDt);
        tvRetweetsDt = (TextView) findViewById(R.id.tvRetweetsDt);
        tvLikesDt = (TextView) findViewById(R.id.tvLikesDt);

        tvScreenNameDt.setText(currentTweet.getUser().getScreenName());
        tvUserNameDt.setText(currentTweet.getUser().getName());
        tvBodyDt.setText(currentTweet.getBody());
        tvRetweetsDt.setText(String.valueOf(currentTweet.getRetweetCount()));
        tvLikesDt.setText(String.valueOf(currentTweet.getFavouritesCount()));

        String dateFormatted = "";
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(twitterFormat);
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("h:mm a, dd MMM yy");
        try {
            Log.d("DEBUG", currentTweet.getCreatedAt());
            Date d = (Date) dateFormat.parse(currentTweet.getCreatedAt());
            dateFormatted = dateFormat1.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tvCreatedAtDt.setText(dateFormatted);

        Glide.with(this).load(currentTweet.getUser().getProfileImageUrl()).apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(getApplicationContext(),5,5))).into(ivProfileImageDt);
    }

    public void replyToTweet(View view) {
        showCompose();
    }

    public void showCompose() {
//        Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
//        i.putExtra("user", Parcels.wrap(user));
//        startActivityForResult(i, REQUEST_CODE);
        newTweet = new Tweet("",0,null,"",0,0);
        FragmentManager fm = getSupportFragmentManager();
        ComposeFragment composeFragment = ComposeFragment.newInstance(connectedUser, newTweet, currentTweet);
        composeFragment.show(fm, "fragment_compose");
    }

    @Override
    public void onFinishComposeDialog(Tweet tweetDefined) {
        if (tweetDefined.getUid() > 0){
            Toast.makeText(getApplicationContext(),"Replied successfully to " + currentTweet.getUser().getScreenName(), Toast.LENGTH_LONG).show();
            //    insertNewTweetFromReplyAndUpdateFeed(); //INSERT NEW TWEET AT CORRECT POSITION
            Intent data = new Intent();
            data.putExtra("new_tweet_from_reply", Parcels.wrap(tweetDefined));

            // Activity finished ok, return the data
            setResult(RESULT_OK, data); // set result code and bundle data for response
            finish(); // closes the activity, pass data to parent
        }
        return;
    }

}
