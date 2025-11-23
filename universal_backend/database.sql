create database if not exists crimewatch;
use crimewatch;
create table if not exists reporters(
    reporter_id int AUTO_INCREMENT PRIMARY KEY,
    alias varchar(50) not null,
    f_name varchar(50) DEFAULT null,
    l_name varchar(50) DEFAULT null,
    email varchar(100) unique,
    phone varchar(15) DEFAULT null,
    password varchar(255) not null,
    date_joined datetime DEFAULT CURRENT_TIMESTAMP,
    last_login datetime DEFAULT null,
    index idx_reporters_alias(alias),
    index idx_reporters_email(email),
    index idx_reporters_phone(phone)
);
create table if not exists admins(
    admin_id int AUTO_INCREMENT PRIMARY KEY,
    password varchar(255) not null,
    date_created DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME DEFAULT null
);
create table if not exists external_orgs(
    org_id int AUTO_INCREMENT PRIMARY KEY,
    org_name varchar(100) not null,
    contact_person varchar(100) DEFAULT null,
    contact_email varchar(100) unique,
    contact_phone varchar(15) DEFAULT null,
    password varchar(255) not null,
    date_added datetime DEFAULT CURRENT_TIMESTAMP,
    last_login datetime DEFAULT null,
    index idx_external_orgs_name(org_name),
    index idx_external_orgs_email(contact_email),
    index idx_external_orgs_phone(contact_phone)
);
create table if not exists crime_types(
    crime_type_id int AUTO_INCREMENT PRIMARY KEY,
    type_name varchar(100) not null,
    description text DEFAULT null,
    index idx_crime_types_name(type_name)
);
create table if not exists areas(
    area_id int AUTO_INCREMENT PRIMARY KEY,
    area_name varchar(100) not null,
    subcounty varchar(100) not null,
    ward varchar(100) not null,
    latitude double not null,
    longitude double not null,
    index idx_areas_name(area_name),
    index idx_areas_subcounty(subcounty),
    index idx_areas_ward(ward),
    index idx_areas_latlong(latitude, longitude)
);
create table if not exists reports(
    report_id int AUTO_INCREMENT PRIMARY KEY,
    reporter_id int not null,
    crime_type_id int default null,
    description text default null,
    area_id int default null,
    latitude double default null,
    longitude double default null,
    status enum ('pending', 'approved', 'rejected') default 'pending',
    report_type enum ('sighting', 'victimization') default 'sighting',
    report_datetime datetime DEFAULT CURRENT_TIMESTAMP,
    reviewed_by int default null,
    reviewed_at datetime DEFAULT NULL,
    created_at datetime DEFAULT CURRENT_TIMESTAMP,
    updated_at datetime DEFAULT NULL,
    constraint fk_reports_reporter foreign key (reporter_id) references reporters(reporter_id) on delete cascade,
    constraint fk_reports_crime_type foreign key (crime_type_id) references crime_types(crime_type_id) on delete set null,
    constraint fk_reports_area foreign key (area_id) references areas(area_id) on delete set null,
    constraint fk_reports_admin foreign key (reviewed_by) references admins(admin_id) on delete set null,
    index idx_reports_status(status),
    index idx_reports_reporter(reporter_id),
    index idx_reports_crimetype(crime_type_id),
    index idx_reports_area(area_id),
    index idx_reports_datetime(report_datetime),
    index idx_reports_latlong(latitude, longitude)
);
create table if not exists report_addons(
    addon_id int AUTO_INCREMENT PRIMARY KEY,
    report_id int not null,
    file_path varchar(255) not null,
    file_type enum ('image', 'video') not null,
    file_size int default null,
    uploaded_at datetime DEFAULT CURRENT_TIMESTAMP,
    constraint fk_report_addons_report foreign key (report_id) references reports(report_id) on delete cascade,
    index idx_report_addons_report(report_id),
    index idx_report_addons_type(file_type)
);
create table if not exists support_chats(
    chat_id int AUTO_INCREMENT PRIMARY KEY,
    reporter_id int not null,
    admin_id int default null,
    issue_summary varchar(255) not null,
    status enum('open','closed') default 'open',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    closed_at DATETIME DEFAULT null,
    constraint fk_support_chats_reporter foreign key (reporter_id) references reporters(reporter_id) on delete cascade,
    constraint fk_support_chats_admin foreign key (admin_id) references admins(admin_id) on delete set null,
    index idx_support_chats_reporter(reporter_id),
    index idx_support_chats_admin(admin_id),
    index idx_support_chats_status(status)
);
create table if not exists chat_messages(
    message_id int AUTO_INCREMENT PRIMARY KEY,
    chat_id int not null,
    sender_type enum ('reporter', 'admin') not null,
    sender_id int not null,
    message_text text not null,
    sent_at datetime DEFAULT CURRENT_TIMESTAMP,
    is_read boolean DEFAULT false,
    constraint fk_message_chat FOREIGN KEY (chat_id) REFERENCES support_chats(chat_id) on delete CASCADE,
    index idx_chat_messages_chat(chat_id),
    index idx_chat_messages_sender(sender_type, sender_id),
    index idx_chat_messages_sent_at(sent_at)
);
create table if not exists org_requests(
    request_id int AUTO_INCREMENT PRIMARY KEY,
    org_id int not null,
    area_id int default null,
    crime_type_id int default null,
    start_date datetime default null,
    end_date datetime default null,
    status enum ('pending', 'approved', 'rejected') default 'pending',
    admin_id int DEFAULT null,
    date_requested datetime DEFAULT CURRENT_TIMESTAMP,
    date_responded datetime DEFAULT null,
    response_payload text DEFAULT null,
    constraint fk_org_requests_org foreign key (org_id) references external_orgs(org_id) on delete cascade,
    constraint fk_org_requests_area foreign key (area_id) references areas(area_id) on delete set null,
    constraint fk_org_requests_crime_type foreign key (crime_type_id) references crime_types(crime_type_id) on delete set null,
    constraint fk_org_requests_admin foreign key (admin_id) references admins(admin_id) on delete set null,
    index idx_org_requests_org(org_id),
    index idx_org_requests_status(status),
    index idx_org_requests_dates(start_date, end_date)
);
create table if not exists data_request_responses(
    response_id int AUTO_INCREMENT PRIMARY KEY,
    request_id int not null,
    sent_by_admin int DEFAULT null,
    file_path varchar(255) not null,
    file_summary text DEFAULT null,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    constraint fk_data_request_responses_request foreign key (request_id) references org_requests(request_id) on delete cascade,
    constraint fk_data_request_responses_admin foreign key (sent_by_admin) references admins(admin_id) on delete set null,
    index idx_data_request_responses_request (request_id),
    index idx_data_request_responses_admin (sent_by_admin)
);
create table if not exists report_approvals(
    approval_id int AUTO_INCREMENT PRIMARY KEY,
    report_id int not null,
    admin_id int not null,
    action enum('approved', 'rejected') not null,
    comment text default null,
    performed_at datetime default CURRENT_TIMESTAMP,
    constraint fk_report_approvals_report foreign key (report_id) references reports(report_id) on delete cascade,
    constraint fk_report_approvals_admin foreign key (admin_id) references admins(admin_id) on delete cascade,
    index idx_report_approvals_report(report_id),
    index idx_report_approvals_admin(admin_id),
    index idx_report_approvals_performed(performed_at)
);
create table if not exists notifications(
    notification_id int AUTO_INCREMENT PRIMARY KEY,
    recipient_type enum('reporter', 'admin', 'external_org') not null,
    recipient_id int not null,
    title VARCHAR(150) not null,
    message text not null,
    is_read boolean DEFAULT false,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    related_table VARCHAR(100) default null,
    related_id int default null,
    index idx_notifications_recipient(recipient_type, recipient_id),
    index idx_notifications_is_read(is_read),
    index idx_notifications_created(created_at)
);
create table if not exists heatmap_queries(
    query_id int AUTO_INCREMENT PRIMARY KEY,
    reporter_id int default null,
    area_id int default null,
    crime_type_id int default null,
    start_date date DEFAULT null,
    end_date date DEFAULT null,
    generated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    result_cache json default null,
    constraint fk_hq_reporter foreign key (reporter_id) references reporters(reporter_id) on delete set null,
    constraint fk_hq_area foreign key (area_id) references areas(area_id) on delete set null,
    constraint fk_hq_crime_type foreign key (crime_type_id) references crime_types(crime_type_id) on delete set null,
    index idx_hq_reporter(reporter_id),
    index idx_hq_area(area_id),
    index idx_hq_crimetype(crime_type_id),
    index idx_hq_generated(generated_at)
);


