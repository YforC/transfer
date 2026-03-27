
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cell_phone_info
-- ----------------------------
DROP TABLE IF EXISTS `cell_phone_info`;
CREATE TABLE `cell_phone_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cell_phone_number` bigint(20) NULL DEFAULT NULL,
  `remain_money` decimal(10, 2) NULL DEFAULT NULL,
  `order_desc` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `status` tinyint(2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cell_phone_info
-- ----------------------------
INSERT INTO `cell_phone_info` VALUES (1, 177123456, 10.00, '1', 0);
INSERT INTO `cell_phone_info` VALUES (2, 13311111111, 10.00, '1', 0);
INSERT INTO `cell_phone_info` VALUES (3, 17711111111, 10.00, '1', 1);
INSERT INTO `cell_phone_info` VALUES (4, 13312345678, 10.00, '1', 0);
INSERT INTO `cell_phone_info` VALUES (5, 13111111111, 10.00, 'test', 0);

SET FOREIGN_KEY_CHECKS = 1;
