-- V67__opening_search_indexes.sql
-- Create indexes for opening search optimization

-- Create a composite index for live jobs by application deadline and featured
CREATE INDEX IF NOT EXISTS idx_job_openings_live_search 
ON job_openings (status, is_deleted, application_deadline, featured);

-- Individual column indexes for common filters
CREATE INDEX IF NOT EXISTS idx_job_openings_domain ON job_openings(domain) WHERE status = 'LIVE' AND is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_job_openings_job_type ON job_openings(job_type) WHERE status = 'LIVE' AND is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_job_openings_work_mode ON job_openings(work_mode) WHERE status = 'LIVE' AND is_deleted = false;

-- Range filter indexes
CREATE INDEX IF NOT EXISTS idx_job_openings_salary_range ON job_openings(salary_min, salary_max) WHERE status = 'LIVE' AND is_deleted = false;

-- Enterprise status filter optimization
CREATE INDEX IF NOT EXISTS idx_enterprise_status_search ON enterprise_accounts(onboarding_status, account_active) WHERE is_deleted = false;
