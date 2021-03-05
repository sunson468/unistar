package com.up1234567.unistar.central.support.data;

import com.up1234567.unistar.common.exception.UnistarNotSupportException;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析实体类，以MongoDB的注解为主
 */
@Data
public class UnistarEntityTable {

    private Class clazz;
    private String table;
    private List<Field> fields;

    /**
     * 实体类对象
     *
     * @param clazz
     */
    public UnistarEntityTable(Class clazz) {
        this.clazz = clazz;
        Document document = (Document) clazz.getAnnotation(Document.class);
        this.table = document.collection();
        this.fields = new ArrayList<>();
        ReflectionUtils.doWithFields(clazz, field -> {
            if (!Modifier.isStatic(field.getModifiers())) this.fields.add(field);
        });
    }

    /**
     * 将实体类实例解析为Map
     *
     * @param entity
     * @return
     */
    public Map<String, Object> toValueMap(Object entity) {
        Map<String, Object> valueMap = new HashMap<>();
        for (Field f : fields) {
            f.setAccessible(true);
            try {
                Class type = f.getType();
                Object value = f.get(entity);
                if (value == null) continue;
                if (type.isEnum()) {
                    valueMap.put(f.getName(), value.toString());
                } else {
                    valueMap.put(f.getName(), value);
                }
            } catch (Exception e) {
                throw new UnistarNotSupportException("unistar entity table cann't convert to map: " + e.getMessage());
            } finally {
                f.setAccessible(false);
            }
        }
        return valueMap;
    }

}
