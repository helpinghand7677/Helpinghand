-- ============================================================
-- Helping Hand - Database Schema
-- Run this whole file in MySQL to set up the database:
--   mysql -u root -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS helping_hand_db;
USE helping_hand_db;

-- ---------------------------------------------------------
-- Customers (registered users, separate from the demo admin
-- account which is checked in AdminLoginServlet)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------
-- Service catalog (matches the SERVICES array from the site)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS services (
    id VARCHAR(80) PRIMARY KEY,
    icon VARCHAR(10),
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    price VARCHAR(30) NOT NULL,
    category VARCHAR(30),
    popular BOOLEAN DEFAULT FALSE
);

-- ---------------------------------------------------------
-- Bookings made from the "Book service" form
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    service_id VARCHAR(80) NOT NULL,
    service_icon VARCHAR(10),
    service_name VARCHAR(120) NOT NULL,
    service_price VARCHAR(30),
    customer_name VARCHAR(120) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(300) NOT NULL,
    area VARCHAR(100),
    latitude DECIMAL(9,6) NULL,
    longitude DECIMAL(9,6) NULL,
    booking_date VARCHAR(20) NOT NULL,
    booking_time VARCHAR(20) NOT NULL,
    issue VARCHAR(500),
    issue_summary VARCHAR(1000) NULL,
    -- Confirmed (waiting for a technician) -> Accepted -> Completed, or Cancelled
    status VARCHAR(20) DEFAULT 'Confirmed',
    technician_id INT NULL,
    accepted_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES services(id)
);

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
    -- SHA-256 hex (see PasswordUtil). Set by the admin at registration; technician logs in with phone + password.
    password_hash VARCHAR(64) NOT NULL DEFAULT '',
    service_id VARCHAR(80) NOT NULL,
    area_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES services(id),
    FOREIGN KEY (area_id) REFERENCES areas(id)
);

ALTER TABLE bookings
    ADD CONSTRAINT fk_bookings_technician FOREIGN KEY (technician_id) REFERENCES technicians(id) ON DELETE SET NULL;

