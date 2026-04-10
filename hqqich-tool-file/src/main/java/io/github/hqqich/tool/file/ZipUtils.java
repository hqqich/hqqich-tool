package io.github.hqqich.tool.file;

import java.io.File;
import java.io.IOException;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

public class ZipUtils {

    private static void addFolderToZip(ZipFile zip, File folder, ZipParameters parameters) throws IOException {
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    addFolderToZip(zip, file, parameters);
                } else {
                    zip.addFile(file, parameters);
                }
            }
        } else {
            zip.addFile(folder, parameters);
        }
    }


    public static void zipDirectoryWithPassword(String directoryPath, String zipFilePath, String password) throws Exception {

        System.out.println("开始压缩目录: " + directoryPath);
        System.out.println("目标文件: " + zipFilePath);
        System.out.println("密码: " + password);

        // 创建ZipFile对象
        ZipFile zipFile = new ZipFile(zipFilePath);

        // 设置密码
        zipFile.setPassword(password.toCharArray());

        // 设置压缩参数, 这些参数都会影响压缩时间
        ZipParameters zipParameters = new ZipParameters();

        /*
        设置 ZIP 文件的压缩算法。
            CompressionMethod.DEFLATE：使用 DEFLATE 算法（标准 ZIP 压缩算法，平衡压缩率和速度）。
            其他可选值（如 CompressionMethod.STORE 表示不压缩，仅存储）。
         */
        zipParameters.setCompressionMethod(CompressionMethod.STORE);
                /*
        设置压缩级别，控制压缩速度和压缩率的权衡。
            CompressionLevel.NORMAL：默认压缩级别（通常为 6，范围 0-9，0 表示不压缩，9 表示最高压缩）。
其他可选值：
CompressionLevel.FASTEST（低压缩率，快速）
CompressionLevel.ULTRA（高压缩率，慢速）
         */
        zipParameters.setCompressionLevel(CompressionLevel.FASTEST);
        /*
        设置是否对文件进行加密。
            true：启用加密。
            false：不加密。
         */
        zipParameters.setEncryptFiles(true);
        /*
        设置加密方法。
            EncryptionMethod.ZIP_STANDARD：使用标准 ZIP 加密方法（较弱的加密）。
            其他可选值（如 EncryptionMethod.AES）提供更强的加密。
         */
        zipParameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);

        // 获取要压缩的目录
        File directoryToZip = new File(directoryPath);

        // 获取总文件数量
        int totalFiles = countFiles(directoryToZip);
        System.out.println("总文件数量: " + totalFiles);

        // 递归添加目录下的所有文件和子目录
        addDirectoryToZip(directoryToZip, directoryToZip, zipFile, zipParameters, totalFiles, new int[]{0});
    }

    private static void addDirectoryToZip(File rootDirectory, File currentDirectory,
            ZipFile zipFile, ZipParameters zipParameters, int totalFiles, int[] processedFiles) throws Exception {
        File[] files = currentDirectory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 如果是目录，递归处理
                addDirectoryToZip(rootDirectory, file, zipFile, zipParameters, totalFiles, processedFiles);
            } else {
                // 计算相对路径
                String relativePath = getRelativePath(rootDirectory, file);

                // 添加文件到ZIP
                zipFile.addFile(file, zipParameters);

                // 更新已处理文件数量并打印进度
                processedFiles[0]++;
                double progress = (processedFiles[0] / (double) totalFiles) * 100;
                System.out.printf("已处理文件: %d/%d (%.2f%%)\n", processedFiles[0], totalFiles, progress);
            }
        }
    }


    private static int countFiles(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return 0;
        }

        int count = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                count += countFiles(file);
            } else {
                count++;
            }
        }
        return count;
    }

    private static String getRelativePath(File rootDirectory, File file) {
        String rootPath = rootDirectory.getAbsolutePath();
        String filePath = file.getAbsolutePath();

        if (!filePath.startsWith(rootPath)) {
            return file.getName();
        }

        return filePath.substring(rootPath.length() + 1);
    }

}
