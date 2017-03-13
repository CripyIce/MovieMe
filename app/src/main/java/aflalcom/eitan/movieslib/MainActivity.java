package aflalcom.eitan.movieslib;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import aflalcom.eitan.movieslib.adapter.LibraryAdapter;
import aflalcom.eitan.movieslib.model.Movie;
import io.realm.Realm;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    private Realm realm;

    private LibraryAdapter libraryAdapter;
    private List<Movie> movieList;

    RecyclerView movieLibraryRV;
    FrameLayout hideLayout;
    LinearLayout fabLayout, manualMovieLL, autoMovieLL;
    FloatingActionButton fab;
    TextView noMoviesTextView;

    private Animation fabOpenAnimation, fabCloseAnimation;
    boolean isFabMenuOpen = false;

    SharedPreferences preferences;
    boolean isNight;
    String itemsPerRow, sortWay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        realm = Realm.getDefaultInstance();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkSettings();

        fabLayout = (LinearLayout) findViewById(R.id.fabLayout);
        manualMovieLL = (LinearLayout) findViewById(R.id.manualMovieLL);
        autoMovieLL = (LinearLayout) findViewById(R.id.autoMovieLL);
        hideLayout = (FrameLayout) findViewById(R.id.hideLayout);

        fabOpenAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabCloseAnimation = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFabMenuOpen) {
                    closeFabMenu();
                } else {
                    openFabMenu();
                }
            }
        });

        autoMovieLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), AddMovieActivity.class), Constants.ADD_MOVIE_ONLINE);
                closeFabMenu();
            }
        });

        manualMovieLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditMovieActivity.class);
                intent.putExtra("Activity", "Add");
                startActivityForResult(intent, Constants.ADD_MOVIE_OFFLINE);
                closeFabMenu();
            }
        });

        hideLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFabMenu();
            }
        });
    }

    public void checkSettings() {
        isNight = preferences.getBoolean("daynight_theme", true);
        itemsPerRow = preferences.getString("items_per_row_list", "2");

        movieLibraryRV = (RecyclerView) findViewById(R.id.movieLibraryRV);
        noMoviesTextView = (TextView) findViewById(R.id.noMoviesTextView);

        if (isNight) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        movieList = new ArrayList<>();
        movieList = getMovieList();
        if (movieList.isEmpty()) {
            noMoviesTextView.setVisibility(View.VISIBLE);
            movieLibraryRV.setVisibility(View.GONE);
            return;
        } else {
            noMoviesTextView.setVisibility(View.GONE);
            movieLibraryRV.setVisibility(View.VISIBLE);
        }

        libraryAdapter = new LibraryAdapter(this, getApplicationContext(), movieList);

        movieLibraryRV.setHasFixedSize(true);
        movieLibraryRV.setLayoutManager(new GridLayoutManager(this, Integer.valueOf(itemsPerRow)));
        movieLibraryRV.setAdapter(libraryAdapter);
        libraryAdapter.notifyDataSetChanged();
    }

    private List<Movie> getMovieList() {
        sortWay = preferences.getString("pref_sortway_list", "2");
        String objectName = "id";
        if (sortWay.equals("1")) {
            objectName = "id";
        } else if (sortWay.equals("2")) {
            objectName = "watched";
        } else if (sortWay.equals("3")) {
            objectName = "userRate";
        }

        List<Movie> list = new ArrayList<>();
        for (Movie movie : realm.where(Movie.class).findAllSorted(objectName, Sort.DESCENDING)) {
            list.add(new Movie(movie.getId(), movie.getTitle(), movie.getPoster(), movie.getYear(), movie.getImdbID(),
                    movie.getUserRate(), movie.isWatched()));
        }

        return list;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                new AlertDialog.Builder(this, R.style.AppTheme_MyAlertDialog)
                        .setTitle(getString(R.string.action_clear))
                        .setMessage(getString(R.string.action_clear_alert))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.clear_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.deleteAll();
                                        checkSettings();
                                    }
                                });
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
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_MyAlertDialog)
                        .setTitle(getString(R.string.action_about))
                        .setMessage(getString(R.string.credits))
                        .setCancelable(false)
                        .setNegativeButton(getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            case R.id.action_exit:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFabMenu() {
        hideLayout.setVisibility(View.VISIBLE);

        ViewCompat.animate(fab).rotation(45.0F).withLayer().setDuration(300).setInterpolator(new OvershootInterpolator(10.0F)).start();
        fabLayout.startAnimation(fabOpenAnimation);
        autoMovieLL.setClickable(true);
        manualMovieLL.setClickable(true);
        isFabMenuOpen = true;
    }

    private void closeFabMenu() {
        hideLayout.setVisibility(View.GONE);

        ViewCompat.animate(fab).rotation(0.0F).withLayer().setDuration(300).setInterpolator(new OvershootInterpolator(10.0F)).start();
        fabLayout.startAnimation(fabCloseAnimation);
        autoMovieLL.setClickable(false);
        manualMovieLL.setClickable(false);
        isFabMenuOpen = false;
    }
}
