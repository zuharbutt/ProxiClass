IF OBJECT_ID('dbo.attendance_records', 'U') IS NOT NULL DROP TABLE attendance_records;
IF OBJECT_ID('dbo.sessions', 'U') IS NOT NULL DROP TABLE sessions;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE users;

CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    full_name VARCHAR(255),
    roll_number VARCHAR(255),
    bluetooth_mac VARCHAR(255),
    section VARCHAR(255),
    department VARCHAR(255),
    fingerprint VARCHAR(255)
);

CREATE TABLE sessions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    course_name VARCHAR(255) NOT NULL,
    section VARCHAR(255) NOT NULL,
    teacher_id BIGINT,
    start_time DATETIME2 NOT NULL,
    end_time DATETIME2,
    status VARCHAR(50),
    attendance_mode VARCHAR(50),
    teacher_lat FLOAT,
    teacher_lng FLOAT,
    teacher_alt FLOAT,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE TABLE attendance_records (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    session_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    marked_at DATETIME2,
    mark_method VARCHAR(50),
    detected_mac VARCHAR(255),
    FOREIGN KEY (session_id) REFERENCES sessions(id),
    FOREIGN KEY (student_id) REFERENCES users(id)
);
