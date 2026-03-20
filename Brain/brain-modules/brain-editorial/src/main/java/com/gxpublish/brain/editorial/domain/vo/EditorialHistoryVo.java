package com.gxpublish.brain.editorial.domain.vo;

import com.gxpublish.brain.editorial.domain.EditorialHistory;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 审校历史视图对象
 *
 * @author gxpublish
 */
@Data
@AutoMapper(target = EditorialHistory.class)
public class EditorialHistoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long reviewId;
    private Long operatorId;
    private String operatorName;
    private String operatorRoleName;
    private Date operateTime;
    private String eventType;
    private String operateType;
    private Map<String, Object> fieldDiff;

}
