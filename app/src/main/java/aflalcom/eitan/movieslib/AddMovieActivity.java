package aflalcom.eitan.movieslib;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import aflalcom.eitan.movieslib.adapter.MovieAdapter;
import aflalcom.eitan.movieslib.model.Movie;

public class AddMovieActivity extends AppCompatActivity {

    private MovieAdapter movieAdapter;
    private List<Movie> movieList;

    ConstraintLayout activity_add_movie;
    TextInputEditText movieTitleInputLayout;
    ProgressBar progressBar;
    Button searchBtn, cancelBtn;
    RecyclerView movieSearchRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        activity_add_movie = (ConstraintLayout) findViewById(R.id.activity_add_movie);
        movieTitleInputLayout = (TextInputEditText) findViewById(R.id.movieTitleInputLayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        searchBtn = (Button) findViewById(R.id.searchBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);

        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(this, getApplicationContext(), movieList);

        movieSearchRV = (RecyclerView) findViewById(R.id.movieSearchRV);
        movieSearchRV.setHasFixedSize(true);
        movieSearchRV.setLayoutManager(new GridLayoutManager(this, 2));
        movieSearchRV.setAdapter(movieAdapter);

        movieTitleInputLayout.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchMovie(v);
                    handled = true;
                }
                return handled;
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchMovie(v);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void searchMovie(View v) {
        try {
            if (!isNetworkConnected()) {
                makeSnackBar(getString(R.string.no_internet));
                return;
            }

            if (movieTitleInputLayout.getText().toString().equals("")) {
                makeSnackBar(getString(R.string.enter_movie));
                return;
            }

            if (movieTitleInputLayout.getText().length() <= 2) {
                makeSnackBar(getString(R.string.short_name));
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            getMovieTitle(URLEncoder.encode(movieTitleInputLayout.getText().toString(), "utf-8"));
            InputMethodManager in = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void makeSnackBar(String text) {
        Snackbar snackbar = Snackbar.make(activity_add_movie, text, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private void getMovieTitle(String title) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                ("http://www.omdbapi.com/?s=" + title + "&type=movie&r=json", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            movieList.clear();
                            movieAdapter.clearData();
                            progressBar.setVisibility(View.GONE);

                            if (response.getString("Response").equals("False")) {
                                makeSnackBar(getString(R.string.movie_not_found));
                                return;
                            }

                            JSONArray arr = response.getJSONArray("Search");

                            String title, poster, imdbID;
                            int year;

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject jsonObject = arr.getJSONObject(i);
                                title = jsonObject.getString("Title");
                                poster = jsonObject.getString("Poster");
                                year = jsonObject.getInt("Year");
                                imdbID = jsonObject.getString("imdbID");
                                movieList.add(new Movie(title, poster, year, imdbID));
                            }

                            movieAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(jsonRequest);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2000 && resultCode == RESULT_OK) {
            Intent intent = getIntent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
