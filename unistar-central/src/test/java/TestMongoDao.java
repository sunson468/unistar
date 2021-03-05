import com.up1234567.unistar.central.CentralApplication;
import com.up1234567.unistar.central.service.stat.impl.StatTraceService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = CentralApplication.class,
        args = {
                "----spring.profiles.active=test",
        }
)
public class TestMongoDao {

    @Autowired
    private StatTraceService statTraceService;

    @Test
    public void test() {
        Assert.assertEquals(3, statTraceService.passedDayStatTotals("DEFAULT_NS", 30).size());
    }

}
