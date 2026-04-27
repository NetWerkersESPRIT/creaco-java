package gui.collab.manager;

import entities.CollabRequest;
import entities.Contract;
import entities.Users;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import services.CollabRequestService;
import services.ContractService;
import utils.SessionManager;
import java.math.BigDecimal;
import java.util.List;

public class DashboardController {

    @FXML private Label totalContractsLabel;
    @FXML private Label activeContractsLabel;
    @FXML private Label totalBudgetLabel;
    @FXML private Label pendingRequestsLabel;

    private final ContractService contractService = new ContractService();
    private final CollabRequestService requestService = new CollabRequestService();

    @FXML
    public void initialize() {
        loadStats();
    }

    private void loadStats() {
        try {
            Users manager = SessionManager.getInstance().getCurrentUser();
            if (manager == null) return;

            List<Contract> contracts = contractService.afficherByManager(manager.getId());
            List<CollabRequest> requests = requestService.afficherByManager(manager.getId());

            long totalContracts = contracts.size();
            long activeContracts = contracts.stream().filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus())).count();
            BigDecimal totalBudget = contracts.stream()
                    .map(Contract::getAmount)
                    .filter(a -> a != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long pendingRequests = requests.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();

            totalContractsLabel.setText(String.valueOf(totalContracts));
            activeContractsLabel.setText(String.valueOf(activeContracts));
            totalBudgetLabel.setText(totalBudget.toPlainString() + " DT");
            pendingRequestsLabel.setText(String.valueOf(pendingRequests));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
