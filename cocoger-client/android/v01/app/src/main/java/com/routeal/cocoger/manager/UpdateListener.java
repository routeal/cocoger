package com.routeal.cocoger.manager;

/**
 * Created by hwatanabe on 10/25/17.
 */

public interface UpdateListener<T> {
    void onAdded(String key, T object);

    void onChanged(String key, T object);

    void onRemoved(String key);
}
