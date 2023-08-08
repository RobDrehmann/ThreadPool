import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

interface ThreadPool {
    void execute(Runnable task);
    void shutdown();
    int size();
}


class ThreadPoolImpl implements ThreadPool {
    private final BlockingQueue<Runnable> Queue;
    private final Thread[] Workers;
    private volatile boolean Shutdown;

    public ThreadPoolImpl(int nTasks, int nWorkers) {
        
        Queue = new LinkedBlockingQueue<>(nTasks);
        Workers = new Thread[nWorkers];
        Shutdown = false;

        for (int i = 0; i < nWorkers; i++) {
            Workers[i] = new WorkerThread();
            Workers[i].start();
        }
    }
     public void shutdown() {
        Shutdown = true;

       for (int i = 0; i < Workers.length; i++) {
         Workers[i].interrupt();
      }
    }

    public int size() {
        return Queue.size();
    }

    public void execute(Runnable task) {
        if (Shutdown) {
            throw new IllegalStateException("ThreadPool is Shutdown");
        }

        try {
            Queue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    private class WorkerThread extends Thread {
        public void run() {
            while (!Shutdown ) {
                try {
                    Runnable task = Queue.take();
                    task.run();
                } catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
                }
            }
        }
    }
}
class MyTask implements Runnable {
    private int param1;
    public MyTask(int param1) {this.param1 = param1; }
    public void run() {
       // the task
       // System.out.println("Thread name: " + Thread.currectThread().getName()};
       }
}
class Client {
    public void foo(ThreadPool threadPool) {
       MyTask task = new MyTask(123);
       threadPool.execute(task);
    }  
}

public class ThreadPoolTest {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPoolImpl(10, 5);
        Client client = new Client();
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            Runnable task = () -> {
                System.out.println("Task " + taskId + " executed on " + Thread.currentThread().getName());
            };
            threadPool.execute(task);
        }
        threadPool.shutdown();
    }
}
