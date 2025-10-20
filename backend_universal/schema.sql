-- Final consolidated schema for crime_app (MySQL / XAMPP)
-- Safe to run in phpMyAdmin or: mysql -u root -p crime_app < schema.sql

CREATE DATABASE IF NOT EXISTS `crime_app` CHARACTER SET = 'utf8mb4' COLLATE = 'utf8mb4_unicode_ci';
USE `crime_app`;

-- -----------------------------------------------------
-- 1) reporters (citizen users)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS reporters (
  reporter_id INT AUTO_INCREMENT PRIMARY KEY,
  alias VARCHAR(50) NOT NULL,
  full_name VARCHAR(100) DEFAULT NULL,
  email VARCHAR(100) UNIQUE,
  phone VARCHAR(20) DEFAULT NULL,
  password_hash VARCHAR(255) NOT NULL,
  date_joined DATETIME DEFAULT CURRENT_TIMESTAMP,
  last_login DATETIME DEFAULT NULL,
  INDEX (alias),
  INDEX (email),
  INDEX (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 2) admins
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS admins (
  admin_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('moderator','superadmin') DEFAULT 'moderator',
  date_created DATETIME DEFAULT CURRENT_TIMESTAMP,
  last_login DATETIME DEFAULT NULL,
  INDEX (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 3) external_orgs
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS external_orgs (
  org_id INT AUTO_INCREMENT PRIMARY KEY,
  org_name VARCHAR(100) NOT NULL,
  contact_person VARCHAR(100) DEFAULT NULL,
  email VARCHAR(100) UNIQUE,
  phone VARCHAR(20) DEFAULT NULL,
  jurisdiction VARCHAR(100) DEFAULT NULL,
  password_hash VARCHAR(255) NOT NULL,
  date_registered DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX (org_name),
  INDEX (email),
  INDEX (jurisdiction)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 4) crime_types
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS crime_types (
  crime_type_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) UNIQUE NOT NULL,
  description TEXT DEFAULT NULL,
  INDEX (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 5) areas (location_reference)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS areas (
  area_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  county VARCHAR(100) DEFAULT NULL,
  subcounty VARCHAR(100) DEFAULT NULL,
  ward VARCHAR(100) DEFAULT NULL,
  latitude DOUBLE DEFAULT NULL,
  longitude DOUBLE DEFAULT NULL,
  INDEX (name),
  INDEX (county),
  INDEX (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 6) reports
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS reports (
  report_id INT AUTO_INCREMENT PRIMARY KEY,
  reporter_id INT NOT NULL,
  crime_type_id INT DEFAULT NULL,
  area_id INT DEFAULT NULL,
  title VARCHAR(255) DEFAULT NULL,
  description TEXT DEFAULT NULL,
  latitude DOUBLE DEFAULT NULL,
  longitude DOUBLE DEFAULT NULL,
  status ENUM('pending','approved','rejected','resolved') DEFAULT 'pending',
  report_type ENUM('sighting','victimization') DEFAULT 'sighting',
  report_datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
  reviewed_by INT DEFAULT NULL,
  reviewed_at DATETIME DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT NULL,
  CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES reporters(reporter_id) ON DELETE CASCADE,
  CONSTRAINT fk_reports_crimetype FOREIGN KEY (crime_type_id) REFERENCES crime_types(crime_type_id) ON DELETE SET NULL,
  CONSTRAINT fk_reports_area FOREIGN KEY (area_id) REFERENCES areas(area_id) ON DELETE SET NULL,
  CONSTRAINT fk_reports_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES admins(admin_id) ON DELETE SET NULL,
  INDEX (reporter_id),
  INDEX (crime_type_id),
  INDEX (area_id),
  INDEX (status),
  INDEX (report_datetime),
  INDEX (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 7) report_addons (media linked to reports)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS report_addons (
  addon_id INT AUTO_INCREMENT PRIMARY KEY,
  report_id INT NOT NULL,
  file_url VARCHAR(255) NOT NULL,
  file_type ENUM('image','video','document') NOT NULL,
  file_size INT DEFAULT NULL,
  uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_addons_report FOREIGN KEY (report_id) REFERENCES reports(report_id) ON DELETE CASCADE,
  INDEX (report_id),
  INDEX (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 8) support_chats
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS support_chats (
  chat_id INT AUTO_INCREMENT PRIMARY KEY,
  reporter_id INT NOT NULL,
  admin_id INT DEFAULT NULL,
  issue_summary VARCHAR(255) DEFAULT NULL,
  status ENUM('open','closed') DEFAULT 'open',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  closed_at DATETIME DEFAULT NULL,
  CONSTRAINT fk_chat_reporter FOREIGN KEY (reporter_id) REFERENCES reporters(reporter_id) ON DELETE CASCADE,
  CONSTRAINT fk_chat_admin FOREIGN KEY (admin_id) REFERENCES admins(admin_id) ON DELETE SET NULL,
  INDEX (reporter_id),
  INDEX (admin_id),
  INDEX (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 9) chat_messages
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_messages (
  message_id INT AUTO_INCREMENT PRIMARY KEY,
  chat_id INT NOT NULL,
  sender_type ENUM('reporter','admin') NOT NULL,
  sender_id INT NOT NULL,
  message_text TEXT NOT NULL,
  sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_read BOOLEAN DEFAULT FALSE,
  CONSTRAINT fk_message_chat FOREIGN KEY (chat_id) REFERENCES support_chats(chat_id) ON DELETE CASCADE,
  INDEX (chat_id),
  INDEX (sender_type, sender_id),
  INDEX (sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 10) org_requests (external orgs requesting data)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS org_requests (
  request_id INT AUTO_INCREMENT PRIMARY KEY,
  org_id INT NOT NULL,
  area_id INT DEFAULT NULL,
  crime_type_id INT DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  status ENUM('pending','approved','rejected') DEFAULT 'pending',
  admin_id INT DEFAULT NULL,
  date_requested DATETIME DEFAULT CURRENT_TIMESTAMP,
  date_responded DATETIME DEFAULT NULL,
  response_payload TEXT DEFAULT NULL, -- optional text / links to files
  CONSTRAINT fk_orgreq_org FOREIGN KEY (org_id) REFERENCES external_orgs(org_id) ON DELETE CASCADE,
  CONSTRAINT fk_orgreq_area FOREIGN KEY (area_id) REFERENCES areas(area_id) ON DELETE SET NULL,
  CONSTRAINT fk_orgreq_crimetype FOREIGN KEY (crime_type_id) REFERENCES crime_types(crime_type_id) ON DELETE SET NULL,
  CONSTRAINT fk_orgreq_admin FOREIGN KEY (admin_id) REFERENCES admins(admin_id) ON DELETE SET NULL,
  INDEX (org_id),
  INDEX (status),
  INDEX (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 11) data_request_responses (store sent files / metadata)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS data_request_responses (
  response_id INT AUTO_INCREMENT PRIMARY KEY,
  request_id INT NOT NULL,
  sent_by_admin INT DEFAULT NULL,
  file_url VARCHAR(255) DEFAULT NULL,
  file_summary TEXT DEFAULT NULL,
  sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_drr_request FOREIGN KEY (request_id) REFERENCES org_requests(request_id) ON DELETE CASCADE,
  CONSTRAINT fk_drr_admin FOREIGN KEY (sent_by_admin) REFERENCES admins(admin_id) ON DELETE SET NULL,
  INDEX (request_id),
  INDEX (sent_by_admin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 12) report_approvals (audit trail for report actions)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS report_approvals (
  approval_id INT AUTO_INCREMENT PRIMARY KEY,
  report_id INT NOT NULL,
  admin_id INT NOT NULL,
  action ENUM('approved','rejected','resolved') NOT NULL,
  comment TEXT DEFAULT NULL,
  performed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_approval_report FOREIGN KEY (report_id) REFERENCES reports(report_id) ON DELETE CASCADE,
  CONSTRAINT fk_approval_admin FOREIGN KEY (admin_id) REFERENCES admins(admin_id) ON DELETE CASCADE,
  INDEX (report_id),
  INDEX (admin_id),
  INDEX (performed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 13) notifications
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
  notification_id INT AUTO_INCREMENT PRIMARY KEY,
  recipient_type ENUM('reporter','org','admin') NOT NULL,
  recipient_id INT NOT NULL,
  title VARCHAR(150) NOT NULL,
  message TEXT NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  related_table VARCHAR(100) DEFAULT NULL,
  related_id INT DEFAULT NULL,
  INDEX (recipient_type, recipient_id),
  INDEX (is_read),
  INDEX (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 14) heatmap_queries
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS heatmap_queries (
  query_id INT AUTO_INCREMENT PRIMARY KEY,
  reporter_id INT DEFAULT NULL,
  area_id INT DEFAULT NULL,
  crime_type_id INT DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  generated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  result_cache JSON DEFAULT NULL,
  CONSTRAINT fk_hq_reporter FOREIGN KEY (reporter_id) REFERENCES reporters(reporter_id) ON DELETE SET NULL,
  CONSTRAINT fk_hq_area FOREIGN KEY (area_id) REFERENCES areas(area_id) ON DELETE SET NULL,
  CONSTRAINT fk_hq_crimetype FOREIGN KEY (crime_type_id) REFERENCES crime_types(crime_type_id) ON DELETE SET NULL,
  INDEX (reporter_id),
  INDEX (area_id),
  INDEX (crime_type_id),
  INDEX (generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 15) analytics_cache (generic cache for expensive queries)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS analytics_cache (
  cache_id INT AUTO_INCREMENT PRIMARY KEY,
  query_hash VARCHAR(128) UNIQUE NOT NULL,
  params JSON NOT NULL,
  data JSON NOT NULL,
  generated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  expires_at DATETIME DEFAULT NULL,
  INDEX (query_hash),
  INDEX (generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 16) sessions (simple token store if you want)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS sessions (
  session_id INT AUTO_INCREMENT PRIMARY KEY,
  user_type ENUM('reporter','admin','org') NOT NULL,
  user_id INT NOT NULL,
  token VARCHAR(512) NOT NULL,
  expires_at DATETIME DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX (user_type, user_id),
  INDEX (token(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 17) feedback
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS feedback (
  feedback_id INT AUTO_INCREMENT PRIMARY KEY,
  given_by_type ENUM('reporter','org') NOT NULL,
  given_by_id INT NOT NULL,
  category ENUM('support','report_review','data_request') NOT NULL,
  rating TINYINT UNSIGNED DEFAULT NULL,
  comments TEXT DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX (given_by_type, given_by_id),
  INDEX (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- 18) activity_log
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS activity_log (
  activity_id INT AUTO_INCREMENT PRIMARY KEY,
  actor_type ENUM('admin','reporter','org','system') NOT NULL,
  actor_id INT DEFAULT NULL,
  action VARCHAR(100) NOT NULL,
  target_table VARCHAR(100) DEFAULT NULL,
  target_id INT DEFAULT NULL,
  timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
  metadata JSON DEFAULT NULL,
  INDEX (actor_type, actor_id),
  INDEX (action),
  INDEX (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Useful sample views (optional)
-- -----------------------------------------------------
-- View: recent pending reports
DROP VIEW IF EXISTS v_recent_pending_reports;
CREATE VIEW v_recent_pending_reports AS
SELECT r.report_id, r.title, r.description, r.report_datetime, rp.alias AS reporter_alias, ct.name AS crime_type, a.name AS area_name
FROM reports r
LEFT JOIN reporters rp ON r.reporter_id = rp.reporter_id
LEFT JOIN crime_types ct ON r.crime_type_id = ct.crime_type_id
LEFT JOIN areas a ON r.area_id = a.area_id
WHERE r.status = 'pending'
ORDER BY r.report_datetime DESC
LIMIT 100;

-- -----------------------------------------------------
-- End of schema
-- -----------------------------------------------------

show tables;
describe reports;
describe heatmap_queries;
SELECT table_name, constraint_name, referenced_table_name
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'crime_app' AND referenced_table_name IS NOT NULL;
show databases;