-- ---------------------------------------------------------
-- Seed data: all 34 services currently on the site
-- ---------------------------------------------------------
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('ac-repair-and-service', '❄️', 'AC Repair & Service', 'Gas refilling, cooling issues, water leakage, AC installation and uninstallation', '₹399', 'repair', 1);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('home-cleaning', '🧹', 'Home Cleaning', 'Deep cleaning for kitchens and bathrooms, full-home cleaning, sofa & carpet shampooing', '₹999', 'cleaning', 1);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('fan-repair', '🔄', 'Fan Repair', 'Ceiling, table & exhaust fan repair — humming noise, slow speed, capacitor change', '₹199', 'repair', 1);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('refrigerator-repair', '🌡️', 'Refrigerator Repair', 'Cooling problems, gas leakage, compressor issues, ice build-up, noisy operation', '₹349', 'repair', 1);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('carpentry', '🔨', 'Carpentry', 'Furniture repair, door & window fitting, lock change, wardrobe assembly, hinge and drawer', '₹249', 'home', 1);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('plumbing', '🪠', 'Plumbing', 'Leak repair, tap & pipe fitting, drainage blockage, bathroom fittings, water tank cleaning', '₹199', 'home', 1);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('home-tuition', '📚', 'Home Tuition', 'Verified teachers for all subjects — Class 1 to 12, CBSE/BSEB. One-on-one sessions', '₹499', 'educ', 1);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('coaching', '🎓', 'Coaching', 'Competitive exam coaching for BPSC, SSC, UPSC, Railway & more. Group and individual', '₹799', 'educ', 1);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('home-painting', '🎨', 'Home Painting', 'Interior & exterior wall painting, texture work, waterproofing coating. Quality paint', '₹7/sq.ft', 'home', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('mistri-construction', '🧱', 'Mistri (Construction)', 'Skilled masons for new construction, renovation, wall repair, tile laying, plastering', '₹599', 'home', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('tv-repair', '📺', 'TV Repair', 'LED, LCD & Smart TV repair — no display, sound issues, panel damage, HDMI port', '₹299', 'repair', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('ro-service', '💧', 'RO Service', 'Water purifier installation, filter change, membrane replacement, low flow, bad taste', '₹249', 'repair', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('septic-tank-pumping', '🚽', 'Septic Tank Pumping', 'Safe & hygienic septic tank emptying, suction & cleaning service. Equipped tank', '₹1,499', 'cleaning', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('water-tank-cleaning', '🪣', 'Water Tank Cleaning', 'Overhead & underground tank cleaning, sludge removal & disinfection. Certified', '₹699', 'cleaning', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('washing-machine-repair', '🫧', 'Washing Machine Repair', 'Top load & front load repair — not spinning, water leakage, error codes, drum issues', '₹349', 'repair', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('cooler-repair-and-cleaning', '🌬️', 'Cooler Repair & Cleaning', 'Desert & window cooler servicing — pad replacement, pump repair, motor fix, deep clean', '₹249', 'repair', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('wedding-planner', '💒', 'Wedding Planner', 'Complete wedding planning — venue, catering, decoration, coordination. End-to-end management', '₹4,999', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('bridal-makeup', '💄', 'Bridal Makeup', 'Professional bridal makeup by certified artists — HD makeup, airbrush, party & reception looks', '₹2,499', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('page-printout', '🖨️', 'Page Printout', 'Black & white and color page printing — A4, A3, single & double sided. Same-day delivery', '₹2/page', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('book-printout', '📖', 'Book Printout', 'Spiral & hardbound book printing, bulk orders welcome. Quality paper, fast turnaround', '₹199', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('banner-and-hoarding', '🪧', 'Banner & Hoarding', 'Flex, vinyl & digital banners — custom size, design included. Event, wedding & shop hoardings', '₹299', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('cup-and-t-shirt-photo-printing', '👕', 'Cup & T-Shirt Photo Printing', 'Custom photo printing on mugs, t-shirts, keychains. Perfect wedding & birthday gifts', '₹199', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('laundry-service', '👗', 'Laundry Service', 'Wash, dry & fold — clothes, sarees, suits, sherwani. Pickup & delivery available at doorstep', '₹49/kg', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('car-and-bike-cleaning', '🚗', 'Car & Bike Cleaning', 'Interior & exterior car wash, dry cleaning, foam wash, bike detailing. At your location', '₹299', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('home-and-shop-cleaning', '🏠', 'Home & Shop Cleaning', 'Pre & post-wedding deep cleaning for home, farmhouse, banquet hall & shops', '₹1,299', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('birthday-decorations', '🎂', 'Birthday Decorations', 'Balloon decoration, theme setup, LED lights, backdrop & cake table arrangement at home', '₹999', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('haldi-mehndi-decorations', '🌿', 'Haldi / Mehndi Decorations', 'Traditional marigold, rose petals, pots & colourful draping for haldi & mehndi ceremonies', '₹1,999', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('stage-decorations', '🎪', 'Stage Decorations', 'Full stage setup — backdrop, flower wall, draping, throne chairs, carpet & lighting for weddings', '₹3,999', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('marwa-decorations', '🌺', 'Marwa Decorations', 'Traditional marwa/mandap decoration with flowers, banana leaves, torans & colourful cloth', '₹2,499', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('photographer', '📸', 'Photographer', 'Professional wedding & event photographer — candid, traditional & portrait shoots. Edited delivery', '₹2,999', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('photo-album', '📷', 'Photo Album', 'Premium hardbound & flexi-cover photo albums — custom design, matte & glossy finish', '₹799', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('drone-shoot', '🚁', 'Drone Shoot', 'Aerial drone videography & photography for weddings, events & baraat processions. 4K quality', '₹1,999', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('lighting-frame', '🖼️', 'Lighting Frame', 'Decorative LED lighting frames & neon signs for photo booths, reception & home décor', '₹1,499', 'wedding', 0);
INSERT INTO services (id, icon, name, description, price, category, popular) VALUES ('wedding-lightning-frame', '✨', 'Wedding Lightning Frame', 'Grand wedding entry & stage lighting frames — sparkle, fairy lights & RGB LED setup', '₹2,999', 'wedding', 0);

-- ---------------------------------------------------------
-- Seed data: 40 Patna areas
-- ---------------------------------------------------------
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
