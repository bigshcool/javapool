package com.rl.javapool.domain;

import com.rl.javapool.connection.MyConnection;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Pool {
    // 1. 连接池大小
    private final int poolSize;
    
    // 2. 连接对象数组
    private Connection[] connections;
    
    // 3. 连接状态数组 0 表示空闲， 1.表示繁忙
    private AtomicIntegerArray states;
    
    // 4. 构造方法初始化
    public Pool(int poolSize){
        this.poolSize = poolSize;
        this.connections = new Connection[poolSize];
        this.states = new AtomicIntegerArray(poolSize);
        for (int i = 0; i < this.connections.length; i++){
            connections[i] = new MyConnection();
        }
    }
    
    // 5. 申请连接
    public Connection borrow(){
        while (true){
            for (int i = 0; i < poolSize; i++){
                if (states.get(i) == 0){
                    if (states.compareAndSet(i, 0, 1)){
                        return connections[i];
                    }
                }
            }
            // 如果没有空闲连接了, 让当前连接进行等待
            synchronized (this){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    
    // 6. 归还连接
    public void free(Connection con){
        for (int i = 0; i < poolSize; i++){
            if (connections[i] == con){
                states.set(i, 0);
                synchronized (this){
                    this.notifyAll();
                }
                break;
            }
        }

    }
}
