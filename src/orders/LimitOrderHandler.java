package orders;

import broker_user_services.OrderObserver;
import order_book.LevelNotFoundExc;
import order_book.OrderBook;

class LimitOrderHandler extends OrderHandler {
	
	LimitOrderHandler(OrderBook book) {
		super(book);
	}
	
	private final void handleLimitOrder(OrderObserver sender) throws LevelNotFoundExc {
		if (limitPrice() < price() && getOrder().isBuy()) {
			orderLevel(limitPrice()).addLiquidity(quantity());
			orderLevel(limitPrice()).attachOrderSender(sender);
		} else if (limitPrice() > price() && !getOrder().isBuy()) {
			orderLevel(limitPrice()).addLiquidity(quantity());
			orderLevel(limitPrice()).attachOrderSender(sender);
		} else passOrderHandling(null); /* limit order with lower(if selling)/higher(if buying) execution price than current price are treated like market orders */
	}
	

	public boolean process(OrderObserver sender) throws LevelNotFoundExc {
		if (getOrder().isLimit()) {
			handleLimitOrder(sender);
		} else passOrderHandling(null);
		return true;
	}

}
