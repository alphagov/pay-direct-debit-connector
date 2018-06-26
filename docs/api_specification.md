# API Specification

## POST /v1/api/accounts/{accountId}/mandates

Creates a new mandate for this given account id

### Mandate request example

```
POST /v1/api/accounts/{accountId}/mandates
Content-Type: application/json
```
```json
{
  "return_url": "https://service.example.com/some-reference-to-this-mandate",
  "agreement_type": "ON_DEMAND",
  "service_reference" : "some-reference-to-this-mandate"
}
```

#### Mandate request description

| Field                    | required | Description                               |
| ------------------------ |:--------:| ----------------------------------------- |
| `return_url`             | Yes      | The URL where the user should be redirected to when the mandate workflow is finished (**must be HTTPS only**). |
| `agreement_type`         | Yes      | The type of agreement, eg ON_DEMAND, ONE_OFF |
| `service_reference`      | No       | An optional reference that will be created by the service for easier identification of the future payments in their system. If not specified will be null |

### Mandate created response

```
HTTP/1.1 201 Created
Location: https://connector.example.com/v1/api/accounts/{accountId}/mandates/test_mandate_id_xyz
Content-Type: application/json
```
```json
{
  "mandate_id": "test_mandate_id_xyz",
  "mandate_type": "ON_DEMAND",
  "service_reference": "some-reference-to-this-mandate",
  "return_url": "https://example.com/return",
  "created_date": "2016-01-01T12:00:00.000Z",
  "state": {
    "status": "created",
    "finished": false
  },
  "links": [
    {
      "href": "https://connector.example.com/v1/api/accounts/{accountId}/mandates/test_mandate_id_xyz",
      "rel": "self",
      "method": "GET"
    },
    {
      "href": "https://frontend.example.com/secure/token_1234567asdf",
      "rel": "next_url",
      "method": "GET"
    },
    {
      "href": "https://frontend.example.com/secure/",
      "rel": "next_url_post",
      "type": "application/x-www-form-urlencoded",
      "params": {
        "chargeTokenId": "token_1234567asdf"
      },
      "method": "POST"
    }
  ]
}
```
