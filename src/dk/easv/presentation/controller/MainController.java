package dk.easv.presentation.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.easv.Main;
import dk.easv.entities.*;
import dk.easv.entities.api.Result;
import dk.easv.presentation.model.MainModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import dk.easv.entities.api.TMDB;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;

public class MainController implements Initializable {
    @FXML
    private BorderPane rootLayout;
    @FXML
    private HBox hbTopMoviesFromSimilarPeople, hbTopAverageRatedMoviesUserDidNotSee, hbTopAverageRatedMovies;
    @FXML
    private Label nameLabel, welcomeLabel;
    @FXML
    private Button logOutButton;
    private MainModel model;
    private User user;
    private String username;
    private long timerStartMillis = 0;
    private String timerMsg = " ";

    private static String getFirstName(String name) {
        int index = name.lastIndexOf(" ");
        if (index > -1) {
            return name.substring(0, index);
        }
        return name;
    }

    public void setUsername(String username) {
        this.username = username;
        nameLabel.setText(username);

        Random rand = new Random();
        List<String> greetings = Arrays.asList("Welcome, ", "Welcome back, ", "Hey! ", "Hello, ", "Howdy! ", "Nice to see you, ");
        welcomeLabel.setText(getFirstName(greetings.get(rand.nextInt(greetings.toArray().length)) + username));
    }

    public void setModel(MainModel model) {
        this.model = model;

        // information about the user
        getLoggedUser();

        // executing threads
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(this::getTopMoviesFromSimilarPeople);
        executor.execute(this::getTopAverageRatedMoviesUserDidNotSee);
        executor.execute(this::getTopAverageRatedMovies);

        logOut();
    }

