package io.github.hqqich.file;

import com.google.common.collect.Sets;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文件处理工具类
 *
 * @author tsinglink
 */
public class FileUtils {

	private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

	public static String FILENAME_PATTERN = "[a-zA-Z0-9_\\-\\|\\.\\u4e00-\\u9fa5]+";

	///**
	// * 输出指定文件的byte数组
	// *
	// * @param filePath 文件路径
	// * @param os       输出流
	// * @return
	// */
	//public static void writeBytes(String filePath, OutputStream os) throws IOException {
	//	FileInputStream fis = null;
	//	try {
	//		File file = new File(filePath);
	//		if (!file.exists()) {
	//			throw new FileNotFoundException(filePath);
	//		}
	//		fis = new FileInputStream(file);
	//		byte[] b = new byte[1024];
	//		int length;
	//		while ((length = fis.read(b)) > 0) {
	//			os.write(b, 0, length);
	//		}
	//	} catch (IOException e) {
	//		throw e;
	//	} finally {
	//		IOUtils.close(os);
	//		IOUtils.close(fis);
	//	}
	//}
    //
	///**
	// * 写数据到文件中
	// *
	// * @param data 数据
	// * @return 目标文件
	// * @throws IOException IO异常
	// */
	//public static String writeImportBytes(byte[] data) throws IOException {
	//	return writeBytes(data, TsingLinkConfig.getImportPath());
	//}
    //
	///**
	// * 写数据到文件中
	// *
	// * @param data      数据
	// * @param uploadDir 目标文件
	// * @return 目标文件
	// * @throws IOException IO异常
	// */
	//public static String writeBytes(byte[] data, String uploadDir) throws IOException {
	//	FileOutputStream fos = null;
	//	String pathName = "";
	//	try {
	//		String extension = getFileExtendName(data);
	//		pathName = DateUtils.datePath() + "/" + IdUtils.fastUUID() + "." + extension;
	//		File file = FileUploadUtils.getAbsoluteFile(uploadDir, pathName);
	//		fos = new FileOutputStream(file);
	//		fos.write(data);
	//	} finally {
	//		IOUtils.close(fos);
	//	}
	//	return FileUploadUtils.getPathFileName(uploadDir, pathName);
	//}


	// 字符串写入文件中
	public static void writeString(String msg, String filePath) {
		try(FileWriter writer = new FileWriter(filePath)) {
			writer.write(msg);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 删除文件
	 *
	 * @param filePath 文件
	 * @return
	 */
	public static boolean deleteFile(String filePath) {
		return deleteFile(new File(filePath));
	}

	public static boolean deleteFile(File file) {
		boolean flag = false;
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}


	public static void deleteDir(String file) {
        log.info("删除目录：{}", file);
		deleteDir(new File(file));
	}

	/**
	 * 删除文件夹<br>
	 * 删除指定的最后一级目录<br>
	 * 就是会删除母目录
	 *
	 * @param file 传入的文件夹
	 */
	public static void deleteDir(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteDir(files[i]);
			}
		}
		file.delete();
	}


