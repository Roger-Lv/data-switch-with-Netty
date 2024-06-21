package org.bdware.sw.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.audit.AuditDoaClient;
import org.bdware.doip.audit.EndpointConfig;
import org.bdware.doip.cluster.client.DOAConfigBuilder;
import org.bdware.doip.cluster.client.DoaClusterClient;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.CountDownLatch;

public class DoipRequestForSwitch {
    static Logger LOGGER = LogManager.getLogger(DoipRequestForSwitch.class);

    @Test
    public void downloadWholeZipFromGitHub() throws Exception {
        //由于Github的代码包比较小，所以直接就是放到body里面来了。到时存文件即可。
        EndpointConfig config = new EndpointConfig();
        config.routerURI = "tcp://8.130.140.101:21042";
        // config.routerURI = "tcp://127.0.0.1:2402";
        config.routerURI = "tcp://118.178.254.228:21042";
        // config.routerURI = "tcp://8.130.136.43:21041";
        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig("tcp://118.178.254.228:21042"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("https://github.com/johnnlp/saved", BasicOperations.Retrieve.getName());
        DoipMessage result = client.sendMessageSync(builder.create(), 10000);
        LOGGER.info("result.header:" + result.header.parameters.attributes.toString());
        FileOutputStream outputFile = new FileOutputStream("./ContractDB/code.zip", false);
        outputFile.write(result.body.getDataAsByteArray());
        outputFile.close();
        LOGGER.info("result is saved in:" + new File("./ContractDB/code.zip").getAbsolutePath());
    }


    @Test
    public void downloadLargeFileFromIDS() throws Exception {
        EndpointConfig enpointConfig = new EndpointConfig();
        enpointConfig.routerURI = "tcp://8.130.140.101:21042";
      //  enpointConfig.routerURI = "tcp://120.46.79.34:21041";
        AuditDoaClient client = new AuditDoaClient("", enpointConfig, null);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("ids:dataset/999a2b6ca6ce8a0d0c88ca99b602613e83d926daf9de1c72bca5db15a399c986", BasicOperations.Retrieve.getName());
        builder.addAttributes("offset", 0);
        builder.addAttributes("count", 10);
        FileOutputStream fout = new FileOutputStream("./ContractDB/largeData.zip", false);
        DoipMessage result = client.sendRawMessageSync(builder.create(), 10000);
        long total = result.header.parameters.attributes.get("total").getAsLong();
        LOGGER.info("total:" + total);
        long bufferSize = 1024 * 5000;
        for (long i = 0; i < total; i += bufferSize) {
            DoipMessageFactory.DoipMessageBuilder builder2 = new DoipMessageFactory.DoipMessageBuilder();
            builder2.createRequest("ids:dataset/b2be7da9fb8384fa06b15be65447aa8f027daff6efd808649deda6f13613bc8e", BasicOperations.Retrieve.getName());
            builder2.addAttributes("offset", i);
            builder2.addAttributes("count", bufferSize);
            DoipMessage result2 = client.sendRawMessageSync(builder2.create(), 10000);
            byte[] buff = result2.body.getEncodedData();
            fout.write(buff);
            LOGGER.info(i + "/" + total + " -> buf.len=" + buff.length);
        }
        LOGGER.info("saved into :" + new File("./ContractDB/largeData.zip").getAbsolutePath());
        fout.close();
    }
    @Test
    public void downloadLargeFileFromIDS2() throws Exception {
        EndpointConfig enpointConfig = new EndpointConfig();
        enpointConfig.routerURI = "tcp://8.130.140.101:21042";
        AuditDoaClient client = new AuditDoaClient("", enpointConfig, null);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("ids:dataset/8bd99052280ea3c402befa4936d9822140c7bfb260c9bf36a35e2d9bbc6ab778", BasicOperations.Retrieve.getName());
        builder.addAttributes("offset", 0);
        builder.addAttributes("count", 10);
        FileOutputStream fout = new FileOutputStream("./ContractDB/dataset.zip", false);
        DoipMessage result = client.sendRawMessageSync(builder.create(), 10000);
        long total = result.header.parameters.attributes.get("total").getAsLong();
        LOGGER.info("total:" + total);
        long bufferSize = 1024 * 5000;
        for (long i = 0; i < total; i += bufferSize) {
            DoipMessageFactory.DoipMessageBuilder builder2 = new DoipMessageFactory.DoipMessageBuilder();
            builder2.createRequest("ids:dataset/8bd99052280ea3c402befa4936d9822140c7bfb260c9bf36a35e2d9bbc6ab778", BasicOperations.Retrieve.getName());
            builder2.addAttributes("offset", i);
            builder2.addAttributes("count", bufferSize);
            DoipMessage result2 = client.sendRawMessageSync(builder2.create(), 10000);
            byte[] buff = result2.body.getEncodedData();
            fout.write(buff);
            LOGGER.info(i + "/" + total + " -> buf.len=" + buff.length);
        }
        LOGGER.info("saved into :" + new File("./ContractDB/dataset.zip").getAbsolutePath());
        fout.close();
    }
    @Test
    public void downloadWholeZipFromGitHub2() throws Exception {
        //由于Github的代码包比较小，所以直接就是放到body里面来了。到时存文件即可。
        // config.routerURI = "tcp://127.0.0.1:2402";
        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig("tcp://8.130.140.101:21042"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("https://gitee.com/pkuhxl/code2.git", BasicOperations.Retrieve.getName());
        DoipMessage result = client.sendMessageSync(builder.create(), 10000);
        LOGGER.info("result.header:" + result.header.parameters.attributes.toString());
        FileOutputStream outputFile = new FileOutputStream("./ContractDB/code2.zip", false);
        outputFile.write(result.body.getDataAsByteArray());
        outputFile.close();
        LOGGER.info("result is saved in:" + new File("./ContractDB/code2.zip").getAbsolutePath());
    }


    @Test
    public void retrireveFromDOI() {
        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig("tcp://8.130.140.101:21042"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("10.48550/arXiv.1805.10616", BasicOperations.Retrieve.getName());
        DoipMessage result = client.sendMessageSync(builder.create(), 10000);
        LOGGER.info("result.header:" + result.header.parameters.attributes.toString());
        LOGGER.info("result.body:" + result.body.getDataAsJsonString());
    }
    @Test
    public void listOperationFromDOI() {
        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig("tcp://8.130.140.101:21042"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("10.48550/arXiv.1805.10616", BasicOperations.ListOps.getName());
        DoipMessage result = client.sendMessageSync(builder.create(), 10000);
        LOGGER.info("result.header:" + result.header.parameters.attributes.toString());
        LOGGER.info("result.body:" + result.body.getDataAsJsonString());
    }



    @Test
    public void retrieveFromIDS() {
        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig("tcp://8.130.140.101:21042"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("ids:dataset/8bd99052280ea3c402befa4936d9822140c7bfb260c9bf36a35e2d9bbc6ab778", BasicOperations.Retrieve.getName());
        DoipMessage result = client.sendMessageSync(builder.create(), 10000);
        LOGGER.info("result.header:" + result.header.parameters.attributes.toString());
        LOGGER.info("result.body:" + result.body.getDataAsJsonString());
    }

    @Test
    public void runFromGitHubToCSTR() {
        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig("tcp://8.130.140.101:21042"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("CSTR:33108.11.8ee08d7464dd4583be280d931776a90d", BasicOperations.Retrieve.getName());
        DoipMessage result = client.sendMessageSync(builder.create(), 10000);
        LOGGER.info("result.header:" + result.header.parameters.attributes.toString());
        LOGGER.info("result.body:" + result.body.getDataAsJsonString());
    }

    @Test
    //没有id
    public void runFromGitHubToSolid() {
        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig("tcp://8.130.140.101:21042"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("ids:dataset/8bd99052280ea3c402befa4936d9822140c7bfb260c9bf36a35e2d9bbc6ab778", BasicOperations.Retrieve.getName());
        DoipMessage result = client.sendMessageSync(builder.create(), 10000);
        LOGGER.info("result.header:" + result.header.parameters.attributes.toString());
        LOGGER.info("result.body:" + result.body.getDataAsJsonString());

    }
}
