package io.github.hqqich.tool.file.webdav;


import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * WebDAV 客户端工具类（基于 Sardine 库实现）
 * <p>
 * 提供 WebDAV 协议的文件操作功能，包括：
 * <ul>
 *     <li>文件上传/下载</li>
 *     <li>目录遍历</li>
 *     <li>文件属性管理</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 *
 * <b>1. 文件上传</b>
 * <pre>
 * // 准备文件数据
 * byte[] fileData = "Hello, WebDAV!".getBytes(StandardCharsets.UTF_8);
 *
 * // 上传到指定路径
 * // 示例路径格式：https://{domain}/remote.php/dav/files/{username}/path/to/file.txt
 * sardine.put("https://dav.example.com/remote.php/dav/files/user/hello.txt", fileData);
 * </pre>
 *
 * <b>2. 目录内容遍历</b>
 * <pre>
 * // 列出目录下所有资源
 * List&lt;DavResource&gt; resources = sardine.list(
 *     "https://dav.example.com/remote.php/dav/files/user/"
 * );
 *
 * // 打印资源名称
 * for (DavResource res : resources) {
 *     System.out.println("资源名称: " + res.getName() +
 *                       " | 类型: " + (res.isDirectory() ? "目录" : "文件") +
 *                       " | 大小: " + res.getContentLength() + " bytes");
 * }
 * </pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *     <li>路径格式需符合 Nextcloud/OwnCloud 的 WebDAV 实现规范</li>
 *     <li>上传前需确保目录已存在，否则会抛出异常</li>
 *     <li>大文件上传建议使用流式接口（Sardine 的 put 方法重载）</li>
 * </ul>
 */
public final class WebDavUtils {

    private final String webDavBaseUrl;
    private final String webDavUsername;
    private final String webDavPassword;

    public WebDavUtils(String webDavBaseUrl, String webDavUsername, String webDavPassword) {
        this.webDavBaseUrl = requireText(webDavBaseUrl, "WebDAV 地址不能为空");
        this.webDavUsername = requireText(webDavUsername, "WebDAV 用户名不能为空");
        this.webDavPassword = requireText(webDavPassword, "WebDAV 密码不能为空");
    }

    /**
     * 上传文件
     * @param file 要上传的文件
     * @return 返回文件url链接
     * @throws IOException IO 异常
     */
    public String upload(File file) throws IOException {
        validateFile(file);
        return upload(file, file.getName());
    }

    /**
     * 上传文件并指定目标文件名
     * @param file 要上传的文件
     * @param fileName WebDAV 目标文件名
     * @return 返回文件 url 链接
     * @throws IOException IO 异常
     */
    public String upload(File file, String fileName) throws IOException {
        validateFile(file);
        String targetFileName = requireText(fileName, "目标文件名不能为空");
        return execute(sardine -> {
            String targetUrl = buildTargetUrl(targetFileName);
            sardine.put(targetUrl, file, getContentType(targetFileName));
            return targetUrl;
        });
    }

    /**
     * 删除 WebDAV 上的文件
     * @param fileName 要删除的文件名
     * @throws IOException IO 异常
     */
    public void deleteFile(String fileName) throws IOException {
        String targetFileName = requireText(fileName, "要删除的文件名不能为空");
        execute(sardine -> {
            String targetUrl = buildTargetUrl(targetFileName);
            sardine.delete(targetUrl);
            return null;
        });
    }

    /**
     * 修改 WebDAV 上的文件名
     * @param sourceFileName 原文件名
     * @param targetFileName 新文件名
     * @return 修改后的文件 url
     * @throws IOException IO 异常
     */
    public String renameFile(String sourceFileName, String targetFileName) throws IOException {
        String sourceName = requireText(sourceFileName, "原文件名不能为空");
        String targetName = requireText(targetFileName, "新文件名不能为空");
        return execute(sardine -> {
            String sourceUrl = buildTargetUrl(sourceName);
            String targetUrl = buildTargetUrl(targetName);
            sardine.move(sourceUrl, targetUrl);
            return targetUrl;
        });
    }

    /**
     * 下载 WebDAV 上的文件到本地
     * @param fileName WebDAV 文件名
     * @param targetFile 本地目标文件
     * @return 下载后的本地文件
     * @throws IOException IO 异常
     */
    public File downloadFile(String fileName, File targetFile) throws IOException {
        String sourceFileName = requireText(fileName, "要下载的文件名不能为空");
        File localTargetFile = prepareTargetFile(targetFile);
        return execute(sardine -> {
            String sourceUrl = buildTargetUrl(sourceFileName);
            try (InputStream inputStream = sardine.get(sourceUrl)) {
                Files.copy(inputStream, localTargetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return localTargetFile;
        });
    }

    private void validateFile(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (!file.exists() || !file.isFile()) {
            throw new IOException("文件不存在或不是普通文件: " + file);
        }
    }

    private File prepareTargetFile(File targetFile) throws IOException {
        if (targetFile == null) {
            throw new IllegalArgumentException("本地目标文件不能为空");
        }
        if (targetFile.exists() && targetFile.isDirectory()) {
            throw new IOException("本地目标文件不能是目录: " + targetFile);
        }
        File parentFile = targetFile.getAbsoluteFile().getParentFile();
        if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs() && !parentFile.exists()) {
            throw new IOException("创建本地目录失败: " + parentFile);
        }
        return targetFile;
    }

    private String buildTargetUrl(String fileName) throws IOException {
        try {
            return trimTrailingSlash(webDavBaseUrl) + "/" + new URI(null, null, fileName, null).toASCIIString();
        } catch (URISyntaxException e) {
            throw new IOException("构建 WebDAV 目标地址失败: " + fileName, e);
        }
    }

    private Sardine createSardine() throws IOException {
        Sardine sardine = SardineFactory.begin(webDavUsername, webDavPassword);
        sardine.enablePreemptiveAuthentication(webDavBaseUrl);
        return sardine;
    }

    private <T> T execute(SardineOperation<T> operation) throws IOException {
        Sardine sardine = createSardine();
        try {
            return operation.execute(sardine);
        } finally {
            sardine.shutdown();
        }
    }

    private static String requireText(String value, String message) {
        Objects.requireNonNull(value, message);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static String trimTrailingSlash(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }

    private static String getContentType(String fileName) {
        String contentType = URLConnection.guessContentTypeFromName(fileName);
        return contentType == null ? "application/octet-stream" : contentType;
    }

    @FunctionalInterface
    private interface SardineOperation<T> {

        T execute(Sardine sardine) throws IOException;
    }
}
