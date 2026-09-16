# 中央厨房 · 备餐批次与出餐配送系统

团餐中央厨房的作业台账：**操作间与设备**、**菜品与备餐批次**、**操作间排期**、**出餐配送**。

## 技术栈

Spring Boot 3.3（Java 17）+ MySQL 8.0 + Redis 7 + Vue 3 + Element Plus + Vite + nginx，全栈 `docker compose` 一键启动。

## 启动

```bash
./start.sh              # 等价于 docker compose up -d --build
```

| 入口 | 地址 |
| --- | --- |
| 前端页面 | http://127.0.0.1:8206/ |
| 后端接口 | http://127.0.0.1:8306/api/ |
| MySQL | 127.0.0.1:3506（库 `central_kitchen`） |
| Redis | 127.0.0.1:6506 |

## 停止

```bash
docker compose down       # 保留数据卷
docker compose down -v    # 连数据卷一起删，下次启动重新灌种子数据
```

容器名统一是 `claude-qd-006-{mysql,redis,backend,frontend}`。

## 业务模块

### 1. 操作间与设备台账（`kitchen` / `equipment`）

操作间编号 `K-xx` 唯一，类型分粗加工 / 热厨 / 凉菜 / 面点 / 洗消，状态 `在用 / 停用 / 维修`；
当天还有没结束的排期时不许停用或报修。
设备编号 `EQ-xxxx` 唯一，归属到某个操作间（也可以先不归），状态 `可用 / 停用 / 维修中`；
所在操作间当天有没结束的排期时，不许改状态。

- 页面：操作间与设备（`/kitchens`）
- 接口：`GET/POST /api/kitchens`、`PUT /api/kitchens/{id}`、`GET/POST /api/equipments`、`PUT /api/equipments/{id}`

### 2. 菜品与备餐批次（`dish` / `meal_batch`）

菜品编号 `D-xxxx` 唯一，类别荤菜 / 素菜 / 汤 / 主食，状态 `在售 / 停用`；停用菜品不能开新批次。
批次号 `BC-xxxx` 自动生成。开一批时校验：菜品在售、操作间在用、**同一道菜同一供应日期同一餐次只能排一批**、
**每批必须留样且不少于 125 克**、必须写清楚是谁做的。
状态机 `备料中 → 加工中 → 已完成`；`备料中 / 加工中` 都能作废；**完工必须登记实际做出来的份数**。

- 页面：菜品与备餐批次（`/batches`）
- 接口：`GET/POST /api/dishes`、`PUT /api/dishes/{id}`、`GET/POST /api/batches`、`POST /api/batches/{id}/advance?action=&actualPortions=`

### 3. 操作间排期（`cooking_slot`）

排期号 `SL-xxxx` 自动生成。一条排期 = 某个操作间 + 某天 + 一个餐次 + 一个班组 + 一段时段。
排期时校验：操作间必须在用、结束时间晚于开始时间、**操作间里不能有非可用设备**、
**同一操作间同日同时段不重叠**、**同一班组同日同时段也不能重叠**。
状态机 `待开始 → 进行中 → 已完成`，前两态可取消。

- 页面：操作间排期（`/slots`）
- 接口：`GET/POST /api/slots`、`POST /api/slots/{id}/advance?action=`

### 4. 出餐配送（`delivery`）

配送单号 `PS-xxxx` 自动生成，挂在一个**已完工**的批次上。一批餐累计发出去的份数（**退回的不算**）
不能超过实际做出来的份数。状态机 `待发 → 在途 → 已签收`，在途未签收的可以退回；发车必须写司机。

- 页面：出餐配送（`/deliveries`）
- 接口：`GET/POST /api/deliveries`、`POST /api/deliveries/{id}/advance?action=&driver=&deliverDate=`

## 目录

```
backend/src/main/java/com/kitchen/central/
├── config/       CORS 配置
├── controller/   REST 入口
├── dto/          BizException + 统一错误响应
├── entity/       6 张业务表
├── repository/   Spring Data JPA
└── service/      业务规则（编号唯一、时段占用、留样与批次唯一、份数核减）
backend/src/main/resources/schema.sql   建表 + 种子数据（挂进 MySQL initdb）
frontend/src/views/                     4 个业务页面
```
