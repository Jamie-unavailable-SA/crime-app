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
