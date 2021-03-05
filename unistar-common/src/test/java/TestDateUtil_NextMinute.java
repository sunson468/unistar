import com.up1234567.unistar.common.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;

public class TestDateUtil_NextMinute {

    // 2021-02-03 09:47:01   1612316821000
    // 2021-02-03 09:47:55   1612316875000
    // 2021-02-03 09:48:00   1612316880000
    // 2021-02-03 09:48:01   1612316881000

    @Test
    public void testNextMinute01() {
        Assert.assertEquals(1612316880000L, DateUtil.nextMinute(1612316821000L));
    }

    @Test
    public void testNextMinute02() {
        Assert.assertEquals(1612316880000L, DateUtil.nextMinute(1612316875000L));
    }

    @Test
    public void testNextMinute03() {
        Assert.assertNotEquals(1612316880000L, DateUtil.nextMinute(1612316881000L));
    }

}
