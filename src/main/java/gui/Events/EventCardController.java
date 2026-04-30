package gui.Events;

import entities.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
    private Label lblWeather;
    @FXML
    private Label lblIcon;
    @FXML
    private ImageView imgEvent;

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

        // Handle Weather
        if (event.getDate() != null && !event.getDate().isEmpty()) {
            try {
                java.time.LocalDate eventDate = java.time.LocalDate.parse(event.getDate());
                new Thread(() -> {
                    String weather = utils.WeatherService.getWeatherForDate(eventDate);
                    javafx.application.Platform.runLater(() -> {
                        lblWeather.setText(weather);
                    });
                }).start();
            } catch (Exception e) {
                lblWeather.setText("");
            }
        } else {
            lblWeather.setText("");
        }

        // Handle Image
        if (event.getImagePath() != null && !event.getImagePath().isEmpty()) {
            try {
                Image img = new Image(event.getImagePath(), true); // true for background loading
                imgEvent.setImage(img);
                lblIcon.setVisible(false);
                imgEvent.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                imgEvent.setVisible(false);
                lblIcon.setVisible(true);
            }
        } else {
            imgEvent.setVisible(false);
            lblIcon.setVisible(true);
        }

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
