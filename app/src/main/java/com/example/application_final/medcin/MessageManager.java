package com.example.application_final.medcin;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;

import com.example.application_final.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manager class to handle Message operations using Threads, Locks, and
 * Semaphores
 * as requested for synchronization management.
 */
public class MessageManager {

    private static MessageManager instance;
    private DatabaseHelper db;

    // Semaphore to limit concurrent database writes (Simulation of limited
    // resource)
    private final Semaphore writeSemaphore = new Semaphore(1);

    // Lock for thread-safety when accessing shared resources or caches
    private final ReentrantLock lock = new ReentrantLock();

    public interface MessageCallback {
        void onSuccess();

        void onError(String error);
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);

        void onError(String error);
    }

    private MessageManager(Context context) {
        db = DatabaseHelper.getInstance(context);
    }

    public static synchronized MessageManager getInstance(Context context) {
        if (instance == null) {
            instance = new MessageManager(context);
        }
        return instance;
    }

    /**
     * Sends a message in a separate thread, properly synchronized using Semaphore.
     */
    public void sendMessage(String sender, String receiver, String content, String time, MessageCallback callback) {
        new Thread(() -> {
            try {
                // Acquire semaphore before writing to DB
                writeSemaphore.acquire();

                // Critical Section
                lock.lock();
                try {
                    // Simulate some processing time
                    Thread.sleep(100);
                    db.addMessage(sender, receiver, content, time);
                } finally {
                    lock.unlock();
                }

                // Release semaphore
                writeSemaphore.release();

                // Notify UI on Main Thread
                new Handler(Looper.getMainLooper()).post(callback::onSuccess);

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    /**
     * Fetches messages in a separate thread.
     */
    public void getMessages(String user1, String user2, DataCallback<List<Message>> callback) {
        new Thread(() -> {
            List<Message> messages = new ArrayList<>();
            try {
                // Lock to ensure we are reading consistent state if we had a local cache
                lock.lock();
                try {
                    Cursor c = db.getMessagesBetween(user1, user2);
                    if (c != null) {
                        while (c.moveToNext()) {
                            String sender = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_MSG_SENDER));
                            String content = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_MSG_CONTENT));
                            String time = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_MSG_TIME));
                            messages.add(new Message(sender, content, time));
                        }
                        c.close();
                    }
                } finally {
                    lock.unlock();
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.onDataLoaded(messages));
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }
}
