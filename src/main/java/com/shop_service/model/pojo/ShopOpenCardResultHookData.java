package com.shop_service.model.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户开卡结果回调数据
 * <p>
 * 开卡接口调用, 银行出结果后回调
 * @author 啊祖
 * @date 2026-01-15 15:01
 **/
@Data
public class ShopOpenCardResultHookData {
    /**
     * 开卡对应的交易ID
     */
    private String transactionId;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 卡片信息
     * 开卡成功才存在
     */
    private CardInfo cardInfo;

    @Data
    public static class CardInfo {
        /**
         * 卡片ID
         */
        private String cardId;

        /**
         * 卡头ID
         */
        private Long cardBinId;

        /**
         * 卡头
         */
        private String cardBin;

        /**
         * 完整卡号
         */
        private String cardNo;

        /**
         * 卡片CVV
         */
        private String cvv;

        /**
         * 卡片过期时间
         */
        private String expireDate;

        /**
         * 卡片币种
         */
        private String currency;

        /**
         * 卡片充值费率
         */
        private BigDecimal fee;

        /**
         * 持卡人姓名
         */
        private String holderUsername;

        /**
         * 持卡人邮箱
         */
        private String holderEmail;

        /**
         * 持卡人账单地址
         */
        private HolderAddress holderAddress;

        /**
         * 卡片状态
         */
        private Integer status;

        /**
         * 银行返回的开卡时间
         */
        private LocalDateTime bankCreateTime;

        @Data
        public static class HolderAddress {
            /**
             * 城市名称
             * 示例: Beijing, Shanghai, Berkeley
             */
            private String city;

            /**
             * 州或省或行政区
             * 示例:
             * - 中国: 省或直辖市
             * - 美国: 州代码, 如 CA, NY
             */
            private String state;

            /**
             * 国家代码
             * 使用 ISO 3166-1 Alpha-2 标准
             * 示例: CN, US
             */
            private String country;

            /**
             * 邮政编码
             * 示例:
             * - 中国: 400000
             * - 美国: 94709
             */
            private String postalCode;

            /**
             * 详细地址第一行
             * 通常包含街道名和门牌号
             * 示例: 1725 Oxford Street
             */
            private String addressLine1;

            /**
             * 详细地址第二行
             * 可选字段
             * 通常用于公寓号, 楼层, 单元号等补充信息
             */
            private String addressLine2;
        }
    }
}
