$headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$headers.Add("Content-Type", "application/json")

$body = ""

$response = Invoke-RestMethod 'http://localhost:9200/frauds' -Method 'PUT' -Headers $headers -Body $body
$response | ConvertTo-Json