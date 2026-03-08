package com.seu.emotionhub.web.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus配置类
 *
 * @author EmotionHub Team
 */
@Configuration
@MapperScan("com.seu.emotionhub.dao.mapper")
public class MyBatisPlusConfig {

    /**
     * 自动填充处理器
     * 自动填充createdAt和updatedAt字段
     */
    @Component
    public static class MyMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            // 插入时自动填充创建时间和更新时间
            this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            // 更新时自动填充更新时间
            this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        }
    }
}
