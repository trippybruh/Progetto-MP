package orders;

import java.util.concurrent.atomic.AtomicInteger;
import broker_context.Market;
import order_book.Book;

public abstract class Order implements OrderStatus {
	
	private static final AtomicInteger IDcounter = new AtomicInteger();
	private final int ID = IDcounter.incrementAndGet(); 
	
	private Market market;
	private OrderSide side;
	private double quantity;
	private double quantityUSD;
	private double requestedPrice;
	private double activationPrice;
	
	public static OrderStatus buildNewMarketOrder(Market market, double priceNow, double qty, OrderSide side) {
		return new Order.
						OrderTypeBuilder(market, qty, side).
						generateMarketOrder(priceNow);
	}

	public static OrderStatus buildNewLimitOrder(Market market, double qty, double reqPrice, OrderSide side) {
		return new Order.
						OrderTypeBuilder(market, qty, side).
						generateLimitOrder(reqPrice);
	}
	
	public static OrderStatus buildNewStopLimitOrder(Market market, double qty, double reqPrice, double actPrice, OrderSide side) {
		return new Order.
						OrderTypeBuilder(market, qty, side).
						generateStopLimitOrder(reqPrice, actPrice);
	}
	
	private static class OrderTypeBuilder {
			
		private Market market;
		private OrderSide side;
		private double quantity;
		
		private OrderTypeBuilder (Market market, double qty, OrderSide side) {
			this.market = market;
			this.quantity = qty;
			this.side = side;
		}
		
		private OrderStatus generateMarketOrder(double priceNow) {
			return new MarketOrder()
					.withCommonDetails(market, quantity, side)
					.addQuantityUSD(priceNow * quantity);
		}
		
		private OrderStatus generateLimitOrder(double reqPrice) {
			return new LimitOrder()
					.withCommonDetails(market, quantity, side)
					.addQuantityUSD(reqPrice * quantity)
					.addRequestedPrice(reqPrice);
		}
		
		private OrderStatus generateStopLimitOrder(double reqPrice, double actPrice) {
			return new StopLimitOrder()
					.withCommonDetails(market, quantity, side)	
					.addQuantityUSD(reqPrice * quantity)
					.addRequestedPrice(reqPrice)
					.addActivationPrice(actPrice);
		}
	}
	
    Order(){}
	
	Order withCommonDetails(Market market, double qty, OrderSide side) {
		this.market = market;
		this.quantity = Book.uniformRounding(qty).doubleValue();
		this.side = side;
		return this;
	}
	
	Order addQuantityUSD(double quantityUSD) {
		this.quantityUSD = Book.uniformRounding(quantityUSD).doubleValue();
		return this;
	}
	
	Order addRequestedPrice(double reqPrice) {
		this.requestedPrice = Book.uniformRounding(reqPrice).doubleValue();
		return this;
	}
	
	Order addActivationPrice(double actPrice) {
		this.activationPrice = Book.uniformRounding(actPrice).doubleValue();
		return this;
	}
	
	public int getID() {
		return ID;
	}
	
	OrderSide getSide() {
		return side;
	}
	
	public Market getMarket() {
		return market;
	}
	
	public double getQuantity() {
		return quantity;
	}
	
	public double getQuantityUSD() {
		return quantityUSD;
	}
	
	public double getRequestedPrice() {
		return requestedPrice;
	}
	
	public double getActivationPrice() {
		return activationPrice;
	}
	
	public boolean isBuy() {
		return side == OrderSide.BUY;
	}
	
	public abstract void updateActivation();
	public abstract void updateFilled();
	public abstract boolean isActive();
	public abstract boolean isFilled();
	public abstract boolean isMarket();
	public abstract boolean isLimit();
	public abstract boolean isStopLimit();
	public abstract void markStopLossOrder(OrderStatus SL);
	public abstract void markTakeProfitOrder(OrderStatus TP);

}
