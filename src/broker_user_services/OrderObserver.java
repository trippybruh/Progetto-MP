package broker_user_services;

public interface OrderObserver {

	void updateLimitOrderState(double price) throws OrderNotFoundExc;
	void updateStopLimitOrderState(double price) throws OrderNotFoundExc;

}