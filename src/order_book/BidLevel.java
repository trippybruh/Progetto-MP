package order_book;

final class BidLevel extends SupplyLevel {
	
	BidLevel(double liquidity, double level) {
		super(liquidity, level);
	}

	String distanceFromPrice(double priceNow) {
		return ((getLevelValue()/priceNow)-1)*100+" % below current price";
	}
	
}
