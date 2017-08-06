package com.codepath.apps.mysimpletweets.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.utils.RoundedCornersTransformation;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

/**
 * Created by STEPHAN987 on 8/5/2017.
 */

public class ComposeFragment extends DialogFragment {
    private User user;
    private TwitterClient client;
    private Tweet lastTweet;


    ImageButton ibCancel;
    ImageView ivProfileImageTw;
    Button btTweet;
    EditText etCompose;
    TextView tvMessage;
    TextView tvUserNameCompose;
    TextView tvScreenNameCompose;
    TextView tvRemainingChars;
    long currentCharCount = 0;
    final long maxChars = 140;
    long charsLeft = 140;


    public interface ComposeDialogListener {
        void onFinishComposeDialog(Tweet tweetDefined);
    }

    public ComposeFragment() {
    }

    public static ComposeFragment newInstance(User user) {
        ComposeFragment ComposeFragment = new ComposeFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", Parcels.wrap(user));
        ComposeFragment.setArguments(args);
        return ComposeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        client = TwitterApplication.getRestClient();
        setupViews(view);

        getDialog().setTitle("COMPOSE TWEET");
    }

    private void setupViews(View viewToUse){
        ibCancel = (ImageButton) viewToUse.findViewById(R.id.ibCancel);
        ivProfileImageTw = (ImageView) viewToUse.findViewById(R.id.ivProfileImageTw);
        btTweet = (Button) viewToUse.findViewById(R.id.btTweet);
        etCompose = (EditText) viewToUse.findViewById(R.id.etCompose);
        tvMessage = (TextView) viewToUse.findViewById(R.id.tvMessage);
        tvUserNameCompose = (TextView) viewToUse.findViewById(R.id.tvUserNameCompose);
        tvScreenNameCompose = (TextView) viewToUse.findViewById(R.id.tvScreenNameCompose);
        tvRemainingChars = (TextView) viewToUse.findViewById(R.id.tvRemainingChars);
        tvRemainingChars.setText(String.valueOf(maxChars));
        user = (User) Parcels.unwrap( getArguments().getParcelable("user"));

        btTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPostTweet(v);
            }
        });

        ibCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelTweet(v);
            }
        });
        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentCharCount = s.toString().length();
                charsLeft = maxChars - currentCharCount;

                if(charsLeft < 0)
                {
                    btTweet.setEnabled(false);
                    tvRemainingChars.setTextColor(Color.RED);
                    tvMessage.setText("Maximum 140 characters allowed!");
                }
                else
                {
                    btTweet.setEnabled(true);
                    tvRemainingChars.setTextColor(Color.BLACK);
                    tvMessage.setText("");
                }
                tvRemainingChars.setText(String.valueOf(charsLeft));
            }
        });

        tvScreenNameCompose.setText(user.getScreenName());
        tvUserNameCompose.setText(user.getName());
        //Picasso.with(getApplicationContext()).load(user.getProfileImageUrl()).transform(new RoundedCornersTransformation(5,5)).into(ivProfileImageTw);
        Glide.with(this).load(user.getProfileImageUrl()).apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(getActivity().getApplicationContext(),5,5))).into(ivProfileImageTw);

    }

    public void onCancelTweet(View view) {
        lastTweet = new Tweet("",0,null,"",0,0);
        ComposeDialogListener composeDialogListener = (ComposeDialogListener) getActivity();
        composeDialogListener.onFinishComposeDialog(lastTweet);

        // Close the dialog and return back to the parent activity
        dismiss();
    }

    public void onPostTweet(View view) {
        //Toast.makeText(getApplicationContext(),"Post attempt", Toast.LENGTH_LONG).show();
        client.postStatusUpdate(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                lastTweet = Tweet.fromJSON(response);

                ComposeDialogListener composeDialogListener = (ComposeDialogListener) getActivity();
                composeDialogListener.onFinishComposeDialog(lastTweet);

                // Close the dialog and return back to the parent activity
                dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getContext(),errorResponse.toString(), Toast.LENGTH_LONG).show();
            }
        }, etCompose.getText().toString());

    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }
}
