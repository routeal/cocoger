package com.routeal.cocoger.ui.main;

/**
 * Created by hwatanabe on 10/25/17.
 */

public interface RecyclerAdapterListener<T> {
    void onAdded(String key, T object);

    void onChanged(String key, T object);

    void onRemoved(String key);
}
