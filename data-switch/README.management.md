## 管理接口说明

交换的管理接口使用DOIP协议。
DOIP请求由以下三个部分组成：
首先，`id`使用网关的标识，操作为`Update`。

其次，网关的其他的参数均在`attributes`中设置。
`attributes`支持的key包括`ip`,`port`,`id`,`spaceIRPURL`、`switchIRPURL`、`switchID`、`managerPublicKey`一共五种。`attributes`的`key`不在以上的中被忽略。

`attributes`的配置项目的说明如下：
- `ip`: 该参数用于指定网关的IP地址。
- `port`: 该参数用于指定该网关监听的端口号。
- `id`: 该参数用于指定该网关的ID。
- `spaceIRPURL`: 该参数用于指定该网关接入的空间的标识解析地址。
- `switchIRPURL`: 该参数用于指定该网关所绑定的交换机的入口地址。
- `switchID`: 该参数用于指定该网关所绑定的交换机的标识。
- `managerPublicKey`: 用于修改管理员公钥。

最后，管理接口的DOIP请求中的标记位中，`Command = true`，且需要管理员签名。
，签名和标记位可通过配置合适的`variables`变量来实现。
`variables`是DOIP请求的一些非Header中的配置项，
如`privateKey`和`publicKey`是用于配置签名的；`isCommand`是用于配置标记位的。
而`switchURI`是用于配置交换机的解析地址的。

### DOIP请求的示例
以下示例中，attributes和variables的switchURI虽然不一样，但是其含意不同。
variables的switchIRPURL 表示这个请求的第一跳交换机的入口解析地址。
而attributes的switchIRPURL 表示配置了这个网关所绑定的交换机的入口解析地址。
variables的switchIRPURL可任意配置一个交换机的入口解析地址，不影响请求的发送。

注意到，attributes中的key并不是全部都需要的。可按需配置。

```json
{
  "id": "32A6F129F15C93108E068DB33013D026",
  "op": "0.DOIP/Update",
  "attributes": {
    "ip": "8.130.136.43",
    "port": 21060,
    "name": "代码数据空间",
    "gatewayIRPURL": "tcp://8.130.136.43:21041",
    "switchId": "32A6F129F15C93108E068DB33013D026",
    "routerId": "bdtest/BDOS/node-router",
    "routerIRPURL": "tcp://8.130.41.205:21041",
    "pragmaticId": "bdtest/BDOS/testPragmaticNetwork",
    "switchNetworkId": "bdtest/BDOS/switchNetowrk",
    "managerPublicKey": "04ebf275a36219b712ab92f5419a7502e237de26f691fe22854a48e14f2a1010943e9f7a183b3710b39a13d34775df25efa78ea2dc5ee1d6906a7628501eeb96dc"
  },
  "variables": {
    "isCommand": true,
    "privateKey": "5993cc0277930089e345ca8f06799c0017f5d99fa7c8d0941f731f8a20166e11",
    "publicKey": "04ebf275a36219b712ab92f5419a7502e237de26f691fe22854a48e14f2a1010943e9f7a183b3710b39a13d34775df25efa78ea2dc5ee1d6906a7628501eeb96dc",
    "switchIRPURL": "tcp://120.46.79.34:21042"
  }
}
```
更多java参考示例见：
[SwitchCommandDOIPTest.java](src%2Ftest%2Fjava%2Forg%2Fbdware%2Fsw%2Fserver%2FSwitchCommandDOIPTest.java)
