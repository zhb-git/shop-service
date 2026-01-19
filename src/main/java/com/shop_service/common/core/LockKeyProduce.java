package com.shop_service.common.core;

import com.shop_service.common.constant.LockServiceType;

/**
 * 锁键生成器
 *
 * @author 啊祖
 * @date 2026-01-12 14:55
 **/
public class LockKeyProduce {
    /**
     * 生成锁键
     *
     * @param type 业务类型
     * @param ids  业务编号(可变参数, 会用 ":" 拼接)
     * @return 锁键
     */
    public static String produce(LockServiceType type, Object... ids) {
        String prefix = type.getValue();

        if (ids == null || ids.length == 0) {
            return prefix;
        }

        StringBuilder sb = new StringBuilder(prefix);
        for (Object id : ids) {
            sb.append(':').append(id == null ? "null" : id.toString());
        }
        return sb.toString();
    }
}
