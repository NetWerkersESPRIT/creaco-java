package gui.collab.contract;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AddContractController {
    @FXML private Label infoLabel;

    @FXML
    public void initialize() {
        infoLabel.setText("Contracts are generated from approved collaboration requests.");
    }
}
