package broker_user_services;

import java.util.*;

import broker_context.Context;
import broker_context.Market;
import order_book.LevelNotFoundExc;
import order_book.PriceLevel;
import orders.Order;
import orders.OrderStatus;
import orders.OrderSide;

public class UserFullServices extends AbstractService implements UserManager {
	
	private WalletManager wallet;
	private UserOrdersTracker ordersTracker;
	private final int userID = new Random().nextInt(10000);
	
	UserFullServices(Context broker, double balance) {
		super(broker);
		this.wallet = createWalletService(broker, balance);
		this.ordersTracker = UserOrdersTracker.createOrderTracker();
	}
	
	public void BuyMarket(double qty, Market market, double percentage) throws IllegalArgumentException, NotEnoughBalanceExc, OrderNotFoundExc {
		double currentPrice = istantPrice(market);
		if (wallet.canAffordBuy(qty, market)) {
			dispatchOrder(Order.buildNewMarketOrder(market, currentPrice, qty, OrderSide.BUY));
			if (percentage > 0) {
				SellStopLimit(qty, market, currentPrice-(currentPrice*(percentage/100)), currentPrice-(currentPrice*(percentage/100))+1); /*SL*/
				SellLimit(qty, market, currentPrice+(currentPrice*(percentage/100))); /*TP*/
				ordersTracker.TPSLsetted();
			}
		}
	}
	
	public void BuyLimit(double qty, Market market, double requestedPrice) throws IllegalArgumentException, NotEnoughBalanceExc {
		if (wallet.canAffordBuy(qty, market)) {
			dispatchOrder(Order.buildNewLimitOrder(market, qty, requestedPrice, OrderSide.BUY));
		}
	}
	
	public void BuyStopLimit(double qty, Market market, double requestedPrice, double actviationPrice) throws IllegalArgumentException, NotEnoughBalanceExc {
		if (wallet.canAffordBuy(qty, market) && paramsValid(OrderSide.BUY, requestedPrice, actviationPrice)) {		
			dispatchOrder(Order.buildNewStopLimitOrder(market, qty, requestedPrice, actviationPrice, OrderSide.BUY));
		}
	}

	public void SellMarket(double qty, Market market, double percentage) throws IllegalArgumentException, NotEnoughBalanceExc, OrderNotFoundExc {
		double currentPrice = istantPrice(market);
		if (wallet.canAffordSell(qty, market)) {
			dispatchOrder(Order.buildNewMarketOrder(market, currentPrice, qty, OrderSide.SELL));
			if (percentage > 0) {
				BuyStopLimit(qty, market, currentPrice+(currentPrice*(percentage/100)), currentPrice+(currentPrice*(percentage/100))+1); /*SL*/
				BuyLimit(qty, market, currentPrice-(currentPrice*(percentage/100))); /*TP*/
				ordersTracker.TPSLsetted();
			}
		} 
	}

	public void SellLimit(double qty, Market market, double requestedPrice) throws IllegalArgumentException, NotEnoughBalanceExc {
		if (wallet.canAffordSell(qty, market)) {
			dispatchOrder(Order.buildNewLimitOrder(market, qty, requestedPrice, OrderSide.SELL));
		} 
	}
		
	public void SellStopLimit(double qty, Market market, double requestedPrice, double actviationPrice) throws IllegalArgumentException, NotEnoughBalanceExc {
		if (wallet.canAffordSell(qty, market) && paramsValid(OrderSide.SELL, requestedPrice, actviationPrice)) {
			dispatchOrder(Order.buildNewStopLimitOrder(market, qty, requestedPrice, actviationPrice, OrderSide.SELL));
		} 
	}
	
	public void cancelLastOrder() throws LevelNotFoundExc, OrderNotFoundExc {
		cancelOrder(ordersTracker.getLastPendingOrder());
	}
	
	public void cancelOrder(int id) throws LevelNotFoundExc, OrderNotFoundExc {
		cancelOrder(ordersTracker.getOrderById(id));
	}
	
	private void cancelOrder(OrderStatus order) throws LevelNotFoundExc {
		getBroker()
		.getCurrentOrderBook(order.getMarket())
		.getLevel(order.getRequestedPrice())
		.detachOrderSender(this);
		ordersTracker.orderCancelled(order);
	}
	
