package com.gxpublish.brain.editorial.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.gxpublish.brain.common.core.domain.R;
import com.gxpublish.brain.common.core.validate.AddGroup;
import com.gxpublish.brain.common.core.validate.EditGroup;
import com.gxpublish.brain.common.log.annotation.Log;
import com.gxpublish.brain.common.log.enums.BusinessType;
import com.gxpublish.brain.common.mybatis.core.page.PageQuery;
import com.gxpublish.brain.common.mybatis.core.page.TableDataInfo;
import com.gxpublish.brain.common.web.core.BaseController;
import com.gxpublish.brain.editorial.domain.bo.EditorialReviewBo;
import com.gxpublish.brain.editorial.domain.vo.EditorialHistoryVo;
import com.gxpublish.brain.editorial.domain.vo.EditorialReviewVo;
import com.gxpublish.brain.editorial.service.IEditorialReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 编辑部审校Controller
 *
 * @author gxpublish
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/editorial/review")
@Tag(name = "编辑部审校", description = "编辑部审校管理")
public class EditorialReviewController extends BaseController {

    private final IEditorialReviewService editorialReviewService;

    /**
     * 查询审校申请列表
     */
    @SaCheckPermission("editorial:review:list")
    @Operation(summary = "查询审校申请列表")
    @GetMapping("/list")
    public TableDataInfo<EditorialReviewVo> list(EditorialReviewBo bo, PageQuery pageQuery) {
        return editorialReviewService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取审校申请详细信息
     */
    @SaCheckPermission("editorial:review:query")
    @Operation(summary = "获取审校申请详细信息")
    @GetMapping(value = "/{id}")
    public R<EditorialReviewVo> getInfo(@PathVariable Long id) {
        return R.ok(editorialReviewService.queryById(id));
    }

    /**
     * 新增审校申请(保存草稿)
     */
    @SaCheckPermission("editorial:review:add")
    @Log(title = "审校申请", businessType = BusinessType.INSERT)
    @Operation(summary = "新增审校申请(保存草稿)")
    @PostMapping
    public R<EditorialReviewVo> add(@Validated(AddGroup.class) @RequestBody EditorialReviewBo bo) {
        return R.ok(editorialReviewService.insertByBo(bo));
    }

    /**
     * 修改审校申请
     */
    @SaCheckPermission("editorial:review:edit")
    @Log(title = "审校申请", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改审校申请")
    @PutMapping
    public R<EditorialReviewVo> edit(@Validated(EditGroup.class) @RequestBody EditorialReviewBo bo) {
        return R.ok(editorialReviewService.updateByBo(bo));
    }

    /**
     * 提交并开启流程
     */
    @SaCheckPermission("editorial:review:add")
    @Log(title = "审校申请", businessType = BusinessType.INSERT)
    @Operation(summary = "提交并开启流程")
    @PostMapping("/submit")
    public R<EditorialReviewVo> submit(@Validated(AddGroup.class) @RequestBody EditorialReviewBo bo) {
        return R.ok(editorialReviewService.submitAndFlowStart(bo));
    }

    /**
     * 删除审校申请
     */
    @SaCheckPermission("editorial:review:remove")
    @Log(title = "审校申请", businessType = BusinessType.DELETE)
    @Operation(summary = "删除审校申请")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return toAjax(editorialReviewService.deleteWithValidByIds(ids));
    }
    
    /**
     * 获取历史记录
     */
    @SaCheckPermission("editorial:review:query")
    @Operation(summary = "获取历史记录")
    @GetMapping("/history/{id}")
    public R<List<EditorialHistoryVo>> history(@PathVariable Long id) {
        return R.ok(editorialReviewService.queryHistoryList(id));
    }
}
