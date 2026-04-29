package gui.Events;

import entities.Reservation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class ReservationCardController {

    @FXML private HBox cardRoot;
    @FXML private Label lblStatusIcon;
    @FXML private Label lblEventName;
    @FXML private Label lblUser;
    @FXML private Label lblStatus;
    @FXML private Label lblDate;

    private Reservation reservation;
    private ReservationController mainController;

    public void setData(Reservation reservation, ReservationController mainController) {
        this.reservation = reservation;
        this.mainController = mainController;

        lblEventName.setText(reservation.getEvent() != null ? reservation.getEvent().getName() : "N/A");
        lblUser.setText(reservation.getUser() != null ? reservation.getUser().getUsername() : "N/A");
        lblStatus.setText(reservation.getStatus());
        lblDate.setText(reservation.getReservedAt());

        // Change icon & badge color based on status
        String status = reservation.getStatus() != null ? reservation.getStatus().toLowerCase() : "";
        switch (status) {
            case "validated":
                lblStatusIcon.setText("✅");
                lblStatus.setStyle("-fx-background-color: #38a169; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 50; -fx-padding: 3 8 3 8;");
                break;
            case "cancelled":
                lblStatusIcon.setText("❌");
                lblStatus.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 50; -fx-padding: 3 8 3 8;");
                break;
            default: // pending
                lblStatusIcon.setText("⏳");
                lblStatus.setStyle("-fx-background-color: #dd6b20; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 50; -fx-padding: 3 8 3 8;");
                break;
        }
    }

    @FXML
    void onCardClicked(MouseEvent event) {
        if (mainController != null) {
            mainController.setSelectedReservation(this.reservation);
        }
    }
}
