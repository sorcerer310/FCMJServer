/*
SQLyog Ultimate v11.24 (32 bit)
MySQL - 5.7.15 : Database - login
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`login` /*!40100 DEFAULT CHARACTER SET utf8 */;

/*Table structure for table `tbl_agency_card` */

CREATE TABLE `tbl_agency_card` (
  `agency_id` int(11) NOT NULL,
  `room_card` int(11) NOT NULL,
  `referrer` int(11) NOT NULL,
  `state` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`agency_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tbl_card_consume_log` */

CREATE TABLE `tbl_card_consume_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `card` int(11) NOT NULL,
  `room` varchar(6) NOT NULL,
  `time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

/*Table structure for table `tbl_email` */

CREATE TABLE `tbl_email` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) DEFAULT NULL COMMENT '玩家ID',
  `email_new` int(11) DEFAULT NULL COMMENT '玩家是否存在新邮件',
  `email_num` int(11) DEFAULT NULL COMMENT '玩家当前存储的邮件数',
  `last_visit_time` timestamp NULL DEFAULT NULL,
  `email_list` blob,
  PRIMARY KEY (`id`),
  UNIQUE KEY `player_id` (`player_id`)
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8;

/*Table structure for table `tbl_marquee` */

CREATE TABLE `tbl_marquee` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  `roll_times` int(11) DEFAULT NULL,
  `start_time` bigint(20) DEFAULT NULL,
  `end_time` bigint(20) DEFAULT NULL,
  `sender` varchar(255) DEFAULT NULL COMMENT '发送人',
  `create_time` bigint(20) DEFAULT NULL,
  `isSendNow` tinyint(1) DEFAULT NULL COMMENT '是否立即发送 0 false 1 true',
  `isSend` tinyint(1) DEFAULT '0' COMMENT '是否已经发送 0 false 1 true',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tbl_notice` */

CREATE TABLE `tbl_notice` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `startTime` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;

/*Table structure for table `tbl_player` */

CREATE TABLE `tbl_player` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `account` varchar(50) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `head` varchar(500) DEFAULT NULL,
  `card` int(11) DEFAULT NULL,
  `cardConsume` int(11) DEFAULT '0',
  `room` int(11) DEFAULT NULL,
  `ip` varchar(50) DEFAULT NULL,
  `points` int(11) DEFAULT NULL,
  `total` int(11) DEFAULT NULL,
  `forbidTime` varchar(50) DEFAULT NULL,
  `sex` varchar(50) DEFAULT NULL,
  `haveNewEmail` int(11) DEFAULT NULL,
  `unionid` varchar(50) DEFAULT NULL,
  `province` varchar(50) DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `country` varchar(50) DEFAULT NULL,
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` int(11) DEFAULT '1',
  `loginTime` datetime DEFAULT NULL,
  `channel` int(11) DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8;

/*Table structure for table `tbl_player_card_consume_log` */

CREATE TABLE `tbl_player_card_consume_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `card` int(11) NOT NULL,
  `room` varchar(6) NOT NULL,
  `time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tbl_room` */

CREATE TABLE `tbl_room` (
  `roomid` int(11) NOT NULL,
  `ownerAccountID` varchar(100) NOT NULL,
  `roundTotal` int(11) NOT NULL,
  `battleTime` int(11) NOT NULL DEFAULT '0',
  `playType` int(11) DEFAULT NULL,
  `createTime` bigint(20) DEFAULT NULL,
  `roomType` int(11) DEFAULT NULL,
  `isInBattle` tinyint(4) DEFAULT NULL,
  `serverId` int(11) DEFAULT NULL,
  PRIMARY KEY (`roomid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tbl_server` */

CREATE TABLE `tbl_server` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(50) DEFAULT NULL,
  `port` int(20) DEFAULT NULL,
  `maxuser` int(50) DEFAULT NULL,
  `httpPort` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

/*Table structure for table `tbl_update_version` */

CREATE TABLE `tbl_update_version` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `channel` int(11) NOT NULL,
  `updateVersoin` int(11) NOT NULL,
  `createDate` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `updateUrl` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
