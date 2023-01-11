package orders;

import broker_user_services.OrderObserver;
import order_book.LevelNotFoundExc;
import order_book.OrderBook;
import order_book.OrderBookUpdater;
import order_book.PriceLevel;

public abstract class OrderHandler {
	
	private OrderStatus order;
	private OrderBook book;
	private OrderHandler nextHandler;
	
	public static OrderHandler marketHandler(OrderBook book) {
		return new MarketOrderHandler(book);
	}
	
	public static OrderHandler limitHandler(OrderBook book) {
		return new LimitOrderHandler(book);
	}
	
	public static OrderHandler stopLimitHandler(OrderBook book) {
		return new StopLimitOrderHandler(book);
	}
	
	OrderHandler(OrderBook book) {
		this.book = book;
	}

	public void setOrder(OrderStatus order) {
		this.order = order;
	}
	
	public void setNextHandler(OrderHandler nextHandler) {
		this.nextHandler = nextHandler;
	}
	
	OrderStatus getOrder() {
		return order;
	}
	
	OrderBookUpdater getOB() {
		return book;
	}
	
	double spread() {
		return book.getSpread();
	}
	
	double price() {
		return book.getCurrentPrice();
	}
	
	double limitPrice() {
		return order.getRequestedPrice();
	}
	
	double quantity() {
		return order.getQuantity();
	}
	
	PriceLevel orderLevel(double level) throws LevelNotFoundExc {
		return book.getLevel(level);
	}
	
	void passOrderHandling(OrderObserver sender) throws LevelNotFoundExc {
		if (nextHandler != null) {
			nextHandler.process(sender);
		} 
	}
	
	public abstract boolean process(OrderObserver sender) throws LevelNotFoundExc;

}
