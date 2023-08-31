package com.rl.javapool.test;

import com.rl.javapool.domain.Pool;

import java.sql.Connection;
import java.util.Random;

public class Test {
    public static void main(String[] args) {
        Pool pool = new Pool(2);
        for (int i = 0; i < 5; i++){
            new Thread(()->{
                Connection connection = pool.borrow();
                try {
                    Thread.sleep(new Random().nextInt(1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                pool.free(connection);
            }).start();
        }
    }
}
