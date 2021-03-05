package com.up1234567.unistar.central.support.data.mysql;

import com.up1234567.unistar.central.support.data.IUnistarDao;
import com.up1234567.unistar.central.support.data.UnistarEntityTable;
import com.up1234567.unistar.central.support.data.extend.UnistarPageCondition;
import com.up1234567.unistar.central.support.data.extend.UnistarPageResult;
import com.up1234567.unistar.common.exception.UnistarNotSupportException;
import com.up1234567.unistar.common.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UnistarMysqlDao implements IUnistarDao {

    private final static String SQL_QOATE = "`";
    private final static String SQL_MARK = "?";
    private final static String SQL_EQUAL = "=";
    private final static String SQL_BLANK = " ";
    private final static String SQL_AND = " AND ";
    private final static String SQL_IN = " IN (?)";
    private final static String TPL_INSERT = "INSERT INTO %s (%s) VALUES (%s)";
    private final static String TPL_COUNT = "SELECT COUNT(0) FROM %s";
    private final static String TPL_SELECT = "SELECT * FROM %s";
    private final static String TPL_WHERE = " WHERE 1=1 %s";
    private final static String TPL_ORDER_BY = " ORDER BY ";
    private final static String TPL_LIMIT = " LIMIT %d";
    private final static String TPL_LIMIT_2 = " LIMIT %d, %d";
    private final static String TPL_UPDATE = "UPDATE %s SET ";
    private final static String TPL_DELETE = "DELETE FROM %s ";
    private final static String TPL_UPDATE_MAX = "GREATEST(`%s`,?)";
    private final static String TPL_UPDATE_MIN = "LEAST(`%s`,?)";

    private JdbcTemplate jdbcTemplate;

    // 解析实体
    private ConcurrentMap<Class, UnistarEntityTable> ENTITY_TABLE = new ConcurrentHashMap<>();

    public UnistarMysqlDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @param size
     * @return
     */
    private String genMarks(int size) {
        return StringUtils.repeat(SQL_MARK, StringUtil.COMMA, size);
    }

    /**
     * 获取实体结构
     *
     * @param clazz
     * @return
     */
    private UnistarEntityTable entityTable(Class clazz) {
        UnistarEntityTable entityTable = ENTITY_TABLE.get(clazz);
        if (entityTable == null) {
            entityTable = new UnistarEntityTable(clazz);
            ENTITY_TABLE.put(clazz, entityTable);
        }
        return entityTable;
    }

    @Override
    public <T> void insert(T entity) {
        UnistarEntityTable table = entityTable(entity.getClass());
        Map<String, Object> valueMap = table.toValueMap(entity);
        List<Object> params = new ArrayList<>(valueMap.size());
        StringBuilder fields = new StringBuilder();
        StringBuilder marks = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            String f = entry.getKey();
            Object v = entry.getValue();
            if (v == null) continue;
            if (first) {
                first = false;
            } else {
                fields.append(StringUtil.COMMA);
                marks.append(StringUtil.COMMA);
            }
            fields.append(SQL_QOATE).append(f).append(SQL_QOATE);
            marks.append(SQL_MARK);
            params.add(v);
        }
        jdbcTemplate.update(String.format(TPL_INSERT, table.getTable(), fields.toString(), marks.toString()), params.toArray());
    }

    @Override
    public <T> void insertAll(List<T> entities) {
        entities.parallelStream().forEach(this::insert);
    }

    @Override
    public <T> T findOneByProps(Map<String, Object> props, Class<T> clazz) {
        UnistarPageCondition condition = new UnistarPageCondition();
        condition.setPage(0);
        condition.setLimit(1);
        props.forEach(condition::addFilter);
        UnistarPageResult<T> result = listByPageCondition(condition, clazz);
        return CollectionUtils.isEmpty(result.getData()) ? null : result.getData().get(0);
    }

    @Override
    public <T> List<T> listByProps(Map<String, Object> props, Class<T> clazz) {
        UnistarPageCondition condition = new UnistarPageCondition();
        condition.setPage(0);
        if (MapUtils.isNotEmpty(props)) props.forEach(condition::addFilter);
        UnistarPageResult<T> result = listByPageCondition(condition, clazz);
        return CollectionUtils.isEmpty(result.getData()) ? new ArrayList<>() : result.getData();
    }

    @Override
    public <T> long count(Map<String, Object> props, Class<T> clazz) {
        UnistarEntityTable table = entityTable(clazz);
        String counter = String.format(TPL_COUNT, table.getTable());
        if (MapUtils.isEmpty(props)) {
            return jdbcTemplate.queryForObject(counter, Long.class);
        } else {
            // 查询条件
            Object[] fv = wrapWhere(props);
            Object[] params = (Object[]) fv[1];
            return jdbcTemplate.queryForObject(counter + fv[0], params, Long.class);
        }
    }

    /**
     * 包装查询条件
     *
     * @param props
     * @return
     */
    private Object[] wrapWhere(Map<String, Object> props) {
        StringBuilder fields = new StringBuilder();
        if (MapUtils.isEmpty(props)) return new Object[]{fields.toString(), new Object[0]};
        List<Object> params = new ArrayList<>(props.size());
        for (Map.Entry<String, Object> prop : props.entrySet()) {
            String f = prop.getKey();
            Object v = prop.getValue();
            if (v == null) continue;
            if (v instanceof Collection) {
                Collection vs = (Collection) v;
                if (CollectionUtils.isEmpty(vs)) continue;
                fields.append(SQL_AND).append(SQL_QOATE).append(f).append(SQL_QOATE).append(String.format(SQL_IN, genMarks(vs.size())));
                params.addAll(vs);
            } else {
                String[] ft = matchQuery(f);
                if (v.getClass().isEnum()) v = v.toString();
                fields.append(SQL_AND).append(SQL_QOATE).append(ft[1]).append(SQL_QOATE).append(StringUtil.withDefault(ft[0], SQL_EQUAL)).append(SQL_MARK);
                params.add(v);
            }
        }
        return new Object[]{String.format(TPL_WHERE, fields.toString()), params.toArray()};
    }

    @Override
    public <T> UnistarPageResult<T> listByPageCondition(UnistarPageCondition condition, Class<T> clazz) {
        UnistarPageResult<T> pageResult = new UnistarPageResult<>();
        pageResult.setPage(condition.getPage());
        pageResult.setSize(condition.getLimit());
        // ====================================================
        UnistarEntityTable table = entityTable(clazz);
        // 查询条件
        Object[] fv = wrapWhere(condition.getFilters());
        Object[] params = (Object[]) fv[1];
        // 查询总数
        if (condition.isCountable()) {
            String counter = String.format(TPL_COUNT, table.getTable()) + fv[0];
            Long count;
            if (params.length > 0) {
                count = jdbcTemplate.queryForObject(counter, params, Long.class);
            } else {
                count = jdbcTemplate.queryForObject(counter, Long.class);
            }
            pageResult.setCount(count == null ? 0 : count);
        }
        StringBuilder selector = new StringBuilder();
        selector.append(String.format(TPL_SELECT, table.getTable()));
        selector.append(fv[0]);
        // SELECT * FROM xxx WHERE 1=1,a=?,b=? ORDER BY a desc, b asc LIMIT 1,n
        if (MapUtils.isNotEmpty(condition.getSorts())) {
            StringBuilder orders = new StringBuilder();
            for (Map.Entry<String, String> sort : condition.getSorts().entrySet()) {
                String f = sort.getKey();
                Object v = sort.getValue();
                if (v == null) continue;
                orders.append(StringUtil.COMMA).append(SQL_QOATE).append(f).append(SQL_QOATE).append(SQL_BLANK).append(v);
            }
            selector.append(TPL_ORDER_BY).append(orders.substring(1));
        }
        // 分页
        if (condition.getPage() > 0) {
            selector.append(String.format(TPL_LIMIT_2, (condition.getPage() - 1) * condition.getLimit(), condition.getLimit()));
        } else if (condition.getLimit() > 0) {
            selector.append(String.format(TPL_LIMIT, condition.getLimit()));
        }
        if (params.length > 0) {
            pageResult.setData(jdbcTemplate.query(selector.toString(), params, new BeanPropertyRowMapper<>(clazz)));
        } else {
            pageResult.setData(jdbcTemplate.query(selector.toString(), new BeanPropertyRowMapper<>(clazz)));
        }
        //
        return pageResult;
    }

    /**
     * 包装更新语句
     *
     * @param updates
     * @return
     */
    private Object[] wrapUpdate(Map<String, Object> updates) {
        StringBuilder fields = new StringBuilder();
        if (MapUtils.isEmpty(updates)) return new Object[]{fields.toString(), new Object[0]};
        List<Object> params = new ArrayList<>(updates.size());
        for (Map.Entry<String, Object> prop : updates.entrySet()) {
            String f = prop.getKey();
            Object v = prop.getValue();
            if (v == null) continue;
            if (v.getClass().isEnum()) v = v.toString();
            //
            fields.append(StringUtil.COMMA).append(SQL_QOATE).append(f).append(SQL_QOATE).append(SQL_EQUAL);
            // 检测
            String[] ft = matchUpdate(f);
            f = ft[1];
            switch (ft[0]) {
                case UPT_ADD:
                case UPT_SUB:
                    int nv = 0;
                    if (v instanceof Integer) {
                        nv = (Integer) v;
                    } else if (v instanceof Long) {
                        nv = ((Long) v).intValue();
                    }
                    if (nv > 0) {
                        v = nv;
                        fields.append(SQL_EQUAL).append(SQL_QOATE).append(f).append(SQL_QOATE).append(ft[0]).append(SQL_MARK);
                    } else {
                        continue;
                    }
                    break;
                case UPT_MAX:
                    // GREATEST(f,v)
                    fields.append(String.format(TPL_UPDATE_MAX, f));
                    break;
                case UPT_MIN:
                    // LEAST(f,v)
                    fields.append(String.format(TPL_UPDATE_MIN, f));
                    break;
                default:
                    fields.append(SQL_MARK);
                    break;
            }
            params.add(v);
        }
        return new Object[]{fields.substring(1), params.toArray()};
    }

    /**
     * @param props
     * @param updates
     * @param clazz
     * @param single
     * @param <T>
     */
    private <T> int updateByPropsIfSingle(Map<String, Object> props, Map<String, Object> updates, Class<T> clazz, boolean single) {
        UnistarEntityTable table = entityTable(clazz);
        //
        StringBuilder sql = new StringBuilder();
        sql.append(String.format(TPL_UPDATE, table.getTable()));
        List<Object> params = new ArrayList<>();
        // 更新
        Object[] ufv = wrapUpdate(updates);
        Object[] uparams = (Object[]) ufv[1];
        if (ArrayUtils.isNotEmpty(uparams)) params.addAll(Arrays.asList(uparams));
        sql.append(SQL_BLANK).append(ufv[0]);
        // 查询
        if (MapUtils.isNotEmpty(props)) {
            Object[] wfv = wrapWhere(props);
            Object[] wparams = (Object[]) wfv[1];
            params.addAll(Arrays.asList(wparams));
            sql.append(SQL_BLANK).append(wfv[0]);
        }
        //
        if (single) sql.append(SQL_BLANK).append(String.format(TPL_LIMIT, 1));
        //
        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

    @Override
    public <T> void updateOneByProps(Map<String, Object> props, Map<String, Object> updates, Class<T> clazz) {
        updateByPropsIfSingle(props, updates, clazz, true);
    }

    @Override
    public <T> long updateMultiByProps(Map<String, Object> props, Map<String, Object> updates, Class<T> clazz) {
        return updateByPropsIfSingle(props, updates, clazz, false);
    }

    @Override
    public <T> void removeByProps(Map<String, Object> props, Class<T> clazz) {
        if (MapUtils.isEmpty(props)) throw new UnistarNotSupportException("unistar data remove must contain props");
        UnistarEntityTable table = entityTable(clazz);
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sql.append(String.format(TPL_DELETE, table.getTable()));
        // 条件
        Object[] wfv = wrapWhere(props);
        Object[] wparams = (Object[]) wfv[1];
        params.addAll(Arrays.asList(wparams));
        sql.append(SQL_BLANK).append(wfv[0]);
        jdbcTemplate.update(sql.toString(), params.toArray());
    }
}
