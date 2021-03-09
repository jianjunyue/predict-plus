package com.predict.plus.algo.model;
  
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Signature;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.proto.framework.ConfigProto;
import org.tensorflow.proto.framework.RunOptions;
import org.tensorflow.proto.framework.SignatureDef;
import org.tensorflow.proto.framework.TensorInfo;
import org.tensorflow.types.TFloat64;

import com.predict.plus.common.utils.ZipUtils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
public class TensorflowModel extends Model implements Serializable {

    String path;
    Session session;
    SavedModelBundle bundle;
    String inputName;
    String outputName;

    /**
     *
     */
    public TensorflowModel(InputStream stream) {
        try {
            Path tensorFlow = Files.createTempDirectory("tensorflow");
            ZipUtils.unTarGz(stream, tensorFlow.toAbsolutePath().toString());
            this.path = tensorFlow.toAbsolutePath().toString() + "/model";
        } catch (IOException e) {
            e.printStackTrace();
        }
        initModel();
    }

    @Override
    protected void initModel() {
        try {
            ConfigProto proto = ConfigProto.newBuilder()
                    .setInterOpParallelismThreads(1)
                    .build();

            RunOptions run = RunOptions.newBuilder()
                    .setTraceLevel(RunOptions.TraceLevel.FULL_TRACE)
                    .build();

            this.bundle = SavedModelBundle
                    .loader(this.path)
                    .withTags(SavedModelBundle.DEFAULT_TAG)
                    .withConfigProto(proto)
                    .withRunOptions(run)
                    .load();

            SignatureDef modelInfo = this.bundle.metaGraphDef().getSignatureDefMap().get(Signature.DEFAULT_KEY);
            if (modelInfo.getInputsCount() != 1 || modelInfo.getInputsCount() != 1) {
                throw new RuntimeException("model input or output count more then one, please check the model define");
            }

            Map<String, TensorInfo> inputsMap = modelInfo.getInputsMap();
            for (Map.Entry<String, TensorInfo> entry : inputsMap.entrySet()) {
                inputName = entry.getValue().getName();
            }

            Map<String, TensorInfo> outputMap = modelInfo.getOutputsMap();

            for (Map.Entry<String, TensorInfo> entry : outputMap.entrySet()) {
                outputName = entry.getValue().getName();
            }
            this.session = this.bundle.session();
        } catch (Exception e) {
            log.error("loading model error", e);
            throw new RuntimeException("tensorflow loading error", e);
        }
    }

    @Override
    public double[] predict(double[] datas, int numInputFeatureDim) {
        int numOfRow = datas.length / numInputFeatureDim;

        double[][] mat = new double[numOfRow][numInputFeatureDim];
        for (int i = 0; i < numOfRow; i++) {
            mat[i] = new double[numInputFeatureDim];
            System.arraycopy(datas, i * numInputFeatureDim, mat[i], 0, numInputFeatureDim);
        }

        return output(mat);
    }

    /**
     *
     * @param mat
     * @return
     */
    public double[] output(double[][] mat) {
        try (Tensor<TFloat64> input = TFloat64.tensorOf(StdArrays.ndCopyOf(mat))) {
            Tensor<TFloat64> output = this.bundle.session().runner().feed(inputName, input).fetch(outputName).run().get(0).expect(TFloat64.DTYPE);
            double[] result = new double[mat.length];
            output.data().scalars().forEachIndexed((i, s) -> result[Long.valueOf(i[0]).intValue()] = s.getDouble());
            return result;
        }
    }

}