	/**
	 * 零拷贝文件
	 */
	public static void zeroCopyFile(File src, File dest) {
		try (FileChannel srcChannel = new FileInputStream(src).getChannel(); FileChannel destChannel = new FileInputStream(dest).getChannel()) {
			srcChannel.transferTo(0, srcChannel.size(), destChannel);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 文件复制 使用java文件工具类Files
	 *
	 * @param srcFile  源文件
	 * @param destFile 目标文件
	 * @throws IOException IO异常
	 */
	public static void copyByJavaFiles(File srcFile, File destFile) throws IOException {
		Files.copy(srcFile.toPath(), new BufferedOutputStream(Files.newOutputStream(destFile.toPath())));
	}

	/**
	 * 文件复制 使用FileInputStream , FileOutputStream
	 *
	 * @param srcFile  源文件
	 * @param destFile 目标文件
	 * @throws IOException IO异常
	 */
	public static void copyByInOutStream(File srcFile, File destFile) throws IOException {
		byte[] bytes = new byte[24 * 1024];
		try (InputStream in = Files.newInputStream(srcFile.toPath());
			 OutputStream out = Files.newOutputStream(destFile.toPath())) {
			int count;
			while ((count = in.read(bytes)) > 0) {
				out.write(bytes, 0, count);
			}
		}
	}

    public static void copyByInOutStream(File srcFile, File destFile, ProgressListener progressListener) throws IOException {
        // 获取文件总大小
        long totalBytes = srcFile.length();
        long transferredBytes = 0;

        byte[] bytes = new byte[24 * 1024]; // 缓冲区大小为24KB
        try (InputStream in = Files.newInputStream(srcFile.toPath());
                OutputStream out = Files.newOutputStream(destFile.toPath())) {
            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
                transferredBytes += count; // 更新已传输的字节数

                // 调用进度回调接口
                if (progressListener != null) {
                    progressListener.onProgress(totalBytes, transferredBytes);
                }
            }
        }
    }

	/**
	 * 文件复制 使用BufferedInputStream , BufferedOutputStream
	 *
	 * @param srcFile  源文件
	 * @param destFile 目标文件
	 * @throws IOException IO异常
	 */
	public static void copyByBufferedInOutStream(File srcFile, File destFile) throws IOException {
		byte[] bytes = new byte[24 * 1024];
		try (InputStream in = new BufferedInputStream(Files.newInputStream(srcFile.toPath()));
			 OutputStream out = new BufferedOutputStream(Files.newOutputStream(destFile.toPath()))) {
			int count;
			while ((count = in.read(bytes)) > 0) {
				out.write(bytes, 0, count);
			}
		}
	}

	/**
	 * 文件复制 使用nio FileChannel + ByteBuffer
	 *
	 * @param srcFile  源文件
	 * @param destFile 目标文件
	 * @throws IOException IO异常
	 */
	public static void copyByNioByteBuffer(File srcFile, File destFile) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(24 * 1024);
		try (FileChannel inChannel = new FileInputStream(srcFile).getChannel();
			 FileChannel outChannel = new FileOutputStream(destFile).getChannel()) {
			while (inChannel.read(buffer) != -1) {
				buffer.flip();
				while (buffer.hasRemaining()) {
					outChannel.write(buffer);
				}
				buffer.clear();
			}
		}
	}

	/**
	 * 文件复制 使用nio FileChannel + transferTo (超过2g的文件需要分批)
	 *
	 * @param srcFile  源文件
	 * @param destFile 目标文件
	 * @throws IOException IO异常
	 */
	public static void copyByNioTransferTo(File srcFile, File destFile) throws IOException {
		try (FileChannel inChannel = new FileInputStream(srcFile).getChannel();
			 FileChannel outChannel = new FileOutputStream(destFile).getChannel()) {
			long size = inChannel.size();
			long pos = 0;
			long count;
			while (pos < size) {
				count = Math.min(size - pos, 2 * 1024 * 1024 * 1024L - 1);
				// pos += outChannel.transferFrom(inChannel, pos, count);
				pos += inChannel.transferTo(pos, count, outChannel);
			}
		}
	}

	/**
	 * 文件复制 使用common-io 中的FileUtils
	 *
	 * @param srcFile  源文件
	 * @param destFile 目标文件
	 * @throws IOException IO异常
	 */
	public static void copyByCommonIoFileUtils(File srcFile, File destFile) throws IOException {
		org.apache.commons.io.FileUtils.copyFile(srcFile, destFile);
	}


	/**
	 * 复制文件夹
	 */
	public static void copyDir(String sourcePath, String newPath) {
		try {
			(new File(newPath)).mkdirs();
			// 与mkdir()都创建文件夹 ，mkdirs()如果父文件夹不存在也会创建
			File fileList = new File(sourcePath);
			String[] strName = fileList.list();
			File temp = null;//游标
			for (int i = 0; i < strName.length; i++) {
				// 如果源文件路径以分隔符File.separator /或者\结尾那就sourcePath
				if (sourcePath.endsWith(File.separator)) {
					temp = new File(sourcePath + strName[i]);
				} else {
					temp = new File(sourcePath + File.separator + strName[i]);
				}
				if (temp.isFile()) {
					// 如果游标遇到文件
					FileInputStream in = new FileInputStream(temp);
					// 复制且改名
					File file = new File(newPath + File.separator + temp.getName().toString());
					FileOutputStream out = new FileOutputStream(file);
					byte[] buffer = new byte[1024 * 8];
					int length;
					while ((length = in.read(buffer)) != -1) {

						out.write(buffer, 0, length);
					}
					out.flush();
					out.close();
					in.close();
				}
				// 如果游标遇到文件夹
				if (temp.isDirectory()) {
					copyDir(sourcePath + File.separator + strName[i], newPath + File.separator + strName[i]);
				}
			}
		} catch (Exception e) {
			log.info("文件夹复制失败!");
		}
	}


