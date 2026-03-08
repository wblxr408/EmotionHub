package com.seu.emotionhub.common.exception;

import com.seu.emotionhub.common.enums.ErrorCode;
import lombok.Getter;

/**
 * 业务异常类
 * 所有业务逻辑中的异常都应该抛出此异常
 *
 * @author EmotionHub Team
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 使用错误码枚举构造异常
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * 使用错误码枚举和自定义消息构造异常
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.message = customMessage;
    }

    /**
     * 使用自定义错误码和消息构造异常
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
