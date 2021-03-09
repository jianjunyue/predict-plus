package com.predict.plus.core.platform.parse.iface;

import java.io.InputStream;

import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
 
 

public interface IModelParse {
	 BoosterAndFeatureConfigModel modelParse(InputStream inputStream, Integer version);
}
