# USRA2021

## 数据库连接问题

1. 连接本地的数据库并进入命令行：
- 在终端中输入`/usr/local/MySQL/bin/mysql -u root -p`, 其中`root`为数据库用户名。

2. 连接远程数据库并进入命令行：
- 假设远程主机的IP为`23.106.134.88`，用户名为`root`,密码为`123`。
- 在终端中输入`mysql -h 23.106.134.88 -u root -p 123`。

3. 修改数据库密码：
- 假设数据库名字为`localhost`，用户名为`root`。
- 在终端中输入`set password for root@localhost = password('123')`即将数据库密码设定为`123`。

4. 检查是否开启加载本地文件：
- 在终端中输入`show variables like 'local_infile';`来查看状态。

	```
	mysql> show variables like 'local_infile';
	+---------------+-------+
	| Variable_name | Value |
	+---------------+-------+
	| local_infile  | OFF   |
	+---------------+-------+
	1 row in set (0.00 sec)
	```
- 在终端中输入`set global local_infile = on;`开启全局本地文件设置。
	```
	mysql> set global local_infile = on;
	Query OK, 0 rows affected (0.00 sec)
	```
5. 导入文件：
	```
	mysql> use cov19_update_0621;
	Database changed
	mysql> LOAD DATA LOCAL INFILE 'test.csv' INTO TABLE COVID19;
	ERROR 2 (HY000): File 'test.csv' not found (OS errno 2 - No such file or directory)
	```


99. 断开数据库：
- 在终端中输入`quit`即可。

	```
	mysql> quit
	Bye
	```