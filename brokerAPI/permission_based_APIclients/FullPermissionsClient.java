package permission_based_APIclients;

import broker_context.Context;
import broker_user_services.AbstractService;
import broker_user_services.NotEnoughBalanceExc;
import broker_user_services.OrderNotFoundExc;
import broker_user_services.UserFullServices;
import broker_user_services.UserManager;
import order_book.LevelNotFoundExc;
import orders.OrderSide;

public final class FullPermissionsClient extends APIClient implements FullClientManager {
	
	private UserManager user;
	
	@SuppressWarnings("unused")
	private FullPermissionsClient() {}
 	
	/**Construct a {@link UserFullServices} istance to enable local simulated trading.
	 * @param balance : starting USD balance for this simulated user
	 */
	FullPermissionsClient(Context broker, double balance) {
		this.user = createUserService(broker, balance);
		activateAlerts();
	}
	
	@Override
	UserManager createUserService(Context broker, double balance){
		return AbstractService.createFullService(broker, balance);
	}
	
	public void setTrade(double qty, OrderSide side, double percentage) {
		try {
			if (percentage > 0) {
				if (side == OrderSide.BUY) {
					user.BuyMarket(qty, market, percentage);
				} else user.SellMarket(qty, market, percentage);
			} else makeMarketOrder(qty, side);
		} catch (NotEnoughBalanceExc exc) {
			exc.getError();
		} catch (IllegalArgumentException exc) {
			System.out.println(exc.getMessage());
		} catch (OrderNotFoundExc exc) {
			exc.getError();
		}
	}

	public void makeMarketOrder(double qty, OrderSide side)  {
		try {
			if (side == OrderSide.BUY) {
				user.BuyMarket(qty, market, -1);
			} else user.SellMarket(qty, market, -1);
		} catch (NotEnoughBalanceExc exc) {
			exc.getError();
		} catch (IllegalArgumentException exc) {
			System.out.println(exc.getMessage());
		} catch (OrderNotFoundExc exc) {
			exc.getError();
		}
	}
	
	public void makeLimitOrder(double qty, OrderSide side, double reqPrice)  {
		try {
			if (side == OrderSide.BUY) {
				user.BuyLimit(qty, market, reqPrice);
			} else user.SellLimit(qty, market, reqPrice);
		} catch (NotEnoughBalanceExc exc) {
			exc.getError();
		} catch (IllegalArgumentException exc) {
			System.out.println(exc.getMessage());
		}
	}
	
	public void makeConditionalLimitOrder(double qty, OrderSide side, double reqPrice, double actPrice) {
		try {
			if (side == OrderSide.BUY) {
				user.BuyStopLimit(qty, market, reqPrice, actPrice);
			} else user.SellStopLimit(qty, market, reqPrice, actPrice);
		} catch (NotEnoughBalanceExc exc) {
			exc.getError();
		} catch (IllegalArgumentException exc) {
			System.out.println(exc.getMessage());
		}
	}
	
	public void cancelOrder(int id) {
		try {
			user.cancelOrder(id);
		} catch (OrderNotFoundExc exc) {
			exc.getError();
		} catch (LevelNotFoundExc exc) {
			exc.getError();;
		}
	}
	
	public void cancelLastOrder() {
		try {
			user.cancelLastOrder();
		} catch (OrderNotFoundExc exc) {
			exc.getError();
		} catch (LevelNotFoundExc exc) {
			exc.getError();;
		}
	}

	public boolean hasActiveOrders() {
		return user.hasPendingOrders();
	}
	
	public String getOrderHistory() {
		return user.getOrderHistory();
	}
	
	public void activateAlerts() {
		user.activateMarketAlerts();
	}
	
	public void deactivateAlerts() {
		user.deactivateMarketAlerts();
	}
	
	public String getLatestPriceAlert() {
		return user.getLatestAlert();
	}
	
	public int getActiveUsers() {
		return user.activeUsers();
	}
	
	public double getIstantPrice() {
		return user.istantPrice(market);
	}

	public String getDefaultMarketsOverviews() {
		return user.getDefaultOverviews();
	}

	public String getDefaultMarketOverview() {
		return user.getDefaultOverview(market);
	}

	public String getWideMarketsOverviews(int groupSize) {
		return user.getGroupedOverviews(groupSize);
	}
	
	public String getWideMarketOverview(int groupSize) {
		return user.getGroupedOverview(market, groupSize);
	}
	
	public String getAccountBalances() {
		return user.getAccountBalances();
	}
	
	public void depositFiat(double funds) {
		user.depositFiat(funds);
	}
	
	public void withdrawFiat(double funds) {
		user.withdrawFiat(funds);
	}
	
	public void withdrawAllFiat() {
		user.withdrawAllFiat();
	}
	
	public void depositShares(double qty) {
		user.depositShares(market, qty);
	}

	public void withdrawShares(double qty) {
		user.withdrawShares(market, qty);
	}

	public void withdrawAllShares() {
		user.withdrawAllShares(market);
	}

}
