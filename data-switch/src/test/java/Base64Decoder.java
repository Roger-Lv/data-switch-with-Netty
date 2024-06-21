import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Decoder {
    public static void main(String[] args) {
        String base64String = "YmR0ZXN0L1JlcG9zaXRvcnkvMGJlZDc1YjEtZWMyYi00MWEwLTk2ZjYtYTAzOTE2YjdhOTk1";

        // 解码Base64字符串
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);

        // 将字节数组转换为字符串
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

        // 输出解码后的字符串
        System.out.println(decodedString);
    }
}
