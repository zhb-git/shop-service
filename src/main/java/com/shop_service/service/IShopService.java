package com.shop_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop_service.common.constant.ShopFundDetailType;
import com.shop_service.model.entity.Shop;
import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.model.pojo.TronAddressControlTransfer;
import com.shop_service.model.request.*;
import com.shop_service.model.response.AdminShopVo;
import com.shop_service.model.response.RespPage;
import com.shop_service.model.response.ShopVo;

import java.math.BigDecimal;

/**
 * 商户服务层
 *
 * @author 啊祖
 * @date 2026-01-14 16:28
 **/
public interface IShopService extends IService<Shop> {
    /**
     * 创建商户
     * @param query 参数
     */
    void create(AdminCreateShopQuery query);

    /**
     * 分页查询
     * @param query 参数
     * @return 结果
     */
    RespPage<AdminShopVo> getAdminShopVoPage(AdminShopPageQuery query);

    /**
     * 更新商户
     * @param query 参数
     */
    void update(AdminUpdateShopQuery query);

    /**
     * 重置商户公钥
     * @param query 参数
     */
    void resetShopPublicKey(AdminResetShopPublicKeyQuery query);

    /**
     * 重置商户回调签名密钥
     * @param query 参数
     */
    void resetShopCallbackSecret(AdminResetCallbackSecretQuery query);

    /**
     * 认证
     * @param ip        ip地址
     * @param shopNo    商户号
     * @param publicKey 公钥
     * @return 下游服务信息
     */
    ShopInfo authentication(String ip, String shopNo, String publicKey);

    /**
     * 获取商户信息
     * @param shopId 商户ID
     * @return 商户信息
     */
    ShopVo getShopVo(Long shopId);

    /**
     * 获取商户信息
     * @param shopId 商户ID
     * @return 商户信息
     */
    ShopInfo getShopInfoById(Long shopId);

    /**
     * 获取商户信息
     * @param shopNo 商户号
     * @return 商户信息
     */
    ShopInfo getShopInfoByNo(String shopNo);

    /**
     * 商户充值地址转账事件处理
     * @param transfer 转账
     */
    void rechargeAddressTransfer(TronAddressControlTransfer transfer);

    /**
     * 添加商户余额
     * @param shopId         商户ID
     * @param amount         金额
     * @param fundDetailType 资金明细类型
     * @param bizNo          业务单号
     * @param remark         备注
     */
    void addBalance(Long shopId, BigDecimal amount, ShopFundDetailType fundDetailType, String bizNo, String remark);

    /**
     * 扣除商户余额
     * @param shopId         商户ID
     * @param amount         金额
     * @param fundDetailType 资金明细类型
     * @param bizNo          业务单号
     * @param remark         备注
     */
    void subBalance(Long shopId, BigDecimal amount, ShopFundDetailType fundDetailType, String bizNo, String remark);
}
