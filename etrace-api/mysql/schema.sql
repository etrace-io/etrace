CREATE DATABASE  IF NOT EXISTS `etrace` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `etrace`;
-- MySQL dump 10.13  Distrib 8.0.20, for macos10.15 (x86_64)
--
-- Host: 127.0.0.1    Database: etrace
-- ------------------------------------------------------
-- Server version	8.0.21

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `api_token`
--

DROP TABLE IF EXISTS `api_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_token` (
                             `id` bigint NOT NULL,
                             `created_at` datetime DEFAULT NULL,
                             `updated_at` datetime DEFAULT NULL,
                             `cid` varchar(255) DEFAULT NULL,
                             `created_by` varchar(255) DEFAULT NULL,
                             `is_always_access` varchar(255) DEFAULT NULL,
                             `status` varchar(255) DEFAULT NULL,
                             `token` varchar(255) DEFAULT NULL,
                             `updated_by` varchar(255) DEFAULT NULL,
                             `user_email` varchar(255) NOT NULL,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `UK_7cia4eeb3klyl3mce3tvybuo1` (`user_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `api_token`
--

LOCK TABLES `api_token` WRITE;
/*!40000 ALTER TABLE `api_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `api_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app`
--

DROP TABLE IF EXISTS `app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app` (
                       `id` bigint NOT NULL,
                       `created_at` datetime DEFAULT NULL,
                       `updated_at` datetime DEFAULT NULL,
                       `app_id` varchar(255) NOT NULL,
                       `app_name` varchar(255) DEFAULT NULL,
                       `app_type` varchar(255) DEFAULT NULL,
                       `critical` varchar(255) DEFAULT NULL,
                       `owner` varchar(255) DEFAULT NULL,
                       `status` varchar(255) DEFAULT NULL,
                       `tenant` varchar(255) DEFAULT NULL,
                       PRIMARY KEY (`id`),
                       UNIQUE KEY `UK_fo5tnbjkos6udvf57gdtr6ro2` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app`
--

LOCK TABLES `app` WRITE;
/*!40000 ALTER TABLE `app` DISABLE KEYS */;
/*!40000 ALTER TABLE `app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `apply_token_log`
--

DROP TABLE IF EXISTS `apply_token_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `apply_token_log` (
                                   `id` bigint NOT NULL,
                                   `created_at` datetime DEFAULT NULL,
                                   `updated_at` datetime DEFAULT NULL,
                                   `apply_reason` varchar(255) DEFAULT NULL,
                                   `audit_opinion` varchar(255) DEFAULT NULL,
                                   `audit_status` varchar(255) DEFAULT NULL,
                                   `created_by` varchar(255) DEFAULT NULL,
                                   `status` varchar(255) DEFAULT NULL,
                                   `updated_by` varchar(255) DEFAULT NULL,
                                   `user_email` varchar(255) NOT NULL,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `UK_advyj91lomm77svc7grmj6a71` (`user_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `apply_token_log`
--

LOCK TABLES `apply_token_log` WRITE;
/*!40000 ALTER TABLE `apply_token_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `apply_token_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chart`
--

DROP TABLE IF EXISTS `chart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chart` (
                         `id` bigint NOT NULL,
                         `created_at` datetime DEFAULT NULL,
                         `updated_at` datetime DEFAULT NULL,
                         `admin_visible` varchar(255) DEFAULT NULL,
                         `created_by` varchar(255) DEFAULT NULL,
                         `description` varchar(255) DEFAULT NULL,
                         `favorite_count` varchar(255) DEFAULT NULL,
                         `global_id` varchar(255) NOT NULL,
                         `is_star` varchar(255) DEFAULT NULL,
                         `status` varchar(255) DEFAULT NULL,
                         `title` varchar(255) DEFAULT NULL,
                         `updated_by` varchar(255) DEFAULT NULL,
                         `view_count` varchar(255) DEFAULT NULL,
                         `config` varchar(255) DEFAULT NULL,
                         `targets` varchar(255) DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `UK_b75w12kgnonflf1j6qk2gn4d6` (`global_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chart`
--

LOCK TABLES `chart` WRITE;
/*!40000 ALTER TABLE `chart` DISABLE KEYS */;
/*!40000 ALTER TABLE `chart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `config`
--

DROP TABLE IF EXISTS `config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `config` (
                          `id` bigint NOT NULL,
                          `created_at` datetime DEFAULT NULL,
                          `updated_at` datetime DEFAULT NULL,
                          `app_name` varchar(255) DEFAULT NULL,
                          `idc` varchar(255) DEFAULT NULL,
                          `key` varchar(255) DEFAULT NULL,
                          `status` varchar(255) DEFAULT NULL,
                          `value` varchar(255) DEFAULT NULL,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `config`
--

LOCK TABLES `config` WRITE;
/*!40000 ALTER TABLE `config` DISABLE KEYS */;
/*!40000 ALTER TABLE `config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dashboard`
--

DROP TABLE IF EXISTS `dashboard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dashboard` (
                             `id` bigint NOT NULL,
                             `created_at` datetime DEFAULT NULL,
                             `updated_at` datetime DEFAULT NULL,
                             `admin_visible` varchar(255) DEFAULT NULL,
                             `created_by` varchar(255) DEFAULT NULL,
                             `description` varchar(255) DEFAULT NULL,
                             `favorite_count` varchar(255) DEFAULT NULL,
                             `global_id` varchar(255) NOT NULL,
                             `is_star` varchar(255) DEFAULT NULL,
                             `status` varchar(255) DEFAULT NULL,
                             `title` varchar(255) DEFAULT NULL,
                             `updated_by` varchar(255) DEFAULT NULL,
                             `view_count` varchar(255) DEFAULT NULL,
                             `chart_ids` varchar(255) DEFAULT NULL,
                             `config` varchar(255) DEFAULT NULL,
                             `layout` varchar(255) DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `UK_flqkpqi3jhe0tap0xh9mgayn8` (`global_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dashboard`
--

LOCK TABLES `dashboard` WRITE;
/*!40000 ALTER TABLE `dashboard` DISABLE KEYS */;
/*!40000 ALTER TABLE `dashboard` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dashboard_app`
--

DROP TABLE IF EXISTS `dashboard_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dashboard_app` (
                                 `id` bigint NOT NULL,
                                 `created_at` datetime DEFAULT NULL,
                                 `updated_at` datetime DEFAULT NULL,
                                 `admin_visible` varchar(255) DEFAULT NULL,
                                 `created_by` varchar(255) DEFAULT NULL,
                                 `description` varchar(255) DEFAULT NULL,
                                 `favorite_count` varchar(255) DEFAULT NULL,
                                 `global_id` varchar(255) NOT NULL,
                                 `is_star` varchar(255) DEFAULT NULL,
                                 `status` varchar(255) DEFAULT NULL,
                                 `title` varchar(255) DEFAULT NULL,
                                 `updated_by` varchar(255) DEFAULT NULL,
                                 `view_count` varchar(255) DEFAULT NULL,
                                 `critical` varchar(255) DEFAULT NULL,
                                 `dashboard_ids` varchar(255) DEFAULT NULL,
                                 `icon` varchar(255) DEFAULT NULL,
                                 `order` varchar(10) DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `UK_6qrm9hsfk4qg0c4lt04bsnm7v` (`global_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dashboard_app`
--

LOCK TABLES `dashboard_app` WRITE;
/*!40000 ALTER TABLE `dashboard_app` DISABLE KEYS */;
/*!40000 ALTER TABLE `dashboard_app` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `datasource`
--

DROP TABLE IF EXISTS `datasource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `datasource` (
                              `id` bigint NOT NULL,
                              `created_at` datetime DEFAULT NULL,
                              `updated_at` datetime DEFAULT NULL,
                              `config` varchar(255) DEFAULT NULL,
                              `name` varchar(255) DEFAULT NULL,
                              `status` varchar(255) DEFAULT NULL,
                              `type` varchar(255) DEFAULT NULL,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datasource`
--

LOCK TABLES `datasource` WRITE;
/*!40000 ALTER TABLE `datasource` DISABLE KEYS */;
/*!40000 ALTER TABLE `datasource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `graph`
--

DROP TABLE IF EXISTS `graph`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `graph` (
                         `id` bigint NOT NULL,
                         `created_at` datetime DEFAULT NULL,
                         `updated_at` datetime DEFAULT NULL,
                         `admin_visible` varchar(255) DEFAULT NULL,
                         `created_by` varchar(255) DEFAULT NULL,
                         `description` varchar(255) DEFAULT NULL,
                         `favorite_count` varchar(255) DEFAULT NULL,
                         `global_id` varchar(255) NOT NULL,
                         `is_star` varchar(255) DEFAULT NULL,
                         `status` varchar(255) DEFAULT NULL,
                         `title` varchar(255) DEFAULT NULL,
                         `updated_by` varchar(255) DEFAULT NULL,
                         `view_count` varchar(255) DEFAULT NULL,
                         `config` varchar(255) DEFAULT NULL,
                         `layout` varchar(255) DEFAULT NULL,
                         `node_ids` varchar(255) DEFAULT NULL,
                         `nodes` varchar(255) DEFAULT NULL,
                         `relations` varchar(255) DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `UK_cboxqslcmecsq46kux0teylg4` (`global_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `graph`
--

LOCK TABLES `graph` WRITE;
/*!40000 ALTER TABLE `graph` DISABLE KEYS */;
/*!40000 ALTER TABLE `graph` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hibernate_sequence` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` VALUES (1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1),(1);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `history_log`
--

DROP TABLE IF EXISTS `history_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `history_log` (
                               `id` bigint NOT NULL,
                               `created_at` datetime DEFAULT NULL,
                               `updated_at` datetime DEFAULT NULL,
                               `created_by` varchar(255) DEFAULT NULL,
                               `history` varchar(255) DEFAULT NULL,
                               `history_id` varchar(255) DEFAULT NULL,
                               `type` varchar(255) DEFAULT NULL,
                               `updated_by` varchar(255) DEFAULT NULL,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `history_log`
--

LOCK TABLES `history_log` WRITE;
/*!40000 ALTER TABLE `history_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `history_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `limit_sql`
--

DROP TABLE IF EXISTS `limit_sql`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `limit_sql` (
                             `id` bigint NOT NULL,
                             `created_at` datetime DEFAULT NULL,
                             `updated_at` datetime DEFAULT NULL,
                             `created_by` varchar(255) DEFAULT NULL,
                             `limit_query_type` varchar(255) DEFAULT NULL,
                             `measurement` varchar(255) DEFAULT NULL,
                             `sql` varchar(255) DEFAULT NULL,
                             `status` varchar(255) DEFAULT NULL,
                             `updated_by` varchar(255) DEFAULT NULL,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `limit_sql`
--

LOCK TABLES `limit_sql` WRITE;
/*!40000 ALTER TABLE `limit_sql` DISABLE KEYS */;
/*!40000 ALTER TABLE `limit_sql` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monitor_entity`
--

DROP TABLE IF EXISTS `monitor_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monitor_entity` (
                                  `id` bigint NOT NULL,
                                  `created_at` datetime DEFAULT NULL,
                                  `updated_at` datetime DEFAULT NULL,
                                  `children` varchar(255) DEFAULT NULL,
                                  `code` varchar(255) DEFAULT NULL,
                                  `config` varchar(255) DEFAULT NULL,
                                  `database` varchar(255) DEFAULT NULL,
                                  `datasource_id` varchar(255) DEFAULT NULL,
                                  `meta_link` varchar(255) DEFAULT NULL,
                                  `meta_placeholder` varchar(255) DEFAULT NULL,
                                  `meta_url` varchar(255) DEFAULT NULL,
                                  `name` varchar(255) DEFAULT NULL,
                                  `parent_id` bigint NOT NULL,
                                  `status` varchar(255) DEFAULT NULL,
                                  `type` varchar(255) DEFAULT NULL,
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monitor_entity`
--

LOCK TABLES `monitor_entity` WRITE;
/*!40000 ALTER TABLE `monitor_entity` DISABLE KEYS */;
/*!40000 ALTER TABLE `monitor_entity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `node`
--

DROP TABLE IF EXISTS `node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `node` (
                        `id` bigint NOT NULL,
                        `created_at` datetime DEFAULT NULL,
                        `updated_at` datetime DEFAULT NULL,
                        `admin_visible` varchar(255) DEFAULT NULL,
                        `created_by` varchar(255) DEFAULT NULL,
                        `description` varchar(255) DEFAULT NULL,
                        `favorite_count` varchar(255) DEFAULT NULL,
                        `global_id` varchar(255) NOT NULL,
                        `is_star` varchar(255) DEFAULT NULL,
                        `status` varchar(255) DEFAULT NULL,
                        `title` varchar(255) DEFAULT NULL,
                        `updated_by` varchar(255) DEFAULT NULL,
                        `view_count` varchar(255) DEFAULT NULL,
                        `app_id` varchar(255) DEFAULT NULL,
                        `chart_ids` varchar(255) DEFAULT NULL,
                        `charts` varchar(255) DEFAULT NULL,
                        `config` varchar(255) DEFAULT NULL,
                        `group_by` varchar(255) DEFAULT NULL,
                        `layout` varchar(255) DEFAULT NULL,
                        `node_type` varchar(255) DEFAULT NULL,
                        `single_node_config` varchar(255) DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `UK_3lt6o0sqvk1mivrcnnupqv6pa` (`global_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `node`
--

LOCK TABLES `node` WRITE;
/*!40000 ALTER TABLE `node` DISABLE KEYS */;
/*!40000 ALTER TABLE `node` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proxy_config`
--

DROP TABLE IF EXISTS `proxy_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proxy_config` (
                                `id` bigint NOT NULL,
                                `created_at` datetime DEFAULT NULL,
                                `updated_at` datetime DEFAULT NULL,
                                `clusters` varchar(255) DEFAULT NULL,
                                `path` varchar(255) DEFAULT NULL,
                                `proxy_path` varchar(255) DEFAULT NULL,
                                `rule` varchar(255) DEFAULT NULL,
                                `server_name` varchar(255) DEFAULT NULL,
                                `status` varchar(255) DEFAULT NULL,
                                `type` int NOT NULL,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proxy_config`
--

LOCK TABLES `proxy_config` WRITE;
/*!40000 ALTER TABLE `proxy_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `proxy_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_key_word`
--

DROP TABLE IF EXISTS `search_key_word`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_key_word` (
                                   `id` bigint NOT NULL,
                                   `created_at` datetime DEFAULT NULL,
                                   `updated_at` datetime DEFAULT NULL,
                                   `name` varchar(255) DEFAULT NULL,
                                   `status` varchar(255) DEFAULT NULL,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_key_word`
--

LOCK TABLES `search_key_word` WRITE;
/*!40000 ALTER TABLE `search_key_word` DISABLE KEYS */;
/*!40000 ALTER TABLE `search_key_word` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_key_word_correlation`
--

DROP TABLE IF EXISTS `search_key_word_correlation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_key_word_correlation` (
                                               `id` bigint NOT NULL,
                                               `created_at` datetime DEFAULT NULL,
                                               `updated_at` datetime DEFAULT NULL,
                                               `correlation_coefficient` int NOT NULL,
                                               `correlation_keyword_id` varchar(255) DEFAULT NULL,
                                               `keyword_id` varchar(255) DEFAULT NULL,
                                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_key_word_correlation`
--

LOCK TABLES `search_key_word_correlation` WRITE;
/*!40000 ALTER TABLE `search_key_word_correlation` DISABLE KEYS */;
/*!40000 ALTER TABLE `search_key_word_correlation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_list`
--

DROP TABLE IF EXISTS `search_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_list` (
                               `id` bigint NOT NULL,
                               `created_at` datetime DEFAULT NULL,
                               `updated_at` datetime DEFAULT NULL,
                               `created_by` varchar(255) DEFAULT NULL,
                               `description` varchar(255) DEFAULT NULL,
                               `icon` varchar(255) DEFAULT NULL,
                               `list_type` varchar(255) DEFAULT NULL,
                               `maintainer_email` varchar(255) DEFAULT NULL,
                               `name` varchar(255) DEFAULT NULL,
                               `star` varchar(255) DEFAULT NULL,
                               `status` varchar(255) DEFAULT NULL,
                               `updated_by` varchar(255) DEFAULT NULL,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_list`
--

LOCK TABLES `search_list` WRITE;
/*!40000 ALTER TABLE `search_list` DISABLE KEYS */;
/*!40000 ALTER TABLE `search_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_list_record_mapping`
--

DROP TABLE IF EXISTS `search_list_record_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_list_record_mapping` (
                                              `id` bigint NOT NULL,
                                              `created_at` datetime DEFAULT NULL,
                                              `updated_at` datetime DEFAULT NULL,
                                              `list_id` varchar(255) DEFAULT NULL,
                                              `record_id` varchar(255) DEFAULT NULL,
                                              `status` varchar(255) DEFAULT NULL,
                                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_list_record_mapping`
--

LOCK TABLES `search_list_record_mapping` WRITE;
/*!40000 ALTER TABLE `search_list_record_mapping` DISABLE KEYS */;
/*!40000 ALTER TABLE `search_list_record_mapping` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_record`
--

DROP TABLE IF EXISTS `search_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_record` (
                                 `id` bigint NOT NULL,
                                 `created_at` datetime DEFAULT NULL,
                                 `updated_at` datetime DEFAULT NULL,
                                 `click_index` int NOT NULL,
                                 `created_by` varchar(255) DEFAULT NULL,
                                 `description` varchar(255) DEFAULT NULL,
                                 `dingtalk_number` varchar(255) DEFAULT NULL,
                                 `favorite_index` int NOT NULL,
                                 `icon` varchar(255) DEFAULT NULL,
                                 `keyword_list` varchar(255) DEFAULT NULL,
                                 `maintainer_email` varchar(255) DEFAULT NULL,
                                 `name` varchar(255) DEFAULT NULL,
                                 `owner_dingtalk_number` varchar(255) DEFAULT NULL,
                                 `owner_dingtalkame` varchar(255) DEFAULT NULL,
                                 `star` varchar(255) DEFAULT NULL,
                                 `status` varchar(255) DEFAULT NULL,
                                 `type` varchar(255) DEFAULT NULL,
                                 `type_name` varchar(255) DEFAULT NULL,
                                 `updated_by` varchar(255) DEFAULT NULL,
                                 `url` varchar(255) DEFAULT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_record`
--

LOCK TABLES `search_record` WRITE;
/*!40000 ALTER TABLE `search_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `search_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_record_keyword_mapping`
--

DROP TABLE IF EXISTS `search_record_keyword_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_record_keyword_mapping` (
                                                 `id` bigint NOT NULL,
                                                 `created_at` datetime DEFAULT NULL,
                                                 `updated_at` datetime DEFAULT NULL,
                                                 `keyword_id` bigint NOT NULL,
                                                 `record_id` bigint NOT NULL,
                                                 `status` varchar(255) DEFAULT NULL,
                                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_record_keyword_mapping`
--

LOCK TABLES `search_record_keyword_mapping` WRITE;
/*!40000 ALTER TABLE `search_record_keyword_mapping` DISABLE KEYS */;
/*!40000 ALTER TABLE `search_record_keyword_mapping` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
                        `id` bigint NOT NULL,
                        `created_at` datetime DEFAULT NULL,
                        `updated_at` datetime DEFAULT NULL,
                        `email` varchar(255) NOT NULL,
                        `password` varchar(255) DEFAULT NULL,
                        `user_name` varchar(255) DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `UK_ob8kqyqqgmefl0aco34akdtpe` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_action`
--

DROP TABLE IF EXISTS `user_action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_action` (
                               `id` bigint NOT NULL,
                               `created_at` datetime DEFAULT NULL,
                               `updated_at` datetime DEFAULT NULL,
                               `favorite_apps` varchar(255) DEFAULT NULL,
                               `favorite_board_ids` varchar(255) DEFAULT NULL,
                               `favorite_graph_ids` varchar(255) DEFAULT NULL,
                               `favorite_list_ids` varchar(255) DEFAULT NULL,
                               `favorite_node_ids` varchar(255) DEFAULT NULL,
                               `favorite_record_ids` varchar(255) DEFAULT NULL,
                               `user_email` varchar(255) DEFAULT NULL,
                               `view_board_ids` varchar(255) DEFAULT NULL,
                               `view_graph_ids` varchar(255) DEFAULT NULL,
                               `view_node_ids` varchar(255) DEFAULT NULL,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_action`
--

LOCK TABLES `user_action` WRITE;
/*!40000 ALTER TABLE `user_action` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_action` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_config`
--

DROP TABLE IF EXISTS `user_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_config` (
                               `id` bigint NOT NULL,
                               `created_at` datetime DEFAULT NULL,
                               `updated_at` datetime DEFAULT NULL,
                               `config` varchar(255) DEFAULT NULL,
                               `user_email` varchar(255) DEFAULT NULL,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_config`
--

LOCK TABLES `user_config` WRITE;
/*!40000 ALTER TABLE `user_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
                             `id` bigint NOT NULL,
                             `created_at` datetime DEFAULT NULL,
                             `updated_at` datetime DEFAULT NULL,
                             `roles` varchar(255) DEFAULT NULL,
                             `user_email` varchar(255) DEFAULT NULL,
                             `user_id` varchar(255) DEFAULT NULL,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-11-11 18:45:29

-- init data
insert into user (id, password, user_name, email) values (1, 'abc', 'admin', 'some111@abc.om'), (2, 'def','user', 'some222@abc.om');
insert into user_role (id, user_id, roles) values (1, 1, 'ROLE_ADMIN'), (2, 2,'ROLE_USER');
