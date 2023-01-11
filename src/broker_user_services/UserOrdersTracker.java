package broker_user_services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

import orders.OrderStatus;

public class UserOrdersTracker {
	
	private List<OrderStatus> sentOrders = new ArrayList<>(); /*all orders */
	private List<OrderStatus> activeOrders = new ArrayList<>(); /*limit orders */
	private List<OrderStatus> inactiveOrders = new ArrayList<>(); /*stop limit orders */
	
	static UserOrdersTracker createOrderTracker() {
		return new UserOrdersTracker();
	}
	
	private UserOrdersTracker() {}
	
	int totalDispatchedOrders() {
		return sentOrders.size();
	}
	
	boolean hasActiveOrders() {
		return !activeOrders.isEmpty();
	}
	
	boolean hasInactiveOrders() {
		return !inactiveOrders.isEmpty();
	}
	
	boolean hasPendingOrders() {
		return hasActiveOrders() || hasInactiveOrders();
	}
	
	void orderDispatched(OrderStatus newOrder) {
		if (!sentOrders.contains(newOrder)) {
			sentOrders.add(newOrder);
		}
		if (newOrder.isLimit() && !activeOrders.contains(newOrder)) {
			activeOrders.add(newOrder);
		} else if (newOrder.isStopLimit() && !inactiveOrders.contains(newOrder)) {
			inactiveOrders.add(newOrder);
		}
	}
	
	void limitExecuted(OrderStatus order) {
		activeOrders.remove(order);
	}
	
	void stopLimitActivated(OrderStatus order) {
		inactiveOrders.remove(order);
		activeOrders.add(order);
	}
	
	void orderCancelled(OrderStatus order) {
		if (order.isLimit()) {
			sentOrders.remove(order);
			activeOrders.remove(order);
		} else if (order.isStopLimit()) {
			sentOrders.remove(order);
			inactiveOrders.remove(order);
		}
	}
	
	void TPSLsetted() throws OrderNotFoundExc {
		OrderStatus originalOrder = sentOrders.get(sentOrders.size()-3); 
		originalOrder.markStopLossOrder(inactiveOrders.get(inactiveOrders.size()-1)); /* SL order is the last stop limit orders */
		originalOrder.markTakeProfitOrder(activeOrders.get(activeOrders.size()-1)); /* last limit order is the TP order*/
	}
	
	OrderStatus getOrderById(int id) throws OrderNotFoundExc {
		if (getAllPendingOrders().size() > 0) {
			Iterator<OrderStatus> sentIter = getAllPendingOrders().iterator();
			while(sentIter.hasNext()) {
				OrderStatus referenceOrder = sentIter.next();
				if (referenceOrder.getID() == id) {
					return referenceOrder;
				}
			}
			throw new OrderNotFoundExc("There isn't any order matching the provided ID ("+id+")");
		}
		throw new OrderNotFoundExc("There isn't any order history associated to this account!");
	}
	
	Stream<OrderStatus> getOrdersByPriceLevel(double priceLevel) throws OrderNotFoundExc {
		return getAllPendingOrders().stream()
				.filter(order -> {return (order.getRequestedPrice() == priceLevel || order.getActivationPrice() == priceLevel);});
	}
	
	OrderStatus getLastPendingOrder() throws OrderNotFoundExc {
		if (hasPendingOrders()) {
			ListIterator<OrderStatus> sentIter = getAllPendingOrders().listIterator(getAllPendingOrders().size());
			while(sentIter.hasPrevious()) {
				OrderStatus referenceOrder = sentIter.previous();
				if (referenceOrder.isLimit() || referenceOrder.isStopLimit()) {
					return referenceOrder;
				}
			}
		} 
		throw new OrderNotFoundExc("There isn't any pending order associated to this account!");

	}
	
	List<OrderStatus> getAllPendingOrders() throws OrderNotFoundExc {
		if (hasPendingOrders()) {
			List<OrderStatus> pending = new ArrayList<>();
			pending.addAll(sentOrders);
			pending.removeIf(order -> order.isMarket() || order.isFilled());
			return pending;
		} 
		throw new OrderNotFoundExc("There isn't any pending order associated to this account!") ;
	}
	
	public String toString() {
		String str = "\nAll dispatched orders["+totalDispatchedOrders()+"] (from last to first): ";
		ListIterator<OrderStatus> sentIter = sentOrders.listIterator(sentOrders.size());
		while (sentIter.hasPrevious()) {
			str += sentIter.previous().toStringForTesting();
		}
		return str;
	}
	
}
