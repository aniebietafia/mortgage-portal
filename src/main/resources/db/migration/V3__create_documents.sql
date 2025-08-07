CREATE TABLE documents (
    id VARCHAR PRIMARY KEY,
    application_id VARCHAR NOT NULL REFERENCES applications(id),
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT,
    presigned_url TEXT
);

-- Create an index on the application_id column for faster lookups
CREATE INDEX idx_documents_application_id ON documents(application_id);
CREATE INDEX idx_documents_file_name ON documents(file_name);
