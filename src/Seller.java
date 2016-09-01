/**
 * This class contains methods and attributes for processing bids and calculated new maximum sale price.
 * It is initialized with a target size of the number of shares to sell, and it maintains a log in the form of 
 * a max TreeMap in order track the bids.
 * 
 * The implementation and tradeoffs are similar to the Buyer class.
 *
 * @author Michael Hinds
 * @version 1.0
*/

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;
import java.util.Map;

public class Seller {
	private TreeMap<Double, Integer> bid_log;
	private int target_size;
	private double target_shares_cost;
	private double min_price_paid_per_share; //track this to know if we need to recalculate the max cost of target shares

	//Constructors
    /**
     * This is a constructor for the Seller class. It instantiates a new Seller instance with 
     * an empty log of bids and requires a target_size int.
     *
     * @param target_size The number of shares to be sold for maximal price
     *
     * @return Seller instance with empty bid log and target_size populated
     */
	public Seller(int target_size){
		 this.bid_log = new TreeMap<Double, Integer>(new ReverseComparator());
		 this.target_size = target_size;
		 this.target_shares_cost = -1;
		 this.min_price_paid_per_share = Double.NEGATIVE_INFINITY;
	}

	//Getters & Setters
    /**
     * Returns the target_size param
     *
     * @return target_size An integer representing the number of shares to sell
     */
	public int getTargetSize(){
		return this.target_size;
	}

    /**
     * Sets the target_size param. It is not recommended to do this while after adding bids to the
     * log as this could lead to an inconsistent state.
     *
     * @param target_size The number of shares to sell
     */
	public void setTargetSize(int target_size){
		this.target_size = target_size;
	}

	/**
	 * This method calculates the maximum price to sell target_size shares. Returns negative infinity if there
	 * aren't enough shares available.
     *
     * @return costOfTargetShares The maximum price to sell target_size shares. Returns Double.NEGATIVE_INFINITY
     * if there aren't enough shares in the log.
	 */
	public double getCostOfTargetShares(){
		int shares_remaining = target_size;
		double total_cost = 0.0;
		Map.Entry<Double,Integer> cur_bid = this.bid_log.firstEntry();
		while (shares_remaining > 0 && cur_bid != null){
			//check if the current bid has enough shares to complete the buy
			if (cur_bid.getValue() < shares_remaining){
				//the current bid doesn't have enough shares to complete the buy

				//Add all the shares at this price point to the sale
				total_cost += cur_bid.getKey() * cur_bid.getValue();
				shares_remaining -= cur_bid.getValue();

			} else { //the current bid has enough shares to complete the sale
				total_cost += cur_bid.getKey() * shares_remaining;
				shares_remaining = 0;
				this.min_price_paid_per_share = cur_bid.getKey();

			}
			//get the next highest priced bid
			cur_bid = bid_log.higherEntry(cur_bid.getKey());
		}
		if(shares_remaining > 0){
			return Double.NEGATIVE_INFINITY;
		} else {
			return total_cost;
		}
	}
	/**
	 * This method adds a bid to the log and recalculates the 
	 * maximum sale price for target_size shares.
     *
     * @param price The price per share of the bid to be added
     * @param size The number of shares in the bid
     *
     * @return
	 * null if the cost of buying target shares is unchanged
	 * Double.NEGATIVE_INFINITY if there are not enough shares to complete the buy
	 * new cost of buying target shares
	**/
	public Double addNewBid(double price, int size){
		double new_cost;
		/*****add new bid to the log****/
		//Keys are prices, so check and see if there are some shares at this price already
		Integer current_shares = this.bid_log.get(price);
		if (current_shares != null){
			//already have shares at this price, so increment the number by size
			this.bid_log.put(price, current_shares+size);
		} else {
			//no shares at this price so create a new key/value pair
			this.bid_log.put(price, size);
		}
		//check if the price should be updated
		if (price <= this.min_price_paid_per_share){
			//the new bid is too low and won't affect max sale
			return null;
		}
		new_cost = getCostOfTargetShares();
		if (new_cost == this.target_shares_cost || (this.target_shares_cost == -1 && new_cost == Double.NEGATIVE_INFINITY)){
			//the new_cost is the same as the old cost
			this.target_shares_cost = new_cost;
			return null;
		} else {
			this.target_shares_cost = new_cost;
			return this.target_shares_cost;
		}
	}

    /**
     * This method removes a bid from the log. Then, it recalculates the maximum price to sell target_size
     * shares.
     *
     * @param price The price per share of the bid to remove
     * @param size The number of shares in the bid
     *
     * @return
     * null if the cost of selling target shares is unchanged
     * Double.NEGATIVE_INFINITY if there are not enough shares to complete the sale
     */
	public Double removeBid(double price, int size) throws Exception {
		Double new_cost;
		//remove shares from log
		Integer current_shares = this.bid_log.get(price);
        if (current_shares == null) {
            throw new Exception("Error: Cannot remove shares. No shares in the seller log at this price: "+price);
        }
		if (size < current_shares) {//there are other bids at this price, so just decrement shares from log
			this.bid_log.put(price,current_shares-size);
		} else {//remove the entry from the log completely
			this.bid_log.remove(price);
		}
		//recalculate price if needed
		if (price >= this.min_price_paid_per_share){
			//invalidates min_price_paid_per_share
			this.min_price_paid_per_share = Double.NEGATIVE_INFINITY;
			new_cost = getCostOfTargetShares();
			if(new_cost == this.target_shares_cost || (this.target_shares_cost == -1 && new_cost == Double.NEGATIVE_INFINITY)){
				this.target_shares_cost = new_cost;
				return null;
			} else {
				this.target_shares_cost = new_cost;
				return this.target_shares_cost;
			}
		} else {
			return null;
		}
	}
}
