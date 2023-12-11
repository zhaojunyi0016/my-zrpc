package com.my.rpc.utils;


import java.util.concurrent.atomic.LongAdder;

/**
 * 雪花算法
 * 机房号 5bit 32 位
 * 机器号 5bit 32 位
 * 时间戳 本质是 long 1970到当前时间的一个毫秒数, 原本是 64bit, 太长了 ,只用 42 就可以了, 选择一个比较近的时候, 比如公司成立的日期
 * <p>
 * 当同一个机房,同一个机器上并发很大的时候, 时间戳可能是一样的, 所以还需要一个序列号
 * 序列号 12bit
 * 最终长度 5 + 5 + 42 + 12 = 64bit
 */
public class SnowflakeIdGenerator {

    // 起始时间戳
    public static final long START_STAMP = DateUtil.get("2022-12-01").getTime();

    // 机房号
    public static final long DATA_CENTER_BIT = 5L;

    // 机器号
    public static final long MACHINE_BIT = 5L;

    // 序列化
    public static final long SEQUENCE_BIT = 12L;

    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BIT);

    // 时间戳 （42） 机房号 （5） 机器号 （5） 序列号 （12）
    // 101010101010101010101010101010101010101011 10101 10101 101011010101


    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT +
            SEQUENCE_BIT;
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;
    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();
    // 时钟回拨的问题，我们需要去处理
    private long lastTimeStamp = -1L;

    public SnowflakeIdGenerator(long dataCenterId, long machineId) {
        // 判断传世的参数是否合法
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX) {
            throw new IllegalArgumentException("你传入的数据中心编号或机器号不合法.");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId() {
        // 第一步：处理时间戳的问题
        long currentTime = System.currentTimeMillis();
        long timeStamp = currentTime - START_STAMP;
        // 判断时钟回拨
        if (timeStamp < lastTimeStamp) {
            throw new RuntimeException("您的服务器进行了时钟回调.");
        }
        // sequenceId需要做一些处理，如果是同一个时间节点，必须自增
        if (timeStamp == lastTimeStamp) {
            sequenceId.increment();
            if (sequenceId.sum() >= SEQUENCE_MAX) {
                timeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
        } else {
            sequenceId.reset();
        }
        // 执行结束将时间戳赋值给lastTimeStamp
        lastTimeStamp = timeStamp;
        long sequence = sequenceId.sum();
        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT
                | machineId << MACHINE_LEFT | sequence;
    }

    private long getNextTimeStamp() {
        // 获取当前的时间戳
        long current = System.currentTimeMillis() - START_STAMP;
        // 如果一样就一直循环，直到下一个时间戳
        while (current == lastTimeStamp) {
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }

    public static void main(String[] args) {
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 2);
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> System.out.println(snowflakeIdGenerator.getId())).start();
        }
    }
}