package com.up1234567.unistar.central.support.data.mongo;

import com.mongodb.client.result.UpdateResult;
import com.up1234567.unistar.central.support.data.IUnistarDao;
import com.up1234567.unistar.central.support.data.extend.UnistarPageCondition;
import com.up1234567.unistar.central.support.data.extend.UnistarPageResult;
import com.up1234567.unistar.common.exception.UnistarNotSupportException;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MongoDB实现
 */
public class UnistarMongoDao implements IUnistarDao {

    private MongoTemplate mongoTemplate;

    public UnistarMongoDao(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param query
     * @param props
     */
    private void wrapQueryWithProps(Query query, Map<String, Object> props) {
        if (MapUtils.isNotEmpty(props)) {
            Map<String, Criteria> criteriaMap = new HashMap<>();
            props.forEach((f, val) -> {
                String field = f;
                Criteria criteria;
                if (val instanceof Collection) {
                    criteria = Criteria.where(field).in((Collection) val);
                } else {
                    String[] ft = matchQuery(f);
                    field = ft[1];
                    switch (ft[0]) {
                        case OPS_NE:
                            if (val == null) {
                                criteria = Criteria.where(field).exists(true);
                            } else {
                                criteria = Criteria.where(field).ne(val);
                            }
                            break;
                        case OPS_GTE:
                            criteria = Criteria.where(field).gte(val);
                            break;
                        case OPS_LTE:
                            criteria = Criteria.where(field).lte(val);
                            break;
                        case OPS_GT:
                            criteria = Criteria.where(field).gt(val);
                            break;
                        case OPS_LT:
                            criteria = Criteria.where(field).lt(val);
                            break;
                        default:
                            criteria = Criteria.where(field).is(val);
                            break;
                    }
                }
                if (criteriaMap.containsKey(field)) {
                    criteriaMap.get(field).andOperator(criteria);
                } else {
                    query.addCriteria(criteria);
                    criteriaMap.put(field, criteria);
                }
            });
        }
    }

    /**
     * @param update
     * @param updates
     */
    private void wrapUpdateWithUpdates(Update update, Map<String, Object> updates) {
        updates.forEach((f, prop) -> {
            String[] ft = matchUpdate(f);
            f = ft[1];
            Number nprop = null;
            if (prop instanceof Integer) {
                nprop = (Integer) prop;
            } else if (prop instanceof Long) {
                nprop = (Long) prop;
            }
            switch (ft[0]) {
                case UPT_ADD:
                    if (nprop != null) update.inc(f, nprop);
                    break;
                case UPT_SUB:
                    if (nprop != null) update.inc(f, -nprop.longValue());
                    break;
                case UPT_MAX:
                    update.max(f, prop);
                    break;
                case UPT_MIN:
                    update.min(f, prop);
                    break;
                default:
                    update.set(f, prop);
                    break;
            }
        });
    }

    @Override
    public <T> void insert(T entity) {
        mongoTemplate.insert(entity);
    }

    @Override
    public <T> void insertAll(List<T> entities) {
        mongoTemplate.insertAll(entities);
    }

    @Override
    public <T> T findOneByProps(Map<String, Object> props, Class<T> clazz) {
        Query query = new Query();
        wrapQueryWithProps(query, props);
        return mongoTemplate.findOne(query, clazz);
    }

    @Override
    public <T> List<T> listByProps(Map<String, Object> props, Class<T> clazz) {
        Query query = new Query();
        wrapQueryWithProps(query, props);
        return mongoTemplate.find(query, clazz);
    }

    @Override
    public <T> long count(Map<String, Object> props, Class<T> clazz) {
        Query query = new Query();
        wrapQueryWithProps(query, props);
        return mongoTemplate.count(query, clazz);
    }

    @Override
    public <T> UnistarPageResult<T> listByPageCondition(UnistarPageCondition condition, Class<T> clazz) {
        UnistarPageResult<T> pageResult = new UnistarPageResult<>();
        pageResult.setPage(condition.getPage());
        pageResult.setSize(condition.getLimit());
        // 包装查询条件
        Query query = new Query();
        wrapQueryWithProps(query, condition.getFilters());
        // 计算总数
        if (condition.isCountable()) {
            pageResult.setCount(mongoTemplate.count(query, clazz));
        }
        // 查询数据是否有
        if (!condition.isCountable() || pageResult.getCount() > 0) {
            // 计算分页
            query.with(PageRequest.of(condition.getPage() - 1, condition.getLimit()));
            // 计算排序
            if (!CollectionUtils.isEmpty(condition.getSorts())) {
                Sort sort = null;
                for (Map.Entry<String, String> entry : condition.getSorts().entrySet()) {
                    Sort s = Sort.by(Sort.Direction.valueOf(entry.getValue()), entry.getKey());
                    if (sort == null) {
                        sort = s;
                    } else {
                        sort = sort.and(s);
                    }
                }
                if (sort != null) query.with(sort);
            }
        }
        pageResult.setData(mongoTemplate.find(query, clazz));
        return pageResult;
    }

    @Override
    public <T> void updateOneByProps(Map<String, Object> props, Map<String, Object> updates, Class<T> clazz) {
        Query query = new Query();
        wrapQueryWithProps(query, props);
        Update update = new Update();
        wrapUpdateWithUpdates(update, updates);
        mongoTemplate.updateFirst(query, update, clazz);
    }

    @Override
    public <T> long updateMultiByProps(Map<String, Object> props, Map<String, Object> updates, Class<T> clazz) {
        Query query = new Query();
        wrapQueryWithProps(query, props);
        Update update = new Update();
        wrapUpdateWithUpdates(update, updates);
        UpdateResult result = mongoTemplate.updateMulti(query, update, clazz);
        return result.getModifiedCount();
    }

    @Override
    public <T> void removeByProps(Map<String, Object> props, Class<T> clazz) {
        if (MapUtils.isEmpty(props)) throw new UnistarNotSupportException("unistar data remove must contain props");
        Query query = new Query();
        wrapQueryWithProps(query, props);
        mongoTemplate.remove(query, clazz);
    }

}
