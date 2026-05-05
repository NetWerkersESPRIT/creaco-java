-- Quiz and Question tables for the resource quiz feature

CREATE TABLE quiz (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    resource_id INT NOT NULL,
    created_date DATETIME NOT NULL,
    FOREIGN KEY (resource_id) REFERENCES ressource(id) ON DELETE CASCADE
);

CREATE TABLE question (
    id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id INT NOT NULL,
    question_text TEXT NOT NULL,
    options JSON NOT NULL,  -- JSON array of 4 options
    correct_answer_index INT NOT NULL CHECK (correct_answer_index BETWEEN 0 AND 3),
    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE
);

-- Indexes for better performance
CREATE INDEX idx_quiz_resource_id ON quiz(resource_id);
CREATE INDEX idx_question_quiz_id ON question(quiz_id);