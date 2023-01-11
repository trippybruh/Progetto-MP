package broker_user_services;

import broker_context.Market;
import orders.OrderData;

public interface InnerWalletOperations {

	void upgradeBalanceBuying(OrderData newOrder);
	void upgradeBalanceSelling(OrderData newOrder);
	boolean canAffordBuy(double qty, Market market) throws NotEnoughBalanceExc, IllegalArgumentException;
	boolean canAffordSell(double qty, Market market) throws NotEnoughBalanceExc, IllegalArgumentException;

}