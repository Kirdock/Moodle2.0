DROP TABLE IF EXISTS USER;

CREATE TABLE USER (
  id INT AUTO_INCREMENT  PRIMARY KEY,
  matrikel_Number VARCHAR(20),
  password VARCHAR(120) NOT NULL,
  username VARCHAR(50) NOT NULL,
  forename VARCHAR(50) ,
  is_Admin CHAR (5),
  surename VARCHAR(50)
);



