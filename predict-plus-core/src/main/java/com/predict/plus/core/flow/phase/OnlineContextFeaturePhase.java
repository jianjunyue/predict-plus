package com.predict.plus.core.flow.phase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.predict.plus.common.utils.LogUtils;
import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.iface.PlatformPhase;
import com.predict.plus.facade.request.ModelPredictRequest;
import com.predict.plus.facade.request.ProductRequest;
import javafx.util.Pair;

import lombok.extern.slf4j.Slf4j;

/**
 * 在线上下文特征初始化封装
 */
@Service
@Slf4j
public class OnlineContextFeaturePhase implements PlatformPhase {

	@Override
	public void execute(PredictContext context) {
		try {
			System.out.println("-----------OnlineContextFeaturePhase----------------");

			ModelPredictRequest request = context.getRequest() ;
			// 用户维度上下文特征
			Map<String, Object> contextAttributes = request.getContextAttributes();
			if (MapUtils.isEmpty(contextAttributes)) {
				contextAttributes = Maps.newHashMap();
			}
			contextAttributes.put("uid", context.getUserId());
			contextAttributes.put("userid", context.getUserId());
			LogUtils.logInfo(context.isLogSwitch(), "OnlineContextFeaturePhase-contextAttributes",
					JSON.toJSONString(contextAttributes));
			context.setUidRawFeatureMap(contextAttributes);

			// pid维度
			List<ProductRequest> productIds = request.getProductIds();
			if (CollectionUtils.isNotEmpty(productIds)) {
				Map<String, Map<String, Object>> prdContextFeatureMap = productIds.stream().map(product -> {
					String pid = product.getProductId();
					Map<String, Object> productIdAttributes = product.getProductIdAttributes() == null
							? Maps.newHashMap()
							: product.getProductIdAttributes();
					productIdAttributes.put("pid", pid);
					return new Pair<String, Map<String, Object>>(pid, productIdAttributes);
				}).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
				context.setPrdRawFeatureMap(prdContextFeatureMap);
			}
			System.out.println(context.getPrdRawFeatureMap());
		} catch (Exception e) {
			log.error("OnlineContextFeaturePhase-Exception", e);
		}

	}

}
