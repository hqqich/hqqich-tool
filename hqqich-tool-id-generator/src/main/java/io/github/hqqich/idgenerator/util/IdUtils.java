package io.github.hqqich.idgenerator.util;

import io.github.hqqich.idgenerator.contract.IIdGenerator;
import io.github.hqqich.idgenerator.contract.IdGeneratorException;
import io.github.hqqich.idgenerator.contract.IdGeneratorOptions;
import io.github.hqqich.idgenerator.idgen.DefaultIdGenerator;
import java.util.UUID;

/**
 * ID生成器工具类
 *
 * @author hqqich
 */
public class IdUtils {

    // 雪花算法
    private static final SnowflakeIdWorker SNOWFLAKE_ID_WORKER = new SnowflakeIdWorker(0, 0);

    // 自定义id生成器
    private static IIdGenerator idGenInstance = null;

    public static IIdGenerator getIdGenInstance() {
        return idGenInstance;
    }

    /**
     * 设置参数，建议程序初始化时执行一次
     */
    public static void setIdGenerator(IdGeneratorOptions options) throws IdGeneratorException {
        idGenInstance = new DefaultIdGenerator(options);
    }

    /**
     * 生成新的Id
     * 调用本方法前，请确保调用了 SetIdGenerator 方法做初始化。
     *
     * @return id
     */
    public static long nextId() throws IdGeneratorException {
        if (idGenInstance == null){
            throw new IdGeneratorException("Please initialize Yitter.IdGeneratorOptions first.");
        }
        return idGenInstance.newLong();
    }


    /**
	 * 获取随机UUID
	 *
	 * @return 随机UUID
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString();
	}

    /**
     * 获取 推特雪花算法下的 uuid
     *
     * @return 18位的uuid，long类型
     * @see SnowflakeIdWorker
     */
    public static long getSnowflakeUUID() {
        return SNOWFLAKE_ID_WORKER.nextId();
    }


    /**
     * 获取 推特雪花算法下的 uuid
     *
     * @return 10位的uuid，int类型
     * @see SnowflakeIdWorker
     */
    public static int getSnowflakeUUIDToInt() {
        return (int) SNOWFLAKE_ID_WORKER.nextId();
    }

}
