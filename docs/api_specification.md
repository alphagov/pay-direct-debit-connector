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

## GET /v1/api/accounts/{gateway_account_id}/transactions/view

This endpoint searches for transactions for the given account id, with filters and pagination

### Request example

```
GET /v1/api/accounts/DIRECT_DEBIT:r6oe9rd7mm1u9r43bi6u1p0qd9/transactions/view?reference=MBK

```

#### Query Parameters description

| Field                     | required | Description                               |
| ------------------------  |:--------:| ----------------------------------------- |
| `reference`               | - | There (partial or full) reference issued by the government service for this payment. |
| `status`                  | - | The transaction of this payment |
| `from_date`               | - | The initial date for search payments |
| `to_date`                 | - | The end date for search payments|
| `page`                    | - | To get the results from the specified page number, should be a non zero +ve number (optional, defaults to 1)|
| `display_size`            | - | Number of records to be returned per page, should be a non zero +ve number (optional, defaults to 500)|
| `email`                   | - | Email of the payment user to search for          |
| `agreement`               | - | Agreement external id |

### Response example
```
{
    "total": 3,
    "count": 1,
    "page": 2,
    "results": [
        {
            "amount": 200,
            "state": {
                "status": "pending",
                "finished": false
            },
            "description": "A test payment 2",
            "reference": "MBK71",
            "email": "citizen@example.com",
            "name": "J. Citizen",
            "transaction_id": "t9037r9pfla4q0cao1mq1ad3a7",
            "created_date": "2018-06-27T09:57:02.127Z",
            "links": {
                "self": {
                    "href": "https://direct-debit-connector.example.com/v1/api/accounts/DIRECT_DEBIT:r6oe9rd7mm1u9r43bi6u1p0qd9/charges/t9037r9pfla4q0cao1mq1ad3a7,
                    "method": "GET"
                }
            }
        }
    ],
    "_links": {
        "next_page": {
            "href": "https://direct-debit-connector.example.com/v1/api/accounts/DIRECT_DEBIT:r6oe9rd7mm1u9r43bi6u1p0qd9/transactions/view?reference=MBK&page_number=3&display_size=100"
        },
        "self": {
            "href": "https://direct-debit-connector.example.com/v1/api/accounts/DIRECT_DEBIT:r6oe9rd7mm1u9r43bi6u1p0qd9/transactions/view?reference=MBK&page_number=2&display_size=100"
        },
        "prev_page": {
            "href": "https://direct-debit-connector.example.com/v1/api/accounts/DIRECT_DEBIT:r6oe9rd7mm1u9r43bi6u1p0qd9/transactions/view?reference=MBK&page_number=1&display_size=100"
        },
        "last_page": {
            "href": "https://direct-debit-connector.example.com/v1/api/accounts/DIRECT_DEBIT:r6oe9rd7mm1u9r43bi6u1p0qd9/transactions/view?reference=MBK&page_number=3&display_size=100"
        },
        "first_page": {
            "href": "https://direct-debit-connector.example.com/v1/api/accounts/DIRECT_DEBIT:r6oe9rd7mm1u9r43bi6u1p0qd9/transactions/view?reference=MBK&page_number=1&display_size=100"
        }
    }
}
```
#### Response field description
```
| Field                                 | Always present | Description                                                       |
| ------------------------------------- |:--------------:| ----------------------------------------------------------------- |
| `total`                               | Yes            | Total number of payments found                                    |
| `count`                               | Yes            | Number of payments displayed on this page                         |
| `page`                                | Yes            | Page number of the current recordset                              |
| `results`                             | Yes            | List of payments                                                  |
| `results[i].amount`                   | Yes            | The amount of this payment in pence                               |
| `results[i].state`                    | Yes            | The current external status of the payment                        |
| `results[i].description`              | Yes            | The payment description                                           |
| `results[i].reference`                | Yes            | There reference issued by the government service for this payment |
| `results[i].email`                    | Yes            | The email address of the user of this payment                     |
| `results[i].name`                     | Yes            | The name of the user of this payment                              |
| `results[i].transaction_id`           | Yes            | The transaction id associated to this payment                     |
| `results[i].created_date`             | Yes            | The created date in ISO_8601 format (```yyyy-MM-ddTHH:mm:ssZ```)  |
| `results[i]._links.self`              | Yes            | Link to the payment                      |
| `_links.self.href`                    | Yes            | Href link of the current page                                     |
| `_links.next_page.href`               | No             | Href link of the next page (based on the display_size requested)  |
| `_links.prev_page.href`               | No             | Href link of the previous page (based on the display_size requested) |
| `_links.first_page.href`              | Yes            | Href link of the first page (based on the display_size requested) |
| `_links.last_page.href`               | Yes            | Href link of the last page (based on the display_size requested)  |
```


