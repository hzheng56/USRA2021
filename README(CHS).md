# USRA2021

## 数据库相关问题

### 1. 连接本地的数据库并进入命令行：
- 在终端中输入`/usr/local/MySQL/bin/mysql -u root -p`, 其中`root`为数据库用户名。

### 2. 连接远程数据库并进入命令行：
- 假设远程主机的IP为`23.106.134.88`，用户名为`root`,密码为`123`。
- 在终端中输入`mysql -h 23.106.134.88 -u root -p 123`。
- 查看本机IP，需要在终端输入`ifconfig en0`。
  ```
  inet 23.106.134.88 netmask 0xffffff00 broadcast 192.168.0.255
  ```

### 3. 修改数据库密码：
- 假设数据库名字为`localhost`，用户名为`root`。
- 在终端中输入`set password for root@localhost = password('123')`即将数据库密码设定为`123`。

### 4. 选择schema：
- 存在多个schema时，在终端中输入`use <schema name>`来选择要使用的数据库。
  ```
  mysql> use proj_cov19
  
  Database changed
  ```

### 5. 导入文件：
- MySQL的`secure_file_priv`参数是用来限制`LOAD DATA`, `SELECT ... OUTFILE`, 以及 `LOAD_FILE()`传到哪个指定目录的。

    - 若`secure_file_priv`的值为null，表示限制`mysqld`不允许导入或导出。
    - `secure_file_priv`的值为/tmp/，表示限制`mysqld`的导入或导出只能发生在/tmp/目录下。
    - `secure_file_priv`的值为空，表示不对`mysqld`的导入或导出做限制。<br><br>

- 解决方案 (Mac/Linux): 在`my.cnf`文件里的`[mysqld]`内加入`secure_file_priv=''`即可。
    - 注意：在代码中只接受导入/导出文件的绝对路径。
  ```
  mysql> show variables like 'secure%';
  +------------------+-------+
  | Variable_name    | Value |
  +------------------+-------+
  | secure_file_priv |       |
  +------------------+-------+
  1 row in set (0.00 sec)
  ```

- 关于配置文件`my.cnf`的位置: 通常位于`/etc`.

    - 打开终端输入`sudo fs_usage | grep my.cnf`，终端会进行全盘搜索。
    - 如果路径`/usr/local/etc`中不存在此文件，则需要输入`sudo nano /etc/my.cnf`创建一个。
    - 格式样板`mysql-log-rotate`位于`/usr/local/mysql/support-files/`。

### 6. 断开数据库：
- 在终端中输入`quit`即可。
  ```
  mysql> quit
  Bye
  ```
### 7. 注意事项：
- 设置`serverTimezone`的属性为`CST`.
- 两个文件夹`db_inputs`和`db_outputs`必须在数据库所在硬盘上创建，从而保证MySQL有权限进行访问。

