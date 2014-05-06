package in.gore;

public class RWLock {
	
	Object  wrtLock = new Object();
	int readCt;
	int wrtCt;
	
	public RWLock() {
		
	}
	
	public void readLock() throws InterruptedException {
		synchronized(wrtLock) {
			// if number of writers is more than 0, then wait.
			while (wrtCt > 0)
				wrtLock.wait();
			
			// number of writers is 0, so increment the read counter.
			readCt++;
		}
	}
	
	public void readUnlock() {
		synchronized(wrtLock) {
			//decrement the read count.
			readCt--;
			
			// if the number of readers is 0, notify all sleep threads.
			if (readCt == 0)
				wrtLock.notifyAll();
		}
	}
	
	public void writeLock() throws InterruptedException {
		synchronized (wrtLock) {
			// wait till all read counters have hit 0.
			while (readCt > 0)
				wrtLock.wait();
			
			// The current logic allows for single writer.
			while (wrtCt > 0)
				wrtLock.wait();
			wrtCt++;
		}
	}
	
	public void writeUnlock() {
		synchronized(wrtLock) {
			// see comment in writeLock, we are assuming single writer only.
			wrtCt--;
			wrtLock.notifyAll();
		}
	}

}
