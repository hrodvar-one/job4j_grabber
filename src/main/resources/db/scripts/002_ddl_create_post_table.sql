CREATE TABLE post (
    id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    text TEXT,
    link TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL
);