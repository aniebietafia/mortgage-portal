CREATE TABLE decisions (
    id VARCHAR PRIMARY KEY,
    application_id VARCHAR NOT NULL UNIQUE REFERENCES applications(id),
    officer_id VARCHAR NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    comments TEXT,
    decision_date TIMESTAMPTZ NOT NULL
);

-- Create an index on the application_id column for faster lookups
CREATE INDEX idx_decisions_application_id ON decisions(application_id);
CREATE INDEX idx_decisions_officer_id ON decisions(officer_id);
