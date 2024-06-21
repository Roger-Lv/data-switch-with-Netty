1. ClientForSwitch 加个链接管理？（√）

   Listener里面两个handler（√）

2. RegisterDOLHandler的从publish消息中自动提取并注册的逻辑。（暂时别管）

3. .RetrieveDOHandler 重构一下channelRead0，使用switch，每种情况封装个函数。（√）

4. 把switch部署到IDS/CSTR空间。（deploy.json写好了）√

5. 测试用例添加fromGitHub->IDS/fromGitHub->CSTR/fromGitHub->DOI/ fromGitHub->Solid（√）

6. 每个空间重新注册定位符。GitHub/IDS/DOI，还缺少复现空间/CSTR和Solid。√

7.是否考虑使用DOIP.hello来替代irp-adapter的接口。因为对于空间而言，do.hello是合理的？
8.是否考虑使用DDO来替换当前的单一repo，因为DDO支持软件定义地把多个repo管理起来？
9.
=====关于评估=====
Benchmark的设计要求：以Retrieve为例。可否找一些Http评测的benchmark。
   1.要有跨空间的标识Retrieve？ 每隔多少会重复标识？应该隔多少个重复一个？有cache和没cache的响应时间能提升多少？
   2.要有跨空间的
参考以下benchmark弄一个？
https://www.distributed-systems.net/my-data/papers/2007.ic.pdf 这里用
We filled the database with information for
500,000 users and 200,000 comments. The TPC-W
benchmark consists of seven database tables filled
with information on 100,000 items and 288,000
customers. For our experiments, we chose the open
source PHP implementation of these benchmarks
(http://jmob.objectweb.org/rubbos.html and http://pgfoundry.org/projects/tpc-w-php/).

https://www.spec.org/web2009/  SPECweb Benchmark？

1.评估LRUCache的效果。本项目中有两个LRUCache，一个是空间缓存，一个是数联网缓存。
需要有造一个benchmark，来评估启用/停用空间缓存的效果。


=========20240510=======
IrpDetector 需要使用DoaClusterClient，支持配置一个软件定义的路由规则。
