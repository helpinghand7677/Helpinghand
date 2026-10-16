-- ============================================================
-- Migration for an EXISTING helping_hand_db: technician password login
-- + technician dashboard (accept / complete jobs).
-- Fresh installs do NOT need this - schema.sql already includes it.
--   mysql -u root -p < migration_technician_login.sql
--
-- Existing technicians get an empty password (cannot log in yet).
-- Set one for each from Admin Panel > Technicians > "Set password".
-- ============================================================
USE helping_hand_db;

ALTER TABLE technicians
    ADD COLUMN password_hash VARCHAR(64) NOT NULL DEFAULT '' AFTER phone;

ALTER TABLE bookings
    ADD COLUMN issue_summary VARCHAR(1000) NULL AFTER issue,
    ADD COLUMN technician_id INT NULL AFTER status,
    ADD COLUMN accepted_at TIMESTAMP NULL AFTER technician_id,
    ADD COLUMN completed_at TIMESTAMP NULL AFTER accepted_at,
    ADD CONSTRAINT fk_bookings_technician FOREIGN KEY (technician_id) REFERENCES technicians(id) ON DELETE SET NULL;
