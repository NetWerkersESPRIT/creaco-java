package services.forum;

import java.sql.SQLException;
import java.util.List;

public interface ForumInterface<T> {
    void ajouter(T t) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<T> afficher() throws SQLException;
    void modifier(int id, T t) throws SQLException;
}