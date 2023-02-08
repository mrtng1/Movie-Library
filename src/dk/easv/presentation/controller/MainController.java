package dk.easv.presentation.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.easv.entities.*;
import dk.easv.entities.api.Result;
import dk.easv.presentation.model.AppModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import dk.easv.entities.api.TMDB;
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
    private ListView<User> lvUsers;
    @FXML
    private ListView<Movie> lvTopForUser, lvTopAvgNotSeen;
    @FXML
    private ListView<UserSimilarity> lvTopSimilarUsers;
    @FXML
    private ListView<TopMovie> lvTopFromSimilar;
    @FXML
    private ImageView imgPoster;

    private AppModel model;
    private long timerStartMillis = 0;
    private String timerMsg = "";

    public void setModel(AppModel model) {
        this.model = model;

        lvUsers.setItems(model.getObsUsers());
        lvTopForUser.setItems(model.getObsTopMovieSeen());
        lvTopAvgNotSeen.setItems(model.getObsTopMovieNotSeen());
        lvTopSimilarUsers.setItems(model.getObsSimilarUsers());
        lvTopFromSimilar.setItems(model.getObsTopMoviesSimilarUsers());

        startTimer("Load users");
        model.loadUsers();
        stopTimer();

        lvUsers.getSelectionModel().selectedItemProperty().addListener(
                (observableValue, oldUser, selectedUser) -> {
                    startTimer("Loading all data for user: " + selectedUser);
                    model.loadData(selectedUser);
                });

        // Select the logged-in user in the listview, automagically trigger the listener above
        lvUsers.getSelectionModel().select(model.getObsLoggedInUser());
        printMethod();
    }

    public void printMethod() {
        System.out.println(model.getObsTopMoviesSimilarUsers());
    }

    private void handleButtonAction(ActionEvent event) {
        try {

            String query = " ";
            String apiKey="46e91ce5acfdab6d23d26f340d638a2d";
            String imagePath="https://image.tmdb.org/t/p/w400/";
            String uri =
                    "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey +
                            "&language=en-US&query=" + query +
                            "&page=1&include_adult=false";

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
                Result r = p.getResults().get(0);
                imgPoster.imageProperty().setValue(new Image(imagePath+r.getPoster_path()));
                System.out.println(imagePath+r.getPoster_path());
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
        System.out.println(timerMsg + " took : " + (System.currentTimeMillis() - timerStartMillis) + "ms");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}