DROP TABLE IF EXISTS USER;

CREATE TABLE USER (
  id INT AUTO_INCREMENT  PRIMARY KEY,
  matrikel_Nummer VARCHAR(20),
  password VARCHAR(120) NOT NULL,
  username VARCHAR(50) NOT NULL,
  forename VARCHAR(50) ,
  is_Admin CHAR (5),
  surname VARCHAR(50)
);


DROP TABLE IF EXISTS SEMESTER;

CREATE TABLE SEMESTER (
  id INT AUTO_INCREMENT  PRIMARY KEY,
  type VARCHAR(1),
  year NUMBER (10)
);


DROP TABLE IF EXISTS COURSE;

CREATE TABLE COURSE (
  id INT AUTO_INCREMENT  PRIMARY KEY,
  semester_id VARCHAR(20),
  number VARCHAR(10) NOT NULL,
  name VARCHAR(100) NOT NULL,
  min_Kreuzel Number (10),
  min_Points Number (10)
);

Alter table course
 ADD CONSTRAINT FOREIGN_KEY_SEMESTER FOREIGN KEY (semester_id) references SEMESTER;
