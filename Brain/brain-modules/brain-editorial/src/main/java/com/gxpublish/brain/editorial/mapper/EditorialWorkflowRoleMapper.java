package com.gxpublish.brain.editorial.mapper;

import com.gxpublish.brain.editorial.domain.vo.EditorialWorkflowRoleRefVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审校流程角色查询。
 */
public interface EditorialWorkflowRoleMapper {

    List<EditorialWorkflowRoleRefVo> selectRoleRefs(@Param("roleKeys") List<String> roleKeys);
}
