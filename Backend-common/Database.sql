create database crime_app;
use crime_app;
create table reporter(
    reporter_id int primary key not null auto_increment,
    reporter_fname varchar(100) not null,
    reporter_lname varchar(50) not null,
    reporter_alias varchar(50) not null,
    reporter_age int check (reporter_age >= 13),
    reporter_location varchar(150),
    password varchar(255) not null,
    pnumber varchar(15),
    date_created timestamp default current_timestamp,
    last_login timestamp null
);

create table crime_type(
    type_id int primary key not null auto_increment,
    type_name varchar(100) not null unique
);

create table location(
    location_id int primary key not null auto_increment,
    location_name varchar(100) not null,
    county varchar(50),
    subcounty varchar(50),
    ward varchar(50)
);

create table administrator(
    admin_id int primary key not null auto_increment,
    password varchar(255) not null,
    last_login timestamp null
);

create table crime_reports(
    report_id int primary key not null auto_increment,
    reporter_id int null,
    crime_type int,
    location int,
    incident_datetime timestamp not null,
    report_datetime timestamp default current_timestamp,
    report_type enum('sighting', 'victimization'),
    description text,
    status enum('pending', 'approved', 'rejected') default 'pending',
    verified_at timestamp null,
    verified_by int null,
    foreign key(reporter_id) references reporter(reporter_id) on delete set null,
    foreign key(crime_type) references crime_type(type_id) on delete cascade,
    foreign key(location) references location(location_id) on delete cascade,
    foreign key(verified_by) references administrator(admin_id) on delete set null
);

create table report_addons(
    media_id int auto_increment primary key not null,
    report_id int,
    media_type enum('image', 'video') not null,
    file_path varchar(255) not null,
    file_size int,
    uploaded_at timestamp default current_timestamp,
    foreign key(report_id) references crime_reports(report_id) on delete cascade
);

create table customer_support(
    support_id int primary key not null auto_increment,
    requester_id int null,
    attendant_id int null,
    description text,
    status enum('open', 'in progress', 'resolved', 'closed') default 'open',
    foreign key(requester_id) references reporter(reporter_id) on delete set null,
    foreign key(attendant_id) references administrator(admin_id) on delete set null
);

create table activity_reports(
    report_id int primary key not null auto_increment,
    created_by int,
    datetime_from timestamp not null,
    datetime_to timestamp null,
    generated_at timestamp default current_timestamp,
    foreign key(created_by) references administrator(admin_id) on delete cascade
);

delimiter //
create trigger before_insert_reporter
before insert on reporter
for each row 
begin
    if new.reporter_alias is null or new.reporter_alias = '' THEN
        set new.reporter_alias = concat('Anon-', lpad(floor(rand() * 9999), 4, '0'));
    end if;
end;
//
delimiter ;

INSERT INTO administrator (admin_id, password, last_login)
VALUES (1, 'admin123', NULL);

CREATE TABLE IF NOT EXISTS external_orgs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    org_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    org_type ENUM('government', 'ngo', 'private') DEFAULT 'government',
    status ENUM('active', 'inactive', 'suspended') DEFAULT 'active',
    date_registered TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

INSERT INTO external_orgs 
(org_id, name, email, password, contact_person, phone, org_type, status) 
VALUES 
('ext001', 'Kenya Crime Research Institute', 'info@kcri.org', 'orgpass123', 'Dr. Jane Mwangi', '+254712345678', 'government', 'active');
-- Make sure there’s at least one reporter, crime_type, location, and admin first.
-- Example reporter:
INSERT INTO reporter (reporter_fname, reporter_lname, reporter_alias, reporter_age, reporter_location, password, pnumber) 
VALUES ('John', 'Doe', 'JDoe', 25, 'Nairobi', 'test123', '+254700123456');

-- Example crime type:
INSERT INTO crime_type (type_name) VALUES ('Robbery');

-- Example location:
INSERT INTO location (location_name, county, subcounty, ward) 
VALUES ('Kilimani', 'Nairobi', 'Dagoretti North', 'Kilimani Ward');

-- Example admin:
INSERT INTO administrator (password) VALUES ('adminpass123');

-- Now insert a crime report:
INSERT INTO crime_reports (
    reporter_id, crime_type, location, incident_datetime, 
    report_type, description, status, verified_at, verified_by
) VALUES (
    1,                -- reporter_id (assuming John Doe got ID=1)
    1,                -- crime_type (Robbery)
    1,                -- location (Kilimani)
    '2025-09-13 20:15:00', 
    'victimization',
    'Victim was robbed at gunpoint near Yaya Centre.',
    'approved',       -- directly mark as approved for testing
    NOW(),            -- verified_at
    1                 -- verified_by (admin_id = 1)
);

INSERT INTO crime_reports (
    reporter_id,
    crime_type,
    location,
    incident_datetime,
    report_type,
    description,
    status
) VALUES (
    1,                -- reporter_id (must exist in reporter table)
    1,                -- crime_type (must exist in crime_type table)
    1,                -- location (must exist in location table)
    '2025-10-02 19:00:00',
    'sighting',
    'Unusual gathering near the market entrance.',
    'pending'
);