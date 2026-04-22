package services;

import entities.Users;

import java.sql.SQLException;
import java.util.List;

public interface UsersInterface<T>{
    void ajouter(T t) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<T> afficher() throws SQLException;
    void modifier(Users user) throws SQLException;

}
