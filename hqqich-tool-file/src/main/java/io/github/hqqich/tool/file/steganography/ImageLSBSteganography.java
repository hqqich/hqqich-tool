package io.github.hqqich.tool.file.steganography;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by ChenHao on 2025-07-10 is 23:12.<br>
 * <h1>基本概念</h1>
 * LSB 方法：修改像素颜色的最低有效位来隐藏信息，对图片视觉影响最小<br>
 * 本示例：仅处理 PNG 格式（无损压缩），将文本隐藏到图片的 RGB 通道中<br>
 *
 * @author hqqich
 */

public class ImageLSBSteganography {

    /**
     * 隐藏文本到图片
     * - 将文本转换为 UTF-8 字节数组
     * - 遍历图片的每个像素的 RGB 通道
     * - 用秘密数据的每一位替换通道值的最低有效位
     * - 前4个字节存储文本长度（小端序）
     *
     * <h3>注意⚠️点</h3>
     * 容量限制：
     * 每个字节需要8个像素通道（RGB共3通道，所以约每2.67个像素存储1个字节）
     * 图片太小会导致无法隐藏大量数据
     * @param image         图片
     * @param secretText    需要隐写的文本
     **/
    public static BufferedImage encodeText(BufferedImage image, String secretText) {
        // 将文本转换为字节数组
        byte[] secretBytes = secretText.getBytes(StandardCharsets.UTF_8);
        int secretLength = secretBytes.length;

        // 检查图片容量是否足够
        // 每个字节需要8个像素通道（3通道RGB），前4字节存储长度
        int requiredPixels = (secretLength * 8) / 3 + 1;
        if (requiredPixels > image.getWidth() * image.getHeight()) {
            throw new IllegalArgumentException("图片太小，无法隐藏这么多信息");
        }

        int width = image.getWidth();
        int byteIndex = 0;
        int bitIndex = 0;
        boolean writingLength = true;
        int lengthBytesWritten = 0;

        // 遍历所有像素
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                for (int channel = 0; channel < 3; channel++) {
                    // 获取当前通道的值（0-255）
                    int shift = (channel == 0) ? 16 : (channel == 1) ? 8 : 0;
                    int channelValue = (pixel >> shift) & 0xFF;

                    // 清除最低有效位
                    channelValue = channelValue & 0xFE;

                    int bitToWrite = 0;

                    if (writingLength) {
                        // 先写入4字节的长度信息（大端序）
                        if (lengthBytesWritten < 4) {
                            bitToWrite = (secretLength >> (24 - (8 * lengthBytesWritten))) & 1;
                            lengthBytesWritten++;
                        } else {
                            writingLength = false;
                        }
                    }

                    if (!writingLength && byteIndex < secretLength) {
                        // 写入秘密数据
                        bitToWrite = (secretBytes[byteIndex] >> (7 - bitIndex)) & 1;
                        bitIndex++;

                        if (bitIndex == 8) {
                            bitIndex = 0;
                            byteIndex++;
                        }
                    }

                    // 设置最低有效位
                    channelValue |= bitToWrite;

                    // 将修改后的通道值写回像素
                    pixel = (pixel & ~(0xFF << shift)) | (channelValue << shift);
                }
                image.setRGB(x, y, pixel);

                // 如果已经写完所有数据，提前退出
                if (!writingLength && byteIndex >= secretLength) {
                    return image;
                }
            }
        }
        return image;
    }


    /**
     * 从图片中提取文本
     * - 读取图片像素
     * - 从每个通道的最低有效位提取数据
     * - 前4个字节组合成文本长度
     * - 读取指定长度的字节并转换为字符串
     * @param image 图片
     **/
    public static String decodeText(BufferedImage image) {
        StringBuilder binaryData = new StringBuilder();
        int width = image.getWidth();
        int bitIndex = 0;
        byte currentByte = 0;
        boolean readingLength = true;
        int textLength = 0;
        int lengthBytesRead = 0;
        int bytesRead = 0;

        outerLoop:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                for (int channel = 0; channel < 3; channel++) {
                    // 获取通道值
                    int shift = (channel == 0) ? 16 : (channel == 1) ? 8 : 0;
                    int channelValue = (pixel >> shift) & 0xFF;

                    // 提取最低有效位
                    int bit = channelValue & 1;

                    if (readingLength) {
                        // 先读取4字节的长度信息（大端序）
                        if (lengthBytesRead < 4) {
                            textLength = (textLength << 1) | bit;
                            lengthBytesRead++;

                            // 如果已经读完4个长度字节
                            if (lengthBytesRead == 4) {
                                readingLength = false;
                                if (textLength == 0) {
                                    break outerLoop; // 没有隐藏数据
                                }
                            }
                        }
                    } else {
                        // 读取秘密数据
                        currentByte = (byte) ((currentByte << 1) | bit);
                        bitIndex++;

                        if (bitIndex == 8) {
                            if (bytesRead >= textLength) {
                                break outerLoop;
                            }
                            binaryData.append((char) (currentByte & 0xFF));
                            currentByte = 0;
                            bitIndex = 0;
                            bytesRead++;
                        }
                    }
                }
            }
        }
        return binaryData.toString();
    }


    public static void main(String[] args) {


        try {
            // 1. 读取原始图片
            BufferedImage originalImage = ImageIO.read(new File("./public/original.png"));

            // 2. 要隐藏的文本
            String secretMessage = "这是一个秘密消息！Hello, Steganography! 1234567890";

            // 3. 隐藏文本到图片
            BufferedImage stegoImage = encodeText(originalImage, secretMessage);

            // 4. 保存隐写图片
            File output = new File("./public/stego.png");
            ImageIO.write(stegoImage, "png", output);
            System.out.println("文本已隐藏到 stego.png");

            // 5. 从隐写图片中提取文本
            BufferedImage imageToDecode = ImageIO.read(output);
            String decodedMessage = decodeText(imageToDecode);
            System.out.println("提取的秘密消息: " + decodedMessage);
            System.out.println("解码结果长度: " + decodedMessage.length());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
