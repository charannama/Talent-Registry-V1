-- ===========================================================================
-- V80__create_express_interest_table.sql
-- ===========================================================================

DROP TABLE IF EXISTS express_interests CASCADE;

CREATE TABLE express_interests (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    enterprise_id UUID NOT NULL,
    student_id UUID NOT NULL,
    opening_id UUID,
    stage VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_express_interest_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise_accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_express_interest_student FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_express_interest_opening FOREIGN KEY (opening_id) REFERENCES openings(id) ON DELETE CASCADE,
    
    -- In PostgreSQL 15+, UNIQUE NULLS NOT DISTINCT can be used. 
    -- However, to be universally compatible with older Postgres versions, 
    -- we specify the constraint directly. If opening_id is null, standard PG allows multiples.
    -- We'll add an exclusion or partial index if the business requirement changes.
    CONSTRAINT uq_enterprise_student_opening UNIQUE (enterprise_id, student_id, opening_id)
);

CREATE INDEX idx_express_interest_enterprise ON express_interests(enterprise_id);
CREATE INDEX idx_express_interest_student ON express_interests(student_id);
CREATE INDEX idx_express_interest_opening ON express_interests(opening_id);
