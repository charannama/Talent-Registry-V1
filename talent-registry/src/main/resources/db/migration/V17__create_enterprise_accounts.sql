CREATE TABLE enterprise_accounts (

    id UUID PRIMARY KEY,

    user_id UUID UNIQUE,

    company_name VARCHAR(255) NOT NULL,

    domain_email VARCHAR(255) UNIQUE NOT NULL,

    company_website VARCHAR(255),

    company_size VARCHAR(50),

    logo_url VARCHAR(500),

    gst_number VARCHAR(100),

    sector VARCHAR(255),

    hiring_manager_name VARCHAR(255),

    hiring_manager_email VARCHAR(255),

    onboarding_status VARCHAR(50) NOT NULL,

    account_active BOOLEAN DEFAULT FALSE,

    onboarded_by VARCHAR(255),

    approved_at TIMESTAMP WITH TIME ZONE,

    rejected_by VARCHAR(255),

    rejected_at TIMESTAMP WITH TIME ZONE,

    rejection_reason TEXT,

    suspended_at TIMESTAMP WITH TIME ZONE,

    suspension_reason TEXT,

    created_at TIMESTAMP WITH TIME ZONE,

    updated_at TIMESTAMP WITH TIME ZONE,

    created_by VARCHAR(255),

    updated_by VARCHAR(255),

    version BIGINT DEFAULT 0,

    is_deleted BOOLEAN DEFAULT FALSE,

    deleted_at TIMESTAMP WITH TIME ZONE,

    deleted_by VARCHAR(255),

    CONSTRAINT fk_enterprise_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);
