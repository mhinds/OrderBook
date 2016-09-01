/**
 * This class implements a pricer for a single instrument. The pricer
 * maintains a logbook along with the min/max cost to buy/sell 
 * target_shares of a given instrument. This is the main entry point for 
 * pricer application. 
 *
 * @author Michael Hinds
 * @version 1.0
 * @since 8-31-16
 */
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.io.IOException;

public class Pricer {

	//Invalid input message
	private static final String improper_input_warning_1 = "Warning invalid input: \"";
	private static final String improper_input_warning_2 = "\"\nValid Inputs: 	\n\t timestamp<int> A<char> order-id<string> side<'b'|'s'> price<double> size<int>"+
										"\n\t timestamp<int> R<char> order-id<string> size<int>";
	//Regular exppressions to parse input lines 
	private static final Pattern add_order_pattern = Pattern.compile(
		"\\s*(\\d+)\\s+A\\s+(\\S+)\\s+([BS])\\s+([0-9.]+)\\s+(\\d+)\\s*");
	private static final Pattern remove_order_pattern = Pattern.compile(
		"\\s*(\\d+)\\s+R\\s+(\\S+)\\s+(\\d+)\\s*");
	private static Matcher add_order_matcher;
	private static Matcher remove_order_matcher;

	//Objects to handle buying and selling new orders
	private static Buyer buyer;
	private static Seller seller;

	//HashMap to associate order_id's with price, shares, and side
	private static HashMap<String,Order> id_to_order = new HashMap<String,Order>();

    /**
     * This method parses a line of input text representing
     * a command to add or remove an offer/bid. It uses regular 
     * expressions to parse the line and then calls the appropriate
     * function to add or remove the order.
     *
     * @param line This is a line from the log book
     */
	public static void parseNewOrder(String line) throws IOException, Exception{
		String order_id, timestamp;
		String side;
		int target_size, size;
		double price;

		remove_order_matcher = remove_order_pattern.matcher(line);
		add_order_matcher = add_order_pattern.matcher(line);

        if(add_order_matcher.matches()){//add order to log book
			//separate fields
			timestamp = add_order_matcher.group(1);
			order_id = add_order_matcher.group(2);
			side = add_order_matcher.group(3);
			price = Double.parseDouble(add_order_matcher.group(4));
			size = Integer.parseInt(add_order_matcher.group(5));

			addOrder(timestamp,order_id,side,price,size);

        } else if(remove_order_matcher.matches()){
			//separate fields
			timestamp = remove_order_matcher.group(1);
			order_id = remove_order_matcher.group(2);
			size = Integer.parseInt(remove_order_matcher.group(3));

			removeOrder(timestamp,order_id,size);

		}  else {
			throw new IOException();
		}
	}

    /**
     * This method adds a new order to the appropriate buer or seller 
     * log book. If there is an update to the min/max buy/sell cost 
     * it will be printed to System.out.
     *
     * @oaram timestamp The timestamp of the order
     * @param order_id The unique identifier of the order
     * @param side The side of the order (buy | sell)
     * @param price The price of the order
     * @param size The number of shares in the order
     */
	public static void addOrder(String timestamp, String order_id, String side, double price, int size) {
		Double min_buy_price, max_sell_price;
		//add the order to the log
		id_to_order.put(order_id, new Order(side,price,size));

		if("S".equals(side)){//offer

			min_buy_price = buyer.addNewOffer(price,size);

			//check to see if there is a new minimum buy price
			if(min_buy_price == null){}//do nothing, no change in min buy price
			else if(min_buy_price == Double.POSITIVE_INFINITY){//there are no longer enough shares to complete the buy
				System.out.println(timestamp +" B NA");
			} else {
				System.out.println(timestamp +" B "+ String.format("%.2f", min_buy_price));
			}
		} else {//bid
			max_sell_price = seller.addNewBid(price,size);

			//check to see if there is a new maximum sale price
			if(max_sell_price == null){}//do nothing, no change in min buy price
			else if(max_sell_price == Double.NEGATIVE_INFINITY){//there are no longer enough shares to complete the sale
				System.out.println(timestamp +" S NA");
			} else {
				System.out.println(timestamp +" S "+ String.format("%.2f", max_sell_price));
			}
		}
	}

    /**
     * This method removes an order from the log. It identifies whether the order 
     * is on the buy or sell side and calls the appropriate method in the buyer or 
     * seller objects. If there is an update to the min/max buy/sell cost, then it
     * is printed to System.out.
     *
     * @oaram timestamp The timestamp of the order
     * @param order_id The unique identifier of the order
     * @param size The number of shares in the order
     */
	public static void removeOrder (String timestamp, String order_id, int size) throws Exception {
		Double min_buy_price, max_sell_price;
		Order order_to_remove = id_to_order.get(order_id);
		String side = order_to_remove.getSide();
		double price = order_to_remove.getPrice();

		if("S".equals(side)){//remove an offer
			min_buy_price = buyer.removeOffer(price,size);

			//check to see if there is a new minimum buy price
			if(min_buy_price == null){}//do nothing, no change in min buy price
			else if(min_buy_price == Double.POSITIVE_INFINITY){//there are no longer enough shares to complete the buy
				System.out.println(timestamp +" B NA");
			} else {
				System.out.println(timestamp +" B "+ String.format("%.2f", min_buy_price));
			}
		} else {//remove a bid
			max_sell_price = seller.removeBid(price,size);

			//check to see if there is a new maximum sale price
			if(max_sell_price == null){}//do nothing, no change in max sell price
			else if(max_sell_price == Double.NEGATIVE_INFINITY){//there are no longer enough shares to complete the sale
				System.out.println(timestamp +" S NA");
			} else {
				System.out.println(timestamp +" S "+ String.format("%.2f", max_sell_price));
			}
		}

	}

    /**
     * This is the main method for the pricer program. It takes a command line argument for
     * the target number of shares to buy/sell. Then, it iterates over lines of input on STDIN
     * until it reaches a null value.
     *
     * @param target_shares The target number of shares to buy and sell 
     */
    public static void main(String[] args) {

		String input_line = "";


		try{
			buyer = new Buyer(Integer.parseInt(args[0]));
			seller = new Seller(Integer.parseInt(args[0]));

            //Process command line input until EOF
			Scanner s = new Scanner(System.in);
			while (s.hasNext()){
                input_line = s.nextLine();

                //Parse Input
				parseNewOrder(input_line);
            }

		} catch (IndexOutOfBoundsException|IOException|NumberFormatException e){
			System.out.println(improper_input_warning_1+input_line+improper_input_warning_2);
		}
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
