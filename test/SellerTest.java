import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SellerTest {

    @Test
    public void testAddNewBid(){
        Seller seller = new Seller(200);
        assertEquals(null, seller.addNewBid(100, 100));
        assertEquals((Double) (200.0*100.0), (Double) seller.addNewBid(100, 100));
        assertEquals((Double) (200.0*1000.0), (Double) seller.addNewBid(1000, 200));
        assertEquals(null, (Double) seller.addNewBid(1000, 200));
    }

    @Test
    public void testRemoveBid() throws Exception{
        Seller seller = new Seller(200);
        seller.addNewBid(90, 100);
        seller.addNewBid(100, 100);
        seller.addNewBid(100, 100);

        assertEquals((Double) (100.0*100.0+90*100), (Double) seller.removeBid(100,100));
        assertEquals((Double) Double.NEGATIVE_INFINITY, (Double) seller.removeBid(100,100));
    }
}
