package aflalcom.eitan.movieslib.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Eitan on 24/09/2016.
 */

public class Movie extends RealmObject {

    private String title;
    private String poster;
    private int year;
    private String imdbID;
    private boolean watched;

    private String released;
    private String rated;
    private String runtime;
    private String genre;
    private String director;
    private String actors;
    private String plot;
    private String lang;
    private String country;
    private String imdbRate;
    private float userRate;

    public float getUserRate() {
        return userRate;
    }

    public void setUserRate(float userRate) {
        this.userRate = userRate;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public String getImdbVotes() {
        return imdbVotes;
    }

    public void setImdbVotes(String imdbVotes) {
        this.imdbVotes = imdbVotes;
    }

    public String getReleased() {
        return released;
    }

    public void setReleased(String released) {
        this.released = released;
    }

    public String getRated() {
        return rated;
    }

    public void setRated(String rated) {
        this.rated = rated;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getImdbRate() {
        return imdbRate;
    }

    public void setImdbRate(String imdbRate) {
        this.imdbRate = imdbRate;
    }

    private String imdbVotes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;

    public Movie() {

    }

    public Movie(String title, String poster, int year, String imdbID) {
        this.title = title;
        this.poster = poster;
        this.year = year;
        this.imdbID = imdbID;
    }

    public Movie(int id, String title, String poster, int year, String imdbID, float userRate, boolean watched) {
        this.id = id;
        this.title = title;
        this.poster = poster;
        this.year = year;
        this.imdbID = imdbID;
        this.userRate = userRate;
        this.watched = watched;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }


}
