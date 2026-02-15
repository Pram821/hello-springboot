package com.example.hello.region.mx;

import com.example.hello.common.RegionService;
import org.springframework.stereotype.Service;

@Service("mxRegionService")
public class MxRegionServiceImpl implements RegionService {
    
    @Override
    public String processRegionRequest(String request) {
        return "Processing request for MX region: " + request;
    }
}
