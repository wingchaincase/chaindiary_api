# Chain diary api

 * [中文](#中文)
    * [部署说明](#部署说明)
        * [环境要求](#环境要求)
        * [数据库](#数据库)
        * [配置说明](#配置说明)
    * [密码学说明](#密码学说明)
 * [English](#English)
    * [Deploying instruction](#Deploying-instruction)
        * [Requirements](#Requirements)
        * [Database](#Database)
        * [Configuation](#Configuation)
    * [Cryptography instruction](#Cryptography-instruction)
 
 
## 中文

链记API

### 部署说明

#### 环境要求
Linux or MacOS  
Java 1.8  
MySQL 5.5以上  
阿里云 OSS  
微信小程序  

#### 数据库
执行下面三个文件中最后注释里的SQL创建数据表：
```
src/main/java/wingchaincase/chaindiaryapi/domain/Note.java
src/main/java/wingchaincase/chaindiaryapi/domain/NoteAttachment.java
src/main/java/wingchaincase/chaindiaryapi/domain/NoteSeed.java
```

#### 配置说明
src/main/resources/application.properties

根据实际配置修改下列配置项：
```
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
spring.redis.host=
spring.redis.port=
spring.redis.database=
note.oss.end-point=
note.oss.end-point.internal=
note.oss.access-key-id=
note.oss.access-key-secret=
note.storage.bucket=
note.storage.key-base=
note.ma.note.appid=
note.ma.note.secret=
```

### 密码学说明

[jec](./src/rust/jec)  

[jblackhole](./src/rust/jblackhole)  

## English

Chain diary API

### Deploying instruction

#### Requirements
Linux or MacOS  
Java 1.8  
MySQL 5.5+  
Alibaba cloud OSS  
Wechat mini program  

#### Database
Execute the sqls in the following files:
```
src/main/java/wingchaincase/chaindiaryapi/domain/Note.java
src/main/java/wingchaincase/chaindiaryapi/domain/NoteAttachment.java
src/main/java/wingchaincase/chaindiaryapi/domain/NoteSeed.java
```

#### Configuation
src/main/resources/application.properties

Modify the following item according to actual condition:
```
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
spring.redis.host=
spring.redis.port=
spring.redis.database=
note.oss.end-point=
note.oss.end-point.internal=
note.oss.access-key-id=
note.oss.access-key-secret=
note.storage.bucket=
note.storage.key-base=
note.ma.note.appid=
note.ma.note.secret=
```

### Cryptography instruction

[jec](./src/rust/jec)  

[jblackhole](./src/rust/jblackhole)  
