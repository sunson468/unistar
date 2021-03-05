package com.up1234567.unistar.common.util;

import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtil {

    /**
     * 产生 0 到  i 的随机数，不包括i
     *
     * @param i
     * @return
     */
    public static int randomInt(int i) {
        return ThreadLocalRandom.current().nextInt(i);
    }

    /**
     * @param collection
     * @param <T>
     * @return
     */
    public static <T> T randomOne(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) return null;
        Optional<T> ret;
        if (collection.size() == 1) {
            ret = collection.stream().findFirst();
        } else {
            ret = collection.stream().skip(randomInt(collection.size())).parallel().findAny();
        }
        return ret.orElse(null);
    }

}
