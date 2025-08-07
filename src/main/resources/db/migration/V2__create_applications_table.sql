CREATE TABLE applications (
    id VARCHAR PRIMARY KEY,
    applicant_id VARCHAR NOT NULL REFERENCES users(id),
    applicant_name VARCHAR(255) NOT NULL,
    national_id VARCHAR(255) NOT NULL UNIQUE,
    property_details TEXT,
    loan_amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

-- Create an index on the applicant_id column for faster lookups
CREATE INDEX idx_applications_applicant_id ON applications(applicant_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_created_at ON applications(created_at);
