package order_book;

import java.util.ArrayList;
import java.util.List;

import broker_user_services.OrderNotFoundExc;
import broker_user_services.OrderObserver;

public class SupplyLevel implements PriceLevel {
	
	private double liquidity;
	private double level;
	private List<OrderObserver> orderSenders = new ArrayList<>();
	
	static PriceLevel newAskLevel(double liquidity, double level) {
		return new AskLevel(liquidity, level);
	}
	
	static PriceLevel newBidLevel(double liquidity, double level) {
		return new BidLevel(liquidity, level);
	}
	
	SupplyLevel(double liquidity, double level) {
		this.liquidity = liquidity;
		this.level = level;
	}
	
	public void addLiquidity(double adding) {
		liquidity += adding;
	}
	
	public void removeLiquidity(double removing) {
		liquidity -= removing;
	}
	
	private PriceLevel cloneGroup(double supplySum) {
		return new SupplyLevel(liquidity+supplySum, level);
	}
	
	public PriceLevel groupLiquidity(PriceLevel next) {
		return cloneGroup(next.getLiquidity());
	}
	
	public void updateLevel(PriceLevel next) {
		next.removeLiquidity(liquidity * -1);
	}
	
	public double getLiquidity() {
		return liquidity;
	}
	
	public double getLevelValue() {
		return level;
	}
	
	public boolean isEmpty() {
		return liquidity < 0;
	}
	
	public boolean isCloseEnough(double referenceLevel) {
		return referenceLevel-0.01 < level && level < referenceLevel + 0.01;
	}
	
	public void attachOrderSender(OrderObserver user) {
		orderSenders.add(user);
	}
	
	public void detachOrderSender(OrderObserver user) {
		orderSenders.remove(user);
	}
	
	public boolean containsOrders() {
		return !orderSenders.isEmpty();
	}
	
	public void notifyLevelCrossed() {
		List<OrderObserver> avoidConcurrentModificationExc = new ArrayList<>();
		avoidConcurrentModificationExc.addAll(orderSenders);
		avoidConcurrentModificationExc.forEach(user -> {
			try {
				user.updateLimitOrderState(level);
				user.updateStopLimitOrderState(level);
			} catch (OrderNotFoundExc e) {}
		});
	}
	
	public String toString() {
		return "("+level+") ---> '"+liquidity+"' shares\n";
	}
	
}
