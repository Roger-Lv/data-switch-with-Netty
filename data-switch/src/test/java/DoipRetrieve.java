import org.bdware.doip.cluster.client.DOAConfigBuilder;
import org.bdware.doip.cluster.client.DoaClusterClient;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipClientImpl;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DoipRetrieve {

    @Test
    public void retrieveCode() throws Exception {

        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig("tcp://8.130.136.43:21042"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("https://github.com/johnnlp/saved", BasicOperations.Retrieve.getName());
        CompletableFuture<DoipMessage> result = new CompletableFuture<>();
        client.sendRawMessage(builder.create(), msg -> {
            result.complete(msg);
        });
        DoipMessage doipResponse = result.get(50, TimeUnit.SECONDS);
        System.out.println(doipResponse.header.parameters.response);
        System.out.println(doipResponse.header.parameters.attributes.toString());
        System.out.println(doipResponse.body.getDataAsJsonString());
    }


    @Test
    public void retrieveData() throws Exception {
        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig("tcp://120.46.79.34:21042"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("ids:dataset/999a2b6ca6ce8a0d0c88ca99b602613e83d926daf9de1c72bca5db15a399c986", BasicOperations.Retrieve.getName());
        CompletableFuture<DoipMessage> result = new CompletableFuture<>();
        client.sendRawMessage(builder.create(), msg -> {
            result.complete(msg);
        });
        DoipMessage doipResponse = result.get(50, TimeUnit.SECONDS);
        System.out.println(doipResponse.header.parameters.response);
        System.out.println(doipResponse.header.parameters.attributes.toString());
        System.out.println(doipResponse.body.getDataAsJsonString());
    }

    @Test
    public void retrievePaper() throws Exception {
        DoaClusterClient client = new DoaClusterClient(DOAConfigBuilder.withIrpConfig("tcp://8.130.115.76:21041"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("10.48550/arXiv.1705.10819", BasicOperations.Retrieve.getName());
        CompletableFuture<DoipMessage> result = new CompletableFuture<>();
        client.sendRawMessage(builder.create(), msg -> {
            result.complete(msg);
        });
        DoipMessage doipResponse = result.get(50, TimeUnit.SECONDS);
        System.out.println(doipResponse.header.parameters.response);
        System.out.println(doipResponse.header.parameters.attributes.toString());
        System.out.println(doipResponse.body.getDataAsJsonString());
    }


    @Test
    public void retrieve() {
        DoipClientImpl client = new DoipClientImpl();
        client.connect(ClientConfig.fromUrl("tcp://127.0.0.1:18060"));
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest("daflkdsajf", BasicOperations.Retrieve.getName());
        AtomicInteger i = new AtomicInteger(0);
        client.sendMessage(builder.create(), new DoipMessageCallback() {
            @Override
            public void onResult(DoipMessage msg) {

                i.incrementAndGet();
            }
        });
        for (; i.get() == 0; ) Thread.yield();
        ;
    }
}
