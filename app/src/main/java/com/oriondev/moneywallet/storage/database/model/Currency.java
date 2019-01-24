package com.oriondev.moneywallet.storage.database.model;

/**
 * This class acts as a Contract between the Schema of the SQLDatabase and the structure
 * of the exported item.
 */
public class Currency extends BaseItem {

    public String mIso;
    public String mName;
    public String mSymbol;
    public int mDecimals;
    public boolean mFavourite;
}