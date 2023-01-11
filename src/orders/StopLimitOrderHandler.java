package orders;

import broker_user_services.OrderObserver;
import order_book.LevelNotFoundExc;
import order_book.OrderBook;

class StopLimitOrderHandler extends OrderHandler {
	
	StopLimitOrderHandler(OrderBook book) {
		super(book);
	}
	
	private final void handleStopLimitOrder(OrderObserver sender) throws LevelNotFoundExc {
		orderLevel(getOrder().getActivationPrice())
		.attachOrderSender(sender);
	}

	public boolean process(OrderObserver sender) throws LevelNotFoundExc {
		if (getOrder().isStopLimit()) {
			handleStopLimitOrder(sender);
		} else passOrderHandling(sender);
		return true;
	}
	
}
