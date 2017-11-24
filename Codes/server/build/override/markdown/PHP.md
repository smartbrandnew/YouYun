# 配置PHP监控
1.  需要安装php环境，Linux可用yum　install php，windows可用安装包安装
2.  从git仓库上面将类库拷贝下来

     `git clone git@github.com:DataDog/php-datadogstatsd.git`



3. 在libraries/datadogstatsd.php下最后一行添加
  
    `DataDogStatsD::increment('your.data.point');`


4. 运行程序

    `php datadogstatsd.php`
    
5. 转到指标监测页面就能看到agent开始工作了
