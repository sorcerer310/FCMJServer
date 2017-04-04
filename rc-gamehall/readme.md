注意：
1.此项目中设计到异步http连接gm，部署的时候工程引入的两个包（httpclient-4.5.2.jar，httpcore-4.4.4.jar）与大厅sfs下的lib下的两个包（httpclient-4.0.3.jar，httpcore-4.0.1.jar）冲突，需要用本工程的替换掉，把sfs下的lib下的删除
2.在deploy.bat中添加 ：copy %RAFO_HOME%\rc-common\build\libs\*.jar %SFS_HOME%\extensions\rafo
                   copy %RAFO_HOME%\rc-gamehall\libs\*.jar %SFS_HOME%\extensions\rafo
                   3.数据库（添加日期2016/10/10）
 3。数据库
 a:如果之前已经创建了login数据库,就只需要执行tbl.server.sql,并且手动添加httpPort列对应的值（默认是8080）
 b。如果没有创建login数据库，就只需要执行db。sql