	/**
	 * 文件名称验证
	 *
	 * @param filename 文件名称
	 * @return true 正常 false 非法
	 */
	public static boolean isValidFilename(String filename) {
		return filename.matches(FILENAME_PATTERN);
	}

	/**
	 * 检查文件是否可下载
	 *
	 * @param resource 需要下载的文件
	 * @return true 正常 false 非法
	 */
	public static boolean checkAllowDownload(String resource) {
		// 禁止目录上跳级别
		if (StringUtils.contains(resource, "..")) {
			return false;
		}

		// 检查允许下载的文件规则
		if (ArrayUtils.contains(MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION, FileTypeUtils.getFileType(resource))) {
			return true;
		}

		// 不在允许下载的文件规则
		return false;
	}

	/**
	 * 百分号编码工具方法
	 *
	 * @param s 需要百分号编码的字符串
	 * @return 百分号编码后的字符串
	 */
	public static String percentEncode(String s) throws UnsupportedEncodingException {
		String encode = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
		return encode.replaceAll("\\+", "%20");
	}

	/**
	 * 获取图像后缀
	 *
	 * @param photoByte 图像数据
	 * @return 后缀名
	 */
	public static String getFileExtendName(byte[] photoByte) {
		String strFileExtendName = "jpg";
		if ((photoByte[0] == 71) && (photoByte[1] == 73) && (photoByte[2] == 70) && (photoByte[3] == 56)
				&& ((photoByte[4] == 55) || (photoByte[4] == 57)) && (photoByte[5] == 97)) {
			strFileExtendName = "gif";
		} else if ((photoByte[6] == 74) && (photoByte[7] == 70) && (photoByte[8] == 73) && (photoByte[9] == 70)) {
			strFileExtendName = "jpg";
		} else if ((photoByte[0] == 66) && (photoByte[1] == 77)) {
			strFileExtendName = "bmp";
		} else if ((photoByte[1] == 80) && (photoByte[2] == 78) && (photoByte[3] == 71)) {
			strFileExtendName = "png";
		}
		return strFileExtendName;
	}

	/**
	 * 获取文件名称 /profile/upload/2022/04/16/tsinglink.png -- tsinglink.png
	 *
	 * @param fileName 路径名称
	 * @return 没有文件路径的名称
	 */
	public static String getName(String fileName) {
		if (fileName == null) {
			return null;
		}
		int lastUnixPos = fileName.lastIndexOf('/');
		int lastWindowsPos = fileName.lastIndexOf('\\');
		int index = Math.max(lastUnixPos, lastWindowsPos);
		return fileName.substring(index + 1);
	}

	/**
	 * 获取不带后缀文件名称 /profile/upload/2022/04/16/tsinglink.png -- tsinglink
	 *
	 * @param fileName 路径名称
	 * @return 没有文件路径和后缀的名称
	 */
	public static String getNameNotSuffix(String fileName) {
		if (fileName == null) {
			return null;
		}
		String baseName = FilenameUtils.getBaseName(fileName);
		return baseName;
	}


	/**
	 * 创建路径<br>
	 * "Z:\\a\\b\\c\\d"  =>  会创建到目录d<br>
	 * 非法的不给创建<br>
	 *
	 * @param filePath 路径名称
	 * @return 是否创建成功
	 */
	public static boolean createFilePath(String filePath) {
		File file = new File(filePath);
		if (file.isDirectory()) {
			return true;
		}
		return file.mkdirs();
	}

	public static boolean createFilePath(File file) {
		if (file.isDirectory()) {
			return true;
		}
		return file.mkdirs();
	}

	/**
	 * 批量创建路径<br>
	 * "Z:\\a\\b\\c\\d"  =>  会创建到目录d<br>
	 * 非法的不给创建<br>
	 *
	 * @param filePath 路径名称的集合
	 * @return 是否创建成功
	 */
	public static boolean createFilePath(List<String> filePath) {
		for (String s : filePath) {
			boolean mkdirs = new File(s).mkdirs();
			if (!mkdirs) return false;
		}
		return true;
	}

	/**
	 * 批量创建路径， 返回错误的路径<br>
	 * "Z:\\a\\b\\c\\d"  =>  会创建到目录d<br>
	 * 非法的不给创建<br>
	 *
	 * @param filePath 路径名称的集合
	 * @return 错误的路径 set集合
	 */
	public static Set<String> createFilePathReturnNoCreate(List<String> filePath) {
		Set<String> result = Sets.newHashSet();
		for (String s : filePath) {
			boolean mkdirs = new File(s).mkdirs();
			if (!mkdirs) result.add(s);
		}
		return result;
	}

