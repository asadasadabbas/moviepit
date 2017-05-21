package com.moviepit;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.moviepit.adapter.CreditListAdapter;
import com.moviepit.model.CharacterModel;
import com.moviepit.model.MovieDetailModel;
import com.moviepit.utilities.APIManager;
import com.moviepit.utilities.CircleProgressBar;
import com.moviepit.utilities.GlobalConfig;
import com.moviepit.utilities.RequestCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MovieDetail extends AppCompatActivity {

    private int movieID;
    private Context mContext;
    private ProgressBar progressBar;
    private String TAG = "MovieDetail";
    private MovieDetailModel movieModel = new MovieDetailModel();
    private ArrayList<CharacterModel> characterArrayList = new ArrayList<CharacterModel>();
    private CreditListAdapter creditListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = MovieDetail.this;

        ImageView imageView = (ImageView) findViewById(R.id.background_image);
        CircleProgressBar circleProgressBar = (CircleProgressBar) findViewById(R.id.custom_progressBar);
        TextView textView = (TextView) findViewById(R.id.percentage_text);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        //Get content from previous activity
        Intent n = getIntent();
        String url = n.getStringExtra("image_url");
        String movieTitle = n.getStringExtra("movie_title");
        movieID = n.getIntExtra("movie_id", 0);
        float percentage = n.getFloatExtra("percentage", 0f);
        String genre = n.getStringExtra("genre");
        collapsingToolbarLayout.setTitle(movieTitle);

        movieModel.setPosterPath(url);
        movieModel.setTitle(movieTitle);
        movieModel.setGenre(genre);

        circleProgressBar.setProgressWithAnimation(percentage);
        animateTextView(0, (int) percentage, textView);

        Glide.with(mContext)
                .load(url)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_layout);
        progressBar.setVisibility(View.VISIBLE);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_horizontal);
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.movie_coordinator_layout);
        creditListAdapter = new CreditListAdapter(mContext, characterArrayList);
        LinearLayoutManager rLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(rLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(creditListAdapter);
        if (GlobalConfig.isConnectedToInternet(mContext, coordinatorLayout)) {
            loadContent();
        } else {
            if (progressBar.getVisibility() == View.VISIBLE)
                progressBar.setVisibility(View.GONE);
        }
    }

    //Load movie details from server
    private void loadContent() {
        String url = GlobalConfig.BASE_URL + GlobalConfig.MOVIES + "/" + movieID +
                GlobalConfig.API_KEY_REFERENCE;

        JSONObject jsonObject = new JSONObject();
        APIManager.jsonObjectVolleyRequest(mContext, url, "GET", jsonObject);
        APIManager.setOnAPICallbackListener(new RequestCallback.APIRequestCallback() {
            @Override
            public void onSuccessJSONResponse(JSONObject response) {
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.GONE);
                setMovieDetailFromResponse(response);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.GONE);
                APIManager.handleErrorResponseOfRequest(mContext, error, "Server Error", TAG);
            }
        });
    }

    //Get Movie detail from server
    private void setMovieDetailFromResponse(JSONObject jsonMovieObject) {
        try {
            String blank = "";
            if (jsonMovieObject.has("status_message") && !jsonMovieObject.isNull("status_message")) {
                String error;
                error = jsonMovieObject.getString("status_message");
                Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
            }

            if (jsonMovieObject.has("backdrop_path") && !jsonMovieObject.isNull("backdrop_path")) {
                movieModel.setBackdropPath(GlobalConfig.BASE_IMAGE_URL + jsonMovieObject.getString("backdrop_path"));
            } else
                movieModel.setBackdropPath(blank);

            if (jsonMovieObject.has("id") && !jsonMovieObject.isNull("id")) {
                movieModel.setId(jsonMovieObject.getInt("id"));
            } else
                movieModel.setId(0);

            if (jsonMovieObject.has("overview") && !jsonMovieObject.isNull("overview")) {
                movieModel.setOverview(jsonMovieObject.getString("overview"));
            } else
                movieModel.setOverview(blank);

            if (jsonMovieObject.has("release_date") && !jsonMovieObject.isNull("release_date")) {
                movieModel.setReleaseDate(jsonMovieObject.getString("release_date"));
            } else
                movieModel.setReleaseDate(blank);

            if (jsonMovieObject.has("budget") && !jsonMovieObject.isNull("budget")) {
                movieModel.setMovieBudget(jsonMovieObject.getInt("budget"));
            } else
                movieModel.setMovieBudget(0);

            if (jsonMovieObject.has("revenue") && !jsonMovieObject.isNull("revenue")) {
                movieModel.setRevenue(jsonMovieObject.getInt("revenue"));
            } else
                movieModel.setRevenue(0);

            if (jsonMovieObject.has("runtime") && !jsonMovieObject.isNull("runtime")) {
                movieModel.setRunTime(jsonMovieObject.getInt("runtime"));
            } else
                movieModel.setRunTime(0);

            if (jsonMovieObject.has("homepage") && !jsonMovieObject.isNull("homepage")) {
                movieModel.setHomepage(jsonMovieObject.getString("homepage"));
            } else
                movieModel.setHomepage(blank);

            if (jsonMovieObject.has("original_language") && !jsonMovieObject.isNull("original_language")) {
                movieModel.setOriginalLanguage(jsonMovieObject.getString("original_language"));
            } else
                movieModel.setOriginalLanguage(blank);

            if (jsonMovieObject.has("status") && !jsonMovieObject.isNull("status")) {
                movieModel.setStatus(jsonMovieObject.getString("status"));
            } else
                movieModel.setStatus(blank);

            if (jsonMovieObject.has("vote_average") && !jsonMovieObject.isNull("vote_average")) {
                movieModel.setVoteAverage((float) jsonMovieObject.getDouble("vote_average"));
            } else
                movieModel.setVoteAverage(0f);

            if (jsonMovieObject.has("vote_count") && !jsonMovieObject.isNull("vote_count")) {
                movieModel.setVoteCount(jsonMovieObject.getInt("vote_count"));
            } else
                movieModel.setVoteCount(0);

            if (jsonMovieObject.has("popularity") && !jsonMovieObject.isNull("popularity")) {
                movieModel.setPopularity((float) jsonMovieObject.getDouble("popularity"));
            } else
                movieModel.setPopularity(0f);
            getCreditList();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //get List of Cast
    private void getCreditList() {
        String url = GlobalConfig.BASE_URL + GlobalConfig.MOVIES + "/" + movieID +
                GlobalConfig.CREDITS + GlobalConfig.API_KEY_REFERENCE;

        JSONObject jsonObject = new JSONObject();
        APIManager.jsonObjectVolleyRequest(mContext, url, "GET", jsonObject);
        APIManager.setOnAPICallbackListener(new RequestCallback.APIRequestCallback() {
            @Override
            public void onSuccessJSONResponse(JSONObject response) {
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.GONE);
                setCreditList(response);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.GONE);
                APIManager.handleErrorResponseOfRequest(mContext, error, "Server Error", TAG);
            }
        });
    }

    //set cast list
    private void setCreditList(JSONObject jsonObject) {
        try {
            String blank = "";

            if (jsonObject.has("status_message") && !jsonObject.isNull("status_message")) {
                String error;
                error = jsonObject.getString("status_message");
                Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
            }

            if (jsonObject.has("cast") && !jsonObject.isNull("cast")) {
                JSONArray jsonArray = jsonObject.getJSONArray("cast");
                for (int i = 0; i < jsonArray.length(); i++) {
                    CharacterModel characterModel = new CharacterModel();
                    JSONObject jsonCharacterObject = jsonArray.getJSONObject(i);
                    if (jsonCharacterObject.has("profile_path") && !jsonCharacterObject.isNull("profile_path")) {
                        characterModel.setProfilePath(GlobalConfig.BASE_IMAGE_URL_W185 + jsonCharacterObject.getString("profile_path"));
                    } else
                        characterModel.setProfilePath(blank);

                    if (jsonCharacterObject.has("name") && !jsonCharacterObject.isNull("name")) {
                        characterModel.setOriginalName(jsonCharacterObject.getString("name"));
                    } else
                        characterModel.setOriginalName(blank);

                    if (jsonCharacterObject.has("character") && !jsonCharacterObject.isNull("character")) {
                        characterModel.setCharacterName(jsonCharacterObject.getString("character"));
                    } else
                        characterModel.setCharacterName(blank);

                    characterArrayList.add(characterModel);
                }
            }
            updateUIView();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Update View content once request is loaded
    private void updateUIView() {
        TextView movieOverView = (TextView) findViewById(R.id.movie_overview);
        TextView statusValue = (TextView) findViewById(R.id.status_value);
        TextView originalLanguageValue = (TextView) findViewById(R.id.original_language_value);
        TextView budgetValue = (TextView) findViewById(R.id.budget_value);
        TextView revenueValue = (TextView) findViewById(R.id.revenue_value);
        TextView genreValue = (TextView) findViewById(R.id.genre_value);
        TextView homePageValue = (TextView) findViewById(R.id.homepage_value);

        movieOverView.setText(movieModel.getOverview());
        statusValue.setText(movieModel.getStatus());
        String language = movieModel.getOriginalLanguage();
        if (movieModel.getOriginalLanguage().equals("en")) {
            language = "English";
        }
        originalLanguageValue.setText(language);
        String budget = "$" + formatNumberValue(movieModel.getMovieBudget());
        String revenue = "$" + formatNumberValue(movieModel.getRevenue());
        budgetValue.setText(budget);
        revenueValue.setText(revenue);
        genreValue.setText(movieModel.getGenre());
        homePageValue.setText(movieModel.getHomepage());
        creditListAdapter.notifyDataSetChanged();
    }

    //Format number according to international accepted format
    private String formatNumberValue(int value){
        return NumberFormat.getNumberInstance(Locale.US).format(value);
    }

    //Animate percentage numbers while displaying
    private void animateTextView(int initialValue, int finalValue, final TextView textview) {

        ValueAnimator valueAnimator = ValueAnimator.ofInt(initialValue, finalValue);
        valueAnimator.setDuration(1500);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                String superScr = valueAnimator.getAnimatedValue().toString() + "%";
                textview.setText(superScr);
            }
        });
        valueAnimator.start();

    }

    //Overriding back press for custom animatiom
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}

