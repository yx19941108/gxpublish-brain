package com.gxpublish.brain.editorial.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 当前登录人可操作的审校任务上下文。
 */
@Data
public class EditorialReviewTaskContextVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long reviewId;

    private Long taskId;

    private Long instanceId;

    private String nodeCode;

    private String nodeName;
}