	private boolean paramsValid(OrderSide side, double reqPrice, double actPrice) throws IllegalArgumentException {
		if (side == OrderSide.BUY && actPrice <= reqPrice) {
			throw new IllegalArgumentException("Your activation price is lower than your buy limit price");
		} else if (side == OrderSide.SELL && actPrice >= reqPrice) {
			throw new IllegalArgumentException("Your activation price is higher than your sell limit price");
		} else return true;
	} 

	private void dispatchOrder(OrderStatus newOrder) {
		try {
			if (getBroker()
					.getCurrentOrderBook(newOrder.getMarket())
					.handledOrder(newOrder, this)) {
				if (newOrder.isMarket()) {
					if (newOrder.isBuy()) {
						wallet.upgradeBalanceBuying(newOrder);
					} else wallet.upgradeBalanceSelling(newOrder);
				    if (getBroker()
						.getCurrentOrderBook(newOrder.getMarket())
						.verifyPriceChange()) {
						if (newOrder.isBuy()) {
							getBroker().notifySuddenPriceRise(newOrder.getMarket());
						} else getBroker().notifySuddenPriceDrop(newOrder.getMarket());
					}
				} 
				ordersTracker.orderDispatched(newOrder);
			}
		}
		catch (LevelNotFoundExc exc) {
			exc.getError();
		}
	}
	
	public boolean hasPendingOrders() {
		return ordersTracker.hasPendingOrders();
	}
	
	public void updateLimitOrderState(double price) throws OrderNotFoundExc {
		if (ordersTracker.hasActiveOrders()) {
			ordersTracker.getOrdersByPriceLevel(price)
			.filter(o -> o.isLimit())
			.forEach(o -> {o.updateFilled(); 
						   o.updateActivation();
						   if (o.isBuy()) {
							   wallet.upgradeBalanceBuying(o);
						   } else wallet.upgradeBalanceSelling(o);
						   ordersTracker.limitExecuted(o);
						   });
		}
	}
	
	public void updateStopLimitOrderState(double price) throws OrderNotFoundExc {
		if (hasPendingOrders()) {
			ordersTracker.getOrdersByPriceLevel(price)
			.filter(o -> o.isStopLimit())
			.forEach(o -> {if (o.isActive()) {
								o.updateFilled();
								o.updateActivation();
								if (o.isBuy()) {
									wallet.upgradeBalanceBuying(o);
								} else wallet.upgradeBalanceSelling(o);
								ordersTracker.limitExecuted(o);
							} else 
								o.updateActivation();
								try {
									PriceLevel reqPriceLevel = 	getBroker()
											.getCurrentOrderBook(o.getMarket())
											.getLevel(o.getRequestedPrice());
									reqPriceLevel.addLiquidity(o.getQuantity());
									reqPriceLevel.attachOrderSender(this); 
									ordersTracker.stopLimitActivated(o);
								} catch (LevelNotFoundExc e) {}
						    });
		}
	}
	
	
	public String toString() {
		return "User: SimulatedUser n."+userID;	
	}

	public void depositShares(Market type, double qty) {
		wallet.depositShares(type, qty);
		
	}

	public void depositFiat(double funds) {
		wallet.depositFiat(funds);
	}

	public void withdrawShares(Market type, double qty) {
		wallet.withdrawShares(type, qty);
	}

	public void withdrawAllShares(Market type) {
		wallet.withdrawAllShares(type);
	}

	public void withdrawFiat(double funds) {
		wallet.withdrawFiat(funds);
	}

	public void withdrawAllFiat() {
		wallet.withdrawAllFiat();
	}
	
	public String getOrderHistory() {
		return ordersTracker.toString();
	}

	public String getAccountBalances() {
		return wallet.getAccountBalances();
	}

	public int activeUsers() {
		return wallet.activeUsers();
	}

	public String getDefaultOverviews() {
		return wallet.getDefaultOverviews();
	}

	public String getDefaultOverview(Market market) {
		return wallet.getDefaultOverview(market);
	}

	public String getGroupedOverviews(int groupSize) {
		return wallet.getGroupedOverviews(groupSize);
	}

	public String getGroupedOverview(Market market, int groupSize) {
		return wallet.getGroupedOverview(market, groupSize);
	}
}
