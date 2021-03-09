package com.predict.plus.algo.model;

public abstract class Model {

    protected abstract void initModel();


    /**
     * @param datas
     * @return
     */
    public abstract double[] predict(double[] datas, int numFeatures);
}
