import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class BuyerTest {

    @Test
    public void testAddNewOffer(){
        Buyer buyer = new Buyer(200);
        assertEquals(null, buyer.addNewOffer(100, 100));
        assertEquals((Double) (200.0*100.0), (Double) buyer.addNewOffer(100, 100));
        assertEquals((Double) (200.0*1.0), (Double) buyer.addNewOffer(1, 200));
        assertEquals(null, (Double) buyer.addNewOffer(1, 200));
    }

    @Test
    public void testRemoveOffer() throws Exception{
        Buyer buyer = new Buyer(200);
        buyer.addNewOffer(90, 100);
        buyer.addNewOffer(100, 100);
        buyer.addNewOffer(100, 100);

        assertEquals((Double) (200.0*100.0), (Double) buyer.removeOffer(90,100));
        assertEquals((Double) Double.POSITIVE_INFINITY, (Double) buyer.removeOffer(100,100));
    }
}
