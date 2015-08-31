package io.bloc.android.blocly.api.model;

/**
 * Created by igor on 31/8/15.
 */
public abstract class Model {
    
    private final long rowId;

    public Model (long rowId) {
        this.rowId = rowId;
    }

    public long getRowId() {

        return rowId;
    }


}
