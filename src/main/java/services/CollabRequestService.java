package services;

import entities.CollabRequest;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollabRequestService {

    private Connection connection;

    public CollabRequestService() {
        connection = MyConnection.getInstance().getConnection();
    }

    public void insert(CollabRequest request) throws SQLException {
        String query = "INSERT INTO collab_request (title, description, budget, startDate, endDate, status, deliverables, paymentTerms, collaboratorId, createdAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, request.getTitle());
            preparedStatement.setString(2, request.getDescription());
            preparedStatement.setBigDecimal(3, request.getBudget());
            preparedStatement.setDate(4, request.getStartDate() != null ? new Date(request.getStartDate().getTime()) : null);
            preparedStatement.setDate(5, request.getEndDate() != null ? new Date(request.getEndDate().getTime()) : null);
            preparedStatement.setString(6, request.getStatus());
            preparedStatement.setString(7, request.getDeliverables());
            preparedStatement.setString(8, request.getPaymentTerms());
            preparedStatement.setInt(9, request.getCollaboratorId());
            preparedStatement.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
            preparedStatement.executeUpdate();
        }
    }

    public List<CollabRequest> getAll() throws SQLException {
        List<CollabRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM collab_request";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                CollabRequest request = new CollabRequest(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getBigDecimal("budget"),
                        resultSet.getDate("startDate"),
                        resultSet.getDate("endDate"),
                        resultSet.getString("status"),
                        resultSet.getString("rejectionReason"),
                        resultSet.getString("deliverables"),
                        resultSet.getString("paymentTerms"),
                        resultSet.getTimestamp("createdAt"),
                        resultSet.getTimestamp("updatedAt"),
                        resultSet.getTimestamp("respondedAt"),
                        resultSet.getInt("creatorId"),
                        resultSet.getInt("revisorId"),
                        resultSet.getInt("collaboratorId")
                );
                requests.add(request);
            }
        }
        return requests;
    }

    public void update(CollabRequest request) throws SQLException {
        String query = "UPDATE collab_request SET title=?, description=?, budget=?, startDate=?, endDate=?, status=?, deliverables=?, paymentTerms=?, collaboratorId=?, updatedAt=? WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, request.getTitle());
            preparedStatement.setString(2, request.getDescription());
            preparedStatement.setBigDecimal(3, request.getBudget());
            preparedStatement.setDate(4, request.getStartDate() != null ? new Date(request.getStartDate().getTime()) : null);
            preparedStatement.setDate(5, request.getEndDate() != null ? new Date(request.getEndDate().getTime()) : null);
            preparedStatement.setString(6, request.getStatus());
            preparedStatement.setString(7, request.getDeliverables());
            preparedStatement.setString(8, request.getPaymentTerms());
            preparedStatement.setInt(9, request.getCollaboratorId());
            preparedStatement.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(11, request.getId());
            preparedStatement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM collab_request WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }
}
