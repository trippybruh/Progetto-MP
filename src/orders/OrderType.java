package orders;

public interface OrderType {
	
	boolean isBuy();
	boolean isMarket();
	boolean isLimit();
	boolean isStopLimit();
	void markStopLossOrder(OrderStatus parentOrder);
	void markTakeProfitOrder(OrderStatus parentOrder);

}
