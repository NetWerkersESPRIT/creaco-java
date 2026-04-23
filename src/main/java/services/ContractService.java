package services;

import entities.Contract;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContractService {

    private Connection connection;

    public ContractService() {
        connection = MyConnection.getInstance().getConnection();
    }

    public void insert(Contract contract) throws SQLException {
        String query = "INSERT INTO contract (contractNumber, title, startDate, endDate, amount, status, signedByCreator, signedByCollaborator, terms, paymentSchedule, confidentialityClause, cancellationTerms, signatureToken, collabRequestId, creatorId, collaboratorId, createdAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, contract.getContractNumber());
            preparedStatement.setString(2, contract.getTitle());
            preparedStatement.setDate(3, contract.getStartDate() != null ? new Date(contract.getStartDate().getTime()) : null);
            preparedStatement.setDate(4, contract.getEndDate() != null ? new Date(contract.getEndDate().getTime()) : null);
            preparedStatement.setBigDecimal(5, contract.getAmount());
            preparedStatement.setString(6, contract.getStatus());
            preparedStatement.setBoolean(7, contract.isSignedByCreator());
            preparedStatement.setBoolean(8, contract.isSignedByCollaborator());
            preparedStatement.setString(9, contract.getTerms());
            preparedStatement.setString(10, contract.getPaymentSchedule());
            preparedStatement.setString(11, contract.getConfidentialityClause());
            preparedStatement.setString(12, contract.getCancellationTerms());
            preparedStatement.setString(13, contract.getSignatureToken());
            preparedStatement.setInt(14, contract.getCollabRequestId());
            if (contract.getCreatorId() != null) {
                preparedStatement.setInt(15, contract.getCreatorId());
            } else {
                preparedStatement.setNull(15, Types.INTEGER);
            }
            preparedStatement.setInt(16, contract.getCollaboratorId());
            preparedStatement.setTimestamp(17, new Timestamp(System.currentTimeMillis()));
            preparedStatement.executeUpdate();
        }
    }

    public List<Contract> getAll() throws SQLException {
        List<Contract> contracts = new ArrayList<>();
        String query = "SELECT * FROM contract";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Contract contract = new Contract(
                        resultSet.getInt("id"),
                        resultSet.getString("contractNumber"),
                        resultSet.getString("title"),
                        resultSet.getDate("startDate"),
                        resultSet.getDate("endDate"),
                        resultSet.getBigDecimal("amount"),
                        resultSet.getString("pdfPath"),
                        resultSet.getString("status"),
                        resultSet.getBoolean("signedByCreator"),
                        resultSet.getBoolean("signedByCollaborator"),
                        resultSet.getTimestamp("creatorSignatureDate"),
                        resultSet.getTimestamp("collaboratorSignatureDate"),
                        resultSet.getString("terms"),
                        resultSet.getString("paymentSchedule"),
                        resultSet.getString("confidentialityClause"),
                        resultSet.getString("cancellationTerms"),
                        resultSet.getString("signatureToken"),
                        resultSet.getTimestamp("createdAt"),
                        resultSet.getTimestamp("sentAt"),
                        resultSet.getInt("collabRequestId"),
                        (Integer) resultSet.getObject("creatorId"),
                        resultSet.getInt("collaboratorId")
                );
                contracts.add(contract);
            }
        }
        return contracts;
    }

    public void update(Contract contract) throws SQLException {
        String query = "UPDATE contract SET contractNumber=?, title=?, startDate=?, endDate=?, amount=?, status=?, signedByCreator=?, signedByCollaborator=?, terms=?, paymentSchedule=?, confidentialityClause=?, cancellationTerms=?, signatureToken=?, collabRequestId=?, creatorId=?, collaboratorId=? WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, contract.getContractNumber());
            preparedStatement.setString(2, contract.getTitle());
            preparedStatement.setDate(3, contract.getStartDate() != null ? new Date(contract.getStartDate().getTime()) : null);
            preparedStatement.setDate(4, contract.getEndDate() != null ? new Date(contract.getEndDate().getTime()) : null);
            preparedStatement.setBigDecimal(5, contract.getAmount());
            preparedStatement.setString(6, contract.getStatus());
            preparedStatement.setBoolean(7, contract.isSignedByCreator());
            preparedStatement.setBoolean(8, contract.isSignedByCollaborator());
            preparedStatement.setString(9, contract.getTerms());
            preparedStatement.setString(10, contract.getPaymentSchedule());
            preparedStatement.setString(11, contract.getConfidentialityClause());
            preparedStatement.setString(12, contract.getCancellationTerms());
            preparedStatement.setString(13, contract.getSignatureToken());
            preparedStatement.setInt(14, contract.getCollabRequestId());
            if (contract.getCreatorId() != null) {
                preparedStatement.setInt(15, contract.getCreatorId());
            } else {
                preparedStatement.setNull(15, Types.INTEGER);
            }
            preparedStatement.setInt(16, contract.getCollaboratorId());
            preparedStatement.setInt(17, contract.getId());
            preparedStatement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM contract WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }
}
