/**
 * This class instantiates objects representing orders int he log book. Each
 * object has a size representing the number of shares and a price representing
 * cost per share.
 *
 * @author Michael Hinds
 * @version 1.0
**/
public class Order {
	private int size;
	private double price;
	private String side;
	//Constructor
    /**
     * Creates a new order object.
     *
     * @param side The side can be buy or sell
     * @param price The price per share of the order
     * @param size The number of shares in the order
     *
     * @return new Order object
     */
	public Order (String side, double price, int size){
		this.side = side;
		this.size = size;
		this.price = price;
	}
	//Getters & Setters
	public String getSide(){
		return this.side;
	}
	public void setSide(String side){
		this.side = side;
	}
	public int getSize(){
		return this.size;
	}
	public void setSize(int size){
		this.size = size;
	}
	public double getPrice(){
		return this.price;
	}
	public void setPrice(double price){
		this.price = price;
	}
}
