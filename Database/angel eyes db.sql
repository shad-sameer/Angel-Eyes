/*
SQLyog Community v13.1.5  (64 bit)
MySQL - 5.6.12-log : Database - angeleyes
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`angeleyes` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `angeleyes`;

/*Table structure for table `caretaker` */

DROP TABLE IF EXISTS `caretaker`;

CREATE TABLE `caretaker` (
  `lid` int(11) DEFAULT NULL,
  `caretaker id` int(11) NOT NULL AUTO_INCREMENT,
  `caretaker name` varchar(50) DEFAULT NULL,
  `place` varchar(50) DEFAULT NULL,
  `pincode` int(11) DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `post` varchar(50) DEFAULT NULL,
  `district` varchar(50) DEFAULT NULL,
  `house name` varchar(50) DEFAULT NULL,
  `dob` varchar(50) DEFAULT NULL,
  `gender` varchar(50) DEFAULT NULL,
  `picture` varchar(100) DEFAULT NULL,
  `email id` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`caretaker id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=latin1;

/*Data for the table `caretaker` */

insert  into `caretaker`(`lid`,`caretaker id`,`caretaker name`,`place`,`pincode`,`phone`,`post`,`district`,`house name`,`dob`,`gender`,`picture`,`email id`,`status`) values 
(44,34,'Fahad','mahe',673310,'917034437264','Mahe','Puducherry','FEBINA','31/07/02','Male','/static/caretaker/20230404-090629.jpg','fahadmuhammed133@gmail.com','approve'),
(48,38,'napi','bshsh',315454,'545454343434','hshshd','shhdh','hshsh','12/04/05','Female','/static/caretaker/20230412-112124.jpg','zehranahila91@gmail.com','reject');

/*Table structure for table `complaints` */

DROP TABLE IF EXISTS `complaints`;

CREATE TABLE `complaints` (
  `complaint id` int(11) NOT NULL AUTO_INCREMENT,
  `lid` int(11) DEFAULT NULL,
  `date` varchar(20) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `reply` varchar(500) DEFAULT NULL,
  `complaint` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`complaint id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;

/*Data for the table `complaints` */

insert  into `complaints`(`complaint id`,`lid`,`date`,`status`,`reply`,`complaint`) values 
(5,44,'2023-04-04','replied','thank','no complaints '),
(6,44,'2023-04-05','replied','ji','hiiiiii'),
(7,44,'2023-04-05','replied','hii\r\n','hlooo'),
(8,47,'2023-04-11','replied','tryr','bad');

/*Table structure for table `emergency` */

DROP TABLE IF EXISTS `emergency`;

CREATE TABLE `emergency` (
  `lid` int(11) DEFAULT NULL,
  `date` varchar(50) DEFAULT NULL,
  `time` varchar(50) DEFAULT NULL,
  `longitude` varchar(50) DEFAULT NULL,
  `latitude` varchar(50) DEFAULT NULL,
  `emergency id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`emergency id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=latin1;

/*Data for the table `emergency` */

insert  into `emergency`(`lid`,`date`,`time`,`longitude`,`latitude`,`emergency id`) values 
(5,'2023-03-31','12:05:23','75.55138333333333','11.676558333333332',12),
(6,'2023-04-04','09:29:03','75.5371705','11.7509841',13),
(6,'2023-04-04','09:29:38','75.537168','11.7509845',14),
(6,'2023-04-04','09:30:11','75.5371705','11.7509841',15),
(6,'2023-04-05','13:20:54','','',16),
(6,'2023-04-05','14:52:22','','',17),
(7,'2023-04-11','13:35:17','','',18),
(9,'2023-04-12','10:33:53','75.551389','11.6765676',19),
(9,'2023-04-12','11:17:48','75.551382','11.6765631',20),
(9,'2023-04-12','12:48:09','','',21);

/*Table structure for table `emergency number` */

DROP TABLE IF EXISTS `emergency number`;

CREATE TABLE `emergency number` (
  `number_id` int(11) NOT NULL AUTO_INCREMENT,
  `number` bigint(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`number_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

/*Data for the table `emergency number` */

insert  into `emergency number`(`number_id`,`number`,`name`) values 
(3,917034437264,'fahad'),
(4,919897969590,'Test'),
(5,919846614325,'zehra');

/*Table structure for table `feedback` */

DROP TABLE IF EXISTS `feedback`;

CREATE TABLE `feedback` (
  `feedback id` int(11) NOT NULL AUTO_INCREMENT,
  `date` varchar(50) DEFAULT NULL,
  `lid` int(11) DEFAULT NULL,
  `feedback` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`feedback id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;

/*Data for the table `feedback` */

insert  into `feedback`(`feedback id`,`date`,`lid`,`feedback`) values 
(4,'2023-04-04',44,'This app is very useful \n'),
(5,'2023-04-05',44,'hlo oooo'),
(6,'2023-04-05',44,'hlo oooo'),
(7,'2023-04-05',44,'hlo oooo'),
(8,'2023-04-11',47,'good'),
(9,'2023-04-11',47,'good');

/*Table structure for table `location` */

DROP TABLE IF EXISTS `location`;

CREATE TABLE `location` (
  `location id` int(11) NOT NULL AUTO_INCREMENT,
  `lid` int(11) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `longitude` varchar(50) DEFAULT NULL,
  `latitude` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`location id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `location` */

/*Table structure for table `login` */

DROP TABLE IF EXISTS `login`;

CREATE TABLE `login` (
  `lid` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  `type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`lid`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=latin1;

/*Data for the table `login` */

insert  into `login`(`lid`,`username`,`password`,`type`) values 
(0,'admin','123','admin'),
(44,'fahadmuhammed133@gmail.com','917034437264','caretaker'),
(47,'fahad@gmail.com','917034437264','caretaker');

/*Table structure for table `object` */

DROP TABLE IF EXISTS `object`;

CREATE TABLE `object` (
  `object name` varchar(50) DEFAULT NULL,
  `object id` int(11) NOT NULL AUTO_INCREMENT,
  `image` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`object id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Data for the table `object` */

/*Table structure for table `person` */

DROP TABLE IF EXISTS `person`;

CREATE TABLE `person` (
  `lid` int(11) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `picture` varchar(150) DEFAULT NULL,
  `person  id` int(11) NOT NULL AUTO_INCREMENT,
  `relation` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`person  id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;

/*Data for the table `person` */

insert  into `person`(`lid`,`name`,`picture`,`person  id`,`relation`) values 
(44,'Shad','/static/familiar_person/20230405-134503.jpg',12,'brother '),
(47,'zehra','/static/familiar_person/20230412-113049.jpg',13,'friend'),
(47,'zehra','/static/familiar_person/20230412-113432.jpg',14,'friend '),
(47,'zehra','/static/familiar_person/20230412-113532.jpg',15,'friend ');

/*Table structure for table `user` */

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(50) DEFAULT NULL,
  `lid` int(11) DEFAULT NULL,
  `caretaker_id` int(11) DEFAULT NULL,
  `place` varchar(50) DEFAULT NULL,
  `pincode` int(11) DEFAULT NULL,
  `phone` bigint(50) DEFAULT NULL,
  `post` varchar(50) DEFAULT NULL,
  `district` varchar(50) DEFAULT NULL,
  `housename` varchar(50) DEFAULT NULL,
  `dob` varchar(50) DEFAULT NULL,
  `gender` varchar(50) DEFAULT NULL,
  `picture` varchar(100) DEFAULT NULL,
  `email_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;

/*Data for the table `user` */

insert  into `user`(`user_id`,`user_name`,`lid`,`caretaker_id`,`place`,`pincode`,`phone`,`post`,`district`,`housename`,`dob`,`gender`,`picture`,`email_id`) values 
(9,'nappi',1,47,'kryd',122333,637388383883,'Ksjdj','hdhhd','hrhd','12/04/05','Female','/static/user/20230412-100757.jpg','zehranahila91@gmail.com'),
(10,'napi',1,47,'hsjs',162663,172737737273,'jshsh','hehsh','hdhsh','12/04/05','Female','/static/user/20230412-103011.jpg','zehranahila91@gmail.com');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
