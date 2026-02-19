# Flyway 开发指南

## 简介

Flyway 是一个数据库迁移工具，能够帮助开发团队管理数据库的结构变更。它通过版本控制的 SQL 脚本来执行数据库模式更新，确保不同开发环境、测试环境和生产环境中的数据库结构一致。

在本项目中，我们使用 Flyway 来管理数据库模式和开发测试数据的迁移脚本。

使用Flyway管理数据库结构变更，当想要改变数据库结构时，只需在存放脚本的目录中编写变更对应的 SQL 脚本，当项目启动时，该变更会自动执行。

## 目录结构

建立了`emotion-hub-migration`模块专门进行数据库迁移。

迁移脚本存储在以下目录中：

- `resource/db/migration/`：存放数据库结构的迁移脚本，文件编号从 `V1` 开始。
- `resource/db/test/`：存放用于开发和测试的测试数据迁移脚本，文件编号从 `V4001` 开始。

每个迁移脚本使用版本号进行命名，确保按顺序执行。

## 命名规则

Flyway 迁移脚本必须遵循以下命名格式：

```
V<版本号>__<描述>.sql
```
`<版本号>`：是一个递增的数字，用来标识脚本的执行顺序。

在 `migration/` 目录下，文件编号从 `V1` 开始。

在 `test/` 目录下，文件编号从 `V4001` 开始。

`<描述>`：是对迁移内容的简短描述。描述应简洁明了，以便开发人员理解迁移的功能。

例如：

`V1__initial_schema.sql`：初始化数据库的架构。

`V4001__insert_test_data.sql`：插入测试数据。

## 编写迁移脚本

**数据库模式迁移**：当需要更改数据库的表结构或添加新的数据库对象（例如表、索引、视图等）时，应创建一个迁移脚本来描述这些变更。

**测试数据迁移**：当需要插入开发或测试数据时，可以创建单独的迁移脚本，存放在 `db/test/` 目录下。

**示例：创建一个新表**

假设我们要在数据库中添加一个 `users` 表，我们可以编写以下迁移脚本：

`V2__create_users_table.sql`:

```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
**示例：插入开发数据**

如果需要插入测试数据，可以创建一个如下的迁移脚本：

`V4001__insert_test_data.sql`:

``` sql
INSERT INTO users (username, email) VALUES
('testuser', 'testuser@example.com'),
('devuser', 'devuser@example.com');
```
## 执行迁移

### 使用 migrate 命令

Flyway 会检查哪些迁移脚本尚未执行，并按版本顺序执行这些脚本。在命令行中，运行以下命令来应用所有迁移：
``` shell
flyway migrate
```
本项目中已经配置了项目运行时自动迁移，不需要在项目运行前手动运行migrate命令。
### 使用 clean 命令

在开发过程中，clean 命令可以帮助你重置数据库，删除所有现有的数据库对象并重新应用所有迁移脚本。使用此命令时，请确保不会丢失生产数据！

```shell
flyway clean
```
>[!WARNING]
>
>仅在开发环境中使用 clean，避免在生产环境中使用此命令。

## 回滚迁移

Flyway 不支持直接回滚迁移脚本。如果需要撤销某些变更，你必须手动编写新的迁移脚本来执行撤销操作。例如，如果你删除了某个表，你需要创建一个新的脚本来重新创建该表。

**示例：删除 `users` 表的迁移脚本**

假设你需要删除 `users` 表，可以创建一个新的迁移脚本：

`V3__drop_users_table.sql`:

``` sql
DROP TABLE IF EXISTS users;
```
>[!IMPORTANT]
>
>在开发过程中，如果改变已执行过的脚本，之后运行项目会导致Flyway报错。如果确实需要改变之前已经执行过的脚本内容，请在修改过后执行clean指令，再运行项目。**并且将修改告知团队其他成员**。

## 迁移脚本的管理

**版本控制**：所有迁移脚本应该提交到版本控制系统中，这样团队成员就可以共享数据库变更。

**按小步前进**：将数据库更改拆分成小的、单独的迁移脚本，以便在出现问题时更容易回溯。

## 环境变量配置

为了增强安全性，项目中的数据库用户名和密码不应写死在配置文件中。可以使用环境变量来管理这些敏感信息。例如，在 `application-dev.yml` 中，数据库用户名和密码的配置方式如下：
``` yml
spring:
    datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/emotion_hub?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    # 注意：本地开发需要在环境变量中配置以下值：
    #   EMOTIONHUB_DB_USERNAME_DEV - 数据库用户名
    #   EMOTIONHUB_DB_PASSWORD_DEV - 数据库密码
    username: ${EMOTIONHUB_DB_USERNAME_DEV}
    password: ${EMOTIONHUB_DB_PASSWORD_DEV}
```

在开发过程中可以在本地添加`EMOTIONHUB_DB_USERNAME_DEV`和`EMOTIONHUB_DB_PASSWORD_DEV`环境变量来管理数据库用户名和密码。

## 执行顺序

Flyway 执行迁移脚本时，会根据 **版本号的顺序** 来决定执行的顺序，而 **不考虑脚本所在的目录**。也就是说，Flyway 会统一按版本号排序，执行所有迁移脚本，而不管这些脚本是存放在 `db/migration/` 目录还是 `db/test/` 目录。

因此，`db/test`目录下的脚本编号从`V4001`开始，`db/migration`目录下的脚本编号从`V1`开始，以保证先建立数据库结构，后插入测试数据。

## 最佳实践

**迁移脚本的命名规范**：始终遵循命名规则，以便其他开发人员能快速理解脚本的功能和执行顺序。

**小步推进**：每次变更都应该对应一个独立的迁移脚本，避免在同一脚本中包含多个不相关的变更。

**测试数据与生产数据分离**：测试数据迁移脚本应与生产环境的迁移脚本分开，以确保开发过程中不会影响生产数据。