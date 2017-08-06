package com.codepath.apps.mysimpletweets.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.utils.RoundedCornersTransformation;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TweetDetailsActivity extends AppCompatActivity {

    private TextView tvScreenNameDt;
    private TextView tvUserNameDt;
    private TextView tvBodyDt;
    private TextView tvCreatedAtDt;
    private TextView tvRetweetsDt;
    private TextView tvLikesDt;
    private ImageView ivProfileImageDt;

    private Tweet currentTweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_details);
        //setSupportActionBar(toolbar);

        currentTweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra("current_tweet"));

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

}
