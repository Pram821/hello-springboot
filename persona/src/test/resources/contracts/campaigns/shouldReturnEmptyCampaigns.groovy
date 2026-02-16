import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return list of campaigns"
    request {
        method GET()
        url "/api/campaigns"
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
    }
}
