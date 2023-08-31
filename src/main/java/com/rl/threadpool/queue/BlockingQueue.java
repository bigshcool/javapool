package com.rl.threadpool.queue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueue<T> {
    // 1. 任务队列
    private Deque<T> deque = new ArrayDeque<>();
    
    // 2. 锁
    private ReentrantLock lock = new ReentrantLock();
    
    // 3. 生产者条件变量
    private Condition fullWaitSet = lock.newCondition();
    
    // 4. 消费者条件变量
    private Condition emptyWaitSet = lock.newCondition();
    
    // 5. 容量
    private int capcity;
    
    // 6 带超时的阻塞获取
    public T poll(long timeout, TimeUnit unit){
        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (deque.isEmpty()){
                try {
                    if (nanos <= 0){
                        return null;
                    }
                    nanos = emptyWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = deque.peekFirst();
            fullWaitSet.signal();
            return t;
        }finally {
            lock.unlock();
        }
    }
    
    
    // 阻塞获取
    public T take(){
        lock.lock();
        try {
            while (deque.isEmpty()){
                try {
                    emptyWaitSet.await();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                T t = deque.removeFirst();
                fullWaitSet.signal();
                return t;
            }
        }finally {
            lock.unlock();
        }
        return null;
    }
    
    // 阻塞添加
    public void put(T element){
        lock.lock();
        try {
            while (deque.size() == capcity){
                try {
                    fullWaitSet.await();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            deque.addLast(element);
            emptyWaitSet.signal();
        }finally {
            lock.unlock();
        }
    }
    
    // 获取大小
    public int size(){
        lock.lock();
        try {
            return deque.size();
        }finally {
            lock.unlock();
        }
    }
}
