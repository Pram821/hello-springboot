import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should create a campaign"
    request {
        method POST()
        url "/api/campaigns"
        headers {
            contentType(applicationJson())
        }
        body([
            name: "Contract Test Campaign",
            campaignType: "FLASH",
            campaignStatus: "DRAFT",
            createdBy: "contract-test"
        ])
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            campaignId: $(anyNonBlankString()),
            campaignName: "Contract Test Campaign",
            campaignType: "FLASH",
            campaignStatus: "DRAFT",
            createdBy: "contract-test",
            entityVersion: 1
        ])
    }
}
