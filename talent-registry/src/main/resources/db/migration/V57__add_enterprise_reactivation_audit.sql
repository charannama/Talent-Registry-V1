-- V57__add_enterprise_reactivation_audit.sql
ALTER TABLE enterprise_accounts
ADD COLUMN reactivated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE enterprise_accounts ADD COLUMN reactivated_by UUID;

-- Add Foreign Key constraints for accountability trace columns
ALTER TABLE enterprise_accounts
ADD CONSTRAINT fk_ent_acc_onboarded_by FOREIGN KEY (onboarded_by) REFERENCES users(id);
ALTER TABLE enterprise_accounts ADD CONSTRAINT fk_ent_acc_rejected_by FOREIGN KEY (rejected_by) REFERENCES users(id);
ALTER TABLE enterprise_accounts ADD CONSTRAINT fk_ent_acc_suspended_by FOREIGN KEY (suspended_by) REFERENCES users(id);
ALTER TABLE enterprise_accounts ADD CONSTRAINT fk_ent_acc_reactivated_by FOREIGN KEY (reactivated_by) REFERENCES users(id);
