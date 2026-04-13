package services;

import entities.Collaborator;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CollaboratorService {

    private Connection connection;

    public CollaboratorService() {
        connection = MyConnection.getInstance().getConnection();
    }

    public void insert(Collaborator collaborator) throws SQLException {
        String query = "INSERT INTO collaborator (name, companyName, email, phone, address, website, domain, description, logo, isPublic, status, addedByUserId, createdAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, collaborator.getName());
            preparedStatement.setString(2, collaborator.getCompanyName());
            preparedStatement.setString(3, collaborator.getEmail());
            preparedStatement.setString(4, collaborator.getPhone());
            preparedStatement.setString(5, collaborator.getAddress());
            preparedStatement.setString(6, collaborator.getWebsite());
            preparedStatement.setString(7, collaborator.getDomain());
            preparedStatement.setString(8, collaborator.getDescription());
            preparedStatement.setString(9, collaborator.getLogo());
            preparedStatement.setBoolean(10, collaborator.isPublic());
            preparedStatement.setString(11, collaborator.getStatus());
            preparedStatement.setInt(12, collaborator.getAddedByUserId());
            preparedStatement.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
            preparedStatement.executeUpdate();
        }
    }

    public List<Collaborator> getAll() throws SQLException {
        List<Collaborator> collaborators = new ArrayList<>();
        String query = "SELECT * FROM collaborator";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                Collaborator collaborator = new Collaborator(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("companyName"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getString("address"),
                        resultSet.getString("website"),
                        resultSet.getString("domain"),
                        resultSet.getString("description"),
                        resultSet.getString("logo"),
                        resultSet.getBoolean("isPublic"),
                        resultSet.getString("status"),
                        resultSet.getTimestamp("createdAt"),
                        resultSet.getInt("addedByUserId")
                );
                collaborators.add(collaborator);
            }
        }
        return collaborators;
    }

    public void update(Collaborator collaborator) throws SQLException {
        String query = "UPDATE collaborator SET name=?, companyName=?, email=?, phone=?, address=?, website=?, domain=?, description=?, logo=?, isPublic=?, status=? WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, collaborator.getName());
            preparedStatement.setString(2, collaborator.getCompanyName());
            preparedStatement.setString(3, collaborator.getEmail());
            preparedStatement.setString(4, collaborator.getPhone());
            preparedStatement.setString(5, collaborator.getAddress());
            preparedStatement.setString(6, collaborator.getWebsite());
            preparedStatement.setString(7, collaborator.getDomain());
            preparedStatement.setString(8, collaborator.getDescription());
            preparedStatement.setString(9, collaborator.getLogo());
            preparedStatement.setBoolean(10, collaborator.isPublic());
            preparedStatement.setString(11, collaborator.getStatus());
            preparedStatement.setInt(12, collaborator.getId());
            preparedStatement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String query = "DELETE FROM collaborator WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }
}
