import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return hello message"
    request {
        method GET()
        url "/seller-center/"
    }
    response {
        status OK()
        body("Hello, Spring Boot!")
        headers {
            contentType(textPlain())
        }
    }
}
