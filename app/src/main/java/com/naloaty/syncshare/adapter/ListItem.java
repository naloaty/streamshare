package com.naloaty.syncshare.adapter;

public abstract class ListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_DEVICE = 1;

    abstract public int getType();
}