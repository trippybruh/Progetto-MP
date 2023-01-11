package order_book;

import broker_user_services.OrderObserver;

interface PriceLevelUpdater {
	
	void addLiquidity(double adding);
	void removeLiquidity(double removing);
	void updateLevel(PriceLevel next);
	void attachOrderSender(OrderObserver user);
	void detachOrderSender(OrderObserver user);
}
