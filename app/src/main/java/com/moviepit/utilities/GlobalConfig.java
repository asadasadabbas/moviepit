package com.moviepit.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.moviepit.R;


public class GlobalConfig {

    //  ---------------------------- API Config--------------------------------
    public static String BASE_URL = "https://api.themoviedb.org/3";
    private static String API_KEY = "api_key";
    private static String API_VALUE = "YOUR_API_KEY";
    public static String API_KEY_REFERENCE = "?" + API_KEY + "=" + API_VALUE;
    public static String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w500";
    public static String BASE_IMAGE_URL_W185 = "https://image.tmdb.org/t/p/w185";

    //  ----------------------------Pagination Keys----------------------------
    public static String PAGE_KEY = "&page=";

    //  ----------------------------Discover API-------------------------------
    public static String MOVIES = "/movie";
    public static String UPCOMING_MOVIES = "/movie/upcoming";
    public static String POPULAR_MOVIES = "/movie/popular";
    public static String CREDITS = "/credits";

    //  ----------------------------Genre API----------------------------------
    public static String FULL_MOVIE_GENRE = "/genre/movie/list";

    //  --------------------------- Show Snackbar -----------------------------
    public static void showSnackBar(String message, CoordinatorLayout coordinatorLayout) {
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("Close", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public static boolean isConnectedToInternet(Context context, CoordinatorLayout coordinatorLayout) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) { // not connected to the internet
            showSnack(context, false, coordinatorLayout);
            return false;
        }
        return true;
    }

    private static void showSnack(Context context, boolean isConnected,
                                  CoordinatorLayout coordinatorLayout) {
        String message = "";
        int color = ContextCompat.getColor(context, R.color.colorPrimary);
        if (!isConnected) {
            message = "Sorry! Not connected to internet";
        } else {
            message = "Connected to internet";
        }
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("Close", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById
                (android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    public static boolean sharedPreferenceFirstTime(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("initial", false)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("initial", true);
            editor.apply();
            return true;
        }
        return false;
    }


}
