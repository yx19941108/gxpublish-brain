package com.gxpublish.brain.editorial.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("dev")
class EditorialHistoryMappingConfigTest {

    @Test
    void shouldEnableAutoResultMapForFieldDiffJsonColumn() throws NoSuchFieldException {
        TableName tableName = EditorialHistory.class.getAnnotation(TableName.class);
        assertNotNull(tableName);
        assertTrue(tableName.autoResultMap(), "field_diff JSON needs MyBatis resultMap type handler support");

        Field fieldDiff = EditorialHistory.class.getDeclaredField("fieldDiff");
        TableField tableField = fieldDiff.getAnnotation(TableField.class);
        assertNotNull(tableField);
        assertEquals(JacksonTypeHandler.class, tableField.typeHandler());
    }
}
