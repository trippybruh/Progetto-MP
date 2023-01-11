package orders;

public interface OrderStatus extends OrderType, OrderData {	
	
	boolean isFilled();
	boolean isActive();
	void updateFilled();
	void updateActivation();
	
}
