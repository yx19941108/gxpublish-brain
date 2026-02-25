package com.gxpublish.brain.workflow.service.impl;

import cn.hutool.core.convert.Convert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.gxpublish.brain.common.translation.annotation.TranslationType;
import com.gxpublish.brain.common.translation.core.TranslationInterface;
import com.gxpublish.brain.workflow.common.ConditionalOnEnable;
import com.gxpublish.brain.workflow.common.constant.FlowConstant;
import com.gxpublish.brain.workflow.service.IFlwCategoryService;
import org.springframework.stereotype.Service;

/**
 * 流程分类名称翻译实现
 *
 * @author AprilWind
 */
@ConditionalOnEnable
@Slf4j
@RequiredArgsConstructor
@Service
@TranslationType(type = FlowConstant.CATEGORY_ID_TO_NAME)
public class CategoryNameTranslationImpl implements TranslationInterface<String> {

    private final IFlwCategoryService flwCategoryService;

    @Override
    public String translation(Object key, String other) {
        return flwCategoryService.selectCategoryNameById(Convert.toLong(key));
    }
}
