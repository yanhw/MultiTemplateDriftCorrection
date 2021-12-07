package dc.model;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import java.util.logging.*;

//use workerThread to achieve concurrency
public class WorkerThread implements Runnable {
	private Process myProcess;
	private List<Path> fileList;
	private CountDownLatch counter;
	private static final Logger logger = Logger.getLogger(WorkerThread.class.getName());
	
	
	public WorkerThread(Process myProcess, List<Path> fileList, CountDownLatch counter) {
		this.myProcess = myProcess;
		this.fileList = fileList;
		this.counter = counter;
//		logger.info("worker thread created");
	}
	
	public void setFileHandler(FileHandler fh) {
		logger.addHandler(fh);
	}

	@Override
	public void run() {
		for (Path filename: fileList) {
			//~ System.out.println(filename.toString());
			myProcess.run(filename.toString());
			counter.countDown();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				logger.warning("sleep interrupted for worker after file: " + filename.toString());
			}
		}
	}	
}
