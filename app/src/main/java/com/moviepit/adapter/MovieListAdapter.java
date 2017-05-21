package com.moviepit.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.moviepit.MovieDetail;
import com.moviepit.R;
import com.moviepit.model.MovieModel;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MyViewHolder> implements View.OnClickListener {

    private Context mContext;
    private ArrayList<MovieModel> movieList;


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView movieTitle, movieReleaseDate, movieAverageVote, movieGenre, moreInfo, movieOverview;
        ImageView moviePoster;
        CardView cardViewMovieItem;
        ProgressBar progressBarImage;

        MyViewHolder(View view) {
            super(view);
            movieTitle = (TextView) view.findViewById(R.id.movie_title);
            movieAverageVote = (TextView) view.findViewById(R.id.movie_vote_average);
            movieReleaseDate = (TextView) view.findViewById(R.id.movie_release_date);
            movieGenre = (TextView) view.findViewById(R.id.movie_genre);
            moviePoster = (ImageView) view.findViewById(R.id.movie_thumbnail);
            movieOverview = (TextView) view.findViewById(R.id.movie_overview);
            cardViewMovieItem = (CardView) view.findViewById(R.id.card_view_movie_item);
            moreInfo = (TextView) view.findViewById(R.id.more_info);
            progressBarImage = (ProgressBar) view.findViewById(R.id.progress_bar_image);
        }

    }

    public MovieListAdapter(Context mContext, ArrayList<MovieModel> movieList) {
        this.mContext = mContext;
        this.movieList = movieList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final MovieModel movieModel = movieList.get(position);

        String vote = movieModel.getVoteAverage() + "";
        String release = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-DD", Locale.getDefault());
        try {
            Date d = dateFormat.parse(movieModel.getReleaseDate());
            release = (String) DateFormat.format("yyyy", d);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.movieTitle.setText(movieModel.getTitle());
        holder.movieAverageVote.setText(vote);
        holder.movieReleaseDate.setText(release);
        holder.movieGenre.setText(movieModel.getGenre());
        holder.movieOverview.setText(movieModel.getOverview());
        Glide.with(mContext)
                .load(movieModel.getPosterPath())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.progressBarImage.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.progressBarImage.setVisibility(View.GONE);
                        return false;
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.moviePoster);


        holder.moreInfo.setOnClickListener(this);
        holder.moreInfo.setTag(position);
        holder.cardViewMovieItem.setOnClickListener(this);
        holder.cardViewMovieItem.setTag(position);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.more_info:
                int pos = (int) view.getTag();
                passIntent(pos);
                break;

            case R.id.card_view_movie_item:
                int position = (int) view.getTag();
                passIntent(position);
                break;
        }
    }

    private void passIntent(int pos){
        MovieModel movieModelInstance = movieList.get(pos);
        Intent n = new Intent(mContext, MovieDetail.class);

        n.putExtra("image_url", movieModelInstance.getPosterPath());
        n.putExtra("movie_title", movieModelInstance.getTitle());
        n.putExtra("movie_id", movieModelInstance.getId());
        n.putExtra("genre", movieModelInstance.getGenre());
        float percentage = movieModelInstance.getVoteAverage() * 10;
        n.putExtra("percentage", percentage);
        mContext.startActivity(n);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }
}