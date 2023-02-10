package dk.easv.presentation.controller;

import dk.easv.presentation.model.MainModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private PasswordField passwordField;
    @FXML private TextField userId;
    @FXML
    private Button exitButton;
    private MainModel model;

    public void login() {
        model.loadUsers();
        model.loginUserFromUsername(userId.getText());
        if (model.getObsLoggedInUser() != null) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/presentation/view/mainWindow.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                Scene scene = new Scene(root);

                //scene.setFill(Color.TRANSPARENT);
                //stage.initStyle(StageStyle.TRANSPARENT);
                stage.setScene(scene);
                stage.show();

                MainController controller = loader.getController();
                controller.setUsername(userId.getText());
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
        model = new MainModel();
        exitButton.setOnAction(event -> {Platform.exit();});
    }
}