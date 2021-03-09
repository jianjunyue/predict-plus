package com.predict.plus.algo.model;


import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;

import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;


public class XgboostModel extends Model implements Serializable {
    Booster booster;
    float minssing;

    XgboostModel(InputStream in, float missing) {
        try {
            this.booster = XGBoost.loadModel(in);
            this.minssing = missing;
            initModel();
        } catch (Exception e) {
            throw new RuntimeException("读取xgboost模型异常");
        }
    }

    @Override
    protected void initModel() {

    }


    @Override
    public double[] predict(double[] datas, int numFeatures) {
        float[] floats = new float[datas.length];
        for (int i = 0; i < datas.length; i++) {
            floats[i] = new BigDecimal(datas[i]).floatValue();
        }
        return predict(floats, numFeatures);
    }

    public double[] predict(float[] datas, int numFeatures) {
        try {
            DMatrix dMatrix = new DMatrix(datas,
                    datas.length / numFeatures,
                    numFeatures,
                    this.minssing);
            float[][] predict = this.booster.predict(dMatrix);
            double[] results = new double[datas.length];
            for (int i = 0; i < predict.length; i++) {
                results[i] = predict[i][0];
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("预测错误", e);
        }

    }
}