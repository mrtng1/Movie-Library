package dk.easv.presentation.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.easv.entities.*;
import dk.easv.entities.api.Result;
import dk.easv.presentation.model.AppModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import dk.easv.entities.api.TMDB;
import javafx.scene.layout.HBox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    @FXML
    private Label nameLabel;
    @FXML
    private HBox hb;
    @FXML
    private ImageView imgPoster;
    private AppModel model;
    private String username;
    private long timerStartMillis = 0;
    private String timerMsg = " ";
    private User user;

    public void setUsername(String username) {
        this.username = username;

        nameLabel.setText(username);
    }

    public void setModel(AppModel model) {
        this.model = model;

        // Loading users...
        startTimer("Load users");
        model.loadUsers();
        stopTimer();

        // Information about the user
        getLoggedUser();

        // Getting top movies from similar people to our USER
        model.getTopMoviesFromSimilarPeople(user);
        System.out.println(model.getTopMoviesFromSimilarPeople(user));
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


    public void apiSet() {
        TopMovie movie = model.getObsTopMoviesSimilarUsers().get(0);
        System.out.println(movie.getMovie());

        try {
            String query = " ";
            String apiKey="46e91ce5acfdab6d23d26f340d638a2d";
            String imagePath="https://image.tmdb.org/t/p/w400/";
            String uri =
                    "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey +
                            "&language=en-US&query=" + query +
                            "&page=1&include_adult=false";

            URL apiUrl = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
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
                Result r = results.isEmpty() ? new Result() : results.get(0);

                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("dk/easv/presentation/view/card.fxml"));
                hb = fxmlLoader.load();
                if (r.getPoster_path()==null){
                    System.out.println("image not found");
                }
                else {
                    imgPoster.imageProperty().setValue(new Image(imagePath + r.getPoster_path()));
                    System.out.println(imagePath + r.getPoster_path());
                }
            }
            conn.disconnect();
        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
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