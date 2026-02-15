import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return empty list when no campaigns exist"
    request {
        method GET()
        url "/api/campaigns"
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body("[]")
    }
}
