package com.shop_service.common.utils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 商户号生成工具类
 *
 * @author 啊祖
 * @date 2026-01-14 16:56
 **/
public class ShopNoGeneratorUtil {
    // 前缀
    private static final String PREFIX = "VSPAY";
    // yyyyMMdd
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.BASIC_ISO_DATE;
    // 安全随机
    private static final SecureRandom RND = new SecureRandom();

    /**
     * 生成商户号: VSPAYyyyyMMdd + 6位随机数字
     */
    public static String nextShopNo() {
        String date = LocalDate.now().format(DATE_FMT);
        int n = RND.nextInt(1_000_000); // 0..999999
        return PREFIX + date + String.format("%06d", n);
    }

    /**
     * 自定义随机位数(纯数字)
     * 例如 digits=8 -> VSPAYyyyyMMdd + 8位随机数字
     */
    public static String nextShopNo(int digits) {
        if (digits <= 0 || digits > 18) {
            throw new IllegalArgumentException("digits must be between 1 and 18");
        }
        String date = LocalDate.now().format(DATE_FMT);

        long bound = 1;
        for (int i = 0; i < digits; i++) {
            bound *= 10;
        }

        long n = nextLong(bound);
        return PREFIX + date + String.format("%0" + digits + "d", n);
    }

    private static long nextLong(long bound) {
        // SecureRandom#nextLong(long) 仅在较新 JDK 才有, 这里做兼容实现
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        long r;
        long m = bound - 1;
        // power of two
        if ((bound & m) == 0L) {
            r = RND.nextLong() & m;
            return r;
        }

        long u;
        do {
            u = RND.nextLong() >>> 1;
            r = u % bound;
        } while (u - r + m < 0L);
        return r;
    }
}
