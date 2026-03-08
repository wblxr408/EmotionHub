package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 通知Mapper接口
 *
 * @author EmotionHub Team
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 查询用户的未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    long countUnread(@Param("userId") Long userId);
}
