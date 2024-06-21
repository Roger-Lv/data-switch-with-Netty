package org.bdware.sw.server.requestToSwitch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.sw.client.MockUserClient;

public class MockUserToLeftSwitchPublishMockKV {
    public static void runMockClient() throws Exception{
        int rightSwitchPort=2043;
        int leftSwitchPort=2042;

        Logger logger = LogManager.getLogger(MockUserToLeftSwitchPublishMockKV.class);
        //MockClient
        MockUserClient mockUserClient = new MockUserClient();

        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();

        builder.createRequest("Solid:86.5000.470/do.hello",BasicOperations.Publish.getName());
        //这个增加的irpAddress字段是对应的irpAdapter的ip:port
        builder.addAttributes("irpAddress","tcp://127.0.0.1:18041");
        //这个增加的switchAdress字段是自身的switch的ip:port
        builder.addAttributes("switchAddress","127.0.0.1:2042");

        int numOfTask = 1;
        for (int i=0;i<numOfTask;i++){
            long startTime = System.currentTimeMillis();
            mockUserClient.sendMessage(builder.create(),"127.0.0.1", leftSwitchPort);
            long endTime = System.currentTimeMillis();
            long duration = endTime-startTime;

            System.out.println(duration + "ms");
        }


    }
    public static void main(String[] args)throws Exception{
        runMockClient();
    }
}
