package io.github.hqqich.tool.idgenerator.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hqqich 序列生成类
 */
public class Seq {

	// 通用序列类型
	public static final String commSeqType = "COMMON";

	// 上传序列类型
	public static final String uploadSeqType = "UPLOAD";

	// 通用接口序列数
	private static AtomicInteger commSeq = new AtomicInteger(1);

	// 上传接口序列数
	private static AtomicInteger uploadSeq = new AtomicInteger(1);

	// 机器标识
	private static String machineCode = "A";

	/**
	 * 获取通用序列号
	 *
	 * @return 序列值
	 */
	public static String getId() {
		return getId(commSeqType);
	}

	/**
	 * 默认16位序列号 yyMMddHHmmss + 一位机器标识 + 3长度循环递增字符串
	 *
	 * @return 序列值
	 */
	public static String getId(String type) {
		AtomicInteger atomicInt = commSeq;
		if (uploadSeqType.equals(type)) {
			atomicInt = uploadSeq;
		}
		return getId(atomicInt, 3);
	}

	/**
	 * 通用接口序列号 yyMMddHHmmss + 一位机器标识 + length长度循环递增字符串
	 *
	 * @param atomicInt 序列数
	 * @param length    数值长度
	 * @return 序列值
	 */
	public final static String getId(AtomicInteger atomicInt, int length) {

        // 获取当前时间
        LocalDateTime currentDateTime = LocalDateTime.now();
        // 创建DateTimeFormatter对象并指定格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        // 将当前时间格式化为指定格式的字符串
        String result = currentDateTime.format(formatter);
		result += machineCode;
		result += getSeq(atomicInt, length);
		return result;
	}

	/**
	 * 序列循环递增字符串[1, 10 的 (length)幂次方), 用0左补齐length位数
	 *
	 * @return 序列值
	 */
	private synchronized static String getSeq(AtomicInteger atomicInt, int length) {
		// 先取值再+1
		int value = atomicInt.getAndIncrement();

		// 如果更新后值>=10 的 (length)幂次方则重置为1
		int maxSeq = (int) Math.pow(10, length);
		if (atomicInt.get() >= maxSeq) {
			atomicInt.set(1);
		}
		// 转字符串，用0左补齐
		return padl(atomicInt.toString(), length, '0');
	}

    /**
     * 字符串左补齐。如果原始字符串s长度大于size，则只保留最后size个字符。
     *
     * @param s    原始字符串
     * @param size 字符串指定长度
     * @param c    用于补齐的字符
     * @return 返回指定长度的字符串，由原字符串左补齐或截取得到。
     */
    private static final String padl(final String s, final int size, final char c) {
        final StringBuilder sb = new StringBuilder(size);
        if (s != null) {
            final int len = s.length();
            if (s.length() <= size) {
                for (int i = size - len; i > 0; i--) {
                    sb.append(c);
                }
                sb.append(s);
            } else {
                return s.substring(len - size, len);
            }
        } else {
            for (int i = size; i > 0; i--) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
