package broker_context;

import broker_user_services.MarketObserver;
import order_book.OrderBook;

public interface Context {
	
	OrderBook getCurrentOrderBook(Market market);
	double getOrderBookPrice(Market market);
	BrokerType getBrokerType();
	int activeUsers();
	void subscribe(MarketObserver user);
	void unsubscribe(MarketObserver user);
	void notifySuddenPriceRise(Market market);
	void notifySuddenPriceDrop(Market market);
	
}
