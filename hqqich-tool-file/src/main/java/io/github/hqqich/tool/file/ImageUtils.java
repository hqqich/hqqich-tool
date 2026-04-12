package io.github.hqqich.tool.file;

import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * 图片处理工具类
 *
 * @author tsinglink
 */
public class ImageUtils {

    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);


    /**
     * 将图片转换成Base64编码
     *
     * @param imgFile 待处理图片地址
     * @return
     */
    public static String getImageBase64(String imgFile) {

        // 将图片文件转化为二进制流
        byte[] data = null;
        // 读取图片字节数组
        try (InputStream in = Files.newInputStream(Paths.get(imgFile))) {
            data = new byte[in.available()];
            in.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(data);
    }


    public static String getImgBaseWithHead(String imgFile) {
        return "data:image/jpeg;base64," + getImageBase64(imgFile);

    }

    public static String getImgBaseWithHead(File imgFile) {
        return "data:image/jpeg;base64," + getImageBase64(imgFile.getAbsolutePath());
    }


    // 引入依赖：org.apache.tika:tika-core
    private static final Tika tika = new Tika();

    public static boolean isImage(@NotNull File saveFile) {

        String mimeType = null;
        try {
            mimeType = tika.detect(saveFile);
        } catch (IOException e) {
            log.error("文件类型判断异常, file = {}", saveFile, e);
            return false;
        }
        return mimeType.startsWith("image/");
    }

    /**
     * 批量压缩图片<br>
     * 对sourceFile进行缩略图压缩，同级目录下保存
     **/
    public static void batchCompress(@NotNull File sourceFile) {

        final String parentDir = sourceFile.getParent();

        // 压缩保存图片



    }

}
