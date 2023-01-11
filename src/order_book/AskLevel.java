package order_book;

final class AskLevel extends SupplyLevel {
	
	AskLevel(double liquidity, double level) {
		super(liquidity, level);
	}
	
	String distanceFromPrice(double priceNow) {
		return ((getLevelValue()/priceNow)-1)*100+" % above current price";
	}
	
}
