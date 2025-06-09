package com.ruerpc;

import com.ruerpc.DateUtil;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author Rue
 * @date 2025/6/6 10:45
 *
 * 唯一请求id生成器
 * 5bit 机房号(数据中心)
 * 5bit 机器号
 * 42bit 自定义起始日期的时间戳
 * 12bit 序列号
 * 共64bit
 */
public class IdGenerator {

    public static final long START_STAMP = DateUtil.get("2022-1-1").getTime();

    public static final long DATA_CENTER_BIT = 5L;
    public static final long MACHINE_BIT = 5L;
    public static final long SEQUENCE_BTI = 12L;

    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long SEQUENCE_MAX = ~(-1L << SEQUENCE_BTI);

    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT + MACHINE_BIT + SEQUENCE_BTI;
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BTI;
    public static final long MACHINE_LEFT = SEQUENCE_BTI;

    private long dataCenterId;
    private long machineId;
    private LongAdder sequenceId = new LongAdder();
    private long lastTimeStamp = -1L;

    public IdGenerator(long dataCenterId, long machineId) {
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX) {
            throw new IllegalArgumentException("illegal data center or machine id. --rue");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId() {
        long currentTime = System.currentTimeMillis();

        long personalizedTimeStamp = currentTime - START_STAMP;

        //todo 时钟回拨
        if (personalizedTimeStamp < lastTimeStamp) {
            throw new RuntimeException("The server experienced a clock callback. --rue");
        }

        //sequenceId如果在同一个时间点生成，则必须自增
        if (personalizedTimeStamp == lastTimeStamp) {
            sequenceId.increment();
            if (sequenceId.sum() >= SEQUENCE_MAX) {
                personalizedTimeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
        } else {
            sequenceId.reset();
        }
        //执行结束赋值给新的lastTimeStamp
        lastTimeStamp = personalizedTimeStamp;

        long sequence = sequenceId.longValue();
        return personalizedTimeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT
                | machineId << MACHINE_LEFT | sequence;
    }

    private long getNextTimeStamp() {
        long current = System.currentTimeMillis() - START_STAMP;
        while (current == lastTimeStamp) {
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }


}
