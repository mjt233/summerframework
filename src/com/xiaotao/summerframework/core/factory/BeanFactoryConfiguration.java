package com.xiaotao.summerframework.core.factory;

import java.util.List;

public interface BeanFactoryConfiguration {
    List<Listener<BeanConfigureInfoInfo>> getListeners();
    List<BeanConfigureInfoInfo> getBeanConfigureInfos();
}
