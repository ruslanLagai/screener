package com.home.project.stocks.client;

import com.home.project.stocks.model.processing.ProcessingResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collection;

@Component
@FeignClient(name = "stores", url = "${notifier.url}")
public interface NotifierClient {

    @PostMapping(value = "/notify")
    ResponseEntity notifyUser(Collection<ProcessingResult> results);
}
