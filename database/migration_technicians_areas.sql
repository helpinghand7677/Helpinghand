-- ============================================================
-- Migration for an EXISTING helping_hand_db (already created from the
-- old schema.sql). Adds: areas, technicians, and area/lat/lng on bookings.
-- Fresh installs do NOT need this - schema.sql already includes it.
--   mysql -u root -p < migration_technicians_areas.sql
-- ============================================================
USE helping_hand_db;

-- ---------------------------------------------------------
-- Service areas (Patna localities). Used for technician registration
-- and for the "select your area" / "use live location" option on the
-- booking form. latitude/longitude are approximate area centres and
-- are used to match a customer's live location to the nearest area.
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS areas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL
);

-- ---------------------------------------------------------
-- Technicians (registered by the admin from the Admin Panel)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS technicians (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    service_id VARCHAR(80) NOT NULL,
    area_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES services(id),
    FOREIGN KEY (area_id) REFERENCES areas(id)
);

ALTER TABLE bookings
    ADD COLUMN area VARCHAR(100) NULL AFTER address,
    ADD COLUMN latitude DECIMAL(9,6) NULL AFTER area,
    ADD COLUMN longitude DECIMAL(9,6) NULL AFTER latitude;

INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Boring Road', 25.6145, 85.117);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Kankarbagh', 25.583, 85.17);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Rajendra Nagar', 25.605, 85.174);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Patliputra Colony', 25.618, 85.098);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Bailey Road', 25.607, 85.085);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Danapur', 25.625, 85.045);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Phulwari Sharif', 25.575, 85.085);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Kurji', 25.601, 85.099);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Anisabad', 25.603, 85.105);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Gardanibagh', 25.605, 85.116);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Digha', 25.639, 85.087);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Ashok Rajpath', 25.6115, 85.15);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Fraser Road', 25.612, 85.14);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Exhibition Road', 25.611, 85.133);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Gandhi Maidan', 25.619, 85.148);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Saguna More', 25.629, 85.056);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Khagaul', 25.585, 85.045);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Patna City (Patna Sahib)', 25.619, 85.23);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Bahadurpur', 25.597, 85.163);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Mithapur', 25.603, 85.147);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Raja Bazar', 25.62, 85.106);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Sri Krishna Puri', 25.615, 85.127);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Kidwaipuri', 25.61, 85.125);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Sheikhpura', 25.59, 85.125);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Jakkanpur', 25.611, 85.113);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Rukanpura', 25.609, 85.08);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Lohia Nagar', 25.629, 85.085);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Bhootnath Road', 25.587, 85.17);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Ashiana Nagar', 25.607, 85.092);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Hanuman Nagar', 25.585, 85.145);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Sultanganj', 25.613, 85.163);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Chitkohra', 25.596, 85.11);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Beur', 25.583, 85.094);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Punaichak', 25.599, 85.125);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Mahendru', 25.615, 85.162);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Kumhrar', 25.59, 85.187);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Bakerganj', 25.621, 85.14);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Bazar Samiti', 25.6, 85.129);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Shastri Nagar', 25.622, 85.112);
INSERT IGNORE INTO areas (name, latitude, longitude) VALUES ('Patna Junction (Station Road)', 25.606, 85.137);
