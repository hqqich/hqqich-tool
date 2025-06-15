package io.github.hqqich.tool.idgenerator;

import io.github.hqqich.tool.idgenerator.contract.IdGeneratorOptions;
import io.github.hqqich.tool.idgenerator.util.IdUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author suzhenyu
 * @date 2021/9/27
 */
// 测试方法平均执行时间
@BenchmarkMode({Mode.All})
// 输出结果的时间粒度为微秒
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
//@Threads(2)
public class StartUpJmh2 {

  //1-漂移算法，2-传统算法
  final static short method = 2;

  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder().include(StartUpJmh2.class.getName() + ".*")
        .warmupIterations(1).measurementIterations(5).forks(2).build();
    new Runner(options).run();
  }

  /**
   * setup初始化容器的时候只执行一次
   */
  @Setup(Level.Trial)
  public void init() {
    IdGeneratorOptions options = new IdGeneratorOptions();
    options.WorkerIdBitLength = 6;
    options.SeqBitLength = 10;
    options.BaseTime = System.currentTimeMillis();
    options.Method = method;
    options.WorkerId = 1;

    // 首先测试一下 IdHelper 方法，获取单个Id
    IdUtils.setIdGenerator(options);
    IdUtils.nextId();
  }

  @Benchmark
  public void testGetPojo() {

    IdUtils.nextId();
  }
}
