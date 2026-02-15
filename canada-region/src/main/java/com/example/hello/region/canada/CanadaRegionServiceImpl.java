package com.example.hello.region.canada;

import com.example.hello.common.RegionService;
import org.springframework.stereotype.Service;

@Service("canadaRegionService")
public class CanadaRegionServiceImpl implements RegionService {
    
    @Override
    public String processRegionRequest(String request) {
        return "Processing request for Canada region: " + request;
    }
}
