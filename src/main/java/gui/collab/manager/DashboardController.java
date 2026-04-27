package gui.collab.manager;

import entities.CollabRequest;
import entities.Contract;
import entities.Users;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import services.CollabRequestService;
import services.ContractService;
import utils.SessionManager;
import java.math.BigDecimal;
import java.util.List;

public class DashboardController {

    @FXML private StackPane contentArea;
    @FXML private Button btnHub;
    @FXML private Button btnReview;
    @FXML private Button btnContracts;

    private final ContractService contractService = new ContractService();
    private final CollabRequestService requestService = new CollabRequestService();

    @FXML
    public void initialize() {
        showHub();
    }

    @FXML
    private void showHub() {
        setActiveTab(btnHub);
        loadView("/gui/collab/manager/hub_overview.fxml");
    }

    @FXML
    private void showReview() {
        setActiveTab(btnReview);
        try {
            java.net.URL resource = getClass().getResource("/gui/collab/manager/review_list.fxml");
            if (resource == null) {
                System.err.println("CRITICAL ERROR: Could not find review_list.fxml at /gui/collab/manager/review_list.fxml");
                return;
            }
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(resource);
            javafx.scene.Parent root = loader.load();
            ReviewListController controller = loader.getController();
            
            if (controller != null) {
                controller.setOnReviewRequested(req -> showReviewDetail(req));
            } else {
                System.err.println("WARNING: ReviewListController is null after loading review_list.fxml");
            }
            
            contentArea.getChildren().setAll(root);
        } catch (Throwable t) {
            System.err.println("CRITICAL ERROR loading Review Dashboard: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void showReviewDetail(entities.CollabRequest req) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/collab/manager/review_detail.fxml"));
            javafx.scene.Parent root = loader.load();
            ReviewDetailController controller = loader.getController();
            
            controller.setRequest(req);
            controller.setCallbacks(() -> showReview());
            
            contentArea.getChildren().setAll(root);
        } catch (Exception e) {
            System.err.println("Error loading Review Detail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showContracts() {
        setActiveTab(btnContracts);
        try {
            java.net.URL resource = getClass().getResource("/gui/collab/manager/contract_list.fxml");
            if (resource == null) {
                System.err.println("Could not find contract_list.fxml");
                return;
            }
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(resource);
            javafx.scene.Parent root = loader.load();
            ContractListController controller = loader.getController();
            
            if (controller != null) {
                controller.setOnConsultRequested(contract -> showContractConsultation(contract));
            }
            
            contentArea.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showContractConsultation(entities.Contract contract) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/collab/manager/contract_consultation.fxml"));
            javafx.scene.Parent root = loader.load();
            ContractConsultationController controller = loader.getController();
            
            controller.setContract(contract);
            controller.setCallbacks(() -> showContracts());
            
            contentArea.getChildren().setAll(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveTab(Button activeBtn) {
        btnHub.getStyleClass().remove("tab-btn-active");
        btnReview.getStyleClass().remove("tab-btn-active");
        btnContracts.getStyleClass().remove("tab-btn-active");
        if (activeBtn != null) activeBtn.getStyleClass().add("tab-btn-active");
    }
}
