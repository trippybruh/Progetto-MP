package broker_user_services;

import broker_context.Market;
import order_book.LevelNotFoundExc;

public interface UserOrderManager extends OrderObserver {
	
	boolean hasPendingOrders();
	void BuyMarket(double qty, Market market, double percentage) throws IllegalArgumentException, NotEnoughBalanceExc, OrderNotFoundExc;
	void SellMarket(double qty, Market market, double percentage) throws IllegalArgumentException, NotEnoughBalanceExc, OrderNotFoundExc;
	void BuyLimit(double qty, Market market, double requestedPrice) throws IllegalArgumentException, NotEnoughBalanceExc;
	void SellLimit(double qty, Market market, double requestedPrice) throws IllegalArgumentException, NotEnoughBalanceExc;
	void BuyStopLimit(double qty, Market market, double requestedPrice, double actviationprice) throws IllegalArgumentException, NotEnoughBalanceExc;
	void SellStopLimit(double qty, Market market, double requestedPrice, double actviationprice) throws IllegalArgumentException, NotEnoughBalanceExc;
	void cancelOrder(int id) throws LevelNotFoundExc, OrderNotFoundExc;
	void cancelLastOrder() throws LevelNotFoundExc, OrderNotFoundExc;
	String getOrderHistory();
	
}
