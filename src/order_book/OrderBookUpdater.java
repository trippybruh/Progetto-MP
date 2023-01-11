package order_book;

public interface OrderBookUpdater {
	
	void updateCurrentPrice(double newPrice);
	void fillEmptyLevels(boolean fillBids);
	
}
