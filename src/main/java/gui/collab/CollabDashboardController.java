package gui.collab;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import java.io.IOException;

public class CollabDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Button btnPartners;
    @FXML private Button btnRequests;
    @FXML private Button btnContracts;
    @FXML private javafx.scene.control.Label bannerTitle;
    @FXML private javafx.scene.layout.VBox heroBanner;

    @FXML
    public void initialize() {
        showPartners();
        applyAiBackground();
    }

    private void applyAiBackground() {
        new Thread(() -> {
            javafx.scene.image.Image bg = utils.GeminiImageService.generateHeroBackground("Global strategic business partnership networks in a modern skyscraper lounge");
            if (bg != null) {
                javafx.application.Platform.runLater(() -> {
                    heroBanner.setBackground(new javafx.scene.layout.Background(
                        new javafx.scene.layout.BackgroundImage(bg, 
                            javafx.scene.layout.BackgroundRepeat.NO_REPEAT, 
                            javafx.scene.layout.BackgroundRepeat.NO_REPEAT, 
                            javafx.scene.layout.BackgroundPosition.CENTER, 
                            new javafx.scene.layout.BackgroundSize(100, 100, true, true, true, true))
                    ));
                });
            }
        }).start();
    }

    @FXML
    private void showPartners() {
        setActiveTab(btnPartners);
        if (bannerTitle != null) bannerTitle.setText("Strategic Partners Network");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/partner/displayPartner.fxml"));
            Parent root = loader.load();
            gui.collab.partner.DisplayPartnerController controller = loader.getController();
            
            controller.setOnAddRequested(() -> {
                showAddPartnerForm();
            });
            
            controller.setOnEditRequested(collab -> {
                showEditPartnerForm(collab);
            });

            controller.setOnViewRequested(collab -> {
                showPartnerDetails(collab);
            });
            
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showPartnerDetails(entities.Collaborator collab) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/partner/viewPartner.fxml"));
            Parent root = loader.load();
            gui.collab.partner.ViewPartnerController controller = loader.getController();
            
            controller.setPartner(collab);
            controller.setCallbacks(
                    () -> showEditPartnerForm(collab),
                    () -> {
                        // Handle delete within the view if needed, or just return
                        showPartners();
                    },
                    () -> { /* New request action */ }
            );
            
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAddPartnerForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/partner/addPartner.fxml"));
            Parent root = loader.load();
            gui.collab.partner.AddPartnerController controller = loader.getController();
            
            controller.setOnCancel(() -> showPartners());
            controller.setOnSave(() -> showPartners());
            
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showEditPartnerForm(entities.Collaborator collab) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/partner/updatePartner.fxml"));
            Parent root = loader.load();
            gui.collab.partner.UpdatePartnerController controller = loader.getController();
            
            controller.setPartner(collab);
            controller.setOnCancel(() -> showPartners());
            controller.setOnSave(() -> showPartners());
            
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showRequests() {
        setActiveTab(btnRequests);
        if (bannerTitle != null) bannerTitle.setText("Collaboration Request Pipeline");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/request/displayRequest.fxml"));
            Parent root = loader.load();
            gui.collab.request.DisplayRequestController controller = loader.getController();
            
            controller.setOnAddRequested(() -> {
                showAddRequestForm();
            });
            
            controller.setOnEditRequested(req -> {
                showEditRequestForm(req);
            });
            
            controller.setOnViewRequested(req -> {
                showRequestDetails(req);
            });
            
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showRequestDetails(entities.CollabRequest req) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/request/viewRequest.fxml"));
            Parent root = loader.load();
            gui.collab.request.ViewRequestController controller = loader.getController();
            
            controller.setRequest(req);
            controller.setCallbacks(
                    () -> showRequests(),
                    partner -> showPartnerDetails(partner),
                    contract -> showContractConsultation(contract)
            );
            
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAddRequestForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/request/addRequest.fxml"));
            Parent root = loader.load();
            gui.collab.request.AddRequestController controller = loader.getController();
            
            controller.setOnCancel(() -> showRequests());
            controller.setOnSave(() -> showRequests());
            
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showEditRequestForm(entities.CollabRequest req) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/request/updateRequest.fxml"));
            Parent root = loader.load();
            gui.collab.request.UpdateRequestController controller = loader.getController();
            
            controller.setRequest(req);
            controller.setOnCancel(() -> showRequests());
            controller.setOnSave(() -> showRequests());
            
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showContracts() {
        setActiveTab(btnContracts);
        if (bannerTitle != null) bannerTitle.setText("My Collaboration Contracts");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/contract/displayContract.fxml"));
            Parent root = loader.load();
            gui.collab.contract.DisplayContractController controller = loader.getController();
            
            controller.setOnViewRequested(contract -> {
                System.out.println("CollabDashboardController: showContracts -> showContractConsultation for " + contract.getContractNumber());
                showContractConsultation(contract);
            });
            
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showContractConsultation(entities.Contract contract) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/collab/contract/viewContract.fxml"));
            Parent root = loader.load();
            gui.collab.contract.ViewContractController controller = loader.getController();
            
            System.out.println("CollabDashboardController: Loading viewContract.fxml for " + contract.getContractNumber());
            controller.setContract(contract);
            controller.setOnBack(() -> {
                System.out.println("CollabDashboardController: Back to list requested");
                showContracts();
            });
            
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveTab(Button activeBtn) {
        btnPartners.getStyleClass().remove("tab-btn-active");
        btnRequests.getStyleClass().remove("tab-btn-active");
        btnContracts.getStyleClass().remove("tab-btn-active");
        
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("tab-btn-active");
        }
    }
}
