-- 中央厨房：建表 + 种子数据
-- 表结构由 Hibernate 兜底（ddl-auto=update），这里只保证首次启动就有数据

CREATE TABLE IF NOT EXISTS kitchen (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  kind VARCHAR(16) NOT NULL DEFAULT '热厨',
  status VARCHAR(16) NOT NULL DEFAULT '在用',
  PRIMARY KEY (id),
  UNIQUE KEY uk_kitchen_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS equipment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  category VARCHAR(16) NOT NULL DEFAULT '灶具',
  kitchen_id BIGINT NULL,
  status VARCHAR(16) NOT NULL DEFAULT '可用',
  PRIMARY KEY (id),
  UNIQUE KEY uk_equipment_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dish (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  category VARCHAR(16) NOT NULL DEFAULT '荤菜',
  status VARCHAR(16) NOT NULL DEFAULT '在售',
  PRIMARY KEY (id),
  UNIQUE KEY uk_dish_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS meal_batch (
  id BIGINT NOT NULL AUTO_INCREMENT,
  batch_no VARCHAR(32) NOT NULL,
  dish_id BIGINT NOT NULL,
  kitchen_id BIGINT NOT NULL,
  serve_date DATE NOT NULL,
  meal VARCHAR(16) NOT NULL,
  plan_portions INT NOT NULL DEFAULT 0,
  actual_portions INT NOT NULL DEFAULT 0,
  chef VARCHAR(32) NOT NULL,
  sample_weight INT NULL,
  status VARCHAR(16) NOT NULL DEFAULT '备料中',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_batch_no (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cooking_slot (
  id BIGINT NOT NULL AUTO_INCREMENT,
  slot_no VARCHAR(32) NOT NULL,
  kitchen_id BIGINT NOT NULL,
  serve_date DATE NOT NULL,
  meal VARCHAR(16) NOT NULL,
  team VARCHAR(32) NOT NULL,
  start_min INT NOT NULL,
  end_min INT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT '待开始',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_slot_no (slot_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS delivery (
  id BIGINT NOT NULL AUTO_INCREMENT,
  delivery_no VARCHAR(32) NOT NULL,
  batch_id BIGINT NOT NULL,
  customer VARCHAR(64) NOT NULL,
  portions INT NOT NULL DEFAULT 0,
  driver VARCHAR(32) NULL,
  deliver_date DATE NULL,
  status VARCHAR(16) NOT NULL DEFAULT '待发',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_delivery_no (delivery_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ingredient (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  expiry_date DATE NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ingredient_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dish_ingredient (
  id BIGINT NOT NULL AUTO_INCREMENT,
  dish_id BIGINT NOT NULL,
  ingredient_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  PRIMARY KEY (id),
  KEY idx_dish_ingredient_dish (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO kitchen (id, code, name, kind, status) VALUES
  (1, 'K-01', '热厨一区', '热厨', '在用'),
  (2, 'K-02', '热厨二区', '热厨', '在用'),
  (3, 'K-03', '凉菜间',   '凉菜', '在用'),
  (4, 'K-04', '面点间',   '面点', '在用'),
  (5, 'K-05', '洗消间',   '洗消', '维修');

INSERT IGNORE INTO equipment (id, code, name, category, kitchen_id, status) VALUES
  (1, 'EQ-2001', '双眼大灶', '灶具',   1,    '可用'),
  (2, 'EQ-2002', '蒸箱',     '蒸箱',   1,    '可用'),
  (3, 'EQ-2003', '双眼大灶', '灶具',   2,    '可用'),
  (4, 'EQ-2004', '和面机',   '和面机', 4,    '可用'),
  (5, 'EQ-2005', '四门冷柜', '冷库',   3,    '维修中'),
  (6, 'EQ-2006', '消毒柜',   '消毒柜', NULL, '可用');

INSERT IGNORE INTO dish (id, code, name, category, status) VALUES
  (1, 'D-1001', '红烧肉',       '荤菜', '在售'),
  (2, 'D-1002', '清炒时蔬',     '素菜', '在售'),
  (3, 'D-1003', '紫菜蛋花汤',   '汤',   '在售'),
  (4, 'D-1004', '米饭',         '主食', '在售'),
  (5, 'D-1005', '旧菜单凉拌菜', '素菜', '停用');

INSERT IGNORE INTO meal_batch (id, batch_no, dish_id, kitchen_id, serve_date, meal, plan_portions, actual_portions, chef, sample_weight, status, created_at, updated_at) VALUES
  (1, 'BC-0001', 1, 1, CURDATE(), '午餐', 300, 0,   '张师傅', 150, '备料中', NOW(), NOW()),
  (2, 'BC-0002', 4, 2, CURDATE(), '午餐', 400, 0,   '刘师傅', 200, '加工中', NOW(), NOW()),
  (3, 'BC-0003', 2, 2, CURDATE(), '午餐', 350, 340, '刘师傅', 180, '已完成', NOW(), NOW()),
  (4, 'BC-0004', 3, 3, CURDATE(), '午餐', 300, 295, '王师傅', 130, '已完成', NOW(), NOW()),
  (5, 'BC-0005', 5, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '晚餐', 200, 0,  '张师傅', 0,   '已作废', NOW(), NOW()),
  (6, 'BC-0006', 1, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '午餐', 280, 270, '张师傅', 140, '已完成', NOW(), NOW());

INSERT IGNORE INTO cooking_slot (id, slot_no, kitchen_id, serve_date, meal, team, start_min, end_min, status, created_at, updated_at) VALUES
  (1, 'SL-0001', 1, CURDATE(), '午餐', '甲班', 420, 540, '进行中', NOW(), NOW()),
  (2, 'SL-0002', 2, CURDATE(), '午餐', '乙班', 420, 540, '待开始', NOW(), NOW()),
  (3, 'SL-0003', 3, CURDATE(), '午餐', '丙班', 600, 700, '待开始', NOW(), NOW()),
  (4, 'SL-0004', 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '午餐', '丁班', 420, 540, '已完成', NOW(), NOW()),
  (5, 'SL-0005', 5, CURDATE(), '午餐', '戊班', 600, 700, '已取消', NOW(), NOW());

INSERT IGNORE INTO delivery (id, delivery_no, batch_id, customer, portions, driver, deliver_date, status, created_at, updated_at) VALUES
  (1, 'PS-0001', 6, '实验中学',   200, '李司机', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '已签收', NOW(), NOW()),
  (2, 'PS-0002', 3, '城东小学',   200, '李司机', CURDATE(), '在途', NOW(), NOW()),
  (3, 'PS-0003', 3, '城西小学',   100, '王司机', NULL, '待发', NOW(), NOW()),
  (4, 'PS-0004', 4, '实验中学',   200, '李司机', CURDATE(), '已退回', NOW(), NOW()),
  (5, 'PS-0005', 4, '城北幼儿园', 50,  '王司机', NULL, '待发', NOW(), NOW());

-- 原料台账：名称、当前库存（克）、保质期
INSERT IGNORE INTO ingredient (id, name, stock, expiry_date) VALUES
  (1, '五花肉',   200000, DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
  (2, '时令蔬菜', 150000, DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
  (3, '鸡蛋',     100000, DATE_ADD(CURDATE(), INTERVAL 10 DAY)),
  (4, '紫菜',     20000,  DATE_ADD(CURDATE(), INTERVAL 90 DAY)),
  (5, '大米',     500000, DATE_ADD(CURDATE(), INTERVAL 180 DAY)),
  (6, '食用油',   100000, DATE_ADD(CURDATE(), INTERVAL 180 DAY)),
  (7, '食盐',     50000,  DATE_ADD(CURDATE(), INTERVAL 360 DAY));

-- 菜品配方：每份用量（克）
INSERT IGNORE INTO dish_ingredient (id, dish_id, ingredient_id, quantity) VALUES
  (1, 1, 1, 150),   -- 红烧肉：五花肉
  (2, 1, 6, 10),    -- 红烧肉：食用油
  (3, 1, 7, 2),     -- 红烧肉：食盐
  (4, 2, 2, 120),   -- 清炒时蔬：时令蔬菜
  (5, 2, 6, 8),     -- 清炒时蔬：食用油
  (6, 2, 7, 2),     -- 清炒时蔬：食盐
  (7, 3, 4, 5),     -- 紫菜蛋花汤：紫菜
  (8, 3, 3, 30),    -- 紫菜蛋花汤：鸡蛋
  (9, 3, 7, 1),     -- 紫菜蛋花汤：食盐
  (10, 4, 5, 100);  -- 米饭：大米
