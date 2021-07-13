# 初始化原始表
drop table if exists 2021sr311;
create table if not exists 2021sr311 (
    DATE varchar(99), AREA varchar(99), REQ varchar(99),
    WARD varchar(99), NBHD varchar(99), LAT varchar(99), LNG varchar(99)
);

#

# 从原始表中提取列, 生成新表
drop table if exists 2021sr311_m;
create table if not exists 2021sr311_m (
    select
        substr(DATE, 9, 2) as YEAR,
        substr(DATE, 1, 2) as MONTH,
        substr(DATE, 4, 2) as DAY,
        substring_index(DATE, ' ', -1) as AMPM,
        substr(substring_index(DATE, ' ', -2), 1, 2) as TIME,
        AREA, REQ, WARD, NBHD, LAT, LNG
    from 2021sr311
);

# 添加ID作为首列
alter table 2021sr311_m add ID integer not null primary key auto_increment first;

# 将TIME列变为24小时制, 并删除多余列
update 2021sr311_m set TIME = TIME + '12' where AMPM = 'PM' and TIME != '12';
alter table 2021sr311_m drop column AMPM;

# 去掉经度列和纬度列的标点
update 2021sr311 set LAT = replace (LAT, '"(', '');
update 2021sr311 set LNG = replace (LNG, ')"', '');

# 将列值转换成integer
alter table 2021sr311_m modify YEAR integer;
alter table 2021sr311_m modify MONTH integer;
alter table 2021sr311_m modify DAY integer;
alter table 2021sr311_m modify TIME integer;

# 将经度和纬度列值转换成double
alter table 2021sr311_m modify LAT double;
alter table 2021sr311_m modify LNG double;