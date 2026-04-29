package gui.Events;

import entities.Event;
import javafx.collections.FXCollections;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;


import javafx.scene.layout.VBox;
import services.EventService;

import java.sql.SQLException;
import java.util.List;

public class EventController {

    @FXML
    private VBox cardsContainer;

    private Event selectedEvent;


    @FXML
    private TextField txtName;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private TextField txtCategory;
    @FXML
    private DatePicker dpDate;
    @FXML
    private TextField txtTime;
    @FXML
    private TextField txtCapacity;
    @FXML
    private TextField txtOrganizer;
    @FXML
    private TextArea txtDescription;

    private EventService eventService = new EventService();

    @FXML
    public void initialize() {
        cbType.setItems(FXCollections.observableArrayList("In Person", "Online"));
        showEvents();
    }

    public void showEvents() {
        try {
            List<Event> list = eventService.afficher();
            cardsContainer.getChildren().clear();

            for (Event event : list) {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/events/EventCard.fxml"));
                    javafx.scene.Node card = loader.load();
                    EventCardController controller = loader.getController();
                    controller.setData(event, this);
                    cardsContainer.getChildren().add(card);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
            
        } catch (SQLException e) {
            showAlert("Error", "Could not load events: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setSelectedEvent(Event event) {
        this.selectedEvent = event;
        if (event != null) {
            txtName.setText(event.getName());
            cbType.setValue(event.getType());
            txtCategory.setText(event.getCategory());
            if (event.getDate() != null && !event.getDate().isEmpty()) {
                dpDate.setValue(java.time.LocalDate.parse(event.getDate()));
            }
            txtTime.setText(event.getTime());
            txtCapacity.setText(String.valueOf(event.getCapacity()));
            txtOrganizer.setText(event.getOrganizer());
            txtDescription.setText(event.getDescription());
        }
    }


    private boolean validateForm() {
        String name = txtName.getText().trim();
        String type = cbType.getValue();
        String category = txtCategory.getText().trim();
        String capacityStr = txtCapacity.getText().trim();
        String organizer = txtOrganizer.getText().trim();
        String time = txtTime.getText().trim();

        if (name.isEmpty() || type == null || type.isEmpty() || category.isEmpty() || organizer.isEmpty() || time.isEmpty()) {
            showAlert("Validation Error", "All fields except description are required.", Alert.AlertType.WARNING);
            return false;
        }

        // Name constraint: No numbers
        if (name.matches(".*\\d.*")) {
            showAlert("Validation Error", "Event name cannot contain numbers.", Alert.AlertType.WARNING);
            return false;
        }

        // Organizer constraint: No numbers
        if (organizer.matches(".*\\d.*")) {
            showAlert("Validation Error", "Organizer name cannot contain numbers.", Alert.AlertType.WARNING);
            return false;
        }

        if (dpDate.getValue() == null) {
            showAlert("Validation Error", "Please select a date.", Alert.AlertType.WARNING);
            return false;
        }

        // Basic Time format check (HH:mm or HH:mm:ss)
        if (!time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$")) {
            showAlert("Validation Error", "Time must be in HH:mm or HH:mm:ss format.", Alert.AlertType.WARNING);
            return false;
        }

        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                showAlert("Validation Error", "Capacity must be a positive number.", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException ex) {
            showAlert("Validation Error", "Capacity must be a valid number.", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    @FXML
    void addEvent(ActionEvent event) {
        if (!validateForm()) return;
        try {
            Event e = new Event();
            e.setName(txtName.getText());
            e.setType(cbType.getValue());
            e.setCategory(txtCategory.getText());
            e.setDate(dpDate.getValue().toString());
            e.setTime(txtTime.getText());
            e.setCapacity(Integer.parseInt(txtCapacity.getText()));
            e.setOrganizer(txtOrganizer.getText());
            e.setDescription(txtDescription.getText());
            e.setCreatedAt(java.time.LocalDateTime.now().toString());

            eventService.ajouter(e);
            showAlert("Success", "Event added successfully!", Alert.AlertType.INFORMATION);
            showEvents();
            clearForm();
        } catch (Exception ex) {
            showAlert("Error", "Could not add event: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void updateEvent(ActionEvent event) {
        if (selectedEvent == null) {
            showAlert("Warning", "Please select an event to update", Alert.AlertType.WARNING);
            return;
        }

        if (!validateForm()) return;

        try {
            selectedEvent.setName(txtName.getText());
            selectedEvent.setType(cbType.getValue());
            selectedEvent.setCategory(txtCategory.getText());
            selectedEvent.setDate(dpDate.getValue().toString());
            selectedEvent.setTime(txtTime.getText());
            selectedEvent.setCapacity(Integer.parseInt(txtCapacity.getText()));
            selectedEvent.setOrganizer(txtOrganizer.getText());
            selectedEvent.setDescription(txtDescription.getText());

            eventService.modifier(selectedEvent);
            showAlert("Success", "Event updated successfully!", Alert.AlertType.INFORMATION);
            showEvents();
        } catch (Exception ex) {
            showAlert("Error", "Could not update event: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }


    @FXML
    void deleteEvent(ActionEvent event) {
        if (selectedEvent == null) {
            showAlert("Warning", "Please select an event to delete", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this event?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try {
                eventService.supprimer(selectedEvent.getId());
                showAlert("Success", "Event deleted successfully!", Alert.AlertType.INFORMATION);
                showEvents();
                clearForm();
                selectedEvent = null;
            } catch (SQLException ex) {
                showAlert("Error", "Could not delete event: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }


    // selectEvent is now handled via setSelectedEvent from EventCardController


    private void clearForm() {
        txtName.clear();
        cbType.setValue(null);
        txtCategory.clear();
        dpDate.setValue(null);
        txtTime.clear();
        txtCapacity.clear();
        txtOrganizer.clear();
        txtDescription.clear();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
