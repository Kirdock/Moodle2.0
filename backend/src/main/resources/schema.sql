CREATE TABLE IF NOT EXISTS USER
(
    matrikel_Nummer VARCHAR(20) PRIMARY KEY,
    password        VARCHAR(120) NOT NULL,
    username        VARCHAR(50)  NOT NULL,
    forename        VARCHAR(50),
    role            CHAR(5),
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
    CONSTRAINT FOREIGN_KEY_SEMESTER FOREIGN KEY (semester_id) references SEMESTER
);


CREATE TABLE IF NOT EXISTS USER_IN_COURSE
(
    course_id       INT,
    matrikel_Nummer VARCHAR(20),
    role            VARCHAR(20),
    PRIMARY KEY (course_id, matrikel_Nummer),
    CONSTRAINT FOREIGN_KEY_USER FOREIGN KEY (matrikel_Nummer) references USER on delete cascade,
    CONSTRAINT FOREIGN_KEY_COURSE FOREIGN KEY (course_id) references COURSE on delete cascade
);


CREATE TABLE IF NOT EXISTS EXERCISE_SHEET
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    course_id       INT,
    sort_Order     INT ,
    name VARCHAR(100),
    min_Kreuzel Number(10),
    min_Points  Number(10),
    submission_Date datetime,
    CONSTRAINT FOREIGN_KEY_COURSE_EXERCISE_SHEET FOREIGN KEY (course_id) references COURSE on delete cascade
);

CREATE TABLE IF NOT EXISTS EXAMPLE
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    exercise_Sheet_id       INT NOT NULL ,
    sort_Order     INT ,
    parentExample_id INT,
    points Number(10),
    weighting  Number(10),
    is_Mandatory char(1),
    CONSTRAINT FOREIGN_KEY_EXERCISE_SHEET FOREIGN KEY (exercise_Sheet_id) references EXERCISE_SHEET on delete cascade,
    CONSTRAINT FOREIGN_KEY_EXAMPLE_PARENT FOREIGN KEY (parentExample_id) references EXAMPLE on delete cascade

);

CREATE TABLE IF NOT EXISTS FINISHES_EXAMPLE
(
    example_id       INT,
    matrikel_Nummer VARCHAR(20),
    state            VARCHAR(5),
    reason  TEXT,
    valid   CHAR(1),
    attachment CLOB,
    PRIMARY KEY (example_id, matrikel_Nummer),
    CONSTRAINT FOREIGN_KEY_USER_FINISHES_EXAMPLE FOREIGN KEY (matrikel_Nummer) references USER on delete cascade,
    CONSTRAINT FOREIGN_KEY_EXAMPLE_FINISHES_EXAMPLE FOREIGN KEY (example_id) references EXAMPLE on delete cascade
);