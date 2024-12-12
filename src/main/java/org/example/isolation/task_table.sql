-- test.test_task definition

CREATE TABLE `test_task`
(
    `id`           int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id`      char(32)     NOT NULL COMMENT '业务主键',
    `task_name`    varchar(100) NOT NULL COMMENT '任务名称',
    `task_status`  tinyint      NOT NULL DEFAULT '0' COMMENT '任务状态',
    `process_id`   int                   DEFAULT NULL COMMENT '所属流程',
    `task_deleted` tinyint(1)   NOT NULL COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `test_task_unique` (`task_id`),
    KEY `process_id_IDX` (`process_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 12
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;