	public static String[] readTxtToArray(File file) {

		StringBuilder str = new StringBuilder();
		try (FileReader fr = new FileReader(file);) {
			int i;
			while ((i = fr.read()) != -1)
				str.append((char) i);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String string = str.toString();
		if (string.contains("\r\n")) {
			return str.toString().split("\r\n");
		} else if (string.contains("\r")) {
			return str.toString().split("\r");
		} else if (string.contains("\n")) {
			return str.toString().split("\n");
		} else {
			return new String[]{string};
		}


	}


	/**
	 * 获取文件夹下面的所有文件,<br>
	 *
	 * @param directoryPath 目录路径字符串
	 * @return [G:\images\imgSuccessNoMarker\1.jpg, G:\images\imgSuccessNoMarker\10.jpg]
	 * @author chenhao
	 * @date 2022/11/4 10:22
	 **/
	public static List<String> geDirectorytFile(String directoryPath) {
		final List<String> result = new ArrayList<>();
		try {
			final AtomicInteger dircount = new AtomicInteger();
			final AtomicInteger filecount = new AtomicInteger();
			Files.walkFileTree(Paths.get(directoryPath),new SimpleFileVisitor<Path>(){
				//进入文件夹触发
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					////System.out.println("=================="+dir);
					dircount.incrementAndGet();
					return super.preVisitDirectory(dir, attrs);
				}
				//进入文件触发
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					result.add(file.toString());
					filecount.incrementAndGet();
					return super.visitFile(file, attrs);
				}
			});
			log.info("文件夹数量：{}文件数量：{}", dircount, filecount);
		} catch (IOException e) {
			log.info("读取文件夹失败, 文件夹不存在");
		}
		return result;
	}



	// 获取文件大小
	public static String getFileSize(String file) {
		File imageFile = new File(file);
		if (!imageFile.isFile()) {
			return null;
		}
		long size = org.apache.commons.io.FileUtils.sizeOf(imageFile);
		//return org.apache.commons.io.FileUtils.byteCountToDisplaySize(size);
		return readableFileSize(size);
	}


	public static String readableFileSize(long size) {
		if (size <= 0) return "0";
		final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}





	/**
	 * /home/tsinglink/models/162/20221125063027<br>
	 * 获取<br>
	 * 20221125063027<br>
	 * @param fullpath
	 * @return
	 * @author: chenhao
	 * @date: 2022/11/26 17:02
	 **/
	public static String getLastStrByPath(String fullpath) {

		//String fullpath = "/home/tsinglink/models/162/20221125063027";

		if (StringUtils.isEmpty(fullpath)) {
			return DateUtils.getTime();
		}

		StringBuilder stringBuilder = new StringBuilder();

		try {
			String[] split = fullpath.split(File.separator);
			int length = split.length;
			stringBuilder.append(split[length-1]);
		} catch (Exception e) {
			try {
				String[] split = fullpath.split("/");
				int length = split.length;
				stringBuilder.append(split[length-1]);
			} catch (Exception exception) {
				String[] split = fullpath.split("\\\\");
				int length = split.length;
				stringBuilder.append(split[length-1]);
			}
		}


		return stringBuilder.toString();

	}

	// 获取指定目录下文件的个数
	public static int getFileCount(String imgsPath) {
		File file = new File(imgsPath);
		if (file.exists()) {
			return file.list().length;
		}
		return 0;
	}

    // 获取指定目录下及其子目录文件的个数
    public static int countFiles(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Provided path is not a valid directory: " + directoryPath);
        }
        return countFilesInDirectory(directory);
    }

    private static int countFilesInDirectory(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    count++; // 计数文件
                } else if (file.isDirectory()) {
                    count += countFilesInDirectory(file); // 递归计数子目录中的文件
                }
            }
        }
        return count;
    }



    // 传入 一个 File  替换文件的后缀为 参数2   返回替换后的文件
    public static File replaceFileSuffix(File file, String suffix) {
        String fileName = file.getName();
        String newFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "." + suffix;
        return new File(file.getParent(), newFileName);
    }



    public static Integer byteToMb(long dataSetSize) {
        return (int) (dataSetSize / 1024 / 1024);
    }

    @NotNull
    public static Path createTmpDir() {

        String tmpdir = System.getProperty("java.io.tmpdir");  // 临时目录

        Path tsinglink = Paths.get(tmpdir, "tsinglink", UUID.randomUUID().toString());

        createFilePath(tsinglink.toFile());

        return tsinglink;
    }
}
