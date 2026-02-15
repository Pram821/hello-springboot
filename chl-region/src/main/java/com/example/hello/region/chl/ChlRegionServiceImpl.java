package com.example.hello.region.chl;

import com.example.hello.common.RegionService;
import org.springframework.stereotype.Service;

@Service("chlRegionService")
public class ChlRegionServiceImpl implements RegionService {
    
    @Override
    public String processRegionRequest(String request) {
        return "Processing request for CHL region: " + request;
    }
}
