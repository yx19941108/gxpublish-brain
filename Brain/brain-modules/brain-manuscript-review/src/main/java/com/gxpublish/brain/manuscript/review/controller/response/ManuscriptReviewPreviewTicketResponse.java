package com.gxpublish.brain.manuscript.review.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源预览短时效票据响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManuscriptReviewPreviewTicketResponse {

    /**
     * 资源主键。
     */
    private Long resourceId;

    /**
     * 带票据的预览地址。
     */
    private String resourceUrl;

    /**
     * 仅用于 preview 的短时效票据。
     */
    private String previewToken;

    /**
     * 秒级过期时间戳。
     */
    private Long expireAtEpochSecond;
}
