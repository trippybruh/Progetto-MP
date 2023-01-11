package order_book;

public interface PriceLevel extends PriceLevelData, PriceLevelUpdater {
	
	PriceLevel groupLiquidity(PriceLevel next);
	void notifyLevelCrossed();
}
