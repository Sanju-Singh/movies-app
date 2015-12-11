package com.sanjusingh.movies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by sanju singh on 12/11/2015.
 */
public class PosterFragment extends Fragment {

    private final String LOG_TAG= PosterFragment.class.getSimpleName();
    private ImageAdapter imageAdapter;

    public PosterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        imageAdapter = new ImageAdapter(getActivity(), new ArrayList<Movie>());

        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);
        gridView.setAdapter(imageAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    public void updateMovies(){
        FetchMovieDBTask fetchMovieDBTask = new FetchMovieDBTask();
        fetchMovieDBTask.execute();
    }

    private class FetchMovieDBTask extends AsyncTask<Void, Void, Movie[]>{

        @Override
        protected Movie[] doInBackground(Void... params) {

            final String BaseUrl = "http://api.themoviedb.org/3/discover/movie?";

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String JSONStr = null;

            try{

                Uri uri = Uri.parse(BaseUrl).buildUpon()
                        .appendQueryParameter("sort_by","popularity.desc")
                        .appendQueryParameter("api_key", BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if(inputStream == null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                StringBuffer buffer = new StringBuffer("");
                while((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0){
                    return null;
                }

                JSONStr = buffer.toString();

            } catch(IOException e){
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if(reader!=null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing Stream", e);
                    }
                }

            }

            return getMoviesFromJSON(JSONStr);

        }

        @Override
        protected void onPostExecute(Movie[] result) {
            if(result != null){
                for(Movie movie : result){
                    imageAdapter.add(movie);
                }
            }

        }

        private Movie[] getMoviesFromJSON(String JSONStr){

            final String BASE_URL = "http://image.tmdb.org/t/p/w185";
            final String MOVIES_RESULT = "results";
            final String POSTER_PATH = "poster_path";
            final String TITLE = "original_title";
            final String OVERVIEW = "overview";
            final String RELEASE_DATE = "release_date";
            final String USER_RATING = "vote_average";
            Movie[] movies = null;

            try {
                JSONObject moviesObject = new JSONObject(JSONStr);
                JSONArray resultArray = moviesObject.getJSONArray(MOVIES_RESULT);

                movies = new Movie[resultArray.length()];

                for(int i=0; i < resultArray.length(); i++){

                    JSONObject movieInfo = resultArray.getJSONObject(i);
                    //posterLinks[i] = BASE_URL + movieInfo.getString(POSTER_PATH);
                    movies[i] = new Movie(
                            movieInfo.getString(TITLE),
                            movieInfo.getString(OVERVIEW),
                            movieInfo.getString(RELEASE_DATE),
                            movieInfo.getDouble(USER_RATING),
                            BASE_URL + movieInfo.getString(POSTER_PATH)
                    );
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return movies;
        }
    }
}