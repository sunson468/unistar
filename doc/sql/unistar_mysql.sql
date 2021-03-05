SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for base_group
-- ----------------------------
DROP TABLE IF EXISTS `base_group`;
CREATE TABLE `base_group`  (
  `group` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `namespace` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  PRIMARY KEY (`group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for base_ns
-- ----------------------------
DROP TABLE IF EXISTS `base_ns`;
CREATE TABLE `base_ns`  (
  `namespace` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`namespace`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for base_operator
-- ----------------------------
DROP TABLE IF EXISTS `base_operator`;
CREATE TABLE `base_operator`  (
  `account` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `nick` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `password` char(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `inited` tinyint(1) NOT NULL DEFAULT 0,
  `roles` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  `namespaces` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  `lastLoginIp` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  `lastLoginTime` bigint(20) UNSIGNED NULL DEFAULT 0,
  `status` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `createTime` bigint(20) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`account`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for base_operator_app
-- ----------------------------
DROP TABLE IF EXISTS `base_operator_app`;
CREATE TABLE `base_operator_app`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `account` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `namespace` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `appname` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_a_ns`(`account`, `namespace`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for base_operator_log
-- ----------------------------
DROP TABLE IF EXISTS `base_operator_log`;
CREATE TABLE `base_operator_log`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `account` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `path` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `ip` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `createTime` bigint(20) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ct`(`createTime`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for base_setting
-- ----------------------------
DROP TABLE IF EXISTS `base_setting`;
CREATE TABLE `base_setting`  (
  `no` tinyint(4) NOT NULL,
  `configkey` char(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`no`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for base_task
-- ----------------------------
DROP TABLE IF EXISTS `base_task`;
CREATE TABLE `base_task`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `task` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `namespace` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uidx_ns_t`(`namespace`, `task`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for base_version
-- ----------------------------
DROP TABLE IF EXISTS `base_version`;
CREATE TABLE `base_version`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `version` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `vershow` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `success` tinyint(1) NOT NULL DEFAULT 0,
  `createTime` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uidx_v`(`version`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for cent_central
-- ----------------------------
DROP TABLE IF EXISTS `cent_central`;
CREATE TABLE `cent_central`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `host` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `port` int(13) NOT NULL,
  `version` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `master` tinyint(1) NULL DEFAULT 0,
  `memoryFree` bigint(20) NULL DEFAULT 0,
  `memoryTotal` bigint(20) NULL DEFAULT 0,
  `memoryMax` bigint(20) NULL DEFAULT 0,
  `processors` int(13) NULL DEFAULT NULL,
  `lastActiveTime` bigint(20) UNSIGNED NULL DEFAULT 0,
  `online` tinyint(1) NULL DEFAULT 0,
  `createTime` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uidx_h_p`(`host`, `port`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for cent_schedule
-- ----------------------------
DROP TABLE IF EXISTS `cent_schedule`;
CREATE TABLE `cent_schedule`  (
  `id` int(13) UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `namespace` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `post` tinyint(1) NOT NULL,
  `cron` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  `group` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `task` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `params` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `parallel` tinyint(4) NULL DEFAULT NULL,
  `postSchedules` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `nextTime` bigint(20) UNSIGNED NULL DEFAULT 0,
  `startTime` bigint(20) UNSIGNED NULL DEFAULT 0,
  `endTime` bigint(20) UNSIGNED NULL DEFAULT 0,
  `inner` tinyint(1) NOT NULL,
  `status` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `createTime` bigint(20) UNSIGNED NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uidx_ns_n`(`namespace`, `name`) USING BTREE,
  INDEX `idx_p_s_nt`(`post`, `status`, `nextTime`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for cent_schedule_task
-- ----------------------------
DROP TABLE IF EXISTS `cent_schedule_task`;
CREATE TABLE `cent_schedule_task`  (
  `no` char(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `namespace` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `schedule` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `preSchedule` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `preNo` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  `preResult` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `task` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `group` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `params` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `appname` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `host` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `port` int(13) NULL DEFAULT NULL,
  `executeTime` bigint(20) UNSIGNED NULL DEFAULT 0,
  `result` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startTime` bigint(20) UNSIGNED NULL DEFAULT 0,
  `finishTime` bigint(20) UNSIGNED NULL DEFAULT 0,
  `type` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `createTime` bigint(20) UNSIGNED NOT NULL,
  PRIMARY KEY (`no`) USING BTREE,
  INDEX `idx_s_et`(`status`, `executeTime`) USING BTREE,
  INDEX `idx_ns_st`(`namespace`, `startTime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for stat_trace
-- ----------------------------
DROP TABLE IF EXISTS `stat_trace`;
CREATE TABLE `stat_trace`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `namespace` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `nodeId` char(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `appname` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `daydate` bigint(20) NULL DEFAULT NULL,
  `tgroup` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `prepath` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  `maxqps` int(13) NULL DEFAULT NULL,
  `count` int(13) UNSIGNED NULL DEFAULT NULL,
  `errors` int(13) UNSIGNED NULL DEFAULT NULL,
  `minTime` int(13) UNSIGNED NULL DEFAULT NULL,
  `maxTime` int(13) UNSIGNED NULL DEFAULT NULL,
  `totalTime` int(13) UNSIGNED NULL DEFAULT NULL,
  `updateTime` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ns_a_n`(`namespace`, `appname`, `nodeId`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for stat_trace_total
-- ----------------------------
DROP TABLE IF EXISTS `stat_trace_total`;
CREATE TABLE `stat_trace_total`  (
  `totalId` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `namespace` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `appname` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `daydate` bigint(20) NULL DEFAULT NULL,
  `maxqps` bigint(20) NULL DEFAULT NULL,
  `count` bigint(20) NULL DEFAULT NULL,
  `errors` bigint(20) NULL DEFAULT NULL,
  `minTime` bigint(20) NULL DEFAULT NULL,
  `maxTime` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`totalId`) USING BTREE,
  INDEX `idx_ns_d`(`namespace`, `daydate`) USING BTREE,
  UNIQUE INDEX `idx_ns_a_d`(`namespace`, `appname`, `daydate`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for us_app
-- ----------------------------
DROP TABLE IF EXISTS `us_app`;
CREATE TABLE `us_app`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `namespace` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `serverable` tinyint(1) NOT NULL,
  `discoverable` tinyint(1) NOT NULL,
  `taskable` tinyint(1) NOT NULL,
  `token` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  `createTime` bigint(20) UNSIGNED NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uidx_ns_n`(`namespace`, `name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for us_app_limit
-- ----------------------------
DROP TABLE IF EXISTS `us_app_limit`;
CREATE TABLE `us_app_limit`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `namespace` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `appname` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `path` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `before` tinyint(1) NULL DEFAULT NULL,
  `auto` tinyint(1) NULL DEFAULT NULL,
  `startTime` bigint(20) NULL DEFAULT NULL,
  `endTime` bigint(20) NULL DEFAULT NULL,
  `period` int(13) NULL DEFAULT NULL,
  `errors` int(13) NULL DEFAULT NULL,
  `recover` int(13) NULL DEFAULT NULL,
  `qps` int(13) NULL DEFAULT NULL,
  `warmup` int(13) NULL DEFAULT NULL,
  `fastfail` tinyint(1) NULL DEFAULT NULL,
  `timeout` int(13) NULL DEFAULT NULL,
  `whiteGroups` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `whiteServices` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `createTime` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uidx_ns_an_p_b_a`(`namespace`, `appname`, `path`, `before`, `auto`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for us_app_node
-- ----------------------------
DROP TABLE IF EXISTS `us_app_node`;
CREATE TABLE `us_app_node`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `namespace` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `appname` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `nodeId` char(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `host` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `port` int(13) NOT NULL,
  `group` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `profiles` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '',
  `serverable` tinyint(1) NULL DEFAULT NULL,
  `weight` int(13) NULL DEFAULT NULL,
  `serviceStatus` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `discoverable` tinyint(1) NULL DEFAULT NULL,
  `discovers` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `discoverStatus` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `taskable` tinyint(1) NULL DEFAULT NULL,
  `tasks` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `taskStatus` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `connectId` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `connectCenter` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `lastConnectTime` bigint(20) UNSIGNED NULL DEFAULT 0,
  `lastDisonnectTime` bigint(20) UNSIGNED NULL DEFAULT 0,
  `manual` tinyint(1) NULL DEFAULT NULL,
  `manualAnurl` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uidx_ns_an_h_p`(`namespace`, `appname`, `host`, `port`) USING BTREE,
  INDEX `idx_ns`(`namespace`) USING BTREE,
  INDEX `idx_nid`(`nodeId`) USING BTREE,
  INDEX `idx_m`(`manual`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for us_config
-- ----------------------------
DROP TABLE IF EXISTS `us_config`;
CREATE TABLE `us_config`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `namespace` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `profile` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `properties` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dependable` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uidx_ns_n_p`(`namespace`, `name`, `profile`) USING BTREE,
  INDEX `idx_d`(`dependable`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for us_config_depend
-- ----------------------------
DROP TABLE IF EXISTS `us_config_depend`;
CREATE TABLE `us_config_depend`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `namespace` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `depends` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `createTime` bigint(20) UNSIGNED NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uidx_ns_n`(`namespace`, `name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;
