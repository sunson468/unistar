package com.up1234567.unistar.central.service.base.impl;

import com.up1234567.unistar.central.support.auth.AuthToken;
import com.up1234567.unistar.central.service.base.IBaseCacheService;
import com.up1234567.unistar.central.support.cache.IUnistarCache;
import com.up1234567.unistar.common.util.DateUtil;
import com.up1234567.unistar.common.util.JsonUtil;
import com.up1234567.unistar.common.util.SecurityUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class BaseCacheService implements IBaseCacheService {

    @Autowired
    private IUnistarCache unistarCache;

    /**
     * 生成TokenId并保存Token
     *
     * @param token
     * @return
     */
    public String genLoginToken(AuthToken token) {
        String accountTokenKey = String.format(CK_TOKEN_A_T, token.getAccount());
        // 先移除
        String oldTokenAccountKey = unistarCache.get(accountTokenKey);
        if (StringUtils.isNotEmpty(oldTokenAccountKey)) {
            unistarCache.del(oldTokenAccountKey);
        }
        //
        String tokenJson = JsonUtil.toJsonString(token);
        String tokenId = SecurityUtil.md5(tokenJson + DateUtil.now());
        String tokenAccountKey = String.format(CK_TOKEN_T_A, tokenId);
        unistarCache.set(tokenAccountKey, tokenJson, TOKEN_INVALID_MINUTE);
        unistarCache.set(accountTokenKey, tokenId);
        return tokenId;
    }

    /**
     * 根据TokenId获取登录信息
     *
     * @param tokenId
     * @return
     */
    public AuthToken fromTokenId(String tokenId) {
        String tokenAccountKey = String.format(CK_TOKEN_T_A, tokenId);
        String tokenAccount = unistarCache.get(tokenAccountKey);
        if (StringUtils.isEmpty(tokenAccount)) return null;
        return JsonUtil.toClass(tokenAccount, AuthToken.class);
    }

    /**
     * 重新激活
     *
     * @param tokenId
     */
    @Async
    public void reactiveToken(String tokenId) {
        String tokenAccountKey = String.format(CK_TOKEN_T_A, tokenId);
        unistarCache.expire(tokenAccountKey, TOKEN_INVALID_MINUTE);
    }

}
