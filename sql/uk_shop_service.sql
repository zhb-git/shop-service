/*
 Navicat Premium Data Transfer

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 90200
 Source Host           : localhost:3306
 Source Schema         : uk_shop_service

 Target Server Type    : MySQL
 Target Server Version : 90200
 File Encoding         : 65001

 Date: 21/01/2026 18:08:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for shop
-- ----------------------------
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop`  (
  `id` bigint NOT NULL COMMENT '主键 (雪花算法)',
  `no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户名字',
  `public_key` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户公钥 (请求)',
  `balance` decimal(18, 2) NOT NULL DEFAULT 0.00 COMMENT '商户余额',
  `ip_whitelist` json NOT NULL COMMENT '请求IP白名单',
  `webhook_secret` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回调签名密钥 (HMAC-SHA256 Webhook Secret)',
  `webhook_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '回调url',
  `webhook_timeout_ms` int NOT NULL COMMENT '回调超时 (毫秒)',
  `webhook_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '是否启用回调',
  `tron_recharge_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '波场链充值地址',
  `tron_recharge_address_private_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '波场链充值地址私钥',
  `recharge_fee` decimal(18, 6) NOT NULL COMMENT '充值费率',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除逻辑: 0-未删 1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop_no`(`no` ASC) USING BTREE,
  INDEX `idx_shop_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_shop_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for shop_card
-- ----------------------------
DROP TABLE IF EXISTS `shop_card`;
CREATE TABLE `shop_card`  (
  `id` bigint NOT NULL COMMENT '主键 (雪花算法)',
  `shop_id` bigint NOT NULL COMMENT '商户ID',
  `shop_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  `card_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡片ID',
  `card_bin_id` bigint NOT NULL COMMENT '卡头ID',
  `card_bin` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡头',
  `card_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '完整卡号',
  `cvv` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡片CVV',
  `expire_date` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡片过期时间',
  `currency` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡片币种',
  `fee` decimal(18, 6) NOT NULL COMMENT '卡片充值费率',
  `holder_username` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '持卡人姓名',
  `holder_email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '持卡人邮箱',
  `holder_address` json NOT NULL COMMENT '持卡人账单地址',
  `status` int NOT NULL COMMENT '卡片状态',
  `bank_create_time` datetime NOT NULL COMMENT '银行返回的开卡时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除逻辑: 0-未删 1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop_card_card_id`(`card_id` ASC) USING BTREE,
  INDEX `idx_shop_card_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_shop_card_card_bin_id`(`card_bin_id` ASC) USING BTREE,
  INDEX `idx_shop_card_deleted`(`deleted` ASC) USING BTREE,
  UNIQUE INDEX `idx_shop_card_card_no`(`card_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商户卡片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for shop_card_bin
-- ----------------------------
DROP TABLE IF EXISTS `shop_card_bin`;
CREATE TABLE `shop_card_bin`  (
  `id` bigint NOT NULL COMMENT '主键 (雪花算法)',
  `shop_id` bigint NOT NULL COMMENT '商户ID',
  `shop_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  `card_bin_id` bigint NOT NULL COMMENT '卡头ID',
  `card_bin` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡BIN',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡片类型',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡头名称',
  `network` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发行组织',
  `country` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发卡地区',
  `avs` tinyint(1) NOT NULL COMMENT '支持 AVS 校验',
  `_3ds` tinyint(1) NOT NULL COMMENT '支持 3DS 校验',
  `day_purchase_limit` decimal(18, 2) NOT NULL COMMENT '单日消费限额',
  `single_purchase_limit` decimal(18, 2) NOT NULL COMMENT '单笔消费限额',
  `lifetime_purchase_limit` decimal(18, 2) NOT NULL COMMENT '累计消费限额',
  `maintain` tinyint(1) NOT NULL COMMENT '是否维护',
  `weigh` int NOT NULL COMMENT '权重',
  `status` tinyint(1) NOT NULL COMMENT '是否可用',
  `create_amount` decimal(18, 2) NOT NULL COMMENT '开卡价格',
  `card_deposit_fee` decimal(18, 6) NOT NULL COMMENT '充值费率',
  `allow_create` tinyint(1) NOT NULL COMMENT '允许开卡',
  `allow_in` tinyint(1) NOT NULL COMMENT '允许充值',
  `allow_out` tinyint(1) NOT NULL COMMENT '允许提现',
  `allow_suspend` tinyint(1) NOT NULL COMMENT '允许冻结卡片',
  `allow_enable` tinyint(1) NOT NULL COMMENT '允许解冻卡片',
  `allow_frozen` tinyint(1) NOT NULL COMMENT '允许冻结卡片余额',
  `allow_unfrozen` tinyint(1) NOT NULL COMMENT '允许解冻卡片余额',
  `allow_destroy` tinyint(1) NOT NULL COMMENT '允许注销',
  `create_min_amount` decimal(18, 2) NOT NULL COMMENT '首充最低限额',
  `create_max_amount` decimal(18, 2) NOT NULL COMMENT '首充最高限额',
  `recharge_min_amount` decimal(18, 2) NOT NULL COMMENT '充值最低限额',
  `recharge_max_amount` decimal(18, 2) NOT NULL COMMENT '充值最高限额',
  `withdraw_min_amount` decimal(18, 2) NOT NULL COMMENT '提现最低限额',
  `withdraw_max_amount` decimal(18, 2) NOT NULL COMMENT '提现最高限额',
  `physical` tinyint(1) NOT NULL COMMENT '是否实体卡',
  `refuse_times` int NOT NULL COMMENT '销卡拒付次数阈值',
  `refuse_rate` decimal(18, 6) NOT NULL COMMENT '销卡拒付率阈值',
  `currency` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '币种',
  `scenes` json NOT NULL COMMENT '适用平台',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除逻辑: 0-未删 1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop_card_bin_shop_cardbin`(`shop_id` ASC, `card_bin` ASC) USING BTREE,
  INDEX `idx_shop_card_bin_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_shop_card_bin_card_bin`(`card_bin` ASC) USING BTREE,
  INDEX `idx_shop_card_bin_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商户卡头表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for shop_card_fund_detail
-- ----------------------------
DROP TABLE IF EXISTS `shop_card_fund_detail`;
CREATE TABLE `shop_card_fund_detail`  (
  `id` bigint NOT NULL COMMENT '主键 (雪花算法)',
  `shop_id` bigint NOT NULL COMMENT '商户ID',
  `shop_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账单类型',
  `card_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡片ID',
  `card_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '完整卡号',
  `amount` decimal(18, 2) NOT NULL COMMENT '实际从卡内扣除/结算的金额',
  `fee` decimal(18, 2) NOT NULL COMMENT '该笔交易所产生的手续费',
  `currency` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结算币种',
  `order_amount` decimal(18, 2) NOT NULL COMMENT '原始订单金额',
  `order_currency` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始订单币种',
  `detail` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '交易详情描述',
  `mcc` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户行业代码',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '备注信息',
  `merchant_country` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户所属国家代码',
  `related_card_transaction_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联的原始交易流水ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账单状态',
  `transaction_time` datetime NOT NULL COMMENT '交易发生的时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除逻辑: 0-未删 1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_card_fund_detail_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_shop_card_fund_detail_card_id`(`card_id` ASC) USING BTREE,
  INDEX `idx_shop_card_fund_detail_tx_time`(`transaction_time` ASC) USING BTREE,
  INDEX `idx_shop_card_fund_detail_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商户卡片资金明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for shop_fund_detail
-- ----------------------------
DROP TABLE IF EXISTS `shop_fund_detail`;
CREATE TABLE `shop_fund_detail`  (
  `id` bigint NOT NULL COMMENT '主键 (雪花算法)',
  `shop_id` bigint NOT NULL COMMENT '商户ID',
  `shop_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  `type` int NOT NULL COMMENT '操作类型',
  `amount` decimal(18, 2) NOT NULL COMMENT '变动金额, 正数表示增加, 负数表示减少',
  `balance_before` decimal(18, 2) NOT NULL COMMENT '操作前余额',
  `balance_after` decimal(18, 2) NOT NULL COMMENT '操作后余额',
  `biz_no` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联业务单号, 来源单号',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除逻辑: 0-未删 1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_fund_detail_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_shop_fund_detail_biz_no`(`biz_no` ASC) USING BTREE,
  INDEX `idx_shop_fund_detail_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商户资金明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for shop_recharge_record
-- ----------------------------
DROP TABLE IF EXISTS `shop_recharge_record`;
CREATE TABLE `shop_recharge_record`  (
  `id` bigint NOT NULL COMMENT '主键 (雪花算法)',
  `shop_id` bigint NOT NULL COMMENT '商户ID',
  `shop_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  `amount` decimal(18, 2) NOT NULL COMMENT '充值金额',
  `fee_amount` decimal(18, 2) NOT NULL COMMENT '手续费',
  `deposit_amount` decimal(18, 2) NOT NULL COMMENT '实际入账',
  `manner` int NOT NULL COMMENT '充值方式',
  `credential` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '充值凭据',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除逻辑: 0-未删 1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop_recharge_record_manner_credential`(`manner` ASC, `credential` ASC) USING BTREE,
  INDEX `idx_shop_recharge_record_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_shop_recharge_record_credential`(`credential` ASC) USING BTREE,
  INDEX `idx_shop_recharge_record_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商户充值记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for shop_webhook_event
-- ----------------------------
DROP TABLE IF EXISTS `shop_webhook_event`;
CREATE TABLE `shop_webhook_event`  (
  `id` bigint NOT NULL COMMENT '主键 (雪花算法)',
  `shop_id` bigint NOT NULL COMMENT '商户ID',
  `shop_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商户号',
  `event_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件ID(对外幂等标识), 建议全局唯一',
  `event_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件类型',
  `webhook_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '回调地址(事件生成时快照)',
  `payload` json NOT NULL COMMENT '事件载荷(JSON)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '投递状态: 0-待发送 1-发送中 2-发送成功 3-发送失败(待重试) 4-终止/放弃',
  `retry_count` int NULL DEFAULT 0 COMMENT '已重试次数',
  `next_retry_time` datetime NULL DEFAULT NULL COMMENT '下一次重试时间',
  `last_send_time` datetime NULL DEFAULT NULL COMMENT '最后一次发送时间',
  `last_error` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后一次错误信息(简短)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除逻辑: 0-未删 1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop_webhook_event_event_id`(`event_id` ASC) USING BTREE,
  INDEX `idx_shop_webhook_event_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_shop_webhook_event_status_next_retry`(`status` ASC, `next_retry_time` ASC) USING BTREE,
  INDEX `idx_shop_webhook_event_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商户回调事件表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
