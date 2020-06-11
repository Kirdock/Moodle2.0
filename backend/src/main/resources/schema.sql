CREATE TABLE IF NOT EXISTS USER
(
    matriculation_Number VARCHAR(20) PRIMARY KEY,
    password        VARCHAR(120) NOT NULL,
    username        VARCHAR(50)  NOT NULL,
    forename        VARCHAR(50),
    is_Admin            CHAR(5),
    email           VARCHAR(200),
    surname         VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS SEMESTER
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(1),
    year NUMBER(10)
);

CREATE TABLE IF NOT EXISTS COURSE
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    semester_id VARCHAR(20),
    number      VARCHAR(10)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    min_Kreuzel Number(10),
    min_Points  Number(10),
    owner       VARCHAR(20),
    description CLOB,
    description_Template CLOB,
    CONSTRAINT FOREIGN_KEY_SEMESTER FOREIGN KEY (semester_id) references SEMESTER on delete cascade ,
    CONSTRAINT FOREIGN_KEY_OWNER FOREIGN KEY (owner) references USER on delete cascade
);


CREATE TABLE IF NOT EXISTS USER_IN_COURSE
(
    course_id       INT,
    matriculation_Number VARCHAR(20),
    role            VARCHAR(20),
    PRIMARY KEY (course_id, matriculation_Number),
    CONSTRAINT FOREIGN_KEY_USER FOREIGN KEY (matriculation_Number) references USER on delete cascade,
    CONSTRAINT FOREIGN_KEY_COURSE FOREIGN KEY (course_id) references COURSE on delete cascade
);


CREATE TABLE IF NOT EXISTS EXERCISE_SHEET
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    course_id       INT,
    name VARCHAR(100),
    min_Kreuzel Number(10),
    min_Points  Number(10),
    submission_Date datetime,
    issue_Date datetime,
    description CLOB,
    include_Third char(5),
    CONSTRAINT FOREIGN_KEY_COURSE_EXERCISE_SHEET FOREIGN KEY (course_id) references COURSE on delete cascade
);

CREATE TABLE IF NOT EXISTS EXAMPLE
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    exercise_Sheet_id       INT NOT NULL ,
    sort_order     INT ,
    parent_Example_id INT,
    points Number(10),
    weighting  Number(10),
    mandatory char(5),
    description CLOB,
    submit_File char(5),
    name varchar(100),
    custom_File_Types CLOB,
    CONSTRAINT FOREIGN_KEY_EXERCISE_SHEET FOREIGN KEY (exercise_Sheet_id) references EXERCISE_SHEET on delete cascade,
    CONSTRAINT FOREIGN_KEY_EXAMPLE_PARENT FOREIGN KEY (parent_Example_id) references EXAMPLE on delete cascade

);

CREATE TABLE IF NOT EXISTS FINISHES_EXAMPLE
(
    example_id       INT,
    matriculation_Number VARCHAR(20),
    state            VARCHAR(20),
    description  CLOB,
    valid   CHAR(5),
    has_Presented CHAR(5),
    attachment BLOB,
    file_Name VARCHAR(200),
    PRIMARY KEY (example_id, matriculation_Number),
    CONSTRAINT FOREIGN_KEY_USER_FINISHES_EXAMPLE FOREIGN KEY (matriculation_Number) references USER on delete cascade,
    CONSTRAINT FOREIGN_KEY_EXAMPLE_FINISHES_EXAMPLE FOREIGN KEY (example_id) references EXAMPLE on delete cascade
);

CREATE TABLE IF NOT EXISTS FILE_TYPE
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(100),
    value            VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS SUPPORT_FILE_TYPE
(
    example_id      INT,
    file_type_id    INT,
    PRIMARY KEY (example_id, file_type_id),
    CONSTRAINT FOREIGN_KEY_EXAMPLE_01 FOREIGN KEY (example_id) references EXAMPLE on delete cascade,
    CONSTRAINT FOREIGN_KEY_FILE_TYPE_01 FOREIGN KEY (file_type_id) references FILE_TYPE on delete cascade
);