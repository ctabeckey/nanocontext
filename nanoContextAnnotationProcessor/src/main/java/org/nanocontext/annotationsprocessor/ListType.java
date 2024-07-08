package org.nanocontext.annotationsprocessor;

import java.util.ArrayList;
import java.util.List;


public class ListType {
    protected List<Object> beanOrValueOrList;

    public List<Object> getBeanOrValueOrList() {
        if (beanOrValueOrList == null) {
            beanOrValueOrList = new ArrayList<Object>();
        }
        return this.beanOrValueOrList;
    }

}
