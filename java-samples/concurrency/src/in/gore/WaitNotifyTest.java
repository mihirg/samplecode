package in.gore;

import java.util.ArrayList;
import java.util.List;



public class WaitNotifyTest {

	public static void main(String args[]) {
		
		final BlockingQueue<String> queue = new BlockingQueue<String>(3);
		
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				System.out.println(queue.get());				
			}
			
		});
		t.setName("Thread 0");
		t.start();
		
		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				queue.get();				
			}
			
		});
		t1.setName("Thread 1");
		t1.start();

		Thread t2 = new Thread(new Runnable() {

			@Override
			public void run() {
				queue.put("Test");
			}
			
		});
		t2.setName("Thread 2");
		t2.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
