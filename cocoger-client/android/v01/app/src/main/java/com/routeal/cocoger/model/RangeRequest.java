package com.routeal.cocoger.model;

import java.io.Serializable;

/**
 * Created by nabe on 8/30/17.
 */

public class RangeRequest implements Serializable {
    private long created;
    private int range;

    public long getCreated() { return created; }

    public void setCreated(long created) { this.created = created; }

    public int getRange() { return range; }

    public void setRange(int range) { this.range = range; }
}
