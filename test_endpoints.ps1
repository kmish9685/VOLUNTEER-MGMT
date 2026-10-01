$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

Write-Host "=== 1. Test Homepage as Logged Out ==="
$home = Invoke-WebRequest -Uri "http://localhost:8080/" -WebSession $session
$hasTotalHours = $home.Content.Contains("Total Hours")
$hasBrowse = $home.Content.Contains("Browse Opportunities")
$hasViewAll = $home.Content.Contains("View All")
$hasViewDetails = $home.Content.Contains("View Details")
$hasBootstrapJs = $home.Content.Contains("bootstrap.bundle.min.js")

Write-Host "Total Hours present: $hasTotalHours"
Write-Host "Browse Opportunities present: $hasBrowse"
Write-Host "View All present: $hasViewAll"
Write-Host "View Details present: $hasViewDetails"
Write-Host "Bootstrap JS in footer: $hasBootstrapJs"

Write-Host "`n=== 2. Test Login as Org and /org/opportunities/new ==="
$loginResp = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method Post -Body @{ email="greenearth@gmail.com"; password="org123" } -WebSession $session
$newOpp = Invoke-WebRequest -Uri "http://localhost:8080/org/opportunities/new" -WebSession $session
Write-Host "Status of /org/opportunities/new: $($newOpp.StatusCode)"
$hasForm = $newOpp.Content.Contains("Post Opportunity")
Write-Host "Post Opportunity form loaded cleanly: $hasForm"

Write-Host "`n=== 3. Post a new Opportunity ==="
$postBody = @{
    title = "Beach Clean 2026"
    description = "Cleaning coastal area"
    eventDate = "2026-11-20"
    startTime = "09:00"
    endTime = "13:00"
    location = "Sunset Beach"
    totalSlots = "30"
}
$postResp = Invoke-WebRequest -Uri "http://localhost:8080/org/opportunities/new" -Method Post -Body $postBody -WebSession $session
Write-Host "Status of post opportunity: $($postResp.StatusCode)"
$orgList = Invoke-WebRequest -Uri "http://localhost:8080/org/opportunities" -WebSession $session
$hasBeach = $orgList.Content.Contains("Beach Clean 2026")
Write-Host "Beach Clean 2026 listed in org opportunities: $hasBeach"

Write-Host "`n=== 4. Test Homepage as Org ==="
$orgHome = Invoke-WebRequest -Uri "http://localhost:8080/" -WebSession $session
$orgHasBrowse = $orgHome.Content.Contains("Browse Opportunities")
$orgHasViewAll = $orgHome.Content.Contains("View All")
$orgHasViewDetails = $orgHome.Content.Contains("View Details")
Write-Host "Org Homepage - hasBrowse: $orgHasBrowse, hasViewAll: $orgHasViewAll, hasViewDetails: $orgHasViewDetails"

Write-Host "`n=== 5. Test Logout ==="
$logout = Invoke-WebRequest -Uri "http://localhost:8080/logout" -WebSession $session
Write-Host "Logout status: $($logout.StatusCode)"

Write-Host "`n=== 6. Test Admin Login and Homepage ==="
$adminLogin = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method Post -Body @{ email="admin@gmail.com"; password="admin123" } -WebSession $session
$adminHome = Invoke-WebRequest -Uri "http://localhost:8080/" -WebSession $session
$adminHasBrowse = $adminHome.Content.Contains("Browse Opportunities")
$adminHasViewAll = $adminHome.Content.Contains("View All")
$adminHasViewDetails = $adminHome.Content.Contains("View Details")
Write-Host "Admin Homepage - hasBrowse: $adminHasBrowse, hasViewAll: $adminHasViewAll, hasViewDetails: $adminHasViewDetails"
$logout = Invoke-WebRequest -Uri "http://localhost:8080/logout" -WebSession $session

Write-Host "`n=== 7. Test Volunteer Login and Homepage ==="
$volLogin = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method Post -Body @{ email="rahul@gmail.com"; password="vol123" } -WebSession $session
$volHome = Invoke-WebRequest -Uri "http://localhost:8080/" -WebSession $session
$volHasBrowse = $volHome.Content.Contains("Browse Opportunities")
$volHasViewAll = $volHome.Content.Contains("View All")
$volHasViewDetails = $volHome.Content.Contains("View Details")
Write-Host "Volunteer Homepage - hasBrowse: $volHasBrowse, hasViewAll: $volHasViewAll, hasViewDetails: $volHasViewDetails"
