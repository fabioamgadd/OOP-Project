package hotel.gui.controllers;

import hotel.enums.Gender;
import hotel.gui.utils.AlertHelper;
import hotel.gui.utils.SceneManager;
import hotel.interfaces.Authenticatable;
import hotel.models.Admin;
import hotel.models.Guest;
import hotel.models.Receptionist;
import hotel.models.Staff;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private javafx.scene.layout.VBox loginPane;
    @FXML private javafx.scene.layout.VBox registerPane;
    @FXML private Button loginTabBtn;
    @FXML private Button registerTabBtn;

    @FXML private TextField    loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Label         loginError;

    @FXML private TextField   regUsername;
    @FXML private PasswordField regPassword;
    @FXML private DatePicker  regDOB;
    @FXML private ComboBox<String> regGender;
    @FXML private TextField   regAddress;
    @FXML private Label       registerError;
    @FXML private Label       registerSuccess;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        regGender.getItems().addAll("Male", "Female");
        loginError.setText("");
        registerError.setText("");
        registerSuccess.setText("");
    }


    @FXML
    private void showLogin() {
        loginPane.setVisible(true);
        loginPane.setManaged(true);
        registerPane.setVisible(false);
        registerPane.setManaged(false);
        loginTabBtn.getStyleClass().remove("btn-secondary");
        if (!loginTabBtn.getStyleClass().contains("btn-primary"))
            loginTabBtn.getStyleClass().add("btn-primary");
        registerTabBtn.getStyleClass().remove("btn-primary");
        if (!registerTabBtn.getStyleClass().contains("btn-secondary"))
            registerTabBtn.getStyleClass().add("btn-secondary");
    }

    @FXML
    private void showRegister() {
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        registerPane.setVisible(true);
        registerPane.setManaged(true);
        registerTabBtn.getStyleClass().remove("btn-secondary");
        if (!registerTabBtn.getStyleClass().contains("btn-primary"))
            registerTabBtn.getStyleClass().add("btn-primary");
        loginTabBtn.getStyleClass().remove("btn-primary");
        if (!loginTabBtn.getStyleClass().contains("btn-secondary"))
            loginTabBtn.getStyleClass().add("btn-secondary");
    }


    @FXML
    private void handleLogin() {
        loginError.setText("");
        String username = loginUsername.getText().trim();
        String password = loginPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            loginError.setText("Please enter both username and password.");
            return;
        }

        SceneManager sm = SceneManager.getInstance();
        Authenticatable user = sm.authService.login(username, password);

        if (user == null) {
            loginError.setText("Invalid username or password.");
            loginPassword.clear();
            return;
        }

        if (user instanceof Guest) {
            sm.showGuestDashboard((Guest) user);
        } else if (user instanceof Admin || user instanceof Receptionist) {
            sm.showStaffDashboard((Staff) user);
        } else {
            loginError.setText("Unknown account type.");
        }
    }


    @FXML
    private void handleRegister() {
        registerError.setText("");
        registerSuccess.setText("");

        String username = regUsername.getText().trim();
        String password = regPassword.getText();
        LocalDate dob   = regDOB.getValue();
        String address  = regAddress.getText().trim();
        String genderStr = regGender.getValue();

        if (username.isEmpty() || password.isEmpty() || dob == null
                || address.isEmpty() || genderStr == null) {
            registerError.setText("All fields are required.");
            return;
        }

        Gender gender = genderStr.equals("Male") ? Gender.MALE : Gender.FEMALE;

        try {
            SceneManager sm = SceneManager.getInstance();
            Guest guest = sm.authService.registerGuest(username, password, dob, address, gender);
            registerSuccess.setText("Account created! ID: " + guest.getGuestId()
                    + "  — You can now log in.");
            registerError.setText("");
            regUsername.clear();
            regPassword.clear();
            regDOB.setValue(null);
            regAddress.clear();
            regGender.setValue(null);
        } catch (IllegalArgumentException e) {
            registerError.setText(e.getMessage());
        }
    }
}
