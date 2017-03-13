package aflalcom.eitan.movieslib;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import aflalcom.eitan.movieslib.model.Movie;
import io.realm.Realm;
import io.realm.RealmResults;

public class ViewMovieActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {
    private Realm realm;

    private static final int RECOVERY_REQUEST = 1;

    int movieID;
    String imdbID, trailerID;
    boolean isWatched;
    float userRate;

    MenuItem watchedItem = null;

    YouTubePlayerSupportFragment youTubePlayerSupportFragment;
    LinearLayout youtubeLL, watchedLL, ratedLL;
    ImageView watchedImageView, ratedImageView;
    TextView watchedTextView, ratedTextView, viewTitleTextView, viewRateTextView, viewDirectorTextView, viewActorsTextView,
            viewLangTextView, viewRatedTextView, viewGenreTextView, viewRuntimeTextView,
            viewPlotTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_movie);
        Toolbar viewMovieToolbar = (Toolbar) findViewById(R.id.viewMovieToolbar);
        setSupportActionBar(viewMovieToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.view_activity_title));

        youtubeLL = (LinearLayout) findViewById(R.id.youtubeLL);
        youTubePlayerSupportFragment =
                (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtubePlayer);

        watchedLL = (LinearLayout) findViewById(R.id.watchedLL);
        ratedLL = (LinearLayout) findViewById(R.id.ratedLL);
        watchedImageView = (ImageView) findViewById(R.id.watchedImageView);
        ratedImageView = (ImageView) findViewById(R.id.ratedImageView);

        watchedTextView = (TextView) findViewById(R.id.watchedTextView);
        ratedTextView = (TextView) findViewById(R.id.ratedTextView);
        viewTitleTextView = (TextView) findViewById(R.id.viewTitleTextView);
        viewRateTextView = (TextView) findViewById(R.id.viewRateTextView);
        viewDirectorTextView = (TextView) findViewById(R.id.viewDirectorTextView);
        viewActorsTextView = (TextView) findViewById(R.id.viewActorsTextView);
        viewLangTextView = (TextView) findViewById(R.id.viewLangTextView);
        viewRatedTextView = (TextView) findViewById(R.id.viewRatedTextView);
        viewGenreTextView = (TextView) findViewById(R.id.viewGenreTextView);
        viewRuntimeTextView = (TextView) findViewById(R.id.viewRuntimeTextView);
        viewPlotTextView = (TextView) findViewById(R.id.viewPlotTextView);

        movieID = getIntent().getIntExtra("movieID", 0);

        realm = Realm.getDefaultInstance();

        getMovie();

        watchedLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (watchedItem != null) {
                    if (isWatched) {
                        setMovieWatched(false);
                        watchedItem.setTitle(R.string.action_watched);
                    } else {
                        setMovieWatched(true);
                        watchedItem.setTitle(R.string.action_unwatch);
                    }
                }
            }
        });

        ratedLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.dialog_rate, null);
                final RatingBar ratingBar = (RatingBar) dialoglayout.findViewById(R.id.ratingBar);

                ratingBar.setRating(userRate);

                new AlertDialog.Builder(ViewMovieActivity.this, R.style.AppTheme_MyAlertDialog)
                        .setView(dialoglayout)
                        .setTitle(getString(R.string.rate_dialog_title))
                        .setPositiveButton(getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setUserRate(ratingBar.getRating());
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    private void setUserRate(final float rate) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Movie movie = realm.where(Movie.class).equalTo("id", movieID).findFirst();
                movie.setUserRate(rate);
                userRate = rate;
                setRatedText(rate);
            }
        });
    }

    private void setMovieWatched(final boolean watched) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Movie movie = realm.where(Movie.class).equalTo("id", movieID).findFirst();
                movie.setWatched(watched);
                isWatched = watched;
                setWatchedText(watched);
            }
        });
    }

    private void getMovieTrailer(String imdbID) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                ("http://api.themoviedb.org/3/movie/" + imdbID +
                        "/videos?api_key=815bd46e93872a75d0373b6879261ec6", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray arr = response.getJSONArray("results");
                            JSONObject jsonObject = arr.getJSONObject(0);

                            trailerID = jsonObject.getString("key");
                            youTubePlayerSupportFragment.initialize(Constants.DEVELOPER_KEY, ViewMovieActivity.this);
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

    private void getMovie() {
        RealmResults<Movie> results = realm.where(Movie.class).equalTo("id", movieID).findAll();

        imdbID = results.first().getImdbID();

        if (!results.first().getImdbID().equals("")) {
            getMovieTrailer(results.first().getImdbID());
        } else {
            youtubeLL.setVisibility(View.GONE);
        }

        viewTitleTextView.setText(results.first().getTitle() + " (" + results.first().getYear() + ")");
        if (!results.first().getImdbRate().equals("")) {
            String a = getString(R.string.imdb_rate) + results.first().getImdbRate();
            viewRateTextView.setText(a);
        } else {
            String a = getString(R.string.imdb_rate) + getString(R.string.imdb_none);
            viewRateTextView.setText(a);
        }

        if (!results.first().getDirector().equals("")) {
            String a = getString(R.string.imdb_director) + results.first().getDirector();
            viewDirectorTextView.setText(a);
        } else {
            String a = getString(R.string.imdb_director) + getString(R.string.imdb_none);
            viewDirectorTextView.setText(a);
        }

        if (!results.first().getActors().equals("")) {
            String a = getString(R.string.imdb_actors) + results.first().getActors();
            viewActorsTextView.setText(a);
        } else {
            String a = getString(R.string.imdb_actors) + getString(R.string.imdb_none);
            viewActorsTextView.setText(a);
        }

        if (!results.first().getLang().equals("") && !results.first().getCountry().equals("")) {
            viewLangTextView.setText(results.first().getLang() + ", " + results.first().getCountry());
        } else {
            viewLangTextView.setText("");
        }
        viewRatedTextView.setText(results.first().getRated());
        viewGenreTextView.setText(results.first().getGenre());
        viewRuntimeTextView.setText(results.first().getRuntime());
        if (!results.first().getPlot().equals("")) {
            viewPlotTextView.setText(results.first().getPlot());
        } else {
            viewPlotTextView.setText(getString(R.string.imdb_no_plot));
        }
        userRate = results.first().getUserRate();
        isWatched = results.first().isWatched();

        setRatedText(userRate);
        setWatchedText(isWatched);
    }

    private void setRatedText(float rated) {
        if (rated != 0.0f) {
            ratedImageView.setVisibility(View.VISIBLE);
            ratedTextView.setText(String.valueOf(userRate));
        } else {
            ratedImageView.setVisibility(View.GONE);
            ratedTextView.setText(R.string.movie_not_rated);
        }
    }

    private void setWatchedText(boolean watched) {
        if (watched) {
            watchedImageView.setVisibility(View.VISIBLE);
            watchedTextView.setText(R.string.watched);
        } else {
            watchedImageView.setVisibility(View.GONE);
            watchedTextView.setText(R.string.movie_not_watched);
        }
    }

    private void deleteMovie() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Movie movie = realm.where(Movie.class).equalTo("id", movieID).findFirst();
                movie.deleteFromRealm();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_movie, menu);
        if (isWatched) {
            menu.findItem(R.id.action_watched).setTitle(R.string.action_unwatch);
        } else {
            menu.findItem(R.id.action_watched).setTitle(R.string.action_watched);
        }

        watchedItem = menu.findItem(R.id.action_watched);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider myShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        Intent myShareIntent = new Intent(Intent.ACTION_SEND);
        myShareIntent.setType("text/plain");
        myShareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
        myShareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text)
                + "\"" + viewTitleTextView.getText().toString() + "\".\n"
                + "http://www.imdb.com/title/" + imdbID);
        myShareActionProvider.setShareIntent(myShareIntent);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.EDIT_MOVIE) {
                getMovie();
            }
            if (requestCode == RECOVERY_REQUEST) {
                youTubePlayerSupportFragment.initialize(Constants.DEVELOPER_KEY, this);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent intent = new Intent(this, EditMovieActivity.class);
                intent.putExtra("Activity", "Edit");
                intent.putExtra("movieID", movieID);
                startActivityForResult(intent, Constants.EDIT_MOVIE);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                return true;
            case R.id.action_rate:
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.dialog_rate, null);
                final RatingBar ratingBar = (RatingBar) dialoglayout.findViewById(R.id.ratingBar);

                ratingBar.setRating(userRate);

                new AlertDialog.Builder(this, R.style.AppTheme_MyAlertDialog)
                        .setView(dialoglayout)
                        .setTitle(getString(R.string.rate_dialog_title))
                        .setPositiveButton(getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setUserRate(ratingBar.getRating());
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            case R.id.action_watched:
                if (isWatched) {
                    setMovieWatched(false);
                    item.setTitle(R.string.action_watched);
                } else {
                    setMovieWatched(true);
                    item.setTitle(R.string.action_unwatch);
                }
                return true;
            case R.id.action_delete:
                new AlertDialog.Builder(this, R.style.AppTheme_MyAlertDialog)
                        .setTitle(getString(R.string.delete_movie_title))
                        .setMessage(getString(R.string.delete_movie_content))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.action_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMovie();
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            case android.R.id.home:
                super.onBackPressed();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean b) {
        if (!b) {
            youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
            youTubePlayer.cueVideo(trailerID);
            youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                @Override
                public void onLoading() {

                }

                @Override
                public void onLoaded(String s) {

                }

                @Override
                public void onAdStarted() {

                }

                @Override
                public void onVideoStarted() {
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                }

                @Override
                public void onVideoEnded() {

                }

                @Override
                public void onError(YouTubePlayer.ErrorReason errorReason) {

                }
            });
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = youTubeInitializationResult.toString();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }
}
