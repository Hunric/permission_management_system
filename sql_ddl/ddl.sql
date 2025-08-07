-- 创建数据库 user_db_0
CREATE DATABASE IF NOT EXISTS user_db_0 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE user_db_0;

-- 分表 1
CREATE TABLE users_0 (
                         `user_id` BIGINT NOT NULL COMMENT '用户ID (雪花算法生成)',
                         `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                         `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                         `email` VARCHAR(100) NULL COMMENT '邮箱',
                         `phone` VARCHAR(20) NULL COMMENT '手机号',
                         `gmt_create` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `gmt_modified` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                         PRIMARY KEY (`user_id`),
                         UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表分片0';

-- 分表 2
CREATE TABLE users_1 (
                         `user_id` BIGINT NOT NULL COMMENT '用户ID (雪花算法生成)',
                         `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                         `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                         `email` VARCHAR(100) NULL COMMENT '邮箱',
                         `phone` VARCHAR(20) NULL COMMENT '手机号',
                         `gmt_create` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `gmt_modified` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                         PRIMARY KEY (`user_id`),
                         UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表分片1';

-- Seata AT 模式 undo_log 表 (适用于 Seata 1.5.x 版本)
CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id` BIGINT NOT NULL COMMENT 'branch transaction id',
    `xid` VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context` VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB NOT NULL COMMENT 'rollback info',
    `log_status` INT NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created` DATETIME(6) NOT NULL COMMENT 'create datetime',
    `log_modified` DATETIME(6) NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';


-- ----------------------------------------------------------------------------------------------
--  divide
-- ----------------------------------------------------------------------------------------------


-- 创建数据库 user_db_1
CREATE DATABASE IF NOT EXISTS user_db_1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE user_db_1;

-- 分表 1
CREATE TABLE users_0 (
                         `user_id` BIGINT NOT NULL COMMENT '用户ID (雪花算法生成)',
                         `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                         `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                         `email` VARCHAR(100) NULL COMMENT '邮箱',
                         `phone` VARCHAR(20) NULL COMMENT '手机号',
                         `gmt_create` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `gmt_modified` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                         PRIMARY KEY (`user_id`),
                         UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表分片0';

-- 分表 2
CREATE TABLE users_1 (
                         `user_id` BIGINT NOT NULL COMMENT '用户ID (雪花算法生成)',
                         `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                         `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                         `email` VARCHAR(100) NULL COMMENT '邮箱',
                         `phone` VARCHAR(20) NULL COMMENT '手机号',
                         `gmt_create` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `gmt_modified` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                         PRIMARY KEY (`user_id`),
                         UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表分片1';

-- Seata AT 模式 undo_log 表 (适用于 Seata 1.5.x 版本)
CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id` BIGINT NOT NULL COMMENT 'branch transaction id',
    `xid` VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context` VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB NOT NULL COMMENT 'rollback info',
    `log_status` INT NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created` DATETIME(6) NOT NULL COMMENT 'create datetime',
    `log_modified` DATETIME(6) NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';


-- ----------------------------------------------------------------------------------------------
--  divide
-- ----------------------------------------------------------------------------------------------


-- 创建数据库 permission_db
CREATE DATABASE IF NOT EXISTS permission_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE permission_db;

-- 1. 角色表 (roles)
CREATE TABLE roles (
                       `role_id` INT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                       `role_code` VARCHAR(20) NOT NULL COMMENT '角色代码 (程序中使用)',
                       `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称 (用于显示)',
                       `gmt_create` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       PRIMARY KEY (`role_id`),
                       UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色信息表';

-- 2. 用户-角色关系表 (user_roles)
CREATE TABLE user_roles (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                            `user_id` BIGINT NOT NULL COMMENT '用户ID (关联users表的user_id)',
                            `role_id` INT NOT NULL COMMENT '角色ID (关联roles表的role_id)',
                            `gmt_create` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_user_id` (`user_id`),
                            KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

-- 初始化数据
INSERT INTO `roles` (`role_id`, `role_code`, `role_name`) VALUES
                                                              (1, 'super_admin', '超级管理员'),
                                                              (2, 'admin', '管理员'),
                                                              (3, 'user', '普通用户');

-- 初始化一个超级管理员，设其 user_id 为 1
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES (1, 1);

-- Seata AT 模式 undo_log 表 (适用于 Seata 1.5.x 版本)
CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id` BIGINT NOT NULL COMMENT 'branch transaction id',
    `xid` VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context` VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB NOT NULL COMMENT 'rollback info',
    `log_status` INT NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created` DATETIME(6) NOT NULL COMMENT 'create datetime',
    `log_modified` DATETIME(6) NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';


-- ----------------------------------------------------------------------------------------------
--  divide
-- ----------------------------------------------------------------------------------------------


-- 创建数据库 logging_db
CREATE DATABASE IF NOT EXISTS logging_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE logging_db;

CREATE TABLE operation_logs (
                                `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
                                `user_id` BIGINT NULL COMMENT '操作者用户ID',
                                `trace_id` VARCHAR(50) NULL COMMENT '分布式链路追踪ID',
                                `action` VARCHAR(50) NOT NULL COMMENT '操作类型 (如 REGISTER, UPDATE_USER)',
                                `ip` VARCHAR(45) NULL COMMENT '操作者IP地址',
                                `detail` TEXT NULL COMMENT '操作详情 (JSON格式, 记录修改内容等)',
                                `gmt_create` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                PRIMARY KEY (`log_id`),
                                KEY `idx_user_id_action` (`user_id`, `action`),
                                KEY `idx_gmt_create` (`gmt_create`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- Seata AT 模式 undo_log 表 (适用于 Seata 1.5.x 版本)
CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id` BIGINT NOT NULL COMMENT 'branch transaction id',
    `xid` VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context` VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB NOT NULL COMMENT 'rollback info',
    `log_status` INT NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created` DATETIME(6) NOT NULL COMMENT 'create datetime',
    `log_modified` DATETIME(6) NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';