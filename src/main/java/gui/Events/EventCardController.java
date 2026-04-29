package gui.Events;

import entities.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class EventCardController {

    @FXML
    private HBox cardRoot;
    @FXML
    private Label lblName;
    @FXML
    private Label lblType;
    @FXML
    private Label lblCategory;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblCapacity;
    @FXML
    private Label lblOrganizer;
    @FXML
    private Label lblIcon;

    private Event event;
    private EventController mainController;

    public void setData(Event event, EventController mainController) {
        this.event = event;
        this.mainController = mainController;

        lblName.setText(event.getName());
        lblType.setText(event.getType());
        lblCategory.setText(event.getCategory());
        lblDate.setText(event.getDate());
        lblCapacity.setText(String.valueOf(event.getCapacity()));
        lblOrganizer.setText(event.getOrganizer());

        // Change icon based on type
        if ("Online".equalsIgnoreCase(event.getType())) {
            lblIcon.setText("💻");
        } else {
            lblIcon.setText("🏢");
        }
    }

    @FXML
    void onCardClicked(MouseEvent event) {
        if (mainController != null) {
            mainController.setSelectedEvent(this.event);
        }
    }
}
