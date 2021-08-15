package com.home.project.stocks.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * Class to implement processing logic: patters & indicators
 */
@Component
public class ProcessingOrchestration {

    private PatternOrchestration patternOrchestration;
    private IndicatorOrchestration indicatorOrchestration;

    @Autowired
    public ProcessingOrchestration(PatternOrchestration patternOrchestration,
                                   IndicatorOrchestration indicatorOrchestration) {
        this.patternOrchestration = patternOrchestration;
        this.indicatorOrchestration = indicatorOrchestration;
    }

//    public void processStock()

}
