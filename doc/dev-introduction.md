# 数联网交换机参考实现

## 介绍

idea开发合约的示例。

## 测试环境配置

1. 在本地准备docker测试环境。
```bash
cd bdserver-docker-compose
docker-compose up -d 
cat bdcontract/manager.keypair
#并复制这组{"publickey".."privateKey"}
```
2. 打开浏览器`http://127.0.0.1:21030/NodePortal.html`。点击右上角`欢迎04xx`按钮，将步骤1中的keyPair复制，并点`导入密钥`。

## 开发环境配置

可安装idea插件：`ideaplugin/yjs-formater-1.0-SNAPSHOT-1653060411648.zip`
用于格式化yjs文件和自动补全。

## 在本机调试运行

1.修改debugconf.json,将cpHost改为从docker可访问的域名或ip。

```
在MAC/Linux环境下，可使用ifconfig | grep inet ,查看本地局域网ip，并配置到cpHost字段。
在Windows环境下，可使用"cpHost":"host.docker.internal"。
其中host.docker.internal是docker中内置的一个域名，可用于访问host。
https://docs.docker.com/desktop/windows/networking/
```

示例的debugconf.json配置：

```json
{
  "agentAddress": "127.0.0.1:21030",
  "publicKey": "04180354fdb6507f8ab98ccfbe165ce11da74ba733f81af86ad6d32216b32cf4f797c559d50ceeefbf4c760c3483840471c67471b90acdffb388cd7d496d9a1610",
  "privateKey": "1d4196947f59532db6f8f4055e58474a48db8f30b476ae3edc66406464521b3b",
  "ypkPath": "./backend/build/iod-switch-0.1.0.ypk",
  "killBeforeStart": "ContractExample",
  "createParam": {
  },
  "cpHost": "host.docker.internal"
}
```

2.启动测试用例：

```
test/java/HelloTest.run
```

注意run方法中的`for (;;);`用于防止测试用例退出。
在windows中可能影响其他测试用例运行。可编写`public static void main(String[] args)`去运行合约。

3.带UI的ypk调试
由于bdcontract会根据启动参数中的ypkPath去解析ypk文件中的资源文件，因此，当需要通过`http://xxx:xx/DOIP/ContractID`这种形式去访问静态资源时，
需要配置ypk路径映射。
一种做法是：

1) 在./bdserver-docker-compose目录下，在启动之后，会生成一个文件`./bdcontract/BDWareProjectDir`。将该目录映射至./backend目录下或是backend的上级目录下。

```
cd 至backend
ln -s /absolute/path/to/BDWareProjectDir ./
```

2) 修改DebugMain的启动路径为BDWareProjectDir所在目录。

3) 修改debugconf.json中的ypkPath，使用相对路径的形式`./BDWareProjectDir/xxx`。

4) 注意需要将编译好的ypk放至BDWareProjectDir中。可编写一个新的gradle task完成脚本化复制。如：

```
task copyToBDWareProjectDir(type: Copy) {
    from "./build/contractexample-${currVersion}.ypk"
    into "../bdserver-docker-compose/bdcontract/BDWareProjectDir/"
}
```

## 在docker内调试运行

打开镜像的`NodePortal.html`页面，以ypk的形式启动。
在启动时，增加`remoteDebugPort`，注意远程调试的端口需要在镜像中映射出来。
可使用docker镜像默认映射端口范围`21050-21100`，例如`21058`。
而后即可通过jdwp可进行远程调试。 如果使用`idea`作为开发环境，则可搜索`idea java remote debug`，查看相关教程。
NodePortal.html可参考：[BDContract管理页面](https://public.internetapi.cn/docs/bdcontract/doc/IDEUsage.html)

## 部署运行

部署至远程服务端执行":backend:deploy"任务：
即执行./backend/build.gradle 中的 deploy。

## 前端开发说明
详见[front/README.md](./front/README.md)
启动好之后，可通过以下路径访问前端：
```json
http://127.0.0.1:21030/DOIP/ContractExample/assets/vite/dist/index.html
http://127.0.0.1:21030/DOIP/ContractExample/assets/html/index.html
```
## 模拟后端数据

## 后端常用方法说明

## 调用相同CP的yjs代码

参考：
Hello.callYJSInSameCP

## 调用其他CP的yjs代码

参考：
Hello.callYJSInOtherCP
