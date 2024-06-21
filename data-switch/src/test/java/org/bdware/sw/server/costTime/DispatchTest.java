package org.bdware.sw.server.costTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.sw.dispatcher.DispatchAnswer;
import org.bdware.sw.dispatcher.Dispatcher;
import org.bdware.sw.nodemanager.AddressOfSwitch;
import org.bdware.sw.statistics.Statistics;



public class DispatchTest
{
    static Logger logger = LogManager.getLogger(DispatchTest.class);
    public static Statistics statistics=new Statistics();
    public static void main(String[] args) throws InterruptedException {
        EndpointConfig endpointConfig = new EndpointConfig();
        //DOI
        endpointConfig.routerURI = "tcp://8.130.115.76:21041";
        Dispatcher dispatcher = new Dispatcher("127.0.0.1",18041,endpointConfig);
        dispatcher.switchNodeManager.addAddressCache("https://github.com/johnnlp/saved",new AddressOfSwitch("right","127.0.0.1",2042));
        long startTime = System.currentTimeMillis();
        int numTask = 1000000;
        for (int i =0;i<numTask;i++){
            try{
            DispatchAnswer dispatchAnswer=dispatcher.dispatch(new DoipMessage("https://github.com/johnnlp/saved", BasicOperations.Retrieve.getName()));
            int situation = dispatchAnswer.situation;
            switch (situation) {
                //将请求移交给ClusterClientForDigitalSpace
                case 0:
                    logger.info("0.将请求移交给ClusterClientForDigitalSpace");
                    synchronized (statistics){
                        statistics.doipOp++;
                    }
                    break;
                //将请求移交给ClientForSwitch
                case 1:
                    logger.info("1.将请求移交给ClientForSwitch");
                    synchronized (statistics){
                        statistics.doipOp++;
                    }
                    break;
                //get mockKV
                case 2:
                    logger.info("2.get mockKV");
                    synchronized (statistics){
                        statistics.doipOp++;
                    }
                    break;
                //wrong situation
                case 99:
                    logger.error("99.wrong situation");
                    synchronized (statistics){
                        statistics.doipOp++;
                    }
                    break;
                default:
                    logger.error("Unknown wrong situation");
            }} catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        long endTime = System.currentTimeMillis();
        System.out.println("cost time:"+(endTime-startTime));
        System.out.println("avg cost time:"+((endTime-startTime)/numTask));
    }
}
