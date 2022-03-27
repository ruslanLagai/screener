package com.home.project.stocks.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author rlagay
 */
@Data
@ConfigurationProperties("twelvedata.api")
public class TwelveDataApiProperties {
    private List<String> key;
    private String url;
}
