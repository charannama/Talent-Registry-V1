-- ===========================================================================
-- V82__schema_fixes.sql
-- ===========================================================================

-- 1. Student Profiles Updates
-- (profile_visible, suspended_at, suspension_reason already exist from V38/V39)
-- suspended_by exists as VARCHAR(255), we must cast it to UUID.
ALTER TABLE student_profiles 
    ALTER COLUMN suspended_by TYPE UUID USING suspended_by::uuid;

ALTER TABLE student_profiles
    ADD CONSTRAINT fk_student_profiles_suspended_by FOREIGN KEY (suspended_by) REFERENCES users(id);

COMMENT ON COLUMN student_profiles.profile_visible IS 'Controls public profile visibility';
COMMENT ON COLUMN student_profiles.suspended_by IS 'Stores administrator who suspended profile';

-- 2. Applications Updates
ALTER TABLE applications 
    ADD COLUMN current_handler_id UUID REFERENCES users(id);

COMMENT ON COLUMN applications.current_handler_id IS 'Tracks HR ownership for workloads';
CREATE INDEX idx_applications_handler ON applications(current_handler_id);

-- 3. Enterprise Accounts Updates
ALTER TABLE enterprise_accounts 
    ADD COLUMN approved_by UUID REFERENCES users(id);

CREATE INDEX idx_enterprise_approved_by ON enterprise_accounts(approved_by);

-- 4. Openings Updates
-- (work_mode and featured already exist from V59 and V66)
ALTER TABLE job_openings 
    ADD COLUMN graduation_year_filter INTEGER;
ALTER TABLE job_openings ADD COLUMN salary_range_min NUMERIC(15,2);
ALTER TABLE job_openings ADD COLUMN salary_range_max NUMERIC(15,2);

COMMENT ON COLUMN job_openings.graduation_year_filter IS 'Campus Hiring Filters';
COMMENT ON COLUMN job_openings.salary_range_min IS 'Salary floor';
COMMENT ON COLUMN job_openings.salary_range_max IS 'Salary ceiling';

CREATE INDEX idx_openings_grad_year ON job_openings(graduation_year_filter);
