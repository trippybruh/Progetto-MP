package order_book;

import broker_user_services.OrderObserver;
import orders.OrderStatus;

public interface OrderBook extends OrderBookData, OrderBookUpdater {
	
	String toStringGroupedBy(int groupSize);
	boolean handledOrder(OrderStatus order, OrderObserver sender) throws LevelNotFoundExc;
	
}
