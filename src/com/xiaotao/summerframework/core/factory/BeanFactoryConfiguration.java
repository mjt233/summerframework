package com.xiaotao.summerframework.core.factory;

import java.util.List;

public interface BeanFactoryConfiguration {
    List<Listener<BeanConfiguration>> getListeners();
    List<BeanConfiguration> getBeanConfigureInfos();
}
