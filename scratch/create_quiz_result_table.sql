-- Création de la table quiz_result pour stocker les résultats des quiz des utilisateurs
CREATE TABLE IF NOT EXISTS quiz_result (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    quiz_id INT NOT NULL,
    score DOUBLE NOT NULL,
    submitted_date VARCHAR(255) NOT NULL,
    answers TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_quiz (user_id, quiz_id)
);

-- Index pour améliorer les performances
CREATE INDEX idx_quiz_result_user_id ON quiz_result(user_id);
CREATE INDEX idx_quiz_result_quiz_id ON quiz_result(quiz_id);