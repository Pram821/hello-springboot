package com.example.hello.region.us;

import com.example.hello.common.RegionService;
import org.springframework.stereotype.Service;

@Service("usRegionService")
public class UsRegionServiceImpl implements RegionService {
    
    @Override
    public String processRegionRequest(String request) {
        return "Processing request for US region: " + request;
    }
}
