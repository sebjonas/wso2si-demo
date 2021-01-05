$response = Invoke-RestMethod 'http://localhost:9200/frauds/_doc/' -Method 'DELETE' -Headers $headers
$response | ConvertTo-Json