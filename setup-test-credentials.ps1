# PowerShell script to set test credentials as environment variables
# Run this script in your PowerShell session before running tests

# EVS credentials
$env:evs.username = "testuser"
$env:evs.password = "testpass123"
$env:evs.email = "test@example.com"

# EPGU credentials
$env:epgu.username = "testuser"
$env:epgu.password = "testpass123"
$env:epgu.email = "test@example.com"

# Other credentials
$env:rpu.username = "testuser"
$env:rpu.password = "testpass123"

$env:uos.username = "testuser"
$env:uos.password = "testpass123"

Write-Host "Test credentials have been set as environment variables."
Write-Host "You can now run your tests with: mvn test -Dtest=Efs1#efs_1_xml"