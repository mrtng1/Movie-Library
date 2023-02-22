package dk.easv.presentation.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.easv.entities.*;
import dk.easv.entities.api.Result;
import dk.easv.presentation.model.MainModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import dk.easv.entities.api.TMDB;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class MainController implements Initializable {
    @FXML
    private BorderPane rootLayout;
    @FXML
    private Label nameLabel;
    @FXML
    private HBox hb;
    @FXML
    private ImageView imgPoster;
    private MainModel model;
    private String username;
    private long timerStartMillis = 0;
    private String timerMsg = " ";
    private User user;

    public void setUsername(String username) {
        this.username = username;
        nameLabel.setText(username);
    }

    public void setModel(MainModel model) {
        this.model = model;

        // Information about the user
        getLoggedUser();

        // Getting top movies from similar people to our USER
        startTimer("Loading movies");
        loopMovies();
        stopTimer();
    }

    public void loopMovies() {
        List<TopMovie> movieTitles = model.getTopMoviesFromSimilarPeople(user);

        int limit = 10;
        int counter = 0;

        for (TopMovie movieTitle : movieTitles) {
            if (counter == limit) {
                break;
            }

            try {
                String query = movieTitle.getTitle();
                String encodedQuery = URLEncoder.encode(query, "UTF-8");

                String apiKey="46e91ce5acfdab6d23d26f340d638a2d";
                String imagePath="https://image.tmdb.org/t/p/w400/";
                String uri =
                        "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey +
                                "&language=en-US&query=" + encodedQuery +
                                "&page=1&include_adult=true";

                URL url = new URL(uri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

                try(Reader reader = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())))){

                    Gson gson = new GsonBuilder().create();
                    TMDB p = gson.fromJson(reader, TMDB.class);

                    List<Result> results = p.getResults();
                    if (results.isEmpty()){
                        System.out.println("No results found for movie: " + query);
                    }
                    else {
                        Result r = results.isEmpty() ? new Result() : results.get(0);

                        if (imgPoster!=null) {
                            imgPoster.imageProperty().setValue(new Image(imagePath + r.getPoster_path()));
                        }

                        System.out.println(imagePath + r.getPoster_path());
                    }
                }
                conn.disconnect();

            } catch (MalformedURLException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();
            }

            //System.out.println(movieTitle.getTitle());
            counter++;
        }
    }

    public void getLoggedUser () {
        String name = username;
        user = null;

        List<User> users = model.getObsUsers();
        for (User user : users) {
            if (user.getName().equals(name)) {
                this.user = user;
                break;
            }
        }

        if (user != null) {
            System.out.println("User found: " + user);
        } else {
            System.out.println("User not found.");
        }
    }

    private void startTimer(String message){
        timerStartMillis = System.currentTimeMillis();
        timerMsg = message;
    }

    private void stopTimer(){
        System.out.println(timerMsg + " took: " + (System.currentTimeMillis() - timerStartMillis) + "ms");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}