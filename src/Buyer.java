/**
 * This class contains methods and attributes for processing offers and calculated new minimum purchase price.
 * It is initialized with a target size of the number of shares to buy, and it maintains a log in the form of
 * a min TreeMap in order track the shares.
 *
 * @author Michael Hinds
 * @version 1.0
*/

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.TreeMap;
import java.util.Map;

public class Buyer {
	private TreeMap<Double, Integer> offer_log;
	private int target_size;
	private double target_shares_cost;
	private double max_price_paid_per_share; //track this to know if we need to recalculate the min cost of target shares
	//Constructors
    /**
     * This is a constructor for the buyer class. It instantiates buyer instances with 
     * empty logs (TreeMap<Double, Integer>) and requires a target_size param.
     *
     * @param target_size The target number of shares to buy
     *
     * @return Buyer instance with empty log and target_size populated
     */
    public Buyer(int target_size){
		 this.offer_log = new TreeMap<Double, Integer>();
		 this.target_size = target_size;
		 this.target_shares_cost = -1;
		 this.max_price_paid_per_share = Double.POSITIVE_INFINITY;
	}

	//Getters & Setters
    /**
     * Returns the target_size param
     *
     * @return target_size An integer representing the number of shares to purchase
     */
	public int getTargetSize(){
		return this.target_size;
	}

    /**
     * Sets the target_size param. It is not recommended to do this while after adding offers to the
     * log as this could lead to an inconsistent state.
     *
     * @param target_size The number of shares to purchase
     */
	public void setTargetSize(int target_size){
		this.target_size = target_size;
	}

	/**
     * This method calculates the minimum cost to buy target_size shares. Returns infinity if there
	 * aren't enough shares available. 
	 * Potential optimization:
	 * + Reduce the size of the tree being traversed by using head method s.t. the highest priced 
	 * share <= max_price_paid_per_share. This would see greatest benefit when number of offers in 
	 * the log are much larger than target_size.

     * @return costOfTargetShares The minimum cost to buy target_size shares. Returns Double.POSITIVE_INFINITY
     * if there aren't enough shares in the log.
	*/
	public double getCostOfTargetShares(){
		int shares_remaining = target_size;
		double total_cost = 0.0;
		Map.Entry<Double,Integer> cur_offer = this.offer_log.firstEntry();

		while (shares_remaining > 0 && cur_offer != null){

			//check if the current offer has enough shares to complete the buy
			if (cur_offer.getValue() < shares_remaining){

				//Add all the shares at this price point to the buy
				total_cost += cur_offer.getKey() * cur_offer.getValue();
				shares_remaining -= cur_offer.getValue();

			} else { //the current offer has enough shares to complete the buy order

				total_cost += cur_offer.getKey() * shares_remaining;
				shares_remaining = 0;
				this.max_price_paid_per_share = cur_offer.getKey();

			}
			//get the next lowest priced offer
			cur_offer = offer_log.higherEntry(cur_offer.getKey());
		}
		if(shares_remaining > 0){
			return Double.POSITIVE_INFINITY;
		} else {
			return total_cost;
		}
	}
	/**
	 * This method adds a offer to the log and recalculates the 
	 * minimum buy price for target_size shares. 
     *
     * @param price The price per share of the offer to be added
     * @param size The number of shares in the offer
     * 
     * @return
     * null if the cost of buying target shares is unchanged
     * Double.POSITIVE_INFINITY if there are not enough shares to complete the buy
     * new cost of buying target_size shares
	**/
	public Double addNewOffer(double price, int size){
		double new_cost;
		/*****add new offer to the log****/
		//Keys are prices, so check and see if there are some shares available at this price already
		Integer current_shares = this.offer_log.get(price);
		if (current_shares != null){
			//already have shares at this price, so increment the number by size
			this.offer_log.put(price, current_shares+size);
		} else {
			//no shares at this price so create a new key/value pair
			this.offer_log.put(price, size);
		}
		//check if the price should be updated
		if (price >= this.max_price_paid_per_share){
			//the new offer is too high and won't affect min price
			return null;
		}
		new_cost = getCostOfTargetShares();

		if (new_cost == this.target_shares_cost || (this.target_shares_cost == -1 && new_cost == Double.POSITIVE_INFINITY)){
			//the new_cost is the same as the old cost
			this.target_shares_cost = new_cost;
			return null;
		} else {
			this.target_shares_cost = new_cost;
			return this.target_shares_cost;
		}
	}

    /**
     * This method removes an offer from the log. Then, it recalculates the minimum price to purchase target_size
     * shares.
     *
     * @param price The price per share of the offer to remove
     * @param size The number of shares in the offer
     *
     * @return
     * null if the cost of buying target shares is unchanged
     * Double.POSITIVE_INFINITY if there are not enough shares to complete the buy
     */
    public Double removeOffer(double price, int size) throws Exception{
		Double new_cost;
		//remove shares from log
		Integer current_shares = this.offer_log.get(price);
        if (current_shares == null) {
            throw new Exception("Error: Cannot remove shares. No shares in the buyer log at this price: "+price);
        }
		if (size < current_shares) {//there are other offers at this price, so just decrement shares from log
			this.offer_log.put(price, current_shares-size);
		} else {//remove the entry from the log completely
			this.offer_log.remove(price);
		}
		//recalculate price if needed
		if (price <= this.max_price_paid_per_share){
			//invalidates max_price_paid_per_share
			this.max_price_paid_per_share = Double.POSITIVE_INFINITY;
			new_cost = getCostOfTargetShares();

			if(new_cost == this.target_shares_cost || (this.target_shares_cost == -1 && new_cost == Double.POSITIVE_INFINITY)){
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
