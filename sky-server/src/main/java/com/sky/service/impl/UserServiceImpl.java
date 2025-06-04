package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    public static final String WX_LONGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 调用微信服务器接口 获取openid
     * @param code
     * @return
     */
    private String getOpenid(String code){
        Map<String, String> paraMap = new HashMap<>();
        paraMap.put("appid", weChatProperties.getAppid());
        paraMap.put("secret", weChatProperties.getSecret());
        paraMap.put("js_code", code);
        paraMap.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LONGIN, paraMap);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = (String)jsonObject.get("openid");

        return openid;
    }


    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    public User login(UserLoginDTO userLoginDTO){
        //调用微信服务器接口 获取openid
        String openid = getOpenid(userLoginDTO.getCode());

        //判断openid是否为空 如果为空表示登录失败 抛出异常 否则是合法微信用户
        if(openid == null || openid.isEmpty()){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //判断该用户openid是否在用户表中 如果没有则是新用户需要自动注册
        User user = userMapper.getByOpenid(openid);
        if(user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();

            userMapper.save(user);
        }
        return user;
    }
}
