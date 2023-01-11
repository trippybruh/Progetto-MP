package orders;

import order_book.OrderBook;
import order_book.PriceLevel;
import broker_user_services.OrderObserver;
import order_book.LevelNotFoundExc;

class MarketOrderHandler extends OrderHandler {
	
	MarketOrderHandler(OrderBook book) {
		super(book);
	}
	
	public boolean process(OrderObserver sender) throws LevelNotFoundExc {
		if (getOrder().isMarket() || getOrder().isLimit()) {
			computeOrder();
		} else return false;
		return true;
	}
	
	private final void computeOrder() throws LevelNotFoundExc {
		int usedLevels = 1;
		if (getOrder().isBuy()) {
			PriceLevel askLevel = orderLevel(price() + spread());
			askLevel.removeLiquidity(quantity());
			if (askLevel.isEmpty()) {
				computeOtherOrders(askLevel);
				usedLevels++;
				PriceLevel nextAskLevel = orderLevel(price() + spread() * usedLevels);
				askLevel.updateLevel(nextAskLevel);
				while (nextAskLevel.isEmpty()) {
					computeOtherOrders(nextAskLevel);
					usedLevels++;
					nextAskLevel.updateLevel(orderLevel(price() + spread() * usedLevels));
					nextAskLevel = orderLevel(price() + spread() * usedLevels);
				}
			}
			orderCheckOut(true, usedLevels);
		} else { 
			PriceLevel bidLevel = orderLevel(price() - spread());
			bidLevel.removeLiquidity(quantity());
			if (bidLevel.isEmpty()) {
				computeOtherOrders(bidLevel);
				usedLevels++;
				PriceLevel nextBidLevel = orderLevel(price() - spread() * usedLevels);
				bidLevel.updateLevel(nextBidLevel);
				while (nextBidLevel.isEmpty()) {
					computeOtherOrders(nextBidLevel);
					usedLevels++;
					nextBidLevel.updateLevel(orderLevel(price() - spread() * usedLevels));
					nextBidLevel = orderLevel(price() - spread() * usedLevels);
				}
			}
			orderCheckOut(false, usedLevels);
		}  
	}
	
	private void orderCheckOut(boolean wasBuy, int usedLevels) {
		if (wasBuy) {
			getOB().updateCurrentPrice(price() + (spread() * (usedLevels-1)));    /* last used level still has liquidity  */
		} else getOB().updateCurrentPrice(price() - (spread() * (usedLevels-1)));  /* resulting price cant be the same of a level which still has unused liquidity */
		if (usedLevels > 1) {
			getOB().fillEmptyLevels(wasBuy);
		}
	}
	
	private void computeOtherOrders(PriceLevel level) {
		if (level.containsOrders()) {
			level.notifyLevelCrossed();
		}
	}

}
