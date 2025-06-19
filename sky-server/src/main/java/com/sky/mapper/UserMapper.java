package com.sky.mapper;


import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {
    /**
     * 根据Openid获取用户id
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    public User getByOpenid(String openid);

    /**
     * 新增用户信息
     * @param user
     */
    void save(User user);

    @Select("select * from user where id = #{id}")
    User getById(Long id);

    /**
     * 根据起始时间查询新增用户
     * @param beginTime
     * @param endTime
     * @return
     */
    @Select("select count(0) from user where create_time between #{beginTime} and #{endTime}")
    Long queryByDay(LocalDateTime beginTime, LocalDateTime endTime);
}
