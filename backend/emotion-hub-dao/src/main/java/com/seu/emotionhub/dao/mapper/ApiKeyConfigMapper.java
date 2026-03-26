package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.ApiKeyConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * API密钥配置Mapper接口
 *
 * @author EmotionHub Team
 */
@Mapper
public interface ApiKeyConfigMapper extends BaseMapper<ApiKeyConfig> {

    /**
     * 查询用户个人指定提供商的默认启用配置
     */
    @Select("SELECT * FROM api_key_config WHERE user_id = #{userId} AND provider = #{provider} AND is_enabled = 1 AND is_default = 1 LIMIT 1")
    ApiKeyConfig selectDefaultByUserAndProvider(@Param("userId") Long userId, @Param("provider") String provider);

    /**
     * 查询用户个人指定提供商的任意启用配置（兜底）
     */
    @Select("SELECT * FROM api_key_config WHERE user_id = #{userId} AND provider = #{provider} AND is_enabled = 1 LIMIT 1")
    ApiKeyConfig selectEnabledByUserAndProvider(@Param("userId") Long userId, @Param("provider") String provider);

    /**
     * 查询平台级默认API配置（user_id IS NULL，优先级最低）
     */
    @Select("SELECT * FROM api_key_config WHERE user_id IS NULL AND provider = #{provider} AND is_enabled = 1 AND is_default = 1 LIMIT 1")
    ApiKeyConfig selectPlatformDefault(@Param("provider") String provider);
}
