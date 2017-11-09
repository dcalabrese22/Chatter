package com.dcalabrese22.dan.chatter.interfaces;

import android.view.View;

/**
 * Created by dcalabrese on 10/6/2017.
 */

public interface OnRecyclerItemClickListener {

    void onItemClick(View view, int position);
    void OnItemLongClick(View view, int position);
}
