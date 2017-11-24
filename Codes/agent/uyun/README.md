自定义安装目录

1.安装格式
之前linux环境的monitor agent安装是按照命令行一键安装的，
安装时用户需要安装自己的需求填写
HOSTNAME;  API_KEY;  REPERTORY_URL等参数
现在需要添加一个安装目录的参数 DIR，参数格式为，例：DIR="/home/monitor"，则agent会安装在/home/monitor目录下。

2.参数说明
现在用户需要添加一个安装目录的参数 DIR，参数格式为，例：DIR="/home/monitor"，则agent会安装在/home/monitor目录下。
注1：路径格式如上所述，最后不要加斜杠 / ;
注2：如果仍然要安装在/opt目录下，DIR="/opt"

3.安装命令
HOSTNAME="主机名称" M_API_KEY=86830b460bd64cce990def8a357c38fd REPERTORY_URL="https://monitor.uyuntest.cn" DIR="/opt" bash -c "$(curl -L https://monitor.uyuntest.cn/downloads/agent/install_agent.sh)"