    public void getTopMoviesFromSimilarPeople() {
        List<TopMovie> movieTitles = model.getTopMoviesFromSimilarPeople(user);

        int limit = 15;
        int counter = 0;

        for (TopMovie movieTitle : movieTitles) {
            if (counter == limit) {
                break;
            }

            try {
                String query = movieTitle.getTitle();

                int colonIndex = query.lastIndexOf(":");
                if (colonIndex != -1) { // If ":" is found in the string
                    query = query.substring(0, colonIndex).trim(); // Remove text after last ":"
                }
                String encodedQuery = URLEncoder.encode(query, "UTF-8");

                String apiKey = "46e91ce5acfdab6d23d26f340d638a2d";
                String imagePath = "https://image.tmdb.org/t/p/w400/";
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

                try (Reader reader = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())))) {

                    Gson gson = new GsonBuilder().create();
                    TMDB p = gson.fromJson(reader, TMDB.class);

                    List<Result> results = p.getResults();
                    if (results.isEmpty()) {
                        System.out.println("No results found for movie: " + query);
                    } else {
                        Result r = results.isEmpty() ? null : results.get(0);

                        // Loading movie cards
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/presentation/view/card.fxml"));
                        AnchorPane root = loader.load();
                        if (r!=null && r.getPoster_path()!=null) {
                            if(!r.getPoster_path().isEmpty()) {
                                ImageView imgPoster = (ImageView) (root.getChildren().get(0));
                                imgPoster.setImage(new Image(imagePath + r.getPoster_path()));
                            }
                        } else System.out.println("No results found for movie");
                        // Add the root to the HBox in the JavaFX Application Thread
                        Platform.runLater(() -> {
                            hbTopMoviesFromSimilarPeople.getChildren().add(root);
                        });
                    }
                }
                conn.disconnect();

            } catch (MalformedURLException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();
            }
            counter++;
        }


    }


    public void getTopAverageRatedMoviesUserDidNotSee() {
        List<Movie> movieTitles = model.getTopAverageRatedMoviesUserDidNotSee(user);
        int limit = 15;
        int counter = 0;

        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future<Void>> futures = new ArrayList<>(); // creating a list to store the future objects

        for (Movie movieTitle : movieTitles) {
            if (counter == limit) {
                break;
            }

            futures.add(executor.submit(() -> {
                try {
                    String query = movieTitle.getTitle();

                    int colonIndex = query.lastIndexOf(":");
                    if (colonIndex != -1) {
                        query = query.substring(0, colonIndex).trim();
                    }
                    String encodedQuery = URLEncoder.encode(query, "UTF-8");

                    String apiKey = "46e91ce5acfdab6d23d26f340d638a2d";
                    String imagePath = "https://image.tmdb.org/t/p/w400/";
                    String uri = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey +
                            "&language=en-US&query=" + encodedQuery +
                            "&page=1&include_adult=true";

                    URL url = new URL(uri);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    if (conn.getResponseCode() != 200) {
                        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                    }

                    try (Reader reader = new BufferedReader(new InputStreamReader((conn.getInputStream())))) {
                        Gson gson = new GsonBuilder().create();
                        TMDB p = gson.fromJson(reader, TMDB.class);
                        List<Result> results = p.getResults();
                        Result r = results.isEmpty() ? null : results.get(0);

                        Platform.runLater(() -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/presentation/view/card.fxml"));
                                AnchorPane root = loader.load();

                                if (r!=null && r.getPoster_path()!=null) {
                                    if(!r.getPoster_path().isEmpty()) {
                                        ImageView imgPoster = (ImageView) (root.getChildren().get(0));
                                        imgPoster.setImage(new Image(imagePath + r.getPoster_path()));
                                    }
                                } else System.out.println("No results found for movie");

                                hbTopAverageRatedMoviesUserDidNotSee.getChildren().add(root);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    conn.disconnect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }));
            counter++;
        }

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
    }

    public void getTopAverageRatedMovies() {
        List<Movie> movieTitles = model.getTopAverageRatedMovies(user);

        int limit = 15;
        int counter = 0;

        ExecutorService executor = Executors.newCachedThreadPool();

        for (Movie movieTitle : movieTitles) {
            if (counter == limit) {
                break;
            }

            String query = movieTitle.getTitle();

            int colonIndex = query.lastIndexOf(":");
            if (colonIndex != -1) { // If ":" is found in the string
                query = query.substring(0, colonIndex).trim(); // Remove text after last ":"
            }
            String encodedQuery = null;
            try {
                encodedQuery = URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            String apiKey = "46e91ce5acfdab6d23d26f340d638a2d";
            String imagePath = "https://image.tmdb.org/t/p/w400/";
            String uri =
                    "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey +
                            "&language=en-US&query=" + encodedQuery +
                            "&page=1&include_adult=true";

            String finalQuery = query;
            executor.execute(() -> {
                try {
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

                    try (Reader reader = new BufferedReader(new InputStreamReader(
                            (conn.getInputStream())))) {

                        Gson gson = new GsonBuilder().create();
                        TMDB p = gson.fromJson(reader, TMDB.class);

                        List<Result> results = p.getResults();
                        if (results.isEmpty()) {
                            System.out.println("No results found for movie: " + finalQuery);
                        } else {
                            Result r = results.isEmpty() ? null : results.get(0);

                            // Loading movie cards
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/presentation/view/card.fxml"));
                            AnchorPane root = loader.load();

                            if (r!=null && r.getPoster_path()!=null) {
                                if(!r.getPoster_path().isEmpty()) {
                                    ImageView imgPoster = (ImageView) (root.getChildren().get(0));
                                    imgPoster.setImage(new Image(imagePath + r.getPoster_path()));
                                }
                            } else System.out.println("No results found for movie");

                            Platform.runLater(() -> hbTopAverageRatedMovies.getChildren().add(root));
                        }
                    }
                    conn.disconnect();

                } catch (MalformedURLException e) {

                    e.printStackTrace();

                } catch (IOException e) {

                    e.printStackTrace();
                }
            });

            counter++;
        }
        executor.shutdown();
    }

    public void getLoggedUser() {
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

    private void logOut() {
        logOutButton.setOnAction(event -> {
            Stage stage = (Stage) rootLayout.getScene().getWindow();
            stage.close();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/presentation/view/loginWindow.fxml"));
                Parent root = loader.load();
                Stage newStage = new Stage();
                Scene scene = new Scene(root);

                Main main = new Main();
                main.movableWindow(scene, newStage);
                scene.setFill(Color.TRANSPARENT);
                newStage.initStyle(StageStyle.TRANSPARENT);
                newStage.setScene(scene);
                newStage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}