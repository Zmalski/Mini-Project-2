package pkg;

public class SlaveThread extends Main implements Runnable {

	@Override
	 public void run() {
		while(true) {
		if (getSize()>0) {
			Request r = new Request();
			synchronized(lock1) {
				r= queue.get(0);
			System.out.println("Consumer: " + Thread.currentThread().getId() + " Assigned request ID: " + r.getId() + " Busy for: " + r.getLengthReq() + " Seconds" + ", CURRENT TIME: " + java.time.LocalTime.now());
			
			try 
			{
				Thread.sleep(r.getLengthReq()*1000);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
				queue.remove(0);
			}
			System.out.println("Consumer: " + Thread.currentThread().getId() + " removed request ID: " + r.getId() + ", CURRENT TIME: " + java.time.LocalTime.now());

		}
		}
		
	}

	 private int getSize() {
		 synchronized(lock1) {return queue.size();}		
		
	}
	
	
}