create table if not exists analytics_cache(
    cache_id int AUTO_INCREMENT PRIMARY KEY,
    query_hash varchar(128) unique not null,
    params json not null,
    data json not null,
    generated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME DEFAULT null,
    index idx_analytics_query_hash(query_hash),
    index idx_analytics_generated(generated_at)
);
create table if not exists sessions(
    session_id int AUTO_INCREMENT PRIMARY KEY,
    user_type enum('reporter', 'admin', 'external_org') not null,
    user_id int not null,
    token varchar(255) not null,
    expires_at datetime not null,
    created_at datetime DEFAULT CURRENT_TIMESTAMP,
    index idx_sessions_user(user_type, user_id),
    index idx_sessions_token(token(255))
);
create table if not exists feedback(
    feedback_id int AUTO_INCREMENT PRIMARY KEY,
    given_by_type enum('reporter', 'org') not null,
    given_by_id int not null,
    category enum('support', 'report_review', 'data_request') not null,
    rating TINYINT UNSIGNED NOT NULL,
    comments text default null,
    created_at datetime default CURRENT_TIMESTAMP,
    index idx_feedback_giver(given_by_type, given_by_id),
    index idx_feedback_category(category)
);
create table if not exists activity_log(
    activity_id int AUTO_INCREMENT PRIMARY KEY,
    actor_type enum('reporter', 'admin', 'external_org', 'system') not null,
    actor_id int default null,
    action varchar(100) not null,
    target_table varchar(100) default null,
    target_id int default null,
    timestamp datetime DEFAULT CURRENT_TIMESTAMP,
    metadata json default null,
    index idx_activity_actor(actor_type, actor_id),
    index idx_activity_action(action),
    index idx_activity_timestamp(timestamp)
);
drop view if exists v_recent_pending_reports;
create view v_recent_pending_reports as
select
    r.report_id,
    r.description,
    r.report_datetime,
    rp.alias as reporter_alias,
    ct.type_name as crime_type,
    a.area_name as area_name
from reports r
left join reporters rp on r.reporter_id = rp.reporter_id
left join crime_types ct on r.crime_type_id = ct.crime_type_id
left join areas a on r.area_id = a.area_id
where r.status = 'pending'
order by r.report_datetime DESC
limit 100;
show tables;
describe heatmap_queries;
SELECT table_name, constraint_name, referenced_table_name
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'crimewatch' AND referenced_table_name IS NOT NULL;
show databases;
