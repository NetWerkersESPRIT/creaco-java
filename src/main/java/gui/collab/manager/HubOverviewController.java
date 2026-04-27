package gui.collab.manager;

import entities.CollabRequest;
import entities.Contract;
import entities.Users;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import services.CollaboratorService;
import services.CollabRequestService;
import services.ContractService;
import utils.SessionManager;
import java.math.BigDecimal;
import java.util.List;

public class HubOverviewController {

    @FXML private Label totalPartnersLabel;
    @FXML private Label activeContractsLabel;
    @FXML private Label totalBudgetLabel;
    @FXML private Label pendingRequestsLabel;
    @FXML private VBox pulsationList;

    private final ContractService contractService = new ContractService();
    private final CollabRequestService requestService = new CollabRequestService();
    private final CollaboratorService partnerService = new CollaboratorService();

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        try {
            Users manager = SessionManager.getInstance().getCurrentUser();
            if (manager == null) return;

            int managerId = manager.getId();
            List<Contract> contracts = contractService.afficherByManager(managerId);
            List<CollabRequest> requests = requestService.afficherByManager(managerId);
            int totalPartners = partnerService.afficher().size(); // Simplify or add specific manager logic

            long activeCount = contracts.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).count();
            BigDecimal totalBudget = contracts.stream()
                    .map(Contract::getAmount)
                    .filter(a -> a != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long pendingCount = requests.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();

            totalPartnersLabel.setText(String.valueOf(totalPartners));
            activeContractsLabel.setText(activeCount + " / " + contracts.size());
            totalBudgetLabel.setText(String.format("%,.0f DT", totalBudget).replace(',', ' '));
            pendingRequestsLabel.setText(pendingCount + " Items");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
