package com.gxpublish.brain.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.gxpublish.brain.common.mybatis.core.mapper.BaseMapperPlus;
import com.gxpublish.brain.generator.domain.GenTableColumn;

/**
 * 业务字段 数据层
 *
 * @author Lion Li
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface GenTableColumnMapper extends BaseMapperPlus<GenTableColumn, GenTableColumn> {

}
