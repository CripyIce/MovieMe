package aflalcom.eitan.movieslib.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import aflalcom.eitan.movieslib.EditMovieActivity;
import aflalcom.eitan.movieslib.R;
import aflalcom.eitan.movieslib.model.Movie;
import io.realm.Realm;

/**
 * Created by Eitan on 25/09/2016.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MyViewHolder> {

    private Activity activity;
    private Context context;
    private List<Movie> movieList;
    private Realm realm;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CardView movieSearchCV;
        ImageView movieSearchImageView;
        TextView movieSearchTitle;

        public MyViewHolder(View itemView) {
            super(itemView);
            realm = Realm.getDefaultInstance();
            movieSearchCV = (CardView) itemView.findViewById(R.id.movieSearchCV);
            movieSearchImageView = (ImageView) itemView.findViewById(R.id.movieSearchImageView);
            movieSearchTitle = (TextView) itemView.findViewById(R.id.movieSearchTitle);
        }
    }

    public MovieAdapter(Activity activity, Context context, List<Movie> movieList) {
        this.activity = activity;
        this.context = context;
        this.movieList = movieList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_search_cv, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        if (!movieList.get(position).getPoster().equals("N/A")) {
            Picasso.with(context).load(movieList.get(position).getPoster())
                    .placeholder(R.drawable.no_image).fit().into(holder.movieSearchImageView);
        }
        holder.movieSearchTitle.setText(movieList.get(position).getTitle()
                + " (" + movieList.get(position).getYear() + ")");

        holder.movieSearchCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (realm.where(Movie.class).equalTo("imdbID", movieList.get(holder.getAdapterPosition()).getImdbID()).count() == 1) {
                    Snackbar snackbar = Snackbar.make(v, activity.getString(R.string.movie_already_exists), Snackbar.LENGTH_SHORT);
                    View view = snackbar.getView();
                    TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    snackbar.show();
                    return;
                }

                Intent intent = new Intent(context, EditMovieActivity.class);
                intent.putExtra("Activity", "Search");
                intent.putExtra("imdbID", movieList.get(holder.getAdapterPosition()).getImdbID());
                activity.startActivityForResult(intent, 2000);
                activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public void clearData() {
        int size = this.movieList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.movieList.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }


}
