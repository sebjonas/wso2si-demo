$headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$headers.Add("Content-Type", "application/json")

$body = "{`n    `"properties`": {`n        `"timestamp`": {`n            `"type`": `"date`",`n            `"format`": `"yyyy-MM-dd HH:mm:ss`"`n        },`n        `"creditCardNo`": {`n            `"type`": `"keyword`"`n        },`n        `"suspiciousTrader`": {`n            `"type`": `"keyword`"`n        },`n        `"coordinates`": {`n            `"type`": `"geo_point`"`n        },`n        `"amount`": {`n            `"type`": `"double`"`n        },`n        `"currency`": {`n            `"type`": `"keyword`"`n        }`n    }`n}"

$response = Invoke-RestMethod 'http://localhost:9200/frauds/_mapping' -Method 'PUT' -Headers $headers -Body $body
$response | ConvertTo-Json