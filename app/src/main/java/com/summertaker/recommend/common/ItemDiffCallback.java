package com.summertaker.recommend.common;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.summertaker.recommend.data.Item;

import java.util.ArrayList;

public class ItemDiffCallback extends DiffUtil.Callback {

    private final ArrayList<Item> mOldList;
    private final ArrayList<Item> mNewList;

    public ItemDiffCallback(ArrayList<Item> oldList, ArrayList<Item> newList) {
        this.mOldList = oldList;
        this.mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).getId() == mNewList.get(
                newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Item oldEmployee = mOldList.get(oldItemPosition);
        final Item newEmployee = mNewList.get(newItemPosition);

        return oldEmployee.getName().equals(newEmployee.getName());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}

