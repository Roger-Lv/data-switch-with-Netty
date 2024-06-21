package org.bdware.sw.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.encrypt.SM2Signer;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipClientImpl;
import org.junit.Before;
import org.junit.Test;
import org.zz.gmhelper.SM2KeyPair;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SwitchCommandDOIPTest {
    private static final Logger LOGGER = LogManager.getLogger(SwitchCommandDOIPTest.class);
    private SM2Signer sm2Signer;

    @Before
    public void setVar() {
        String sm2KeyPairStr = "{\"privateKey\":\"5993cc0277930089e345ca8f06799c0017f5d99fa7c8d0941f731f8a20166e11\",\"publicKey\":\"04ebf275a36219b712ab92f5419a7502e237de26f691fe22854a48e14f2a1010943e9f7a183b3710b39a13d34775df25efa78ea2dc5ee1d6906a7628501eeb96dc\"}";
        sm2Signer = new SM2Signer(SM2KeyPair.fromJson(sm2KeyPairStr));
    }

    @Test
    public void resetPortNoSign() {
        try {
            String id = "10F2F2A7D914FA860FE5D60B2BFE1F5A";
            DoipClientImpl client = new DoipClientImpl();
            client.connect(ClientConfig.fromUrl("tcp://127.0.0.1:18060"));
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createRequest(id, BasicOperations.Update.getName());
            builder.addAttributes("gatewayId", "dafd11213");
            builder.setIsCommand(true);
            CompletableFuture<DoipMessage> result = new CompletableFuture<>();
            client.sendRawMessage(builder.create(), msg -> {
                result.complete(msg);
            });
            DoipMessage doipResponse = result.get(5, TimeUnit.SECONDS);
            LOGGER.info(doipResponse.header.parameters.response);
            LOGGER.info(doipResponse.header.parameters.attributes.toString());
            LOGGER.info(doipResponse.body.getDataAsJsonString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void resetPortWithSignature() {
        try {
            String id = "10F2F2A7D914FA860FE5D60B2BFE1F5A";
            DoipClientImpl client = new DoipClientImpl();
            client.connect(ClientConfig.fromUrl("tcp://127.0.0.1:18060"));
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createRequest(id, BasicOperations.Update.getName());
            builder.addAttributes("gatewayId", "dafd11213");
            builder.setIsCommand(true);
            DoipMessage request = builder.create();
            sm2Signer.signMessage(request);
            CompletableFuture<DoipMessage> result = new CompletableFuture<>();
            client.sendRawMessage(builder.create(), msg -> {
                result.complete(msg);
            });
            DoipMessage doipResponse = result.get(5, TimeUnit.SECONDS);
            LOGGER.info(doipResponse.header.parameters.response);
            LOGGER.info(doipResponse.header.parameters.attributes.toString());
            LOGGER.info(doipResponse.body.getDataAsJsonString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void retrieveConfig() {
        try {
            String id = "10F2F2A7D914FA860FE5D60B2BFE1F5A";
            id = "F5A529F4A807E1B3404BEB9E1FED75C3";
            id = "AA38BCF18462D9AAD5F0A7735FD7EC4E";
            id = "670E241C9937B3537047C87053E3AA36";
            DoipClientImpl client = new DoipClientImpl();
            //client.connect(ClientConfig.fromUrl("tcp://8.130.115.76:21052"));
            //     client.connect(ClientConfig.fromUrl("tcp://8.130.136.43:21060"));
            client.connect(ClientConfig.fromUrl("tcp://8.130.140.101:21053"));
            client.connect(ClientConfig.fromUrl("tcp://162.105.16.61:21060"));
            //  client.connect(ClientConfig.fromUrl("tcp://162.105.16.61:21052"));


            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createRequest(id, BasicOperations.Retrieve.getName());
            builder.setIsCommand(true);
            DoipMessage request = builder.create();
            sm2Signer = new SM2Signer(SM2KeyPair.fromJson("{\"privateKey\":\"481343eac82e0d18f8ea3a9f82a8f065543d720209e9f0d8d508f7e343883c45\",\"publicKey\":\"042731bc66608ba21a4301cd6522e3d6a6c7964f8dc3618cfe5d0aae493229a98623de23dfb35a5f9b7b4ac53e1f82ea79325ddf96d88a6bbcaf075df7e98acc5a\"}"), true, false);
            sm2Signer.signMessage(request);

            CompletableFuture<DoipMessage> result = new CompletableFuture<>();
            client.sendRawMessage(builder.create(), msg -> {
                result.complete(msg);
            });
            DoipMessage doipResponse = result.get(5, TimeUnit.SECONDS);
            LOGGER.info(doipResponse.header.parameters.response);
            LOGGER.info(doipResponse.header.parameters.attributes.toString());
            LOGGER.info(doipResponse.body.getDataAsJsonString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void retrieveConfigRemote() {
        try {
            String id = "B0D40F0EFABF8BA8D9C0EC67F93F164B";
            DoipClientImpl client = new DoipClientImpl();
            client.connect(ClientConfig.fromUrl("tcp://8.130.115.76:21060"));
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createRequest(id, BasicOperations.Retrieve.getName());
            builder.setIsCommand(true);
            DoipMessage request = builder.create();
            sm2Signer.signMessage(request);
            CompletableFuture<DoipMessage> result = new CompletableFuture<>();
            client.sendRawMessage(builder.create(), msg -> {
                result.complete(msg);
            });
            DoipMessage doipResponse = result.get(5, TimeUnit.SECONDS);
            LOGGER.info(doipResponse.header.parameters.response);
            LOGGER.info(doipResponse.header.parameters.attributes.toString());
            LOGGER.info(doipResponse.body.getDataAsJsonString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void resetGatewayIDRemote() {
        try {
            String id = "B0D40F0EFABF8BA8D9C0EC67F93F164B";
            DoipClientImpl client = new DoipClientImpl();
            client.connect(ClientConfig.fromUrl("tcp://8.130.115.76:21060"));
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createRequest(id, BasicOperations.Update.getName());
            builder.addAttributes("gatewayId", "dafd11213");
            builder.setIsCommand(true);
            DoipMessage request = builder.create();
            sm2Signer.signMessage(request);
            CompletableFuture<DoipMessage> result = new CompletableFuture<>();
            client.sendRawMessage(builder.create(), msg -> {
                result.complete(msg);
            });
            DoipMessage doipResponse = result.get(5, TimeUnit.SECONDS);
            LOGGER.info(doipResponse.header.parameters.response);
            LOGGER.info(doipResponse.header.parameters.attributes.toString());
            LOGGER.info(doipResponse.body.getDataAsJsonString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
