package org.bdware.sw.download;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.sw.SM3Tool;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GitHubCodeDownloader {
    static Logger logger = LogManager.getLogger(GitHubCodeDownloader.class);
    static ExecutorService es;

    public static void main(String[] args) {

        //获取id
        String doiinline = args[0];
        //输出的目录
        String outputDir = args[1];
        //存放偏移量
        int offset = Integer.parseInt(args[2]);
        //想要运行成功下载的数量
        //这里如果为0就是一直下(不限数量)
        Integer count = Integer.parseInt(args[3]);
        //这里记录下载的情况
        String downloadRecordFilePath = args[4];
        //线程池线程数量
        int threadCount = 4;
        if (args.length >= 6)
            threadCount = Integer.valueOf(args[5]);
        es = Executors.newFixedThreadPool(threadCount);
        //GitHub token 防止被403
        String accessToken = "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";
        ObjectMapper mapper = new ObjectMapper();
        List<String> idList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(doiinline));
            String line;
            while ((line = br.readLine()) != null) {
                JsonNode node = mapper.readTree(line);
                String id = node.get("id").textValue();
                if (id.startsWith("https://github.com/"))
                    idList.add(id);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        int downloadSuccessNum = 0;
        int start = 0;
        for (String githubUrl : idList) {
            start++;
            //如果在偏移量中就跳过
            if (!inoffset(start, offset, count))
                continue;
            System.out.println("GitHub URL: " + githubUrl);
            String sm3Hash = SM3Tool.toSM3(githubUrl);
            //这样第一层就有256个子目录
            String subDir1 = sm3Hash.substring(0, 2);
            //这样第二层就有256个子目录
            String subDir2 = sm3Hash.substring(2, 4);
            // 下载文件并保存到指定路径
            String fileName = outputDir + "/" + subDir1 + "/" + subDir2 + "/" + sm3Hash + ".zip";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(downloadRecordFilePath, true))) {
                downloadFileFromGitHub(githubUrl, fileName, accessToken);
                downloadSuccessNum++;
                writer.write("Downloaded success: " + githubUrl + ", success num is: "
                        + downloadSuccessNum + ", the index is:" + start + "\n");
                logger.info("Downloaded success: " + githubUrl + ", success num is: "
                        + downloadSuccessNum + ", the index is:" + start);
                if (count != null)
                    if (downloadSuccessNum >= count && count != 0) {
                        logger.info("finish Download, success num is: " + downloadSuccessNum + ", the index is:" + start);
                        break;
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static boolean inoffset(int i, int offset, int count) {
        return i >= offset && i < offset + count;
    }

    private static void downloadFileFromGitHub(String githubFileUrl, String filePath, String accessToken) throws IOException {
        if (new File(filePath).exists()) {
            logger.info("Ignore:" + githubFileUrl + " --> " + filePath);
            return;
        }
        if (!new File(filePath).getParentFile().exists())
            new File(filePath).getParentFile().mkdirs();
        // 构建 GitHub API 的 URL
        String githubApiUrl = convertToGitHubApiUrl(githubFileUrl);
        FileOutputStream fout = new FileOutputStream(filePath, false);
        // 发送 HTTP 请求获取文件内容
        HttpURLConnection connection = (HttpURLConnection) new URL(githubApiUrl).openConnection();
        try {
            // 添加 Authorization 头部
            connection.setRequestProperty("Authorization", "token " + accessToken);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 获取父目录路径
                String parentDirectory = new File(filePath).getParent();

                // 检查目录是否存在，如果不存在则创建
                File parentDir = new File(parentDirectory);
                if (!parentDir.exists()) {
                    if (parentDir.mkdirs()) {
                        logger.info("Directory created: " + parentDirectory);
                    } else {
                        logger.error("Failed to create directory: " + parentDirectory);
                        return;
                    }
                }

                byte[] buff = new byte[102400];
                InputStream inputStream = connection.getInputStream();
                int r = 0;
                for (; (r = inputStream.read(buff)) > 0; ) {
                    fout.write(buff, 0, r);
                }
                logger.info("File downloaded successfully.");
            } else {
                logger.error("Failed to download file. HTTP Response Code: " + connection.getResponseCode());
            }
        } finally {
            connection.disconnect();
        }
    }


    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int bytesRead;
        byte[] data = new byte[1024];
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }


    private static String convertToGitHubApiUrl(String githubFileUrl) {
        // 从 githubFileUrl 中提取用户名、仓库名、分支名
        String[] parts = githubFileUrl.split("/");
        String username = parts[3];
        String repository = parts[4];

        // 构建 GitHub API 地址
        return "https://api.github.com/repos/" + username + "/" + repository + "/zipball";
    }

}
