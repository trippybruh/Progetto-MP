package order_book;

interface PriceLevelData {
	
	public double getLevelValue();
	public double getLiquidity();
	public boolean isEmpty();
	public boolean isCloseEnough(double level);
	boolean containsOrders();
	
}
