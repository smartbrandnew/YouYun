# 注
本工程只是为了适用开发者模式，为了脱离租户依旧能通过默认账户引用

# 集成步骤
修改Spring配置,添加proxy部分的配置

<import resource="classpath:uyun/bat/common/proxy/spring-dubbo.xml" />

代码片段

if (userService == null)
			userService = ProxyFactory.createProxy(UserService.class);

# 內置用戶信息
tenantId=admin
userId=admin
token=admin
user.realName=超级管理员
			
# 开发者模式
1.修改conf\config.properties
bat.developr.mode=true
2.修改nginx\conf\nginx.conf
将底下3行去除注释
#若是开发者模式，请将以下3行注释去掉
#add_header Set-Cookie 'token=admin';
#add_header Set-Cookie 'userId=admin';
#add_header Set-Cookie 'tenantId=admin';

3.将agent的apikey设置为admin与内置租户id保持一致

4.由于前端界面的首页头上部分是共用一个组件，所以其会请求租户获取产品列表，此时产生401依旧会跳转租户登陆界面
修改方案
web\build\chunk.1.js
case 401       修改为其他不会出现的状态码就好
