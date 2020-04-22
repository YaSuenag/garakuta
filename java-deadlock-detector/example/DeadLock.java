import java.util.concurrent.*;

public class DeadLock implements Runnable{

  private final String lock1;
  private final String lock2;
  private final CountDownLatch latch;

  public DeadLock(String lock1, String lock2, CountDownLatch latch){
    this.lock1 = lock1;
    this.lock2 = lock2;
    this.latch = latch;
  }

  public void run(){
    try{
      synchronized(lock1){
        System.out.println(Thread.currentThread().getName() + " holds " + lock1);
        latch.countDown();
        latch.await();

        System.out.println(Thread.currentThread().getName() + " waits " + lock2);
        synchronized(lock2){
          lock2.wait();
        }

      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void main(String[] args){
    String lockA = "lock A";
    String lockB = "lock B";
    String lockC = "lock C";
    CountDownLatch latch = new CountDownLatch(3);

    Thread threadA = new Thread(new DeadLock(lockA, lockB, latch), "Thread A");
    Thread threadB = new Thread(new DeadLock(lockB, lockC, latch), "Thread B");
    Thread threadC = new Thread(new DeadLock(lockC, lockA, latch), "Thread C");

    threadA.start();
    threadB.start();
    threadC.start();
  }

}
