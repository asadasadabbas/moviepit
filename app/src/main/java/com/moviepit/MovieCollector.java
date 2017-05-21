package com.moviepit;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.moviepit.adapter.MovieListAdapter;
import com.moviepit.model.GenreModel;
import com.moviepit.model.MovieModel;
import com.moviepit.utilities.APIManager;
import com.moviepit.utilities.GlobalConfig;
import com.moviepit.utilities.RequestCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MovieCollector extends AppCompatActivity {

    private MovieListAdapter movieListAdapter;
    private ArrayList<MovieModel> movieArrayList = new ArrayList<MovieModel>();
    private ArrayList<GenreModel> genreArrayList = new ArrayList<GenreModel>();
    private Context mContext;
    private CoordinatorLayout coordinatorLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String TAG = "MovieCollector";
    private ProgressBar progressBar, progressLoadMore;
    private LinearLayoutManager rLayoutManager;
    private RecyclerView recyclerViewMovie;
    private static boolean loadingMoreItems = false;
    private static int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_collector);
        mContext = MovieCollector.this;

        recyclerViewMovie = (RecyclerView) findViewById(R.id.recycler_view_movie);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.movie_coordinator_layout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressLoadMore = (ProgressBar) findViewById(R.id.progress_bar_load_more);

        movieListAdapter = new MovieListAdapter(mContext, movieArrayList);
        recyclerViewMovie.setHasFixedSize(true);
        rLayoutManager = new LinearLayoutManager(mContext);
        rLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewMovie.setLayoutManager(rLayoutManager);
        recyclerViewMovie.setAdapter(movieListAdapter);
        refreshView(); // Updating View

        //Swipe refreshing layout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                refreshView();
            }
        });

        recyclerViewMovie.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int firstVisibleItem, visibleItemCount, totalItemCount;

                firstVisibleItem = rLayoutManager.findFirstVisibleItemPosition();
                totalItemCount = rLayoutManager.getItemCount();
                visibleItemCount = recyclerViewMovie.getChildCount();


                if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
                    if (!loadingMoreItems)
                        if (GlobalConfig.isConnectedToInternet(mContext, coordinatorLayout)) {
                            progressLoadMore.setVisibility(View.VISIBLE);
                            configureSettings();
                        } else {
                            if (progressBar.getVisibility() == View.VISIBLE)
                                progressBar.setVisibility(View.GONE);
                        }
                }
            }
        });
    }

    //refresh View
    private void refreshView() {
        if (GlobalConfig.isConnectedToInternet(mContext, coordinatorLayout)) {
            page = 1;
            if (movieArrayList.size() > 0) {
                movieArrayList.clear();
            }
            configureSettings();
        } else {
            if (progressBar.getVisibility() == View.VISIBLE)
                progressBar.setVisibility(View.GONE);
        }
    }

    //Configure load from server
    private void configureSettings() {
        if (GlobalConfig.sharedPreferenceFirstTime(mContext)) {
            collectGenre(); // Need to be called once for a session
        } else {
            getMovieList();
        }
    }

    // Get All Popular Movies
    private void getMovieList() {
        loadingMoreItems = true;
        String url = GlobalConfig.BASE_URL + GlobalConfig.POPULAR_MOVIES +
                GlobalConfig.API_KEY_REFERENCE + GlobalConfig.PAGE_KEY + page;

        JSONObject jsonObject = new JSONObject();
        APIManager.jsonObjectVolleyRequest(mContext, url, "GET", jsonObject);
        APIManager.setOnAPICallbackListener(new RequestCallback.APIRequestCallback() {
            @Override
            public void onSuccessJSONResponse(JSONObject response) {
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.GONE);
                if (progressLoadMore.getVisibility() == View.VISIBLE)
                    progressLoadMore.setVisibility(View.GONE);

                setMovieListForResponse(response);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                loadingMoreItems = false;
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.GONE);
                if (progressLoadMore.getVisibility() == View.VISIBLE)
                    progressLoadMore.setVisibility(View.GONE);
                APIManager.handleErrorResponseOfRequest(mContext, error, "Server Error", TAG);
            }
        });
    }

    // Set Movie into a list
    private void setMovieListForResponse(JSONObject jsonObject) {
        String blank = "";
        try {
            if (jsonObject.has("results") && !jsonObject.isNull("results")) {
                JSONArray jsonArray = jsonObject.getJSONArray("results");
                for (int i = 0; i < jsonArray.length(); i++) {
                    MovieModel movieModel = new MovieModel();
                    JSONObject jsonMovieObject = jsonArray.getJSONObject(i);

                    if (jsonMovieObject.has("id") && !jsonMovieObject.isNull("id")) {
                        movieModel.setId(jsonMovieObject.getInt("id"));
                    } else
                        movieModel.setId(0);

                    if (jsonMovieObject.has("title") && !jsonMovieObject.isNull("title")) {
                        movieModel.setTitle(jsonMovieObject.getString("title"));
                    } else
                        movieModel.setTitle(blank);

                    if (jsonMovieObject.has("overview") && !jsonMovieObject.isNull("overview")) {
                        movieModel.setOverview(jsonMovieObject.getString("overview"));
                    } else
                        movieModel.setOverview(blank);

                    if (jsonMovieObject.has("release_date") && !jsonMovieObject.isNull("release_date")) {
                        movieModel.setReleaseDate(jsonMovieObject.getString("release_date"));
                    } else
                        movieModel.setReleaseDate(blank);

                    if (jsonMovieObject.has("poster_path") && !jsonMovieObject.isNull("poster_path")) {
                        movieModel.setPosterPath(GlobalConfig.BASE_IMAGE_URL + jsonMovieObject.getString("poster_path"));
                    } else
                        movieModel.setPosterPath(blank);

                    if (jsonMovieObject.has("backdrop_path") && !jsonMovieObject.isNull("backdrop_path")) {
                        movieModel.setBackdropPath(GlobalConfig.BASE_IMAGE_URL + jsonMovieObject.getString("backdrop_path"));
                    } else
                        movieModel.setBackdropPath(blank);

                    if (jsonMovieObject.has("genre_ids") && !jsonMovieObject.isNull("genre_ids")) {
                        JSONArray jsonGenreIds = jsonMovieObject.getJSONArray("genre_ids");
                        String genreText = "";
                        for (int j = 0; j < jsonGenreIds.length(); j++) {
                            if (j == jsonGenreIds.length() - 1) {
                                genreText = genreText + getGenreFromId(jsonGenreIds.getInt(j));
                            } else {
                                genreText = genreText + getGenreFromId(jsonGenreIds.getInt(j)) + ",";
                            }
                        }
                        movieModel.setGenre(genreText);
                    } else
                        movieModel.setGenre(blank);

                    if (jsonMovieObject.has("vote_count") && !jsonMovieObject.isNull("vote_count")) {
                        movieModel.setVoteCount(jsonMovieObject.getInt("vote_count"));
                    } else
                        movieModel.setVoteCount(0);

                    if (jsonMovieObject.has("popularity") && !jsonMovieObject.isNull("popularity")) {
                        movieModel.setPopularity((float) jsonMovieObject.getDouble("popularity"));
                    } else
                        movieModel.setPopularity(0f);

                    if (jsonMovieObject.has("vote_average") && !jsonMovieObject.isNull("vote_average")) {
                        movieModel.setVoteAverage((float) jsonMovieObject.getDouble("vote_average"));
                    } else
                        movieModel.setVoteAverage(0f);
                    movieArrayList.add(movieModel);
                }
            } else {
                Toast.makeText(mContext, "No items available", Toast.LENGTH_SHORT).show();
            }
            movieListAdapter.notifyDataSetChanged();
            page++;
            swipeRefreshLayout.setRefreshing(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadingMoreItems = false;
    }

    private String getGenreFromId(int genreId) {
        String genreName = "";
        for (int i = 0; i < genreArrayList.size(); i++) {
            GenreModel genreModel = genreArrayList.get(i);
            if (genreId == genreModel.getId()) {
                genreName = genreModel.getName();
                break;
            }
        }
        return genreName;
    }

    // Call to get Genre for all movies
    private void collectGenre() {
        String url = GlobalConfig.BASE_URL + GlobalConfig.FULL_MOVIE_GENRE +
                GlobalConfig.API_KEY_REFERENCE;

        JSONObject jsonObject = new JSONObject();
        APIManager.jsonObjectVolleyRequest(mContext, url, "GET", jsonObject);
        APIManager.setOnAPICallbackListener(new RequestCallback.APIRequestCallback() {
            @Override
            public void onSuccessJSONResponse(JSONObject response) {
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.GONE);
                setGenreListForResponse(response);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.GONE);
                APIManager.handleErrorResponseOfRequest(mContext, error, "Server Error", TAG);
            }
        });
    }

    // Set Genre into a list
    private void setGenreListForResponse(JSONObject jsonObject) {
        String blank = "";
        try {
            if (jsonObject.has("genres") && !jsonObject.isNull("genres")) {
                JSONArray jsonArray = jsonObject.getJSONArray("genres");
                for (int i = 0; i < jsonArray.length(); i++) {
                    GenreModel genreModel = new GenreModel();
                    JSONObject jsonGenreObject = jsonArray.getJSONObject(i);

                    if (jsonGenreObject.has("id") && !jsonGenreObject.isNull("id")) {
                        genreModel.setId(jsonGenreObject.getInt("id"));
                    } else
                        genreModel.setId(0);

                    if (jsonGenreObject.has("name") && !jsonGenreObject.isNull("name")) {
                        genreModel.setName(jsonGenreObject.getString("name"));
                    } else
                        genreModel.setName(blank);

                    genreArrayList.add(genreModel);
                }
            }
            getMovieList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).
                edit().clear().apply();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).
                edit().clear().apply();
        finish();
    }
}
