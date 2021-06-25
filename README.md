# USRA2021

## Database Q&A

### 1. Connect to local database
- Input `/usr/local/MySQL/bin/mysql -u root -p` in the Terminal, where `root` is the username of database.
- Input password of the database.

### 2. Connect to remote database
- Suppose the IP of remote host is `23.106.134.88`, username is `root`.
- Input `mysql -h 23.106.134.88 -u root -p 123` in the Terminal.
- Input password of the database.

### 3. Change database password
- Suppose the database name is `localhost`，username is `root`.
- Input `set password for root@localhost = password('123')` to set the database password to `123`.

### 4. Create a schema
- Just create a new schema through IntelliJ.
- Input `use <database name>` in the Terminal to start the database.
  ```
  mysql> use proj_cov19
  Database changed
  ```

### 5. Import a file
- The `secure_file_priv` parameter is to constrain `LOAD DATA`, `SELECT ... OUTFILE`, and `LOAD_FILE()`.

	- If `secure_file_priv` is valued null, it means disallow `mysqld` to perform import/export.
	- If `secure_file_priv` is valued /tmp/, it means `mysqld` can only import/export files to /tmp/.
	- If `secure_file_priv` is valued empty, it means allow `mysqld` to perform import/export without limits.<br><br>

- Solution (Mac/Linux): In `[mysqld]` of `my.cnf`, add `secure_file_priv=''`.
	- Note: absolute path is required to import a file.
  ```
  mysql> show variables like 'secure%';
  +------------------+-------+
  | Variable_name    | Value |
  +------------------+-------+
  | secure_file_priv |       |
  +------------------+-------+
  1 row in set (0.00 sec)
  ```

- Location of `my.cnf` usually is at `/etc`.

	- Input `sudo fs_usage | grep my.cnf` in the Terminal, the Terminal will search the location of `my.cnf`.
	- If there is no `my.cnf` in the path `/usr/local/etc`, then input `sudo nano /etc/my.cnf` to create one.
	- Sample file `mysql-log-rotate` locates at `/usr/local/mysql/support-files/`.

### 6. Shutdown database
- Input `quit` in the Terminal while the connection is on.
  ```
  mysql> quit
  Bye
  ```
### 7. NOTES
- To connect to MySQL, must set a valid value for `serverTimezone` (i.e. `CST`).
- The directories `db_inputs` and `db_outputs` must be created at the local drive in order to have complete authorization for MySQL to visit.

