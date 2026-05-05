package gui.Events;

import entities.Event;
import entities.Users;
import entities.Reservation;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import services.EventService;
import services.UsersService;
import services.ReservationService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationController {

    @FXML
    private VBox cardsContainer;

    @FXML
    private ComboBox<Event> cbEvent;
    @FXML
    private ComboBox<Users> cbUser;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private TextField txtReservedAt;

    private ReservationService reservationService = new ReservationService();
    private EventService eventService = new EventService();
    private UsersService usersService = new UsersService();

    private Reservation selectedReservation;

    @FXML
    public void initialize() {
        setupComboBoxes();
        showReservations();
    }

    private void setupComboBoxes() {
        try {
            // Events
            List<Event> events = eventService.afficher();
            cbEvent.setItems(FXCollections.observableArrayList(events));
            cbEvent.setConverter(new StringConverter<Event>() {
                @Override public String toString(Event object) { return object != null ? object.getName() : ""; }
                @Override public Event fromString(String string) { return null; }
            });

            // Users
            List<Users> users = usersService.afficher();
            cbUser.setItems(FXCollections.observableArrayList(users));
            cbUser.setConverter(new StringConverter<Users>() {
                @Override public String toString(Users object) { return object != null ? object.getUsername() : ""; }
                @Override public Users fromString(String string) { return null; }
            });

            // Status
            cbStatus.setItems(FXCollections.observableArrayList("Pending", "Validated", "Cancelled"));

            txtReservedAt.setText(LocalDateTime.now().toString().substring(0, 10));

        } catch (SQLException e) {
            showAlert("Error", "Could not initialize form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void showReservations() {
        try {
            List<Reservation> list = reservationService.afficher();
            cardsContainer.getChildren().clear();

            for (Reservation reservation : list) {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/events/ReservationCard.fxml"));
                    javafx.scene.Node card = loader.load();
                    ReservationCardController controller = loader.getController();
                    controller.setData(reservation, this);
                    cardsContainer.getChildren().add(card);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            showAlert("Error", "Could not load reservations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setSelectedReservation(Reservation reservation) {
        this.selectedReservation = reservation;
        if (reservation != null) {
            for (Event e : cbEvent.getItems()) {
                if (reservation.getEvent() != null && e.getId() == reservation.getEvent().getId()) {
                    cbEvent.setValue(e);
                    break;
                }
            }
            for (Users p : cbUser.getItems()) {
                if (reservation.getUser() != null && p.getId() == reservation.getUser().getId()) {
                    cbUser.setValue(p);
                    break;
                }
            }
            cbStatus.setValue(reservation.getStatus());
            txtReservedAt.setText(reservation.getReservedAt());
        }
    }

    @FXML
    void addReservation(ActionEvent event) {
        if (cbEvent.getValue() == null || cbUser.getValue() == null || cbStatus.getValue() == null) {
            showAlert("Warning", "Please fill all fields", Alert.AlertType.WARNING);
            return;
        }
        try {
            Reservation r = new Reservation();
            r.setEvent(cbEvent.getValue());
            r.setUser(cbUser.getValue());
            r.setStatus(cbStatus.getValue());
            r.setReservedAt(txtReservedAt.getText());

            reservationService.ajouter(r);
            showAlert("Success", "Reservation created successfully!", Alert.AlertType.INFORMATION);
            showReservations();
            clearForm();
        } catch (SQLException ex) {
            showAlert("Error", "Could not create reservation: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void updateReservation(ActionEvent event) {
        if (selectedReservation == null) {
            showAlert("Warning", "Please select a reservation to update", Alert.AlertType.WARNING);
            return;
        }
        try {
            selectedReservation.setEvent(cbEvent.getValue());
            selectedReservation.setUser(cbUser.getValue());
            selectedReservation.setStatus(cbStatus.getValue());
            selectedReservation.setReservedAt(txtReservedAt.getText());

            reservationService.modifier(selectedReservation);
            showAlert("Success", "Reservation updated successfully!", Alert.AlertType.INFORMATION);
            showReservations();
        } catch (SQLException ex) {
            showAlert("Error", "Could not update reservation: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void deleteReservation(ActionEvent event) {
        if (selectedReservation == null) {
            showAlert("Warning", "Please select a reservation to cancel", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Are you sure you want to cancel this reservation?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try {
                reservationService.supprimer(selectedReservation.getId());
                showAlert("Success", "Reservation cancelled successfully!", Alert.AlertType.INFORMATION);
                showReservations();
                clearForm();
                selectedReservation = null;
            } catch (SQLException ex) {
                showAlert("Error", "Could not delete reservation: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void clearForm() {
        cbEvent.setValue(null);
        cbUser.setValue(null);
        cbStatus.setValue(null);
        txtReservedAt.setText(LocalDateTime.now().toString().substring(0, 10));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
