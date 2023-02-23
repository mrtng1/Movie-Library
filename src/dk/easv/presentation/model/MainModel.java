package dk.easv.presentation.model;

import dk.easv.entities.*;
import dk.easv.logic.LogicManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class MainModel {
    LogicManager logic = new LogicManager();
    // Models of the data in the view
    private final ObservableList<User>  obsUsers = FXCollections.observableArrayList();
    private final SimpleObjectProperty<User> obsLoggedInUser = new SimpleObjectProperty<>();

    public void loadUsers(){
        obsUsers.clear();
        obsUsers.addAll(logic.getAllUsers());
    }

    public List<TopMovie> getTopMoviesFromSimilarPeople(User u) {
        return logic.getTopMoviesFromSimilarPeople(u);
    }

    public List<Movie> getTopAverageRatedMoviesUserDidNotSee(User u) {
        return logic.getTopAverageRatedMoviesUserDidNotSee(u);
    }

    public List<Movie> getTopAverageRatedMovies(User u) {
        return logic.getTopAverageRatedMovies(u);
    }

    public ObservableList<User> getObsUsers() {
        return obsUsers;
    }

    public User getObsLoggedInUser() {
        return obsLoggedInUser.get();
    }

    public boolean loginUserFromUsername(String userName) {
        User u = logic.getUser(userName);
        obsLoggedInUser.set(u);
        if (u==null)
            return false;
        else
            return true;
    }
}