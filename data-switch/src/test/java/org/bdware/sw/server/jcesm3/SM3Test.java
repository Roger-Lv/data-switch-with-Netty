package org.bdware.sw.server.jcesm3;

import org.rocksdb.util.ByteUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class SM3Test
{
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException {
        // 测试代码
        String input = "原始数据";
        MessageDigest messageDigest = MessageDigest.getInstance("SM3","SwxaJCE");
        messageDigest.update(input.getBytes());
        byte [] output = messageDigest.digest();
        System.out.println(output.length);
        System.out.println(bytesToHex(output));
    }
}
