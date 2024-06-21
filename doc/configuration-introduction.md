# 配置说明
`ip`,`name`,`port`是交换机本身的配置。
`localDataSpaceRouterURI`是网关的配置。
`publishIdTopics`是订阅和识别标识流。需要通过网关给交换机推送ID流，网关推的标识流中的DOIPMessage的标识得是这个交换机。
`switchID`是交换机的ID。
`publicServiceConfig`是啥玩意？
`staticRouteEntry`是为了自动注册的时候用的东西。
`statics` 临时使用的一个配置项，需移除。
```JSON
{
  "ip": "127.0.0.1",
  "name": "测试空间18060",
  "port": 18060,
  "publicKey": "04180354fdb6507f8ab98ccfbe165ce11da74ba733f81af86ad6d32216b32cf4f797c559d50ceeefbf4c760c3483840471c67471b90acdffb388cd7d496d9a1610",
  "privateKey": "1d4196947f59532db6f8f4055e58474a48db8f30b476ae3edc66406464521b3b",
  "gatewayIRPURL": "tcp://8.130.115.76:21043",
  "gatewayId": "geteway2321321135",
  "routerIRPURL": "",
  "routerId": "",
  "switchNetworkDOL": "",
  "publishIdTopics": [
    {
      "targetID": "CSTR:100871.11.idoentryrepo",
      "topicID": "CSTR:100871.11.findata_dp_stream"
    }
  ],
  "switchID": "10F2F2A7D914FA860FE5D60B2BFE1F5A",
  "publicServiceConfig": {
    "routerURI": "tcp://127.0.0.1:21041",
    "pragmaticId": "bdtest/testPragmaticNetwork",
    "locatingId": "unknown"
  },

  "statics": {
    "dpCount": 0,
    "doipOp": 876998,
    "doCountInfo": {
      "dir": "./input",
      "suffix": ".json"
    },
    "lastDP": {
      "data": [
        {
          "abstract": "no_id",
          "content": "<s>暂时没有DPML</s>"
        }
      ],
      "count": 1
    }
  }
}
```