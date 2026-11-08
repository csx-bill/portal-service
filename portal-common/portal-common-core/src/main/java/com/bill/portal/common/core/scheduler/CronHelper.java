package com.bill.portal.common.core.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;

import java.time.*;
import java.util.Optional;

/**
 * Cron 时间计算工具 —— 纯函数，可直接单测。
 */
@Slf4j
public final class CronHelper {

    private CronHelper() {}

    /**
     * 根据调度类型计算下次执行时间。
     *
     * @param type           调度类型
     * @param cronExpression Cron 表达式（仅 CRON 类型需要）
     * @param baseTime       基准时间（LocalDateTime，对应 DB DATETIME）
     * @param zone           时区
     * @return 下次执行时间（Instant，用于计算 delay 毫秒数）
     */
    public static Optional<Instant> computeNext(
            ScheduleType type,
            String cronExpression,
            LocalDateTime baseTime,
            ZoneId zone
    ) {
        LocalDateTime now = LocalDateTime.now(zone);

        return switch (type) {
            case IMMEDIATE -> Optional.empty();
            case ONCE      -> Optional.empty();
            case DAILY     -> Optional.of(nextDaily(baseTime, now, zone));
            case WEEKLY    -> Optional.of(nextWeekly(baseTime, now, zone));
            case MONTHLY   -> Optional.of(nextMonthly(baseTime, now, zone));
            case CRON      -> nextByCron(cronExpression, now, zone);
        };
    }

    // ────── 每天：基准时间 + N 天，跳过已过期 ──────

    static Instant nextDaily(LocalDateTime baseTime, LocalDateTime now, ZoneId zone) {
        LocalDateTime next = LocalDateTime.of(now.toLocalDate(), baseTime.toLocalTime());
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }
        return next.atZone(zone).toInstant();
    }

    // ────── 每周：基准时间的星期几 + N 周 ──────

    static Instant nextWeekly(LocalDateTime baseTime, LocalDateTime now, ZoneId zone) {
        DayOfWeek targetDay = baseTime.getDayOfWeek();
        ZonedDateTime next = ZonedDateTime.of(now.toLocalDate(), baseTime.toLocalTime(), zone);
        while (!next.isAfter(now.atZone(zone)) || next.getDayOfWeek() != targetDay) {
            next = next.plusDays(1);
        }
        return next.toInstant();
    }

    // ────── 每月：基准时间的几号 + N 月（处理月末溢出） ──────

    static Instant nextMonthly(LocalDateTime baseTime, LocalDateTime now, ZoneId zone) {
        int targetDay = Math.min(baseTime.getDayOfMonth(), now.toLocalDate().lengthOfMonth());

        ZonedDateTime next = ZonedDateTime.of(
            LocalDateTime.of(now.getYear(), now.getMonth(), targetDay,
                             baseTime.getHour(), baseTime.getMinute(), baseTime.getSecond()),
            zone
        );

        if (!next.isAfter(now.atZone(zone))) {
            next = next.plusMonths(1);
            int maxDay = next.toLocalDate().lengthOfMonth();
            if (targetDay > maxDay) {
                next = next.withDayOfMonth(maxDay); // 例：1月31日 → 2月28日
            }
        }
        return next.toInstant();
    }

    // ────── Cron 表达式 ──────

    static Optional<Instant> nextByCron(String cronExpr, LocalDateTime now, ZoneId zone) {
        if (cronExpr == null || cronExpr.isBlank()) return Optional.empty();
        try {
            CronExpression cron = CronExpression.parse(cronExpr);
            LocalDateTime next = cron.next(now);
            return Optional.ofNullable(next).map(lt -> lt.atZone(zone).toInstant());
        } catch (Exception e) {
            log.error("Cron 表达式解析失败: '{}'", cronExpr, e);
            return Optional.empty();
        }
    }

    /** 安全上限（防 NTP 回拨） */
    public static long maxDelayMs(ScheduleType type) {
        return switch (type) {
            case DAILY   -> Duration.ofDays(2).toMillis();
            case WEEKLY  -> Duration.ofDays(8).toMillis();
            case MONTHLY -> Duration.ofDays(32).toMillis();
            case CRON    -> Duration.ofDays(367).toMillis();
            default      -> Duration.ofDays(2).toMillis();
        };
    }
}
