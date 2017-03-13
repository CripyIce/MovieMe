package aflalcom.eitan.movieslib.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import aflalcom.eitan.movieslib.EditMovieActivity;
import aflalcom.eitan.movieslib.MainActivity;
import aflalcom.eitan.movieslib.R;
import aflalcom.eitan.movieslib.ViewMovieActivity;
import aflalcom.eitan.movieslib.model.Movie;
import io.realm.Realm;

/**
 * Created by Eitan on 25/09/2016.
 */

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.MyViewHolder> {

    private Activity activity;
    private Context context;
    private List<Movie> movieList;
    private Realm realm;
    private SharedPreferences preferences;
    private String itemsPerRow;

    class MyViewHolder extends RecyclerView.ViewHolder {

        CardView movieLibraryCV;
        LinearLayout watchedLibraryLL, rateLibraryLL;
        ImageView movieLibraryImageView, moreImageView;
        TextView rateLibraryTextView, movieLibraryTitle, watchedLibraryTextView;

        MyViewHolder(View itemView) {
            super(itemView);
            realm = Realm.getDefaultInstance();
            movieLibraryCV = (CardView) itemView.findViewById(R.id.movieLibraryCV);
            watchedLibraryLL = (LinearLayout) itemView.findViewById(R.id.watchedLibraryLL);
            rateLibraryLL = (LinearLayout) itemView.findViewById(R.id.rateLibraryLL);
            movieLibraryImageView = (ImageView) itemView.findViewById(R.id.movieLibraryImageView);
            moreImageView = (ImageView) itemView.findViewById(R.id.moreImageView);
            rateLibraryTextView = (TextView) itemView.findViewById(R.id.rateLibraryTextView);
            movieLibraryTitle = (TextView) itemView.findViewById(R.id.movieLibraryTitle);
            watchedLibraryTextView = (TextView) itemView.findViewById(R.id.watchedLibraryTextView);

            if (Integer.valueOf(itemsPerRow) >= 3) {
                rateLibraryTextView.setVisibility(View.GONE);
                watchedLibraryTextView.setVisibility(View.GONE);
            }
        }
    }

    public LibraryAdapter(Activity activity, Context context, List<Movie> movieList) {
        this.activity = activity;
        this.context = context;
        this.movieList = movieList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_library_cv, parent, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        itemsPerRow = preferences.getString("items_per_row_list", "2");
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        if (movieList.get(position).getUserRate() == 0.0f) {
            holder.rateLibraryLL.setVisibility(View.INVISIBLE);
        } else {
            holder.rateLibraryLL.setVisibility(View.VISIBLE);
            holder.rateLibraryTextView.setText(String.valueOf(movieList.get(holder.getAdapterPosition()).getUserRate()));
        }

        if (movieList.get(position).isWatched()) {
            holder.watchedLibraryLL.setVisibility(View.VISIBLE);
        } else {
            holder.watchedLibraryLL.setVisibility(View.INVISIBLE);
        }

        if (!movieList.get(position).getPoster().equals("N/A")) {
            if (!movieList.get(position).getPoster().equals("")) {
                Picasso.with(context).load(movieList.get(position).getPoster()).fit()
                        .placeholder(R.drawable.no_image).into(holder.movieLibraryImageView);
            }
        }
        holder.movieLibraryTitle.setText(movieList.get(position).getTitle()
                + " (" + movieList.get(position).getYear() + ")");

        holder.movieLibraryCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ViewMovieActivity.class);
                intent.putExtra("Activity", "Main");
                intent.putExtra("movieID", movieList.get(holder.getAdapterPosition()).getId());
                activity.startActivityForResult(intent, 3000);
                activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });
        holder.moreImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(view.getContext(), view);
                popup.inflate(R.menu.menu_view_movie);
                if (movieList.get(holder.getAdapterPosition()).isWatched()) {
                    popup.getMenu().getItem(3).setTitle(R.string.action_unwatch);
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_edit:
                                final Intent intent = new Intent(activity, EditMovieActivity.class);
                                intent.putExtra("Activity", "Edit");
                                intent.putExtra("movieID", movieList.get(holder.getAdapterPosition()).getId());
                                activity.startActivityForResult(intent, 1002);
                                return true;
                            case R.id.action_share:
                                ShareActionProvider myShareActionProvider =
                                        (ShareActionProvider) MenuItemCompat.getActionProvider(item);
                                Intent myShareIntent = new Intent(Intent.ACTION_SEND);
                                myShareIntent.setType("text/plain");
                                myShareIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getResources().getString(R.string.app_name));
                                myShareIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_text)
                                        + "\"" + movieList.get(holder.getAdapterPosition()).getTitle() + "\".\n"
                                        + "http://www.imdb.com/title/" + movieList.get(holder.getAdapterPosition()).getImdbID());
                                myShareActionProvider.setShareIntent(myShareIntent);
                                return true;
                            case R.id.action_rate:
                                LayoutInflater inflater = activity.getLayoutInflater();
                                final View dialoglayout = inflater.inflate(R.layout.dialog_rate, null);
                                final RatingBar ratingBar = (RatingBar) dialoglayout.findViewById(R.id.ratingBar);

                                ratingBar.setRating(movieList.get(holder.getAdapterPosition()).getUserRate());

                                new AlertDialog.Builder(activity, R.style.AppTheme_MyAlertDialog)
                                        .setView(dialoglayout)
                                        .setTitle(activity.getString(R.string.rate_dialog_title))
                                        .setPositiveButton(activity.getString(R.string.ok_btn), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                setUserRate(movieList.get(holder.getAdapterPosition()).getId(), ratingBar.getRating());
                                                holder.rateLibraryTextView.setText(String.valueOf(ratingBar.getRating()));
                                                movieList.get(holder.getAdapterPosition()).setUserRate(ratingBar.getRating());
                                                notifyItemChanged(holder.getAdapterPosition());
                                            }
                                        })
                                        .setNegativeButton(activity.getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                                return true;
                            case R.id.action_watched:
                                if (movieList.get(holder.getAdapterPosition()).isWatched()) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            Movie movie = realm.where(Movie.class).equalTo("id", movieList.get(holder.getAdapterPosition()).getId()).findFirst();
                                            movie.setWatched(false);
                                            movieList.get(holder.getAdapterPosition()).setWatched(false);
                                        }
                                    });
                                } else {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            Movie movie = realm.where(Movie.class).equalTo("id", movieList.get(holder.getAdapterPosition()).getId()).findFirst();
                                            movie.setWatched(true);
                                            movieList.get(holder.getAdapterPosition()).setWatched(true);
                                        }
                                    });
                                }
                                notifyItemChanged(holder.getAdapterPosition());
                                return true;
                            case R.id.action_delete:
                                new AlertDialog.Builder(activity, R.style.AppTheme_MyAlertDialog)
                                        .setTitle(activity.getString(R.string.delete_movie_title))
                                        .setMessage(activity.getString(R.string.delete_movie_content))
                                        .setCancelable(false)
                                        .setPositiveButton(activity.getString(R.string.action_delete), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                deleteMovie(movieList.get(holder.getAdapterPosition()).getId());
                                                movieList.remove(holder.getAdapterPosition());
                                                notifyItemRemoved(holder.getAdapterPosition());
                                                ((MainActivity) activity).checkSettings();
                                            }
                                        })
                                        .setNegativeButton(activity.getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                                return true;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void setUserRate(final int movieID, final float rate) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Movie movie = realm.where(Movie.class).equalTo("id", movieID).findFirst();
                movie.setUserRate(rate);
            }
        });
    }

    private void deleteMovie(final int movieID) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Movie movie = realm.where(Movie.class).equalTo("id", movieID).findFirst();
                movie.deleteFromRealm();
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }
}
