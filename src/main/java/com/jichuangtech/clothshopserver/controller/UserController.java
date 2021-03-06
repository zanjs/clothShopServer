package com.jichuangtech.clothshopserver.controller;

import com.jichuangtech.clothshopserver.service.SessionService;
import com.jichuangtech.clothshopserver.utils.HttpRequestUtils;
import com.jichuangtech.clothshopserver.utils.JsonMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

/**
 * Created by Bingo on 2017/7/21.
 * 用户模块接口
 */
@Api(description = "用户模块接口")
@RestController
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private SessionService sessionService;

    @Value("${wx_session_api}")
    private String wxSessionApi;
    @Value("${app_id}")
    private String appId;
    @Value("${secret}")
    private String secret;

    /**
     * @param code 小程序编码
     * @return
     */
    @ApiOperation(value = "小程序登录", notes = "必须要传code过来,根据这个code,服务器这边会生成一个session_key返回。当做身份验证。<br/>调用正确返回 sessionId。调用失败返回错误信息")
    @RequestMapping(value = "/onlogin", method = RequestMethod.GET)
    public String onlogin(@ApiParam(name = "验证码", required = true) String code) {
        LOGGER.info("user.code is {}", code);
        String requestUrl = wxSessionApi + "?" +
                "appid=" + appId + "&" +
                "secret=" + secret + "&" +
                "js_code=" + code + "&" +
                "grant_type=" + "authorization_code";
        String result = HttpRequestUtils.httpGet(requestUrl);
        Map map = JsonMapper.nonDefaultMapper().fromJson(result, Map.class);
        String sessionKey = (String) map.get("session_key");
        if (sessionKey == null) {
            return (String) map.get("errmsg");
        }
        String openid = (String) map.get("openid");
        int randomValue = new Random(10).nextInt();
        //随机去一个数当sessionId
        String sessionThirdId = randomValue + "&" + System.currentTimeMillis() + openid;
        sessionService.put(sessionThirdId, sessionKey + "_" + openid);
        return sessionThirdId;
    }
}
