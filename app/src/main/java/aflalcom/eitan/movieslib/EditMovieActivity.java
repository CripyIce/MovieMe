package aflalcom.eitan.movieslib;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import aflalcom.eitan.movieslib.model.Movie;
import io.realm.Realm;
import io.realm.RealmResults;

public class EditMovieActivity extends AppCompatActivity {

    private Realm realm;

    String stringExtra;

    ImageView manualPosterImageView;
    EditText manualTitleEditText, manualDirectorEditText, manualActorsEditText, manualLangEditText,
            manualGenreEditText, manualPlotEditText;
    Button manualOkBtn, manualCancelBtn;

    int year;
    String imdbid = "";
    String released = "";
    String rated = "";
    String runtime = "";
    String country = "";
    String imdbRate = "";
    String imdbVotes = "";
    String title = "";
    String poster = "";
    String genre = "";
    String director = "";
    String actors = "";
    String plot = "";
    String lang = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_movie);
        getSupportActionBar().setTitle(getString(R.string.edit_activity_title));

        realm = Realm.getDefaultInstance();

        final int movieID;
        year = Calendar.getInstance().get(Calendar.YEAR);

        manualPosterImageView = (ImageView) findViewById(R.id.manualPosterImageView);
        manualTitleEditText = (EditText) findViewById(R.id.manualTitleEditText);
        manualDirectorEditText = (EditText) findViewById(R.id.manualDirectorEditText);
        manualActorsEditText = (EditText) findViewById(R.id.manualActorsEditText);
        manualLangEditText = (EditText) findViewById(R.id.manualLangEditText);
        manualGenreEditText = (EditText) findViewById(R.id.manualGenreEditText);
        manualPlotEditText = (EditText) findViewById(R.id.manualPlotEditText);
        manualOkBtn = (Button) findViewById(R.id.manualOkBtn);
        manualCancelBtn = (Button) findViewById(R.id.manualCancelBtn);

        stringExtra = getIntent().getStringExtra("Activity");
        movieID = getIntent().getIntExtra("movieID", 0);

        if (stringExtra.equals("Main") || stringExtra.equals("Search")) {
            getMovieIMDB(getIntent().getStringExtra("imdbID"));
        } else if (stringExtra.equals("Edit")) {
            getDBMovie(movieID);
            manualOkBtn.setText(getString(R.string.save_btn));
        }

        manualPosterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] items = getResources().getStringArray(R.array.poster_items);
                new AlertDialog.Builder(EditMovieActivity.this, R.style.AppTheme_MyAlertDialog)
                        .setTitle(getString(R.string.change_poster))
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0) {
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(intent, Constants.REQUEST_CAMERA);
                                } else if (item == 1) {
                                    Intent intent = new Intent();
                                    intent.setType(Constants.IMAGE_TYPE);
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent,
                                            getString(R.string.select_picture)), Constants.SELECT_PICTURE);
                                } else if (item == 2) {
                                    URLdialog();
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        })
                        .show();

            }
        });

        manualCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        manualOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (manualTitleEditText.getText().toString().isEmpty()) {
                    manualTitleEditText.setError(getString(R.string.enter_movie));
                    return;
                }

                if (stringExtra.equals("Add") || stringExtra.equals("Search")) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Movie movie = realm.createObject(Movie.class);
                            int nextID = realm.where(Movie.class).max("id").intValue();
                            movie.setId(nextID + 1);
                            movie.setTitle(manualTitleEditText.getText().toString());
                            movie.setPoster(poster);
                            movie.setYear(year);
                            movie.setImdbID(imdbid);
                            movie.setReleased(released);
                            movie.setRated(rated);
                            movie.setRuntime(runtime);
                            movie.setGenre(manualGenreEditText.getText().toString());
                            movie.setDirector(manualDirectorEditText.getText().toString());
                            movie.setActors(manualActorsEditText.getText().toString());
                            movie.setPlot(manualPlotEditText.getText().toString());
                            movie.setLang(manualLangEditText.getText().toString());
                            movie.setCountry(country);
                            movie.setImdbRate(imdbRate);
                            movie.setImdbVotes(imdbVotes);
                            movie.setWatched(false);
                            movie.setUserRate(0.0f);
                        }
                    });
                } else if (stringExtra.equals("Edit")) {
                    updateDBMovie(movieID);
                }

                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void URLdialog() {
        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.dialog_enterurl, null);
        final EditText urlEditText = (EditText) dialoglayout.findViewById(R.id.urlEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(EditMovieActivity.this, R.style.AppTheme_MyAlertDialog)
                .setView(dialoglayout)
                .setTitle(getString(R.string.enter_url_hint))
                .setPositiveButton(getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (urlEditText.getText().toString().isEmpty()) {
                    urlEditText.setError(getString(R.string.enter_url_hint));
                    return;
                }

                Picasso.with(getParent())
                        .load(urlEditText.getText().toString())
                        .resize(manualPosterImageView.getWidth(), manualPosterImageView.getHeight()).centerCrop()
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                manualPosterImageView.setImageBitmap(bitmap);
                                poster = urlEditText.getText().toString();
                                dialog.dismiss();
                            }

                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                                urlEditText.setError(getString(R.string.enter_url_error));
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                manualPosterImageView.setTag(this);
                            }
                        });
            }
        });
    }

    private void getMovieIMDB(final String imdbID) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                ("http://www.omdbapi.com/?i=" + imdbID + "&plot=full&r=json", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            title = response.getString("Title");
                            poster = response.getString("Poster");
                            genre = response.getString("Genre");
                            director = response.getString("Director");
                            actors = response.getString("Actors");
                            plot = response.getString("Plot");
                            lang = response.getString("Language");
                            year = response.getInt("Year");
                            imdbid = imdbID;
                            released = response.getString("Released");
                            rated = response.getString("Rated");
                            runtime = response.getString("Runtime");
                            country = response.getString("Country");
                            imdbRate = response.getString("imdbRating");
                            imdbVotes = response.getString("imdbVotes");

                            if (!poster.equals("N/A")) {
                                Picasso.with(getApplicationContext()).load(poster)
                                        .placeholder(R.drawable.no_image).fit().into(manualPosterImageView);
                            }
                            manualTitleEditText.setText(title);
                            manualDirectorEditText.setText(director);
                            manualActorsEditText.setText(actors);
                            manualLangEditText.setText(lang);
                            manualGenreEditText.setText(genre);
                            manualPlotEditText.setText(plot);
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

    private void updateDBMovie(final int id) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Movie movie = realm.where(Movie.class).equalTo("id", id).findFirst();
                movie.setPoster(poster);
                movie.setTitle(manualTitleEditText.getText().toString());
                movie.setDirector(manualDirectorEditText.getText().toString());
                movie.setActors(manualActorsEditText.getText().toString());
                movie.setLang(manualLangEditText.getText().toString());
                movie.setGenre(manualGenreEditText.getText().toString());
                movie.setPlot(manualPlotEditText.getText().toString());
            }
        });
    }

    private void getDBMovie(int id) {
        RealmResults<Movie> results = realm.where(Movie.class).equalTo("id", id).findAll();
        poster = results.first().getPoster();
        if (!poster.equals("N/A")) {
            if (!results.first().getPoster().equals("")) {
                Picasso.with(getApplicationContext()).load(results.first().getPoster())
                        .placeholder(R.drawable.no_image).fit().into(manualPosterImageView);
            }
        }
        manualTitleEditText.setText(results.first().getTitle());
        manualDirectorEditText.setText(results.first().getDirector());
        manualActorsEditText.setText(results.first().getActors());
        manualLangEditText.setText(results.first().getLang());
        manualGenreEditText.setText(results.first().getGenre());
        manualPlotEditText.setText(results.first().getPlot());
    }

    private void saveImage(Bitmap image) {
        try {
            File pictureFile = createImageFile();
            if (pictureFile == null) {
                Log.d(MainActivity.class.getSimpleName(),
                        "Error creating media file, check storage permissions: ");
                return;
            }

            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        poster = "file:" + image.getAbsolutePath();
        return image;
    }

    private void onCaptureImageResult(Intent data) throws IOException {
        Bitmap image = (Bitmap) data.getExtras().get("data");
        manualPosterImageView.setImageBitmap(image);
        saveImage(image);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_CAMERA) {
                try {
                    onCaptureImageResult(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (requestCode == Constants.SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                try {
                    manualPosterImageView.setImageBitmap(new GetImage(selectedImageUri, getContentResolver()).getBitmap());
                    saveImage(new GetImage(selectedImageUri, getContentResolver()).getBitmap());

                } catch (IOException e) {
                    Log.e(MainActivity.class.getSimpleName(), "Failed to load image", e);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
