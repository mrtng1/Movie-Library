package dk.easv.presentation.controller;

import dk.easv.presentation.model.AppModel;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LogInController implements Initializable {
    @FXML
    private AnchorPane loginPane;
    @FXML private PasswordField passwordField;
    @FXML private TextField userId;
    @FXML
    private Button exitButton;
    private AppModel model;

    @FXML
    private void makeAnimation() {
        TranslateTransition animation = new TranslateTransition();
        animation.setNode(loginPane);
        animation.setDuration(Duration.millis(200));
        animation.setByX(50);
        animation.setByY(-50);
        animation.play();
    }

    public void logIn(ActionEvent actionEvent) {
        model.loadUsers();
        model.loginUserFromUsername(userId.getText());
        if (model.getObsLoggedInUser() != null) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/presentation/view/App.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();

                AppController controller = loader.getController();
                controller.setModel(model);

            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load App.fxml");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Wrong username or password");
            alert.showAndWait();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model = new AppModel();
        exitButton.setOnAction(event -> {Platform.exit();});
    }
}